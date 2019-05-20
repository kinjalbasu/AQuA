import edu.stanford.nlp.ling.*;
//import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
//import edu.stanford.nlp.parser.shiftreduce.ShiftReduceParser;
import edu.stanford.nlp.parser.nndep.DependencyParser;
import edu.stanford.nlp.process.DocumentPreprocessor;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import edu.stanford.nlp.trees.*;
import javafx.util.Pair;
import java.io.StringReader;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by dhruv on 9/24/2017.
 */
public class Sentence {

    public static final String DELIMITER = " ";
    //public static LexicalizedParser parser;
    //public static ShiftReduceParser parser;
    public static  DependencyParser parser;
    public static StanfordCoreNLP pipeline;

    protected String sentenceString = "";
    protected List<TypedDependency> dependencies = null;
    protected Word semanticRoot = null;
    protected List<Word> wordList = new ArrayList<>();
    protected List<Rule> preProcessRules = new ArrayList<>();

    public static void SetupLexicalizedParser() {
        //parser = LexicalizedParser.loadModel("edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz");
        //parser = ShiftReduceParser.loadModel("edu/stanford/nlp/models/srparser/englishSR.ser.gz");
        parser = DependencyParser.loadFromModelFile(DependencyParser.DEFAULT_MODEL);
        Properties props = new Properties();
        props.setProperty("annotators", "tokenize, ssplit, pos, lemma, ner");
        pipeline = new StanfordCoreNLP(props);
    }

    protected Sentence(String sentence) {
        int currentEventId = Word.eventId;
        List<Word> wordList = ProcessSentence(sentence, false);
        sentence = IsQuestion(sentence) ? PreprocessSentence(wordList,true) : PreprocessSentence(wordList,false);
        Word.eventId = currentEventId;
        this.wordList = ProcessSentence(sentence, true);
        List<TypedDependency> dependencies = GetDependencies(sentence);
        this.sentenceString = sentence;
        this.dependencies = dependencies;
        this.semanticRoot = GenerateSemanticTree(dependencies, this.wordList);
        Word.SetWordIds(this.wordList);
    }

    private List<Word> getFormattedList(List<Word> wordList) {
        List<Word> clonedList = wordList.stream().map(Word::new).collect(Collectors.toList());
        return clonedList.stream().map(w -> {
            w.setWord(w.getWord().toLowerCase());
            w.setLemma(w.getLemma().toLowerCase());
            return w;
        }).collect(Collectors.toList());
    }


    private String PreprocessSentence(List<Word> inputList, boolean isQ) {
        List<Word> lowerCasedList = getFormattedList(inputList);
        StringBuilder builder = new StringBuilder();
        Pair<List<Word>, List<Rule>> result = ProcessOrganizations(lowerCasedList);
        lowerCasedList = result.getKey();
        this.preProcessRules.addAll(result.getValue());

        result = ProcessDates(lowerCasedList);
        lowerCasedList = result.getKey();
        this.preProcessRules.addAll(result.getValue());

        result = ProcessBirthAndDeathDates(lowerCasedList);
        lowerCasedList = result.getKey();
        this.preProcessRules.addAll(result.getValue());

        this.preProcessRules.addAll(GeneratePreProcessRules(lowerCasedList));
        boolean hasFoundBracket = false;
        for (Word word : inputList) {
            if (word.getWord().equalsIgnoreCase("-LRB-")) hasFoundBracket = true;
            else if (word.getWord().equalsIgnoreCase("-RRB-")) {
                hasFoundBracket = false;
                continue;
            }
            if (hasFoundBracket) continue;
            builder.append(word.getWord() + " ");
        }
        if (!isQ) builder.append(".");
        return builder.toString().trim();
    }

    private Pair<List<Word>, List<Rule>> ProcessBirthAndDeathDates(List<Word> inputList) {
        List<Rule> rules = new ArrayList<>();
        List<Word> wordList = new ArrayList<>();
        List<Word> wordCollection = new ArrayList<>();
        boolean hasFoundBracket = false;
        Word previousNounWord = inputList.size() > 0 ? inputList.get(0) : null;
        if (previousNounWord != null) wordList.add(previousNounWord);
        for (int i = 1; i < inputList.size(); i++) {
            Word currentWord = inputList.get(i);
            if (currentWord.getWord().equalsIgnoreCase("-LRB-")) {
                hasFoundBracket = true;
                wordCollection = new ArrayList<>();
                wordCollection.add(currentWord);
                continue;
            } else if (currentWord.getWord().equalsIgnoreCase("-RRB-")) {
                hasFoundBracket = false;
                wordCollection.add(currentWord);
                if (!CheckForLifeSpanFormat(wordCollection) || previousNounWord == null) {
                    wordList.addAll(wordCollection);
                    continue;
                }

                Word birthDate = wordCollection.get(1);
                Word deathDate = wordCollection.get(3);
                Word birthDateWord = new Word("_start_date", false);
                Word deathDateWord = new Word("_end_date", false);

                List<Literal> terms = new ArrayList<>();
                terms.add(new Literal(previousNounWord));
                terms.add(new Literal(birthDate));
                Literal head = new Literal(birthDateWord, terms);
                Rule rule = new Rule(head, null, false);
                rules.add(rule);

                terms = new ArrayList<>();
                terms.add(new Literal(previousNounWord));
                terms.add(new Literal(deathDate));
                head = new Literal(deathDateWord, terms);
                rule = new Rule(head, null, false);
                rules.add(rule);
                continue;
            }
            if (hasFoundBracket) {
                wordCollection.add(currentWord);
                continue;
            }
            if (currentWord.IsNoun() && !hasFoundBracket) previousNounWord = currentWord;
            wordList.add(currentWord);
        }

        return new Pair<>(wordList, rules);
    }

    private boolean CheckForLifeSpanFormat(List<Word> wordCollection) {
        if (wordCollection.size() != 5) return false;
        if (!wordCollection.get(0).getWord().equalsIgnoreCase("-LRB-")) return false;
        if (wordCollection.get(1).getNERTag() != NamedEntityTagger.NamedEntityTags.DATE) return false;
        if (wordCollection.get(3).getNERTag() != NamedEntityTagger.NamedEntityTags.DATE) return false;
        if (!wordCollection.get(2).getWord().equalsIgnoreCase("--")) return false;
        if (!wordCollection.get(4).getWord().equalsIgnoreCase("-RRB-")) return false;
        return true;
    }

    private Pair<List<Word>, List<Rule>> ProcessOrganizations(List<Word> inputList) {
        List<Rule> rules = new ArrayList<>();
        List<Word> wordList = new ArrayList<>();
        List<Word> organizationWords = new ArrayList<>();
        Word orgWord = new Word("organization", false);
        for (Word word : inputList) {
            if (word.getPOSTag().equals("NNP") && word.getNERTag() == NamedEntityTagger.NamedEntityTags.ORGANIZATION) {
                organizationWords.add(word);
            } else {
                if (organizationWords.size() != 0) {
                    Word organization = Word.CreateCompoundWord(organizationWords);
                    organization.SetNERTag(NamedEntityTagger.NamedEntityTags.ORGANIZATION);
                    wordList.add(organization);
                    organizationWords = new ArrayList<>();

                    List<Literal> terms = new ArrayList<>();
                    terms.add(new Literal(organization));
                    Literal head = new Literal(orgWord, terms);
                    rules.add(new Rule(head, null, false));
                }
                wordList.add(word);
            }
        }

        if (organizationWords.size() != 0) {
            Word organization = Word.CreateCompoundWord(organizationWords);
            organization.SetNERTag(NamedEntityTagger.NamedEntityTags.ORGANIZATION);
            wordList.add(organization);

            List<Literal> terms = new ArrayList<>();
            terms.add(new Literal(organization));
            Literal head = new Literal(orgWord, terms);
            rules.add(new Rule(head, null, false));
        }

        return new Pair<>(wordList, rules);
    }

    private Pair<List<Word>, List<Rule>> ProcessDates(List<Word> inputList) {
        List<Rule> rules = new ArrayList<>();
        List<Word> wordList = new ArrayList<>();
        List<Word> timeWords = new ArrayList<>();
        Word timeWord = new Word("time", false);
        for (Word word : inputList) {
            if (word.getNERTag() == NamedEntityTagger.NamedEntityTags.DATE && !word.getPOSTag().equals(":")) {
                timeWords.add(word);
            } else {
                if (timeWords.size() != 0) {
                    Word date = Word.CreateCompoundWord(timeWords);
                    date.SetNERTag(NamedEntityTagger.NamedEntityTags.DATE);
                    wordList.add(date);

                    List<Literal> terms = new ArrayList<>();
                    terms.add(new Literal(date));
                    Literal head = new Literal(timeWord, terms);
                    rules.add(new Rule(head, null, false));
                    rules.addAll(GenerateRulesForDateParts(date, timeWords));
                    timeWords = new ArrayList<>();
                }
                wordList.add(word);
            }
        }

        if (timeWords.size() != 0) {
            Word date = Word.CreateCompoundWord(timeWords);
            date.SetNERTag(NamedEntityTagger.NamedEntityTags.DATE);
            wordList.add(date);

            List<Literal> terms = new ArrayList<>();
            terms.add(new Literal(date));
            Literal head = new Literal(timeWord, terms);
            rules.add(new Rule(head, null, false));
            rules.addAll(GenerateRulesForDateParts(date, timeWords));
        }

        return new Pair<>(wordList, rules);
    }

    private List<Rule> GenerateRulesForDateParts(Word date, List<Word> timeWords) {
        List<Rule> rules = new ArrayList<>();
        for (Word timePart : timeWords) {
            Word predicate = new Word("time", false);
            List<Literal> terms = new ArrayList<>();
            terms.add(new Literal(date));
            if (timePart.IsDay()) {
                predicate = new Word("day", false);
                terms.add(new Literal(timePart));
            } else if (timePart.IsMonth()) {
                predicate = new Word("month", false);
                terms.add(new Literal(timePart));
            } else if (timePart.IsYear()) {
                predicate = new Word("year", false);
                terms.add(new Literal(timePart));
            }

            Literal head = new Literal(predicate, terms);
            Rule rule = new Rule(head, null, false);
            rules.add(rule);
        }

        return rules;
    }

    private List<Rule> GeneratePreProcessRules(List<Word> inputList) {
        List<Rule> rules = new ArrayList<>();
        rules.addAll(GenerateAbbreviationRules(inputList));
        rules.addAll(GenerateCardinalRules(inputList));
        return rules;
    }

    private List<Rule> GenerateCardinalRules(List<Word> inputList) {
        List<Rule> rules = new ArrayList<>();
        Word numPredicate = new Word("number", false);
        for (Word word : inputList) {
            if (word.IsNumber()) {
                List<Literal> bodyList = new ArrayList<>();
                bodyList.add(new Literal(word));
                Literal head = new Literal(numPredicate, bodyList);
                Rule rule = new Rule(head, null, false);
                rules.add(rule);
            }
        }
        return rules;
    }

    private List<Rule> GenerateAbbreviationRules(List<Word> inputList) {
        // This would only check if the abbreviation is ahead of the long form
        // e.g. National_Football_League (NFL)
        List<Rule> rules = new ArrayList<>();
        List<Word> wordCollection = new ArrayList<>();
        boolean hasFoundBracket = false;
        Word previousNounWord = null;
        for (int i = 1; i < inputList.size(); i++) {
            Word currentWord = inputList.get(i);
            if (currentWord.getWord().equalsIgnoreCase("-LRB-")) {
                hasFoundBracket = true;
                wordCollection = new ArrayList<>();
                continue;
            } else if (currentWord.getWord().equalsIgnoreCase("-RRB-")) {
                hasFoundBracket = false;
                if (previousNounWord == null) continue;
                Word predicateWord = new Word("_abbreviation", false);
                List<Literal> terms = new ArrayList<>();
                Word abbreviation = Word.CreateCompoundWord(wordCollection);
                terms.add(new Literal(abbreviation));
                terms.add(new Literal(new Word(previousNounWord.getWord().toLowerCase(), false)));
                Literal head = new Literal(predicateWord, terms);
                Rule rule = new Rule(head, null, false);
                rules.add(rule);
                continue;
            }
            if (hasFoundBracket) wordCollection.add(currentWord);
            if (currentWord.IsNoun() && !hasFoundBracket) previousNounWord = currentWord;
        }

        return rules;
    }

    private static List<TypedDependency> GetDependencies(String sentence) {
        String[] words = String.format("%s .", sentence).split(DELIMITER);

        MaxentTagger tagger = new MaxentTagger("edu/stanford/nlp/models/pos-tagger/english-left3words/english-left3words-distsim.tagger");
        DocumentPreprocessor tokenizer = new DocumentPreprocessor(new StringReader(sentence));
  /*      Tree parse = new Tree() {
            @Override
            public Tree[] children() {
                return new Tree[0];
            }

            @Override
            public TreeFactory treeFactory() {
                return null;
            }
        };*/


        List<HasWord> s = tokenizer.iterator().next();
        List<TaggedWord> tagged = tagger.tagSentence(s);
        GrammaticalStructure gs = parser.predict(tagged);
        //List<CoreLabel> rawWords = SentenceUtils.toCoreLabelList(words);
        //Tree parse = parser.apply(rawWords);
        //TreebankLanguagePack tlp = new PennTreebankLanguagePack();
        //GrammaticalStructureFactory grammaticalStructureFactory = tlp.grammaticalStructureFactory();
        //GrammaticalStructure grammaticalStructure = grammaticalStructureFactory.newGrammaticalStructure(parse);
        List<TypedDependency> dependencies = gs.typedDependenciesCCprocessed();
        return lowerCasedDependencies(dependencies);
    }

    private static List<TypedDependency> lowerCasedDependencies(List<TypedDependency> dependencies) {
        List<TypedDependency> lowerCasedDependencies = dependencies.stream().map(d -> {
            if (d.dep().backingLabel().word() != null)
                d.dep().backingLabel().setWord(d.dep().backingLabel().word().toLowerCase());
            if (d.dep().backingLabel().value() != null)
                d.dep().backingLabel().setValue(d.dep().backingLabel().value().toLowerCase());
            if (d.gov().backingLabel().word() != null)
                d.gov().backingLabel().setWord(d.gov().backingLabel().word().toLowerCase());
            if (d.gov().backingLabel().value() != null)
                d.gov().backingLabel().setValue(d.gov().backingLabel().value().toLowerCase());
            return d;
        }).collect(Collectors.toList());
        return lowerCasedDependencies;
    }

    private static List<Word> ProcessSentence(String sentenceString, boolean isPreProcessed) {
        List<Word> wordList = new ArrayList<>();
        Annotation annotation = new Annotation(sentenceString);
        pipeline.annotate(annotation);
        // For multiple sentences
        //List<CoreMap> sentences = annotation.get(CoreAnnotations.SentencesAnnotation.class);
        int index = 1;
        for (CoreLabel token : annotation.get(CoreAnnotations.TokensAnnotation.class)) {
            Word word = null;
            String wordString = token.get(CoreAnnotations.TextAnnotation.class);
            String lemmaString = token.getString(CoreAnnotations.LemmaAnnotation.class);
            String posTag = token.get(CoreAnnotations.PartOfSpeechAnnotation.class);
            String NERTag = token.get(CoreAnnotations.NamedEntityTagAnnotation.class);
            if(NERTag.equalsIgnoreCase("NUMBER")){
                String v1 = token.get(CoreAnnotations.NormalizedNamedEntityTagAnnotation.class);
                Double d = Double.parseDouble(v1);
                String v2 = String.valueOf(d.intValue());
                word = new Word(index, v2, lemmaString, posTag, NERTag, isPreProcessed);
            }
            else{
                word = new Word(index, wordString, lemmaString, posTag, NERTag, isPreProcessed);
            }
            wordList.add(word);
            index++;
        }

        return wordList;
    }

    private static Word GenerateSemanticTree(List<TypedDependency> dependencies, List<Word> wordList) {
        Word rootWord = null;
        HashMap<String, Word> wordMap = new HashMap<>();
        for (Word word : wordList) {
            wordMap.put(word.toString(), word);
        }

        for (TypedDependency dependency : dependencies) {
            String dependantString = dependency.dep().backingLabel().toString();
            String independantString = dependency.gov().backingLabel().toString();
            if (dependency.reln().getShortName().equals("root")) {
                if (wordMap.containsKey(dependantString)) {
                    rootWord = wordMap.get(dependantString);
                    continue;
                }
            }

            Word dependentWord = null;
            if (wordMap.containsKey(dependantString)) {
                dependentWord = wordMap.get(dependantString);
            }

            Word independentWord = null;
            if (wordMap.containsKey(independantString)) {
                independentWord = wordMap.get(independantString);
            }

            if (independentWord != null && dependentWord != null) {
                independentWord.AddDependency(dependentWord, dependency.reln());
            }
        }

        return rootWord;
    }

    public static String DependenciesToString(Sentence sentence) {
        StringBuilder builder = new StringBuilder();
        for (TypedDependency dependency : sentence.dependencies) {
            builder.append(dependency.toString() + "\n");
        }

        return builder.toString();
    }

    public List<Rule> GenerateRules() {
        List<Rule> rules = this.preProcessRules;

        for (Word word : this.wordList) {
            if (word.getPOSTag().equalsIgnoreCase(",")) continue;
            rules.addAll(word.GenerateRules());
        }

        rules.addAll(GenerateAlternateCopulaRules());
        return rules;
    }

    private List<Rule> GenerateAlternateCopulaRules() {
        List<Rule> rules = new ArrayList<>();
        for (Word word : this.wordList) {
            if (word.IsNoun() || word.IsAdjective()) {
                rules.addAll(word.GenerateAlternateCopulaRules(this.wordList));
            }
        }

        return rules;
    }

    public static Sentence ParseSentence(String sentence) {
        if (IsQuestion(sentence)) {
            return new Question(sentence);
        }

        return new Sentence(sentence);
    }

    private static boolean IsQuestion(String sentence) {
        if (sentence.endsWith("?"))
            return true;
        return false;
    }

    @Override
    public String toString() {
        return sentenceString;
    }

    public List<String> GetAllNouns() {
        List<String> nouns = new ArrayList<>();
        for (Word word : this.wordList) {
            if (word.getPOSTag().equals("NN") ||
                    word.getPOSTag().equals("NNS")) {
                nouns.add(word.getLemma());
            }
        }
        return nouns;
    }
}
