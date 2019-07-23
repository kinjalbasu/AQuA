import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ClevrQuestionQuantity {

    public static List<Rule> getQuantityRules(Question question) {
        List<Rule> rules = new ArrayList<>();
        List<Word> nouns = question.wordList.stream().filter(w -> w.getPOSTag().toLowerCase().matches("nn|nnp|nns|nnps")).collect(Collectors.toList());
        ClevrQuestionCommonRules.generateFacts(question, nouns);

        if (!question.semanticRoot.getRelationMap().getOrDefault("nsubj", new ArrayList<>()).isEmpty() &&
                !question.semanticRoot.getRelationMap().getOrDefault("expl", new ArrayList<>()).isEmpty()) {
            rules.addAll(getSingleObjectCount(question));
        }

        rules = ClevrQuestionCommonRules.modifyCommonQueryRules(rules, false);

        rules.addAll(ClevrQuestionCommonRules.getCommonQueryRules(question, false));

        return rules;
    }

    private static List<Rule> getSingleObjectCount(Question question) {
        List<Rule> rules = new ArrayList<>();
        Literal head = null;
        List<Literal> body = new ArrayList<>();

        Word object = question.semanticRoot.getRelationMap().get("nsubj").get(0);

        List<Literal> terms = new ArrayList<>();
        terms.add(new Literal(new Word(object.getLemma(), false)));
        terms.add(new Literal(new Word(Integer.toString(object.getWordIndex()), false)));
        terms.add(new Literal(new Word("L", true)));
        body.add(new Literal(new Word("find_all_filters", false), terms));

        terms = new ArrayList<>();
        terms.add(new Literal(new Word("L", true)));
        terms.add(new Literal(new Word("Ids", true)));
        body.add(new Literal(new Word("list_object", false), terms));

        terms = new ArrayList<>();
        terms.add(new Literal(new Word("Ids", true)));
        terms.add(new Literal(new Word("A", true)));
        body.add(new Literal(new Word("list_length", false), terms));
        rules.add(new Rule(head, body, true));
        return rules;
    }
}
