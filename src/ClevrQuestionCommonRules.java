import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ClevrQuestionCommonRules {

    public static List<Rule> getCommonQueryRules(Question question, boolean isBooleanQuestion) {
        Literal head = null;
        List<Literal> body = new ArrayList<>();
        List<Literal> terms = new ArrayList<>();
        List<Rule> commonRules = new ArrayList<>();
        if (isBooleanQuestion) {
            terms.add(new Literal(new Word(question.sentenceString.replaceAll("'", ""), false)));
            terms.add(new Literal(new Word("yes", false)));
            head = new Literal(new Word("question_answer", false), terms);

            terms = new ArrayList<>();
            terms.add(new Literal(new Word(question.sentenceString.replaceAll("'", ""), false)));
            body.add(new Literal(new Word("find_ans", false), terms));

            commonRules.add(new Rule(head, body, true));


            terms = new ArrayList<>();
            terms.add(new Literal(new Word(question.sentenceString.replaceAll("'", ""), false)));
            terms.add(new Literal(new Word("no", false)));
            head = new Literal(new Word("question_answer", false), terms);


            body = new ArrayList<>();
            terms = new ArrayList<>();
            terms.add(new Literal(new Word(question.sentenceString.replaceAll("'", ""), false)));
            body.add(new Literal(new Word("find_ans", false), terms, true));

            commonRules.add(new Rule(head, body, true));

        }
        else{
            terms.add(new Literal(new Word(question.sentenceString.replaceAll("'", ""), false)));
            terms.add(new Literal(new Word("A", true)));
            head = new Literal(new Word("question_answer", false), terms);

            terms = new ArrayList<>();
            terms.add(new Literal(new Word(question.sentenceString.replaceAll("'", ""), false)));
            terms.add(new Literal(new Word("A", true)));
            body.add(new Literal(new Word("find_ans", false), terms));

            commonRules.add(new Rule(head, body, true));

        }

        terms = new ArrayList<>();
        terms.add(new Literal(new Word("Q", true)));
        terms.add(new Literal(new Word("A", true)));
        head = new Literal(new Word("query", false), terms);


        body = new ArrayList<>();
        terms = new ArrayList<>();
        terms.add(new Literal(new Word("Q", true)));
        body.add(new Literal(new Word("question", false), terms));

        terms = new ArrayList<>();
        terms.add(new Literal(new Word("Q", true)));
        terms.add(new Literal(new Word("A", true)));
        body.add(new Literal(new Word("question_answer", false), terms));

        commonRules.add(new Rule(head, body, true));

        head = null;
        body = new ArrayList<>();
        terms = new ArrayList<>();
        terms.add(new Literal(new Word(question.sentenceString.replaceAll("'", ""), false)));
        body.add(new Literal(new Word("question", false), terms));
        commonRules.add(0, new Rule(head, body, true));

        return commonRules;
    }

    public static List<Rule> modifyCommonQueryRules(List<Rule> rules, boolean isBooleanQuestion) {

        Literal head = null;
        //List<Literal> body = new ArrayList<>();
        List<Literal> terms = new ArrayList<>();
        terms.add(new Literal(new Word("Q", true)));
        if (!isBooleanQuestion){
            terms.add(new Literal(new Word("A",true)));
        }
        head = new Literal(new Word("find_ans", false), terms);
        terms = new ArrayList<>();
        terms.add(new Literal(new Word("Q", true)));
        Literal questionLiteral = new Literal(new Word("question", false), terms);

        for (Rule r : rules) {
            r.setHead(head);
            List<Literal> body = r.getBody();
            body.add(0, questionLiteral);
            r.setBody(body);
        }


        return rules;
    }


    public static void generateFacts(Question question, List<Word> nouns) {
        SemanticRelationsGeneration.initializeLists();
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
            for (String s :
                    factsList) {
                bw.write(s);
                bw.newLine();
            }
            bw.close();

            Runtime rt = Runtime.getRuntime();
            Process proc = rt.exec("gringo " + semanticPath + " " + rulesPath + " -t");
            BufferedReader stdInput = new BufferedReader(new
                    InputStreamReader(proc.getInputStream()));
            BufferedReader stdError = new BufferedReader(new
                    InputStreamReader(proc.getErrorStream()));
            BufferedWriter rel_bw = new BufferedWriter(new FileWriter(semanticPath, true));
            rel_bw.newLine();
            String s = null;
            List<String> values = new ArrayList<>();
            while ((s = stdInput.readLine()) != null) {
                if (s.startsWith("#") || s.startsWith("_")) {
                    continue;
                }
                if (s.startsWith("value")) {
                    values.add(s);
                }
                rel_bw.write(s);
                rel_bw.newLine();
            }
            values = getValues(values, nouns);

            for (String val :
                    values) {
                rel_bw.write(val);
                rel_bw.newLine();
            }
            //add values.
            rel_bw.close();
            String err = null;
            while ((err = stdError.readLine()) != null) {
                //  System.out.println(err);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private static List<String> getValues(List<String> values, List<Word> nouns) {
        List<String> valuesFindAll = new ArrayList<>();
        Map<String, List<String>> valueMap = new HashMap<>();
        for (Word n : nouns) {
            valueMap.put(n.getLemma() + "_" + n.getWordIndex(), new ArrayList<>());
        }
        values.stream().forEach(s -> {
            String[] keyVal = s.substring(s.indexOf("(") + 1, s.indexOf(")")).split(",");
            valueMap.get(keyVal[1]).add(keyVal[0].split("_")[0]);
        });
        valueMap.forEach((k, v) -> {
            String val = "values(" + k.split("_")[0] + "," + k.split("_")[1] + ",[" + v.stream().collect(Collectors.joining(",")) + "]).";
            valuesFindAll.add(val);
        });
        return valuesFindAll;
    }


}
