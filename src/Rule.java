import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Created by dhruv on 9/24/2017.
 */
public class Rule implements Comparable<Rule> {
    private Literal head;
    private List<Literal> body;
    private boolean isQuestion = false;
    public LiteralType maxRuleQuality = LiteralType.FACT;

    public Rule(Literal head, List<Literal> body, boolean isQuestion) {
        this.head = head;
        this.body = body;
        this.isQuestion = isQuestion;
    }

    @Override
    public String toString() {
        if (head == null && body == null) return "";
        if (head == null && body.size() == 0) return "";

        if (this.body == null || this.body.size() == 0) {
            return this.head.toString();
        }

        if (head == null) {
            return BodyToString(this.body);
        }

        String headLiteral = this.head.toString();
        String bodyLiteral = BodyToString(this.body);

        return String.format("%s :- %s", headLiteral, bodyLiteral);
    }

    private String BodyToString(List<Literal> body) {
        StringBuilder builder = new StringBuilder();
        for (Literal literal : body) {
            builder.append(String.format("%s,", literal.toString()));
        }

        return builder.substring(0, builder.length() - 1).toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Rule rule = (Rule) o;

        return this.toString().equals(rule.toString());
    }

    @Override
    public int hashCode() {
        return 1;
    }

    @Override
    public int compareTo(Rule rule) {
        int rank = Rule.GetRuleRank(this);
        int otherRank = Rule.GetRuleRank(rule);

        if (rank == otherRank) {
            return this.toString().compareTo(rule.toString());
        }

        return otherRank - rank;
    }

    private static boolean IsFact(Rule rule) {
        if (rule.head != null && rule.body == null) {
            return true;
        }

        return false;
    }

    private static int GetRuleRank(Rule rule) {
        if (rule.isQuestion) {
            return 1;
        }

        if (IsFact(rule)) {
            return 3;
        }

        return 2;
    }

    public static Rule AggregateAllRules(List<Rule> rules) {
        Set<Literal> literalSet = new TreeSet<>();
        for (Rule rule : rules) {
            if (rule.head != null) {
                if (rule.body != null && rule.body.size() == 0) continue;
                literalSet.add(rule.head);
            } else if (rule.body != null && rule.body.size() > 0) {
                literalSet.addAll(rule.body);
            }
        }

        List<Literal> bodyList = new ArrayList<>(literalSet);
        Rule rule = new Rule(null, bodyList, false);
        return rule;
    }

    public static Rule ApplyConstraint(Rule eventQuery, Rule constraint) {
        List<Rule> rules = new ArrayList<>();
        rules.add(eventQuery);
        rules.add(constraint);
        Rule combinedRule = Rule.AggregateAllRules(rules);
        return combinedRule;
    }

    public static Rule FilterRule(Rule inputRule, LiteralType maxLiteralType) {
        if (inputRule.head != null && inputRule.body != null && inputRule.body.size() > 0) return null;
        int maxLiteralQuality = maxLiteralType.ordinal();
        List<Literal> filteredLiterals = new ArrayList<>();
        for (Literal bodyTerm : inputRule.body) {
            LiteralType literalType = bodyTerm.GetLiteralType();
            int literalQuality = literalType.ordinal();
            if (literalQuality < maxLiteralQuality) continue;
            filteredLiterals.add(bodyTerm);
        }

        Rule filteredRule = new Rule(null, filteredLiterals, true);
        filteredRule.maxRuleQuality = maxLiteralType;
        return filteredRule;
    }

    public void SetQuery(String sentenceString, QuestionInformation information) {
        if (this.head != null) return;
        Word predicate = new Word("question", false);
        List<Literal> terms = new ArrayList<>();
        terms.add(new Literal(new Word(String.format("%s", sentenceString), false)));
        terms.add(new Literal(new Word(String.format("%s", GetConfidenceStringFromValue(this.maxRuleQuality.ordinal() + 1)), false)));
        terms.add(new Literal(new Word(String.format("%s", information.questionWord.GetAnswerID()), true)));
        Literal queryHead = new Literal(predicate, terms);
        this.head = queryHead;
    }

    private String GetConfidenceStringFromValue(int value) {
        switch (value) {
            case 1:
                return "certain";
            case 2:
                return "likely";
            case 3:
                return "possible";
        }
        return "guess";
    }
}
