import edu.mit.jwi.Dictionary;
import edu.mit.jwi.IDictionary;
import edu.mit.jwi.item.*;
import javafx.util.Pair;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.List;

public class WordNet {
    public static final String CONCEPT_SENSE_FORMAT = "%s@%s";
    private static IDictionary dictionary;
    private static HashMap<String, Concept> conceptMap = new HashMap<>();
    private static HashMap<String, List<Concept>> baseConceptMap = new HashMap<>();

    public static void BuildOntology(Set<String> nouns) throws IOException {
        InitializeDictionary();
        GenerateHypernymOntology(nouns);
        AggregateConcepts();
    }

    private static void AggregateConcepts() {
        for(Concept concept: conceptMap.values()){
            List<Concept> conceptList = new ArrayList<>();
            if(baseConceptMap.containsKey(concept.baseConcept)){
                conceptList = baseConceptMap.get(concept.baseConcept);
            }

            conceptList.add(concept);
            baseConceptMap.put(concept.baseConcept, conceptList);
        }
    }

    public static void GenerateHypernymOntology(Set<String> nouns) {
        for(String noun : nouns) {
            GenerateHypernymOntology(noun);
        }
    }

    public static void InitializeDictionary() throws IOException {
        conceptMap = new HashMap<>();
        baseConceptMap = new HashMap<>();
        String wnhome = "C:\\Program Files (x86)\\WordNet\\2.1"; //System.getenv("WNHOME");
        String path = wnhome + File.separator + "dict";
        URL url = new URL("file", null, path);
        IDictionary dictionary = new Dictionary(url);
        dictionary.open();
        WordNet.dictionary = dictionary;
    }

    public static void GenerateHypernymOntology(String word){
        IIndexWord idxWord = dictionary.getIndexWord(word, POS.NOUN);
        if(idxWord == null){
            return;
        }

        HashMap<Pair<Integer, String>, List<IWordID>> senseMap = GetSenses(dictionary, idxWord);
        for(Pair<Integer, String> senseRank : senseMap.keySet()){
            String sense = senseRank.getValue();
            Concept concept = new Concept(word, sense);
            String wordSense = String.format(CONCEPT_SENSE_FORMAT, word, sense);

            if(conceptMap.containsKey(wordSense)) {
                concept = conceptMap.get(wordSense);
            }

            List<IWordID> wordIDList = senseMap.get(senseRank);
            for(IWordID id : wordIDList) {
                GetHypernyms(id, sense, concept);
            }

            conceptMap.put(wordSense, concept);
        }
    }

    private static void GetHypernyms(IWordID wordID, String sense, Concept parentConcept){
        IWord iWord = dictionary.getWord(wordID);
        ISynset synset = iWord.getSynset();
        List<ISynsetID> hypernyms = synset.getRelatedSynsets(Pointer.HYPERNYM);
        if(hypernyms.size() == 0) hypernyms = synset.getRelatedSynsets(Pointer.HYPERNYM_INSTANCE);
        HashMap<String, IWordID> hypernymMap = GetHypernymsMap(dictionary, hypernyms, sense);
        if(hypernymMap.isEmpty()) {
            return;
        }

        IWordID hypernym;
        if(hypernymMap.containsKey(sense)){
            hypernym = hypernymMap.get(sense);
        }
        else if(hypernymMap.containsKey("noun.Tops")){
            sense = "noun.Tops";
            hypernym = hypernymMap.get(sense);
        }
        else {
            sense = hypernymMap.keySet().iterator().next();
            hypernym = hypernymMap.get(sense);
        }

        String hypernymSense = String.format(CONCEPT_SENSE_FORMAT, hypernym.getLemma(), sense);
        if(conceptMap.containsKey(hypernymSense)){
            Concept concept = conceptMap.get(hypernymSense);
            parentConcept.SetHypernym(sense, concept);
            return;
        }

        Concept concept = new Concept(hypernym.getLemma(), sense);
        parentConcept.SetHypernym(sense, concept);
        hypernymSense = String.format(CONCEPT_SENSE_FORMAT, hypernym.getLemma(), sense);
        conceptMap.put(hypernymSense, concept);

        GetHypernyms(hypernym, sense, concept);
    }

    private static HashMap<String, IWordID> GetHypernymsMap(IDictionary dictionary, List<ISynsetID> hypernyms, String baseSense) {
        HashMap<String, IWordID> hypernymMap = new HashMap<>();
        for(ISynsetID sid : hypernyms) {
            List<IWord> words = dictionary.getSynset(sid).getWords();
            Iterator<IWord> iterator = words.iterator();
            IWord synset = iterator.next();
            String sense = synset.getSenseKey().getLexicalFile().toString();

            if(sense.equals(baseSense) || sense.equals("noun.Tops")) {
                if(!hypernymMap.containsKey(sense)) {
                    hypernymMap.put(sense, synset.getID());
                }
            }
        }

        if(hypernymMap.isEmpty() && hypernyms.size() == 1){
            ISynsetID hypernym = hypernyms.get(0);
            List<IWord> words = dictionary.getSynset(hypernym).getWords();
            Iterator<IWord> iterator = words.iterator();
            IWord synset = iterator.next();
            String sense = synset.getSenseKey().getLexicalFile().toString();
            hypernymMap.put(sense, synset.getID());
        }

        return hypernymMap;
    }

    private static HashMap<Pair<Integer, String>, List<IWordID>> GetSenses(IDictionary dictionary, IIndexWord idxWord) {
        List<IWordID> wordIDs = idxWord.getWordIDs();
        HashMap<Pair<Integer, String>, List<IWordID>> senseMap = new HashMap<>();
        int id = 1;
        for(IWordID wordID : wordIDs){
            IWord word = dictionary.getWord(wordID);
            String sense = word.getSenseKey().getLexicalFile().getName();
            List<IWordID> wordIDList = new ArrayList<>();
            if(senseMap.containsKey(sense)) {
                wordIDList = senseMap.get(sense);
            }

            wordIDList.add(wordID);
            Pair<Integer, String> pair = new Pair<>(id++, sense);
            senseMap.put(pair, wordIDList);
        }

        return senseMap;
    }

    public static String GenerateConceptToASPCode(List<Concept> concepts) {
        List<Rule> rules = new ArrayList<>();
        rules.addAll(ConvertToHypernymRules(concepts));
        return GenerateASPCode(rules);
    }

    private static HashSet<Rule> ConvertToHypernymRules(List<Concept> concepts) {
        HashSet<Rule> rules = new HashSet<>();
        for(Concept concept : concepts) {
            Word predicateWord = new Word(concept.baseConcept, false);
            Literal objectLiteral = new Literal(new Word("X", true));
            Literal senseLiteral = new Literal(new Word(concept.sense, false));
            List<Literal> bodyList = new ArrayList<>();
            bodyList.add(objectLiteral);
            bodyList.add(senseLiteral);
            Literal body = new Literal(predicateWord, bodyList);
            bodyList = new ArrayList<>();
            bodyList.add(body);
            for (String sense : concept.hypernymMap.keySet()) {
                List<Concept> hypernyms = concept.hypernymMap.get(sense);
                for (Concept hypernym : hypernyms) {
                    List<Literal> terms = new ArrayList<>();
                    Word superclassWord = new Word(hypernym.baseConcept, false);
                    terms.add(objectLiteral);
                    String senseString = sense.replace("noun.", "noun_");
                    terms.add(new Literal(new Word(senseString, false)));
                    Literal head = new Literal(superclassWord, terms);
                    Rule rule = new Rule(head, bodyList, false);
                    rules.add(rule);
                }
            }
        }

        return rules;
    }

    private static String GenerateASPCode(List<Rule> rules) {
        StringBuilder builder = new StringBuilder();
        for(Rule rule : rules){
            builder.append(rule.toString() + ".");
            builder.append("\n");
        }

        return builder.toString();
    }

    public static List<Rule> WriteOntology(StorageManager manager, boolean shouldWriteToFile) throws IOException {
        List<Rule> rules = new ArrayList<>();
        for(String baseConcept : baseConceptMap.keySet()){
            List<Concept> concepts = baseConceptMap.get(baseConcept);
            if(shouldWriteToFile) {
                String aspCode = GenerateConceptToASPCode(concepts);
                manager.WriteConceptToFile(aspCode, baseConcept, null, shouldWriteToFile);
            }
            else {
                rules.addAll(ConvertToHypernymRules(concepts));
                rules.addAll(ConvertToWordSenseDisambiguationRules(baseConcept, concepts));
            }
        }

        return rules;
    }

    public static List<Rule> ConvertToWordSenseDisambiguationRules(String baseConcept, List<Concept> concepts) {
        List<Rule> rules = new ArrayList<>();
        for(Concept concept : concepts){
            Rule rule = GenerateDefaultRule(concept);
            rules.add(rule);
        }

        if(concepts.size() < 1) return rules;
        rules.addAll(GeneratePreferenceRules(baseConcept, concepts));
        return rules;
    }

    private static List<Rule> GeneratePreferenceRules(String baseConcept, List<Concept> concepts) {
        if(concepts.size() < 1) return new ArrayList<>();
        List<Rule> rules = new ArrayList<>();
        concepts = GetSenseRanks(baseConcept, concepts);
        if(concepts.size() < 1) return new ArrayList<>();
        Rule rule = GenerateDefaultPreference(baseConcept, concepts);
        rules.add(rule);
        Word baseWord = new Word(baseConcept, false);
        Literal variable = new Literal(new Word("X", true));
        for(int i=1;i<concepts.size();i++){
            Concept senseConcept = concepts.get(i);
            Literal sense = new Literal(new Word(senseConcept.sense, false));
            List<Literal> terms = new ArrayList<>();
            terms.add(variable);
            terms.add(sense);
            Literal head = new Literal(baseWord, terms);
            Literal strongException = new Literal(baseWord, terms);
            strongException.isNAF = true;
            strongException.isClassicalNegation = true;

            terms = new ArrayList<>();
            terms.add(variable);
            Literal baseCheck = new Literal(baseWord, terms);

            List<Literal> bodyList = new ArrayList<>();
            bodyList.add(baseCheck);
            bodyList.add(strongException);

            for(int j=0;j<concepts.size();j++){
                senseConcept = concepts.get(j);
                sense = new Literal(new Word(senseConcept.sense, false));
                terms = new ArrayList<>();
                terms.add(variable);
                terms.add(sense);
                Literal otherSense = new Literal(baseWord, terms);
                if(j < i){
                    otherSense.isClassicalNegation = true;
                }
                else if(j > i){
                    otherSense.isNAF = true;
                }
                else continue;
                bodyList.add(otherSense);
            }

            rule = new Rule(head, bodyList, false);
            rules.add(rule);
        }
        return rules;
    }

    private static Rule GenerateDefaultPreference(String baseConcept, List<Concept> concepts) {
        Word baseWord = new Word(baseConcept, false);
        Literal variable = new Literal(new Word("X", true));
        Concept defaultConcept = concepts.get(0);
        Literal sense = new Literal(new Word(defaultConcept.sense, false));
        List<Literal> terms = new ArrayList<>();
        terms.add(variable);
        terms.add(sense);
        Literal head = new Literal(baseWord, terms);

        Literal strongException = new Literal(baseWord, terms);
        strongException.isClassicalNegation = true;
        strongException.isNAF = true;

        terms = new ArrayList<>();
        terms.add(variable);
        Literal baseLiteral = new Literal(baseWord, terms);

        List<Literal> bodyList = new ArrayList<>();
        bodyList.add(baseLiteral);
        bodyList.add(strongException);

        for(int i=1;i<concepts.size();i++) {
            Concept senseConcept = concepts.get(i);
            sense = new Literal(new Word(senseConcept.sense, false));
            terms = new ArrayList<>();
            terms.add(variable);
            terms.add(sense);

            Literal senseLiteral = new Literal(baseWord, terms);
            senseLiteral.isNAF = true;
            bodyList.add(senseLiteral);
        }

        Rule defaultRule = new Rule(head, bodyList, false);
        return defaultRule;
    }

    private static List<Concept> GetSenseRanks(String baseConcept, List<Concept> concepts) {
        IIndexWord idxWord = dictionary.getIndexWord(baseConcept, POS.NOUN);
        if(idxWord == null) {
            return new ArrayList<>();
        }
        HashMap<Pair<Integer, String>, List<IWordID>> senseMap = GetSenses(dictionary, idxWord);
        HashMap<String, Integer> senseRankMap = GetRankMap(senseMap.keySet());
        for(Concept concept : concepts){
            if(senseRankMap.containsKey(concept.sense)) {
                int rank = senseRankMap.get(concept.sense);
                concept.SetRank(rank);
            }
        }
        Collections.sort(concepts);
        return concepts;
    }

    private static HashMap<String, Integer> GetRankMap(Set<Pair<Integer, String>> senseRanks) {
        HashMap<String, Integer> senseRankMap = new HashMap<>();
        for(Pair<Integer, String> senseRank : senseRanks){
            String sense = senseRank.getValue().replaceAll("\\.", "_");
            senseRankMap.put(sense, senseRank.getKey());
        }

        return senseRankMap;
    }

    public static Rule GenerateDefaultRule(Concept concept) {
        String senseType = concept.sense.split("_")[1];
        Word baseWordPredicate = new Word(concept.baseConcept, false);
        Literal variable = new Literal(new Word("X", true));
        Literal senseLiteral = new Literal(new Word(concept.sense, false));
        List<Literal> terms = new ArrayList<>();
        terms.add(variable);
        terms.add(senseLiteral);
        Literal head = new Literal(baseWordPredicate, terms);
        Literal strongException = new Literal(baseWordPredicate, terms);
        strongException.isNAF = true;
        strongException.isClassicalNegation = true;

        List<Literal> bodyList = new ArrayList<>();
        terms = new ArrayList<>();
        terms.add(variable);
        bodyList.add(new Literal(baseWordPredicate, terms));

        Word propertyPredicateWord = new Word("properties_" + senseType, false);
        Literal propertyException = new Literal(propertyPredicateWord, terms);
        bodyList.add(propertyException);
        bodyList.add(strongException);

        Rule rule = new Rule(head, bodyList, false);
        return rule;
    }

    public static String WriteStoryFacts(StorageManager manager, String aspCode, boolean shouldWriteToFile) throws IOException {
        ArrayList<String> headers = new ArrayList<>();
        for(String header : baseConceptMap.keySet()){
            header = Concept.GetSanitizedString(header);
            String relativeHeaderPath = String.format("%s\\\\%s", StorageManager.ONTOLOGY_FOLDER, header);
            headers.add(relativeHeaderPath);
        }

        if(shouldWriteToFile) {
            manager.WriteStoryToFile(aspCode, headers, shouldWriteToFile);
        }

        StringBuilder builder = new StringBuilder();
        for(String header : headers){
            builder.append(header + "\n");
        }

        builder.append(aspCode);
        return builder.toString();
    }

    public static List<Rule> GenerateBaseRulesForNouns(Set<String> nouns) {
        List<Rule> rules = new ArrayList<>();
        for (String noun : nouns) {
            IIndexWord idxWord = dictionary.getIndexWord(noun, POS.NOUN);
            if (idxWord == null) continue;
            Word baseWord = new Word(noun, false);
            List<Literal> terms = new ArrayList<>();
            terms.add(new Literal(baseWord));
            Literal head = new Literal(baseWord, terms);
            Rule rule = new Rule(head, null, false);
            rules.add(rule);
        }
        return rules;
    }

    public static boolean IsDictionaryWord(Word word) {
        try {
            if(WordNet.dictionary == null){
                InitializeDictionary();
            }
        }
        catch (IOException exception){
            System.out.print("Unable to reach dictionary");
            return false;
        }

        IIndexWord idxWord = dictionary.getIndexWord(word.getLemma(), POS.NOUN);
        return idxWord != null;
    }
}
