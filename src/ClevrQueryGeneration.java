import org.json.JSONException;

import java.io.*;
import java.net.URISyntaxException;
import java.util.*;
import java.util.stream.Collectors;

public class ClevrQueryGeneration {


    public static List<Rule> getClevrQuery(Question question) {
        List<Rule> rules = new ArrayList<>();
        if (question.information.answerType == AnswerType.BOOLEAN || question.information.questionType == QuestionType.TRUE_FALSE) {
            rules = getBooleanRules(question);
        }
        return rules;
    }

    private static List<Rule> getBooleanRules(Question question) {
        List<Rule> rules = new ArrayList<>();
        List<Word> nouns = question.wordList.stream().filter(w -> w.getPOSTag().toLowerCase().matches("nn|nnp|nns|nnps")).collect(Collectors.toList());
        //Map<String,List<String>> commonSenseConceptsMap = nouns.stream().collect(Collectors.toMap(n -> n.getWord(), n-> SemanticRelationsGeneration.getConcept(n.getWord())));

        generateFacts(question);
        //Map<Word,List<Word>> properties = getProperties(question,nouns);
        if(nouns.size() == 1){
            rules.addAll(getExistentialRule(nouns.get(0)));
        }
        return rules;
    }

    private static Collection<? extends Rule> getExistentialRule(Word word) {
        List<Rule> rules = new ArrayList<>();
        Literal head = null;
        List<Literal> body = new ArrayList<>();

        List<Literal> terms = new ArrayList<>();
        terms.add(new Literal(new Word(word.getLemma(),false)));
        terms.add(new Literal(new Word("L",true)));
        body.add(new Literal(new Word("find_all_filters",false),terms));

        terms = new ArrayList<>();
        terms.add(new Literal(new Word("L",true)));
        terms.add(new Literal(new Word("Ids",true)));
        body.add(new Literal(new Word("list_object",false),terms));

        terms = new ArrayList<>();
        terms.add(new Literal(new Word("Ids",true)));
        terms.add(new Literal(new Word("C",true)));
        body.add(new Literal(new Word("list_length",false),terms));

        terms = new ArrayList<>();
        terms.add(new Literal(new Word("N",true)));
        terms.add(new Literal(new Word(word.getLemma(),false)));
        body.add(new Literal(new Word("quantification",false),terms));

        terms = new ArrayList<>();
        terms.add(new Literal(new Word("C",true)));
        terms.add(new Literal(new Word("N",true)));
        body.add(new Literal(new Word("gte",false),terms));


        rules.add(new Rule(head,body,true));
        return rules;
    }

    private static Map<Word, List<Word>> getProperties(Question question, List<Word> nouns) {
        Map<Word,List<Word>> properties = new HashMap<>();

        for(Word noun : nouns){
            List<Word> mods = getModifier(noun);
            properties.put(noun,mods);
        }

        return properties;
    }

    private static List<Word> getModifier(Word noun) {
        List<Word> modifiers = new ArrayList<>();
        if(noun.getRelationMap().containsKey("amod")) {
            modifiers.addAll(noun.getRelationMap().get("amod"));
        }

        return modifiers;
    }

    private static void generateFacts(Question question) {
        SemanticRelationsGeneration.generateSemanticRelations(question);
        List<String> factsList = new ArrayList<>();
        factsList.addAll(SemanticRelationsGeneration.getPosFacts());
        factsList.addAll(SemanticRelationsGeneration.getDependenciesFacts());
        factsList.addAll(SemanticRelationsGeneration.getConceptualRelations());
        factsList.addAll(SemanticRelationsGeneration.getSementicRelations());
        String semanticPath = "resources/clevr/clevrSemanticRules.lp";
        String rulesPath = "resources/Rules.lp";
        try {
            File semanticFile = new File(semanticPath);
            FileWriter fw = new FileWriter(semanticFile);
            BufferedWriter bw = new BufferedWriter(fw);
            for (String s:
                    factsList) {
                bw.write(s);
                bw.newLine();
            }
            bw.close();

            Runtime rt = Runtime.getRuntime();
            Process proc = rt.exec("gringo "  + semanticPath + " " + rulesPath + " -t");
            BufferedReader stdInput = new BufferedReader(new
                    InputStreamReader(proc.getInputStream()));
            BufferedReader stdError = new BufferedReader(new
                    InputStreamReader(proc.getErrorStream()));
            BufferedWriter rel_bw = new BufferedWriter(new FileWriter(semanticPath, true));
            rel_bw.newLine();
            String s =null;
            while((s = stdInput.readLine()) != null){
                if(s.startsWith("#") || s.startsWith("_")) continue;
                rel_bw.write(s);
                rel_bw.newLine();
            }
            rel_bw.close();
            String err = null;
            while((err = stdError.readLine()) != null){
              //  System.out.println(err);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
