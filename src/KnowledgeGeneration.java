import edu.mit.jwi.item.IIndexWord;
import edu.mit.jwi.item.POS;
import javafx.util.Pair;

import java.io.*;
import java.util.*;

public class KnowledgeGeneration {

    public static final String END_OF_SENTENCE = "\\.";
    public static final boolean SHOULD_WRITE_TO_FILE = false;

    public static Pair<List<Rule>, List<Rule>> RepresentKnowledge(StorageManager manager, String content, BufferedWriter bw)
            throws IOException {
       // List<String> posFacts = new ArrayList<>();
       // List<String> dependenciesFacts = new ArrayList<>();

        List<Rule> storyRules = new ArrayList<>();
        Sentence.SetupLexicalizedParser();
        List<Sentence> sentenceList = new ArrayList<>();
        String[] sentencesString = content.split(END_OF_SENTENCE);
        Set<String> nouns = new HashSet<>();
        for(String sentenceString : sentencesString){
           // /*******************/System.out.println(sentenceString);
            Sentence sentence = Sentence.ParseSentence(sentenceString.trim());
            System.out.println(sentence.toString()+"\n");
            SemanticRelationsGeneration.generateSemanticRelations(sentence);
            //posFacts.addAll(result.getKey());
            //dependenciesFacts.addAll(result.getValue());

            ///*******************/ System.out.println(Sentence.DependenciesToString(sentence));
            sentenceList.add(sentence);
            nouns.addAll(sentence.GetAllNouns());
            List<Rule> sentenceRules = sentence.GenerateRules();
            //*******************/ PrintRules(sentenceRules);
            storyRules.addAll(sentenceRules);
        }
        //Write POS and SemanticDependencies
        for (String s:
             SemanticRelationsGeneration.getPosFacts()) {
            bw.write(s);
            bw.newLine();
        }
        bw.newLine();
        bw.newLine();
        for (String s:
                SemanticRelationsGeneration.getDependenciesFacts()) {
            bw.write(s);
            bw.newLine();
        }
        bw.newLine();
        bw.newLine();
        for (String s:
                SemanticRelationsGeneration.getSementicRelations()) {
            bw.write(s);
            bw.newLine();
        }
        bw.newLine();
        bw.newLine();
        for (String s:
                SemanticRelationsGeneration.getConceptualRelations()) {
            bw.write(s);
            bw.newLine();
        }



        WordNet.BuildOntology(nouns);
        List<Rule> ontologyRules = WordNet.WriteOntology(manager, SHOULD_WRITE_TO_FILE);
        List<Rule> baseRules = WordNet.GenerateBaseRulesForNouns(nouns);
        storyRules.addAll(baseRules);
        return new Pair<>(storyRules, ontologyRules);
    }

    private static void PrintRules(List<Rule> rules) {
        for(Rule rule : rules){
            System.out.println(rule);
        }
    }
}