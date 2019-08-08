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
        } else if (question.semanticRoot.getPOSTag().equalsIgnoreCase("wp") &&
                !question.semanticRoot.getRelationMap().getOrDefault("nsubj", new ArrayList<>()).isEmpty() &&
                question.dependencies.stream().filter(d -> d.reln().toString().equalsIgnoreCase("nmod")
                        && question.wordList.get(d.gov().index() - 1).getLemma().equalsIgnoreCase("make")).findFirst().isPresent()) {
            rules.addAll(getSimpleMadeRules(question));
        } else if (question.semanticRoot.getPOSTag().equalsIgnoreCase("vbz") &&
                !question.semanticRoot.getRelationMap().getOrDefault("nsubj", new ArrayList<>()).isEmpty() &&
                !question.semanticRoot.getRelationMap().getOrDefault("dobj", new ArrayList<>()).isEmpty() &&
                question.dependencies.stream().filter(d -> d.reln().toString().equalsIgnoreCase("nmod:behind")).findFirst().isPresent()) {
            rules.addAll(getReferencedObject(question));
        } else if (question.information.questionWord.getLemma().equalsIgnoreCase("how") &&
                question.information.questionType == QuestionType.WHAT) {
            rules.addAll(getSizeRules(question));
        }

        rules = ClevrQuestionCommonRules.modifyCommonQueryRules(rules, false);

        rules.addAll(ClevrQuestionCommonRules.getCommonQueryRules(question, false));

        return rules;
    }

    private static List<Rule> getSizeRules(Question question) {
        List<Rule> rules = new ArrayList<>();
        String attribute = "size";
        String referencedObject = null;
        String referencedObjectIndex = null;
        if (question.semanticRoot.getPOSTag().equalsIgnoreCase("VBZ") &&
                !question.semanticRoot.getRelationMap().getOrDefault("nsubj", new ArrayList<>()).isEmpty()) {
            referencedObject = question.semanticRoot.getRelationMap().get("nsubj").get(0).getLemma();
            referencedObjectIndex = Integer.toString(question.semanticRoot.getRelationMap().get("nsubj").get(0).getWordIndex());
            rules.add(getSimpleValueRuleLiterals(attribute, referencedObjectIndex, referencedObject));
        }
        return rules;
    }

    private static List<Rule> getReferencedObject(Question question) {
        List<Rule> rules = new ArrayList<>();

        String direction = null;
        String referencedObjectIndex = null;

        if (question.dependencies.stream().filter(d -> d.reln().toString().equalsIgnoreCase("nmod:behind")).findFirst().isPresent()) {
            direction = "behind";
            referencedObjectIndex = Integer.toString(question.dependencies.stream()
                    .filter(d -> d.reln().toString().equalsIgnoreCase("nmod:behind")).findFirst().get().dep().index());
        } else if (question.dependencies.stream().filter(d -> d.reln().toString().equalsIgnoreCase("nmod:in_front_of")).findFirst().isPresent()) {
            direction = "front";
            referencedObjectIndex = Integer.toString(question.dependencies.stream()
                    .filter(d -> d.reln().toString().equalsIgnoreCase("nmod:in_front_of")).findFirst().get().dep().index());
        }


        if (direction != null && referencedObjectIndex != null) {

            String object = question.semanticRoot.getRelationMap().get("nsubj").get(0).getLemma();
            String objectIndex = Integer.toString(question.semanticRoot.getRelationMap().get("nsubj").get(0).getWordIndex());

            String referencedObject = question.wordList.get(Integer.parseInt(referencedObjectIndex) - 1).getLemma();

            String attribute = question.semanticRoot.getRelationMap().get("dobj").get(0).getLemma();

            rules.add(getReferencedLiterals(attribute, object, objectIndex, referencedObject, referencedObjectIndex, direction));
        }
        return rules;
    }

    private static List<Rule> getSimpleMadeRules(Question question) {
        List<Rule> rules = new ArrayList<>();
        String attribute = "material";
        String referencedObject = question.semanticRoot.getRelationMap().get("nsubj").get(0).getLemma();
        String referencedObjectIndex = Integer.toString(question.semanticRoot.getRelationMap().get("nsubj").get(0).getWordIndex());
        rules.add(getSimpleValueRuleLiterals(attribute, referencedObjectIndex, referencedObject));
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

    private static Rule getReferencedLiterals(String attribute, String object, String objectIndex, String referencedObject,
                                              String referencedObjectIndex, String direction) {
        Literal head = null;
        List<Literal> body = new ArrayList<>();

        List<Literal> terms = new ArrayList<>();
        terms.add(new Literal(new Word(referencedObject, false)));
        terms.add(new Literal(new Word(referencedObjectIndex, false)));
        terms.add(new Literal(new Word("L1", true)));
        body.add(new Literal(new Word("find_all_filters", false), terms));


        terms = new ArrayList<>();
        terms.add(new Literal(new Word("L1", true)));
        terms.add(new Literal(new Word("[H|T]", true)));
        body.add(new Literal(new Word("list_object", false), terms));

        terms = new ArrayList<>();
        terms.add(new Literal(new Word("H", true)));
        terms.add(new Literal(new Word("L2", true)));
        if (direction.equalsIgnoreCase("behind")) {
            body.add(new Literal(new Word("get_behind_list", false), terms));
        } else if (direction.equalsIgnoreCase("front")) {
            body.add(new Literal(new Word("get_front_list", false), terms));
        }

        terms = new ArrayList<>();
        terms.add(new Literal(new Word(object, false)));
        terms.add(new Literal(new Word(objectIndex, false)));
        terms.add(new Literal(new Word("L3", true)));
        body.add(new Literal(new Word("find_all_filters", false), terms));

        terms = new ArrayList<>();
        terms.add(new Literal(new Word("L3", true)));
        terms.add(new Literal(new Word("L2", true)));
        terms.add(new Literal(new Word("Ids", true)));
        body.add(new Literal(new Word("filter_all", false), terms));

        terms = new ArrayList<>();
        terms.add(new Literal(new Word("Ids", true)));
        terms.add(new Literal(new Word(attribute, false)));
        terms.add(new Literal(new Word("A", true)));
        body.add(new Literal(new Word("get_att_val", false), terms));

        return new Rule(head, body, true);

    }


}
