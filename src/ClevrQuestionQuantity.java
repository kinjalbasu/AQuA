import java.util.ArrayList;
import java.util.Collection;
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
        } else if (question.semanticRoot.getPOSTag().equalsIgnoreCase("JJ") &&
                question.semanticRoot.getLemma().matches("left|right")) {
            rules.addAll(getReferencedRule(question));
        } else if (question.semanticRoot.getPOSTag().equalsIgnoreCase("vbp") &&
                (!question.semanticRoot.getRelationMap().getOrDefault("nmod:behind", new ArrayList<>()).isEmpty() ||
                        !question.semanticRoot.getRelationMap().getOrDefault("nmod:in_front_of", new ArrayList<>()).isEmpty())) {
            rules.addAll(getReferencedRule(question));
        } else if (question.semanticRoot.getPOSTag().matches("NNS") &&
                question.dependencies.stream().filter(d -> d.reln().getShortName().equalsIgnoreCase("conj")
                        && d.reln().getSpecific().equalsIgnoreCase("or")
                        && question.wordList.get(d.gov().index() - 1).getLemma().equalsIgnoreCase(question.semanticRoot.getLemma())).findFirst().isPresent()) {
            rules.addAll(getOrRules(question));
        }
        rules = ClevrQuestionCommonRules.modifyCommonQueryRules(rules, false);

        rules.addAll(ClevrQuestionCommonRules.getCommonQueryRules(question, false));

        return rules;
    }

    private static List<Rule> getOrRules(Question question) {
        List<Rule> rules = new ArrayList<>();
        if (question.semanticRoot.getPOSTag().matches("NNS") &&
                question.dependencies.stream().filter(d -> d.reln().getShortName().equalsIgnoreCase("conj")
                        && d.reln().getSpecific().equalsIgnoreCase("or")
                        && question.wordList.get(d.gov().index() - 1).getLemma().equalsIgnoreCase(question.semanticRoot.getLemma())).findFirst().isPresent() &&
                !question.semanticRoot.getRelationMap().getOrDefault("nsubj", new ArrayList<>()).isEmpty()) {
            String object1Index = Integer.toString(question.semanticRoot.getWordIndex());
            String object1 = question.semanticRoot.getLemma();
            String object2Index = Integer.toString(question.semanticRoot.getRelationMap().get("conj").get(0).getWordIndex());
            String object2 = question.semanticRoot.getRelationMap().get("conj").get(0).getLemma();
            String parentObjectIndex = Integer.toString(question.semanticRoot.getRelationMap().get("nsubj").get(0).getWordIndex());
            String parentObject = question.semanticRoot.getRelationMap().get("nsubj").get(0).getLemma();

            rules.add(getOrPredicates(object1, object1Index, object2, object2Index, parentObject, parentObjectIndex));
        }
        return rules;
    }

    private static Rule getOrPredicates(String object1, String object1Index, String object2, String object2Index, String parentObject, String parentObjectIndex) {

        Literal head = null;
        List<Literal> body = new ArrayList<>();

        List<Literal> terms = new ArrayList<>();
        terms.add(new Literal(new Word(parentObject, false)));
        terms.add(new Literal(new Word(parentObjectIndex, false)));
        terms.add(new Literal(new Word("L1", true)));
        body.add(new Literal(new Word("find_all_filters", false), terms));

        terms = new ArrayList<>();
        terms.add(new Literal(new Word("L1", true)));
        terms.add(new Literal(new Word("L2", true)));
        body.add(new Literal(new Word("list_object", false), terms));


        terms = new ArrayList<>();
        terms.add(new Literal(new Word(object1, false)));
        terms.add(new Literal(new Word(object1Index, false)));
        terms.add(new Literal(new Word("L3", true)));
        body.add(new Literal(new Word("find_all_filters", false), terms));

        terms = new ArrayList<>();
        terms.add(new Literal(new Word("L3", true)));
        terms.add(new Literal(new Word("L2", true)));
        terms.add(new Literal(new Word("Ids1", true)));
        body.add(new Literal(new Word("filter_all", false), terms));


        terms = new ArrayList<>();
        terms.add(new Literal(new Word(object2, false)));
        terms.add(new Literal(new Word(object2Index, false)));
        terms.add(new Literal(new Word("L4", true)));
        body.add(new Literal(new Word("find_all_filters", false), terms));

        terms = new ArrayList<>();
        terms.add(new Literal(new Word("L4", true)));
        terms.add(new Literal(new Word("L2", true)));
        terms.add(new Literal(new Word("Ids2", true)));
        body.add(new Literal(new Word("filter_all", false), terms));

        terms = new ArrayList<>();
        terms.add(new Literal(new Word("Ids1", true)));
        terms.add(new Literal(new Word("Ids2", true)));
        terms.add(new Literal(new Word("Ids", true)));
        body.add(new Literal(new Word("union", false), terms));

        terms = new ArrayList<>();
        terms.add(new Literal(new Word("Ids", true)));
        terms.add(new Literal(new Word("A", true)));
        body.add(new Literal(new Word("list_length", false), terms));

        return new Rule(head, body, true);
    }

    private static List<Rule> getReferencedRule(Question question) {
        List<Rule> rules = new ArrayList<>();
        if (question.semanticRoot.getPOSTag().equalsIgnoreCase("JJ") &&
                question.semanticRoot.getLemma().matches("left|right")
                && !question.semanticRoot.getRelationMap().getOrDefault("nmod:of", new ArrayList<>()).isEmpty()) {
            String direction = question.semanticRoot.getLemma();
            String referencedIndex = Integer.toString(question.semanticRoot.getRelationMap().get("nmod:of").get(0).getWordIndex());
            String referencedObject = question.semanticRoot.getRelationMap().get("nmod:of").get(0).getLemma();
            String countingObjectIndex = null;
            String countingObject = null;

            if (!question.semanticRoot.getRelationMap().getOrDefault("nsubjpass", new ArrayList<>()).isEmpty()) {
                countingObjectIndex = Integer.toString(question.semanticRoot.getRelationMap().get("nsubjpass").get(0).getWordIndex());
                countingObject = question.semanticRoot.getRelationMap().get("nsubjpass").get(0).getLemma();
            } else if (!question.semanticRoot.getRelationMap().getOrDefault("nsubj", new ArrayList<>()).isEmpty()) {
                countingObjectIndex = Integer.toString(question.semanticRoot.getRelationMap().get("nsubj").get(0).getWordIndex());
                countingObject = question.semanticRoot.getRelationMap().get("nsubj").get(0).getLemma();
            }

            rules.add(getReferencedPredicates(direction, countingObject, countingObjectIndex, referencedObject, referencedIndex));
        } else if (question.semanticRoot.getPOSTag().equalsIgnoreCase("vbp") &&
                (!question.semanticRoot.getRelationMap().getOrDefault("nmod:behind", new ArrayList<>()).isEmpty() ||
                        !question.semanticRoot.getRelationMap().getOrDefault("nmod:in_front_of", new ArrayList<>()).isEmpty())) {
            String direction = null;
            String referencedObject = null;
            String referencedIndex = null;
            if (!question.semanticRoot.getRelationMap().getOrDefault("nmod:behind", new ArrayList<>()).isEmpty()) {
                direction = "behind";
                referencedObject = question.semanticRoot.getRelationMap().get("nmod:behind").get(0).getLemma();
                referencedIndex = Integer.toString(question.semanticRoot.getRelationMap().get("nmod:behind").get(0).getWordIndex());
            } else if (!question.semanticRoot.getRelationMap().getOrDefault("nmod:in_front_of", new ArrayList<>()).isEmpty()) {
                direction = "front";
                referencedObject = question.semanticRoot.getRelationMap().get("nmod:in_front_of").get(0).getLemma();
                referencedIndex = Integer.toString(question.semanticRoot.getRelationMap().get("nmod:in_front_of").get(0).getWordIndex());
            }

            String countingObjectIndex = Integer.toString(question.semanticRoot.getRelationMap().get("nsubj").get(0).getWordIndex());
            String countingObject = question.semanticRoot.getRelationMap().get("nsubj").get(0).getLemma();

            rules.add(getReferencedPredicates(direction, countingObject, countingObjectIndex, referencedObject, referencedIndex));
        }


        //rules.add(new Rule(head,body,true));
        return rules;
    }

    private static Rule getReferencedPredicates(String direction, String countingObject, String countingObjectIndex, String referencedObject, String referencedIndex) {
        Literal head = null;
        List<Literal> body = new ArrayList<>();

        List<Literal> terms = new ArrayList<>();
        terms.add(new Literal(new Word(referencedObject, false)));
        terms.add(new Literal(new Word(referencedIndex, false)));
        terms.add(new Literal(new Word("L1", true)));
        body.add(new Literal(new Word("find_all_filters", false), terms));


        terms = new ArrayList<>();
        terms.add(new Literal(new Word("L1", true)));
        terms.add(new Literal(new Word("[H|T]", true)));
        body.add(new Literal(new Word("list_object", false), terms));


        terms = new ArrayList<>();
        terms.add(new Literal(new Word("H", true)));
        terms.add(new Literal(new Word("L2", true)));
        if (direction.equalsIgnoreCase("left")) {
            body.add(new Literal(new Word("get_left_list", false), terms));
        } else if (direction.equalsIgnoreCase("right")) {
            body.add(new Literal(new Word("get_right_list", false), terms));
        } else if (direction.equalsIgnoreCase("behind")) {
            body.add(new Literal(new Word("get_behind_list", false), terms));
        } else if (direction.equalsIgnoreCase("front")) {
            body.add(new Literal(new Word("get_front_list", false), terms));
        }

        terms = new ArrayList<>();
        terms.add(new Literal(new Word(countingObject, false)));
        terms.add(new Literal(new Word(countingObjectIndex, false)));
        terms.add(new Literal(new Word("L3", true)));
        body.add(new Literal(new Word("find_all_filters", false), terms));


        terms = new ArrayList<>();
        terms.add(new Literal(new Word("L3", true)));
        terms.add(new Literal(new Word("L2", true)));
        terms.add(new Literal(new Word("Ids", true)));
        body.add(new Literal(new Word("filter_all", false), terms));

        terms = new ArrayList<>();
        terms.add(new Literal(new Word("Ids", true)));
        terms.add(new Literal(new Word("A", true)));
        body.add(new Literal(new Word("list_length", false), terms));

        return new Rule(head, body, true);
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
