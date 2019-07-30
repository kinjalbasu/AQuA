import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class ClevrQuestionValue {


    public static List<Rule> getValueRules(Question question) {
        List<Rule> rules = new ArrayList<>();
        List<Word> nouns = question.wordList.stream().filter(w -> w.getPOSTag().toLowerCase().matches("nn|nnp|nns|nnps")).collect(Collectors.toList());
        ClevrQuestionCommonRules.generateFacts(question, nouns);
        if (question.semanticRoot.getPOSTag().equalsIgnoreCase("vbz") && nouns.size() == 2) {
            rules.addAll(getSimpleValueRules(question));
        } else if (question.semanticRoot.getPOSTag().equalsIgnoreCase("wp") && nouns.size() == 2) {
            rules.addAll(getSimpleValueRules(question));
        }

        rules = ClevrQuestionCommonRules.modifyCommonQueryRules(rules, false);

        rules.addAll(ClevrQuestionCommonRules.getCommonQueryRules(question, false));

        return rules;
    }

    private static List<Rule> getSimpleValueRules(Question question) {
        List<Rule> rules = new ArrayList<>();

        if (question.semanticRoot.getPOSTag().equalsIgnoreCase("vbz") &&
                !question.semanticRoot.getRelationMap().getOrDefault("dobj", new ArrayList<>()).isEmpty() &&
                !question.semanticRoot.getRelationMap().getOrDefault("nsubj", new ArrayList<>()).isEmpty()) {
            String attribute = question.semanticRoot.getRelationMap().get("dobj").get(0).getLemma();
            String referencedObject = question.semanticRoot.getRelationMap().get("nsubj").get(0).getLemma();
            String referencedObjectIndex = Integer.toString(question.semanticRoot.getRelationMap().get("nsubj").get(0).getWordIndex());

            rules.add(getSimpleValueRuleLiterals(attribute, referencedObjectIndex, referencedObject));
        } else if (question.semanticRoot.getPOSTag().equalsIgnoreCase("wp") &&
                !question.semanticRoot.getRelationMap().getOrDefault("nsubj", new ArrayList<>()).isEmpty() &&
                question.dependencies.stream()
                        .filter(d -> d.reln().toString().equalsIgnoreCase("nmod:of")).findFirst().isPresent()) {
            String attribute = question.semanticRoot.getRelationMap().get("nsubj").get(0).getLemma();
            String referencedObjectIndex = Integer.toString(question.dependencies.stream()
                    .filter(d -> d.reln().toString().equalsIgnoreCase("nmod:of")).findFirst().get().dep().index());
            String referencedObject = question.wordList.get(Integer.parseInt(referencedObjectIndex) - 1).getLemma();
            rules.add(getSimpleValueRuleLiterals(attribute, referencedObjectIndex, referencedObject));
        }
        return rules;
    }

    private static Rule getSimpleValueRuleLiterals(String attribute, String referencedObjectIndex, String referencedObject) {
        Literal head = null;
        List<Literal> body = new ArrayList<>();

        List<Literal> terms = new ArrayList<>();
        terms.add(new Literal(new Word(referencedObject, false)));
        terms.add(new Literal(new Word(referencedObjectIndex, false)));
        terms.add(new Literal(new Word("L", true)));
        body.add(new Literal(new Word("find_all_filters", false), terms));


        terms = new ArrayList<>();
        terms.add(new Literal(new Word("L", true)));
        terms.add(new Literal(new Word("Ids", true)));
        body.add(new Literal(new Word("list_object", false), terms));

        terms = new ArrayList<>();
        terms.add(new Literal(new Word("Ids", true)));
        terms.add(new Literal(new Word(attribute, false)));
        terms.add(new Literal(new Word("A", true)));
        body.add(new Literal(new Word("get_att_val", false), terms));

        return new Rule(head, body, true);
    }


}
