//package scasp;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.*;

import javafx.util.Pair;

public class Scasp_question {


    //public static String content = "What does AC stand for?";
    public static void TestSetupParser() {
        Sentence.SetupLexicalizedParser();
    }


    public static void InitializeTest() {
        Word.eventId = 1;
    }

    public static void PrintRules(TreeSet<String> ruleString) {
        for (String rule : ruleString) {
          //  System.out.println(String.format("%s.", rule));
        }
    }

    public static void printQuestion(String content, BufferedWriter bw) throws IOException {

//        TestSetupParser();
        InitializeTest();

        // TODO code application logic here
        //String content = "Since what year did ABC stylize abc's logo, as abc ?";
        Question question = new Question(content);
        //System.out.println(Sentence.DependenciesToString(question));
        //----------------------------------------------------------------------------------------------------
        /*if (question.information.questionType == QuestionType.WHO && question.semanticRoot.getPOSTag().equals("NN")){
            String keyWord = question.semanticRoot.getWord();
            String resp = HttpRequest.getKnowledge(keyWord);
        }*/

        //----------------------------------------------------------------------------------------------------
        //List<Rule> semanticRules = SemanticQuery.getSemanticQueries(question);
        List<Rule> rules = question.GenerateAllRules();
        LiteralType type = LiteralType.FACT;
        //System.out.println("/*----------------  " + type.toString() + "  ------------------*/");
        for (Rule rule : rules) {
            if (type != rule.maxRuleQuality) {
                type = rule.maxRuleQuality;
                //System.out.println("/*----------------  " + type.toString() + "  ------------------*/");
            }
            //System.out.println(String.format("Assert.assertTrue(ruleString.contains(\"%s\"));", rule.toString()));
        }

        //System.out.print("\n\n");
        //List<String> ruleString = new ArrayList<>();
        String sentence = question.sentenceString.replaceAll("'", "");
        bw.write(String.format("question('%s', 1).", sentence));
        bw.newLine();
        for (Rule rule : rules) {
            //System.out.println(String.format("%s.", rule.toString()));
            //ruleString.add(rule.toString());
            bw.write(String.format("%s.", rule.toString()));
            bw.newLine();
        }
    }
}
