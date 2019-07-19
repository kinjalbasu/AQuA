import java.util.ArrayList;
import java.util.List;

public class ClevrQuestionCommonRules {

    public static List<Rule> getCommonQueryRules(Question question, boolean isBooleanQuestion) {
        Literal head = null;
        List<Literal> body = new ArrayList<>();
        List<Literal> terms = new ArrayList<>();
        List<Rule> commonRules = new ArrayList<>();
        if (isBooleanQuestion) {
            terms.add(new Literal(new Word(question.sentenceString.replaceAll("'", ""), false)));
            terms.add(new Literal(new Word("true", false)));
            head = new Literal(new Word("question_answer", false), terms);

            terms = new ArrayList<>();
            terms.add(new Literal(new Word(question.sentenceString.replaceAll("'", ""), false)));
            body.add(new Literal(new Word("find_ans", false), terms));

            commonRules.add(new Rule(head, body, true));


            terms = new ArrayList<>();
            terms.add(new Literal(new Word(question.sentenceString.replaceAll("'", ""), false)));
            terms.add(new Literal(new Word("false", false)));
            head = new Literal(new Word("question_answer", false), terms);


            body = new ArrayList<>();
            terms = new ArrayList<>();
            terms.add(new Literal(new Word(question.sentenceString.replaceAll("'", ""), false)));
            body.add(new Literal(new Word("find_ans", false), terms, true));

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
        if (isBooleanQuestion) {
            Literal head = null;
            //List<Literal> body = new ArrayList<>();
            List<Literal> terms = new ArrayList<>();

            terms.add(new Literal(new Word("Q", true)));
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
        }


        return rules;
    }

}
