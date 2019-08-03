import edu.stanford.nlp.trees.TypedDependency;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class ClevrQuestionQuantity {

    public static List<Rule> getQuantityRules(Question question) {
        List<Rule> rules = new ArrayList<>();
        List<Word> nouns = question.wordList.stream().filter(w -> w.getPOSTag().toLowerCase().matches("nn|nnp|nns|nnps")).collect(Collectors.toList());
        ClevrQuestionCommonRules.generateFacts(question, nouns);
        if (question.information.questionType == QuestionType.WHAT && question.semanticRoot.getPOSTag().equalsIgnoreCase("NN") &&
                ((!question.semanticRoot.getRelationMap().getOrDefault("nsubj", new ArrayList<>()).isEmpty() &&
                        question.semanticRoot.getRelationMap().get("nsubj").get(0).getLemma().equalsIgnoreCase("number")) ||
                        (!question.semanticRoot.getRelationMap().getOrDefault("dobj", new ArrayList<>()).isEmpty() &&
                                question.semanticRoot.getRelationMap().get("dobj").get(0).getLemma().equalsIgnoreCase("number"))) &&
                !question.semanticRoot.getRelationMap().getOrDefault("case", new ArrayList<>()).isEmpty() &&
                ((question.semanticRoot.getRelationMap().get("case").get(0).getLemma().equalsIgnoreCase("behind")) ||
                        (question.semanticRoot.getRelationMap().get("case").get(0).getLemma().equalsIgnoreCase("in") &&
                                question.dependencies.stream().filter(d -> d.reln().toString().equalsIgnoreCase("mwe")
                                        && question.wordList.get(d.dep().index() - 1).getLemma().equalsIgnoreCase("front")).findFirst().isPresent())
                )) {
            rules.addAll(getReferencedRule(question));
        } else if (!question.semanticRoot.getRelationMap().getOrDefault("nsubj", new ArrayList<>()).isEmpty() &&
                !question.semanticRoot.getRelationMap().getOrDefault("expl", new ArrayList<>()).isEmpty() &&
                question.semanticRoot.getRelationMap().getOrDefault("nmod:of", new ArrayList<>()).isEmpty()) {
            rules.addAll(getSingleObjectCount(question));
        }
        //How many other objects are there of the same size as the yellow metallic block ?
        else if (!question.semanticRoot.getRelationMap().getOrDefault("nsubj", new ArrayList<>()).isEmpty() &&
                !question.semanticRoot.getRelationMap().getOrDefault("expl", new ArrayList<>()).isEmpty() &&
                !question.semanticRoot.getRelationMap().getOrDefault("nmod:of", new ArrayList<>()).isEmpty() &&
                question.dependencies.stream().filter(d -> d.reln().toString().equalsIgnoreCase("nmod:as")).findFirst().isPresent()) {
            rules.addAll(getComparisonRules(question));
        }

   /*     else if (question.semanticRoot.getPOSTag().equalsIgnoreCase("VBP") && question.semanticRoot.getLemma().equalsIgnoreCase("be") &&
                !question.semanticRoot.getRelationMap().getOrDefault("nsubj", new ArrayList<>()).isEmpty() &&
                ((!question.semanticRoot.getRelationMap().getOrDefault("dobj", new ArrayList<>()).isEmpty())
                        ||
                        (question.semanticRoot.getRelationMap().get("nsubj").size() == 2 &&
                                question.semanticRoot.getRelationMap().get("nsubj").stream().filter(w -> w.getPOSTag().equalsIgnoreCase("jj")).findFirst().isPresent()
                        ))) {
            rules.addAll(getComplexSingleObjectCount(question));
        } */

        else if (question.semanticRoot.getPOSTag().equalsIgnoreCase("JJ") &&
                question.semanticRoot.getLemma().matches("left|right")) {
            rules.addAll(getReferencedRule(question));
        } else if (question.semanticRoot.getPOSTag().equalsIgnoreCase("vbp") &&
                (!question.semanticRoot.getRelationMap().getOrDefault("nmod:behind", new ArrayList<>()).isEmpty() ||
                        !question.semanticRoot.getRelationMap().getOrDefault("nmod:in_front_of", new ArrayList<>()).isEmpty())) {
            rules.addAll(getReferencedRule(question));
        } else if (question.semanticRoot.getPOSTag().matches("NNS") &&
                !question.semanticRoot.getRelationMap().getOrDefault("conj", new ArrayList<>()).isEmpty() &&
                question.dependencies.stream().filter(d -> d.reln().getShortName().equalsIgnoreCase("conj")
                        && d.reln().getSpecific().equalsIgnoreCase("or")
                        && question.wordList.get(d.gov().index() - 1).getLemma().equalsIgnoreCase(question.semanticRoot.getLemma())).findFirst().isPresent()) {
            rules.addAll(getOrRules(question));
        } else if (question.semanticRoot.getPOSTag().equalsIgnoreCase("vbp") &&
                !question.semanticRoot.getRelationMap().getOrDefault("nsubj", new ArrayList<>()).isEmpty() &&
                question.dependencies.stream().filter(d -> d.reln().toString().equalsIgnoreCase("conj:or")).findFirst().isPresent()) {
            rules.addAll(getOrRules(question));

        } else if (question.semanticRoot.getPOSTag().equalsIgnoreCase("nn") &&
                !question.semanticRoot.getRelationMap().getOrDefault("amod", new ArrayList<>()).isEmpty() &&
                question.semanticRoot.getRelationMap().get("amod").get(0).getLemma().equalsIgnoreCase("same")) {
            rules.addAll(getComparisonRules(question));
        } else if (question.semanticRoot.getPOSTag().equalsIgnoreCase("vbp") &&
                question.dependencies.stream().filter(d -> d.reln().toString().equalsIgnoreCase("amod")
                        && d.dep().value().equalsIgnoreCase("same")).findFirst().isPresent()) {
            rules.addAll(getComparisonRules(question));
        } else if (question.semanticRoot.getPOSTag().equalsIgnoreCase("vbn") && question.semanticRoot.getLemma().equalsIgnoreCase("make") &&
                question.dependencies.stream().filter(d -> d.reln().toString().equalsIgnoreCase("amod")
                        && question.wordList.get(d.dep().index() - 1).getLemma().equalsIgnoreCase("same")).findFirst().isPresent()) {
            rules.addAll(getComparisonRules(question));
        }
        rules = ClevrQuestionCommonRules.modifyCommonQueryRules(rules, false);

        rules.addAll(ClevrQuestionCommonRules.getCommonQueryRules(question, false));

        return rules;
    }

    private static List<Rule> getComparisonRules(Question question) {
        List<Rule> rules = new ArrayList<>();
        if (question.semanticRoot.getPOSTag().equalsIgnoreCase("nn")) {
            String comparisonAttribute = question.semanticRoot.getLemma();
            String countingObjectIndex = Integer.toString(question.semanticRoot.getRelationMap().get("nsubj").get(0).getWordIndex());
            String countingObject = question.semanticRoot.getRelationMap().get("nsubj").get(0).getLemma();
            TypedDependency c = question.dependencies.stream().filter(d -> d.reln().toString().equalsIgnoreCase("nmod:as")
                    && question.wordList.get(d.gov().index() - 1).getLemma().equalsIgnoreCase(comparisonAttribute)).findFirst().orElse(null);
            String comparatorObjectIndex = c != null ? Integer.toString(question.wordList.get(c.dep().index() - 1).getWordIndex()) : null;
            String comparatorObject = c != null ? question.wordList.get(c.dep().index() - 1).getLemma() : null;

            if (comparatorObject != null) {
                rules.add(getComparisonRuleLiterals(countingObjectIndex, countingObject, comparatorObjectIndex,
                        comparatorObject, comparisonAttribute, false));
            }
        } else if (question.semanticRoot.getPOSTag().equalsIgnoreCase("vbp") &&
                !question.semanticRoot.getRelationMap().getOrDefault("nsubj", new ArrayList<>()).isEmpty() &&
                question.semanticRoot.getRelationMap().get("nsubj").stream().filter(w -> !w.getRelationMap().getOrDefault("amod", new ArrayList<>()).isEmpty()
                        && w.getRelationMap().get("amod").get(0).getLemma().equalsIgnoreCase("many")).findFirst().isPresent() &&
                question.semanticRoot.getRelationMap().get("nsubj").stream().filter(w -> !w.getRelationMap().getOrDefault("amod", new ArrayList<>()).isEmpty()
                        && w.getRelationMap().get("amod").get(0).getLemma().equalsIgnoreCase("same")).findFirst().isPresent() &&
                question.dependencies.stream().filter(d -> d.reln().toString().equalsIgnoreCase("nmod:as")).findFirst().isPresent()) {


            String comparisonAttribute = question.semanticRoot.getRelationMap().get("nsubj").stream().filter(w -> !w.getRelationMap().getOrDefault("amod", new ArrayList<>()).isEmpty()
                    && w.getRelationMap().get("amod").get(0).getLemma().equalsIgnoreCase("same")).findFirst().get().getLemma();
            Word o = question.semanticRoot.getRelationMap().get("nsubj").stream().filter(w -> !w.getRelationMap().getOrDefault("amod", new ArrayList<>()).isEmpty()
                    && w.getRelationMap().get("amod").get(0).getLemma().equalsIgnoreCase("many")).findFirst().get();
            String countingObjectIndex = Integer.toString(o.getWordIndex());
            String countingObject = o.getLemma();
            int comparatorObjectIndex = question.dependencies.stream().filter(d -> d.reln().toString().equalsIgnoreCase("nmod:as")).findFirst().get().dep().index();
            String comparatorObject = question.wordList.get(comparatorObjectIndex - 1).getLemma();
            if (o.getRelationMap().get("amod").stream().filter(w -> w.getLemma().equalsIgnoreCase("other")).findFirst().isPresent()) {
                rules.add(getComparisonRuleLiterals(countingObjectIndex, countingObject, Integer.toString(comparatorObjectIndex),
                        comparatorObject, comparisonAttribute, true));
            } else {
                rules.add(getComparisonRuleLiterals(countingObjectIndex, countingObject, Integer.toString(comparatorObjectIndex),
                        comparatorObject, comparisonAttribute, false));
            }
        } else if (question.semanticRoot.getPOSTag().equalsIgnoreCase("vbp") &&
                !question.semanticRoot.getRelationMap().getOrDefault("nsubj", new ArrayList<>()).isEmpty() &&
                question.semanticRoot.getRelationMap().get("nsubj").stream().filter(w -> !w.getRelationMap().getOrDefault("amod", new ArrayList<>()).isEmpty()
                        && w.getRelationMap().get("amod").get(0).getLemma().equalsIgnoreCase("many")).findFirst().isPresent() &&
                !question.semanticRoot.getRelationMap().getOrDefault("dobj", new ArrayList<>()).isEmpty() &&
                !question.semanticRoot.getRelationMap().get("dobj").get(0).getRelationMap().getOrDefault("amod", new ArrayList<>()).isEmpty() &&
                question.semanticRoot.getRelationMap().get("dobj").get(0).getRelationMap().get("amod").get(0).getLemma().equalsIgnoreCase("same") &&
                question.dependencies.stream().filter(d -> d.reln().toString().equalsIgnoreCase("nmod:as")).findFirst().isPresent()) {
            Word o = question.semanticRoot.getRelationMap().get("nsubj").stream().filter(w -> !w.getRelationMap().getOrDefault("amod", new ArrayList<>()).isEmpty()
                    && w.getRelationMap().get("amod").get(0).getLemma().equalsIgnoreCase("many")).findFirst().get();

            String countingObjectIndex = Integer.toString(o.getWordIndex());
            String countingObject = o.getLemma();
            int comparatorObjectIndex = question.dependencies.stream().filter(d -> d.reln().toString().equalsIgnoreCase("nmod:as")).findFirst().get().dep().index();
            String comparatorObject = question.wordList.get(comparatorObjectIndex - 1).getLemma();
            String comparisonAttribute = question.semanticRoot.getRelationMap().get("dobj").get(0).getLemma();
            if (o.getRelationMap().get("amod").stream().filter(w -> w.getLemma().equalsIgnoreCase("other")).findFirst().isPresent()) {
                rules.add(getComparisonRuleLiterals(countingObjectIndex, countingObject, Integer.toString(comparatorObjectIndex),
                        comparatorObject, comparisonAttribute, true));
            } else {
                rules.add(getComparisonRuleLiterals(countingObjectIndex, countingObject, Integer.toString(comparatorObjectIndex),
                        comparatorObject, comparisonAttribute, false));
            }

        } else if (question.semanticRoot.getPOSTag().equalsIgnoreCase("vbp") &&
                !question.semanticRoot.getRelationMap().getOrDefault("expl", new ArrayList<>()).isEmpty() &&
                !question.semanticRoot.getRelationMap().getOrDefault("nmod:of", new ArrayList<>()).isEmpty()) {

            Word o = null;
            if (question.information.questionType == QuestionType.WHAT && !question.semanticRoot.getRelationMap().getOrDefault("nsubj", new ArrayList<>()).isEmpty() &&
                    question.semanticRoot.getRelationMap().get("nsubj").get(0).getLemma().equalsIgnoreCase("number")) {
                TypedDependency c = question.dependencies.stream().filter(d -> d.reln().toString().equalsIgnoreCase("nmod:of")
                        && question.wordList.get(d.gov().index() - 1).getLemma().equalsIgnoreCase("number")).findFirst().get();
                o = question.wordList.get(c.dep().index() - 1);
            } else {
                o = question.semanticRoot.getRelationMap().get("nsubj").get(0);
            }

            String countingObjectIndex = Integer.toString(o.getWordIndex());
            String countingObject = o.getLemma();
            String comparisonAttribute = question.semanticRoot.getRelationMap().get("nmod:of").get(0).getLemma();

            if(question.dependencies.stream().filter(d -> d.reln().toString().equalsIgnoreCase("nmod:as")).findFirst().isPresent()){
                int comparatorObjectIndex = question.dependencies.stream().filter(d -> d.reln().toString().equalsIgnoreCase("nmod:as")).findFirst().get().dep().index();
                String comparatorObject = question.wordList.get(comparatorObjectIndex - 1).getLemma();
                if (o.getRelationMap().get("amod").stream().filter(w -> w.getLemma().equalsIgnoreCase("other")).findFirst().isPresent()) {
                    rules.add(getComparisonRuleLiterals(countingObjectIndex, countingObject, Integer.toString(comparatorObjectIndex),
                            comparatorObject, comparisonAttribute, true));
                } else {
                    rules.add(getComparisonRuleLiterals(countingObjectIndex, countingObject, Integer.toString(comparatorObjectIndex),
                            comparatorObject, comparisonAttribute, false));
                }
            }
            else if(question.dependencies.stream().filter(d -> d.reln().toString().equalsIgnoreCase("acl:relcl")).findFirst().isPresent()){
                int comparatorObjectIndex = question.dependencies.stream().filter(d -> d.reln().toString().equalsIgnoreCase("acl:relcl")).findFirst().get().dep().index();
                String comparatorObject = question.wordList.get(comparatorObjectIndex - 1).getLemma();
                if (o.getRelationMap().get("amod").stream().filter(w -> w.getLemma().equalsIgnoreCase("other")).findFirst().isPresent()) {
                    rules.add(getComparisonRuleLiterals(countingObjectIndex, countingObject, Integer.toString(comparatorObjectIndex),
                            comparatorObject, comparisonAttribute, true));
                } else {
                    rules.add(getComparisonRuleLiterals(countingObjectIndex, countingObject, Integer.toString(comparatorObjectIndex),
                            comparatorObject, comparisonAttribute, false));
                }
            }

        } else if (question.semanticRoot.getPOSTag().equalsIgnoreCase("vbp") &&
                !question.semanticRoot.getRelationMap().getOrDefault("nsubj", new ArrayList<>()).isEmpty() &&
                !question.semanticRoot.getRelationMap().getOrDefault("dobj", new ArrayList<>()).isEmpty() &&
                question.semanticRoot.getRelationMap().get("dobj").stream().filter(w -> !w.getRelationMap().getOrDefault("amod", new ArrayList<>()).isEmpty()
                        && w.getRelationMap().get("amod").get(0).getLemma().equalsIgnoreCase("many")).findFirst().isPresent() &&
                question.semanticRoot.getRelationMap().get("nsubj").stream().filter(w -> !w.getRelationMap().getOrDefault("amod", new ArrayList<>()).isEmpty()
                        && w.getRelationMap().get("amod").get(0).getLemma().equalsIgnoreCase("same")).findFirst().isPresent() &&
                question.dependencies.stream().filter(d -> d.reln().toString().equalsIgnoreCase("nmod:as")).findFirst().isPresent()) {
            String comparisonAttribute = question.semanticRoot.getRelationMap().get("nsubj").stream().filter(w -> !w.getRelationMap().getOrDefault("amod", new ArrayList<>()).isEmpty()
                    && w.getRelationMap().get("amod").get(0).getLemma().equalsIgnoreCase("same")).findFirst().get().getLemma();
            Word o = question.semanticRoot.getRelationMap().get("dobj").stream().filter(w -> !w.getRelationMap().getOrDefault("amod", new ArrayList<>()).isEmpty()
                    && w.getRelationMap().get("amod").get(0).getLemma().equalsIgnoreCase("many")).findFirst().get();
            String countingObjectIndex = Integer.toString(o.getWordIndex());
            String countingObject = o.getLemma();
            int comparatorObjectIndex = question.dependencies.stream().filter(d -> d.reln().toString().equalsIgnoreCase("nmod:as")).findFirst().get().dep().index();
            String comparatorObject = question.wordList.get(comparatorObjectIndex - 1).getLemma();
            if (o.getRelationMap().get("amod").stream().filter(w -> w.getLemma().equalsIgnoreCase("other")).findFirst().isPresent()) {
                rules.add(getComparisonRuleLiterals(countingObjectIndex, countingObject, Integer.toString(comparatorObjectIndex),
                        comparatorObject, comparisonAttribute, true));
            } else {
                rules.add(getComparisonRuleLiterals(countingObjectIndex, countingObject, Integer.toString(comparatorObjectIndex),
                        comparatorObject, comparisonAttribute, false));
            }
        } else if (question.information.questionType == QuestionType.WHAT &&
                !question.semanticRoot.getRelationMap().getOrDefault("dobj", new ArrayList<>()).isEmpty() &&
                question.semanticRoot.getRelationMap().get("dobj").get(0).getLemma().equalsIgnoreCase("number") &&
                question.dependencies.stream().filter(d -> d.reln().toString().equalsIgnoreCase("nmod:as")).findFirst().isPresent() &&
                question.dependencies.stream().filter(d -> d.reln().toString().equalsIgnoreCase("nmod:of")).findFirst().isPresent()) {
            String comparisonAttribute = question.semanticRoot.getRelationMap().get("nsubj").stream()
                    .filter(d -> d.getRelationMap().get("amod").get(0).getLemma().equalsIgnoreCase("same")).findFirst().get().getLemma();
            TypedDependency c = question.dependencies.stream().filter(d -> d.reln().toString().equalsIgnoreCase("nmod:of")
                    && question.wordList.get(d.gov().index() - 1).getLemma().equalsIgnoreCase("number")).findFirst().get();
            Word o = question.wordList.get(c.dep().index() - 1);
            String countingObjectIndex = Integer.toString(o.getWordIndex());
            String countingObject = o.getLemma();
            int comparatorObjectIndex = question.dependencies.stream().filter(d -> d.reln().toString().equalsIgnoreCase("nmod:as")).findFirst().get().dep().index();
            String comparatorObject = question.wordList.get(comparatorObjectIndex - 1).getLemma();
            if (!o.getRelationMap().getOrDefault("amod", new ArrayList<>()).isEmpty() &&
                    o.getRelationMap().get("amod").stream().filter(w -> w.getLemma().equalsIgnoreCase("other")).findFirst().isPresent()) {
                rules.add(getComparisonRuleLiterals(countingObjectIndex, countingObject, Integer.toString(comparatorObjectIndex),
                        comparatorObject, comparisonAttribute, true));
            } else {
                rules.add(getComparisonRuleLiterals(countingObjectIndex, countingObject, Integer.toString(comparatorObjectIndex),
                        comparatorObject, comparisonAttribute, false));
            }
        } else if (question.semanticRoot.getPOSTag().equalsIgnoreCase("vbn") && question.semanticRoot.getLemma().equalsIgnoreCase("make") &&
                question.dependencies.stream().filter(d -> d.reln().toString().equalsIgnoreCase("nmod:of")
                        && question.wordList.get(d.gov().index() - 1).getLemma().equalsIgnoreCase("number")).findFirst().isPresent() &&
                question.dependencies.stream().filter(d -> d.reln().toString().equalsIgnoreCase("nmod:as")).findFirst().isPresent()) {

            String comparisonAttribute = question.semanticRoot.getRelationMap().get("nmod:of").get(0).getLemma();
            TypedDependency c = question.dependencies.stream().filter(d -> d.reln().toString().equalsIgnoreCase("nmod:of")
                    && question.wordList.get(d.gov().index() - 1).getLemma().equalsIgnoreCase("number")).findFirst().get();
            Word o = question.wordList.get(c.dep().index() - 1);
            String countingObjectIndex = Integer.toString(o.getWordIndex());
            String countingObject = o.getLemma();
            int comparatorObjectIndex = question.dependencies.stream().filter(d -> d.reln().toString().equalsIgnoreCase("nmod:as")).findFirst().get().dep().index();
            String comparatorObject = question.wordList.get(comparatorObjectIndex - 1).getLemma();

            if (!o.getRelationMap().getOrDefault("amod", new ArrayList<>()).isEmpty() &&
                    o.getRelationMap().get("amod").stream().filter(w -> w.getLemma().equalsIgnoreCase("other")).findFirst().isPresent()) {
                rules.add(getComparisonRuleLiterals(countingObjectIndex, countingObject, Integer.toString(comparatorObjectIndex),
                        comparatorObject, comparisonAttribute, true));
            } else {
                rules.add(getComparisonRuleLiterals(countingObjectIndex, countingObject, Integer.toString(comparatorObjectIndex),
                        comparatorObject, comparisonAttribute, false));
            }
        }
        return rules;
    }

    private static Rule getComparisonRuleLiterals(String countingObjectIndex, String countingObject,
                                                  String comparatorObjectIndex, String comparatorObject, String comparisonAttribute, boolean isOtherExist) {
        Literal head = null;
        List<Literal> body = new ArrayList<>();

        List<Literal> terms = new ArrayList<>();
        terms.add(new Literal(new Word(comparatorObject, false)));
        terms.add(new Literal(new Word(comparatorObjectIndex, false)));
        terms.add(new Literal(new Word("L1", true)));
        body.add(new Literal(new Word("find_all_filters", false), terms));


        terms = new ArrayList<>();
        terms.add(new Literal(new Word("L1", true)));
        terms.add(new Literal(new Word("Ids1", true)));
        body.add(new Literal(new Word("list_object", false), terms));

        terms = new ArrayList<>();
        terms.add(new Literal(new Word("Ids1", true)));
        terms.add(new Literal(new Word(comparisonAttribute, false)));
        terms.add(new Literal(new Word("Val", true)));
        body.add(new Literal(new Word("get_att_val", false), terms));

        terms = new ArrayList<>();
        terms.add(new Literal(new Word(countingObject, false)));
        terms.add(new Literal(new Word(countingObjectIndex, false)));
        terms.add(new Literal(new Word("L2", true)));
        body.add(new Literal(new Word("find_all_filters", false), terms));

        terms = new ArrayList<>();
        StringBuilder sbLiteral = new StringBuilder();
        sbLiteral.append("[[").append(comparisonAttribute).append(",Val]|L2]");
        //String literal = "[["+ comparisonAttribute + ",Val]|L2]";
        String literal = sbLiteral.toString();
        terms.add(new Literal(new Word(literal, true)));
        terms.add(new Literal(new Word("Ids2", true)));
        body.add(new Literal(new Word("list_object", false), terms));

        if (isOtherExist) {
            terms = new ArrayList<>();
            terms.add(new Literal(new Word("Ids2", true)));
            terms.add(new Literal(new Word("Ids1", true)));
            terms.add(new Literal(new Word("Ids", true)));
            body.add(new Literal(new Word("list_subtract", false), terms));

            terms = new ArrayList<>();
            terms.add(new Literal(new Word("Ids", true)));
            terms.add(new Literal(new Word("A", true)));
            body.add(new Literal(new Word("list_length", false), terms));
        } else {
            terms = new ArrayList<>();
            terms.add(new Literal(new Word("Ids2", true)));
            terms.add(new Literal(new Word("A", true)));
            body.add(new Literal(new Word("list_length", false), terms));
        }
        return new Rule(head, body, true);

    }


    private static List<Rule> getComplexSingleObjectCount(Question question) {
        List<Rule> rules = new ArrayList<>();

        if (!question.semanticRoot.getRelationMap().getOrDefault("dobj", new ArrayList<>()).isEmpty()) {
            String countingObject = question.semanticRoot.getRelationMap().get("dobj").get(0).getLemma();
            String countingObjectIndex = Integer.toString(question.semanticRoot.getRelationMap().get("dobj").get(0).getWordIndex());
            String filter = question.semanticRoot.getRelationMap().get("nsubj").get(0).getLemma();
            rules.add(getComplexSingleObjectLiterals(countingObjectIndex, countingObject, filter));
        } else if (question.semanticRoot.getRelationMap().get("nsubj").size() == 2) {
            Word countingObject = question.semanticRoot.getRelationMap().get("nsubj").stream().filter(w -> w.getPOSTag().matches("NNS|NN")).findFirst().get();
            String countingObjectIndex = Integer.toString(countingObject.getWordIndex());
            String filter = question.semanticRoot.getRelationMap().get("nsubj").stream().filter(w -> w.getPOSTag().matches("JJ")).findFirst().get().getLemma();
            rules.add(getComplexSingleObjectLiterals(countingObjectIndex, countingObject.getLemma(), filter));
        }
        return rules;
    }

    private static Rule getComplexSingleObjectLiterals(String countingObjectIndex, String countingObject, String filter) {
        Literal head = null;
        List<Literal> body = new ArrayList<>();
        List<Literal> terms = new ArrayList<>();
        terms.add(new Literal(new Word(countingObject, false)));
        terms.add(new Literal(new Word(countingObjectIndex, false)));
        terms.add(new Literal(new Word("L1", true)));
        body.add(new Literal(new Word("find_all_filters", false), terms));


        terms = new ArrayList<>();
        terms.add(new Literal(new Word("[" + filter + "]", false)));
        terms.add(new Literal(new Word("[X]", true)));
        body.add(new Literal(new Word("get_properties", false), terms));

        terms = new ArrayList<>();
        terms.add(new Literal(new Word("[X|L1]", true)));
        terms.add(new Literal(new Word("Ids", true)));
        body.add(new Literal(new Word("list_object", false), terms));

        terms = new ArrayList<>();
        terms.add(new Literal(new Word("Ids", true)));
        terms.add(new Literal(new Word("A", true)));
        body.add(new Literal(new Word("list_length", false), terms));

        return new Rule(head, body, true);
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

            String parentObjectIndex = null;
            String parentObject = null;
            if (question.information.questionType == QuestionType.WHAT) {
                TypedDependency c1 = question.dependencies.stream().filter(d -> d.reln().toString().equalsIgnoreCase("nmod:of") &&
                        question.wordList.get(d.gov().index() - 1).getLemma().equalsIgnoreCase("number")).findFirst().get();
                parentObjectIndex = Integer.toString(c1.dep().index());
                parentObject = question.wordList.get(c1.dep().index() - 1).getLemma();
            } else {
                parentObjectIndex = Integer.toString(question.semanticRoot.getRelationMap().get("nsubj").get(0).getWordIndex());
                parentObject = question.semanticRoot.getRelationMap().get("nsubj").get(0).getLemma();
            }


            rules.add(getOrPredicates(object1, object1Index, object2, object2Index, parentObject, parentObjectIndex));


        } else if (question.semanticRoot.getPOSTag().equalsIgnoreCase("VBP") &&
                question.semanticRoot.getRelationMap().getOrDefault("dobj", new ArrayList<>()).isEmpty()) {
            TypedDependency c = question.dependencies.stream().filter(d -> d.reln().toString().equalsIgnoreCase("conj:or")).findFirst().get();
            int object1Index = c.gov().index();
            String object1 = question.wordList.get(object1Index - 1).getLemma();
            int object2Index = c.dep().index();
            String object2 = question.wordList.get(object2Index - 1).getLemma();
            Word parentObject = question.semanticRoot.getRelationMap().get("nsubj").stream().filter(d -> d.getWordIndex() != object1Index
                    && d.getWordIndex() != object2Index).findFirst().orElse(null);
            if (parentObject == null) {
                TypedDependency c1 = question.dependencies.stream().filter(d -> d.reln().toString().equalsIgnoreCase("nmod:of") &&
                        question.wordList.get(d.gov().index() - 1).getLemma().equalsIgnoreCase("number")).findFirst().get();
                parentObject = question.wordList.get(c1.dep().index() - 1);
            }


            rules.add(getOrPredicates(object1, Integer.toString(object1Index), object2, Integer.toString(object2Index), parentObject.getLemma(), Integer.toString(parentObject.getWordIndex())));
        } else if (question.semanticRoot.getPOSTag().equalsIgnoreCase("VBP") &&
                !question.semanticRoot.getRelationMap().getOrDefault("dobj", new ArrayList<>()).isEmpty() &&
                !question.semanticRoot.getRelationMap().get("dobj").get(0).getRelationMap().getOrDefault("amod",new ArrayList<>()).isEmpty() &&
                question.semanticRoot.getRelationMap().get("dobj").get(0).getRelationMap().get("amod").stream()
                        .filter(w -> w.getLemma().equalsIgnoreCase("many")).findFirst().isPresent())  {
            TypedDependency c = question.dependencies.stream().filter(d -> d.reln().toString().equalsIgnoreCase("conj:or")).findFirst().get();
            int object1Index = c.gov().index();
            String object1 = question.wordList.get(object1Index - 1).getLemma();
            int object2Index = c.dep().index();
            String object2 = question.wordList.get(object2Index - 1).getLemma();
            Word parentObject = question.semanticRoot.getRelationMap().get("dobj").get(0);

            rules.add(getOrPredicates(object1, Integer.toString(object1Index), object2, Integer.toString(object2Index),
                    parentObject.getLemma(), Integer.toString(parentObject.getWordIndex())));
        }
        return rules;

    }

    private static Rule getOrPredicates(String object1, String object1Index, String object2, String
            object2Index, String parentObject, String parentObjectIndex) {

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
        if (question.information.questionType == QuestionType.WHAT &&
                question.wordList.stream().filter(w -> w.getLemma().matches("front|behind")).findFirst().isPresent() &&
                !question.wordList.stream().filter(w -> w.getLemma().matches("left|right")).findFirst().isPresent()) {
            String direction = null;
            if(question.semanticRoot.getPOSTag().equalsIgnoreCase("vbp")){
                if(!question.semanticRoot.getRelationMap().getOrDefault("nmod:behind", new ArrayList<>()).isEmpty()){
                    direction = "behind";
                }
                else if(!question.semanticRoot.getRelationMap().getOrDefault("nmod:in_front_of", new ArrayList<>()).isEmpty()){
                    direction = "front";
                }
            }
            if (!question.semanticRoot.getRelationMap().getOrDefault("case",new ArrayList<>()).isEmpty() &&
                    question.semanticRoot.getRelationMap().get("case").get(0).getLemma().equalsIgnoreCase("behind")) {
                direction = "behind";
            } else if (!question.semanticRoot.getRelationMap().getOrDefault("case",new ArrayList<>()).isEmpty() &&
                    question.semanticRoot.getRelationMap().get("case").get(0).getLemma().equalsIgnoreCase("in") &&
                    question.dependencies.stream().filter(d -> d.reln().toString().equalsIgnoreCase("mwe")
                            && question.wordList.get(d.dep().index() - 1).getLemma().equalsIgnoreCase("front")).findFirst().isPresent()) {
                direction = "front";
            }


            String referencedIndex = null;
            String referencedObject = null;
            if(question.semanticRoot.getPOSTag().equalsIgnoreCase("vbp")){
                TypedDependency c = question.dependencies.stream().filter(d -> (d.reln().toString().equalsIgnoreCase("nmod:behind") ||
                        d.reln().toString().equalsIgnoreCase("nmod:in_front_of"))).findFirst().orElse(null);
                if(c != null){
                    referencedIndex = Integer.toString(c.dep().index());
                    referencedObject = question.wordList.get(Integer.parseInt(referencedIndex)-1).getLemma();
                }
            }
            else {
                referencedIndex = Integer.toString(question.semanticRoot.getWordIndex());
                referencedObject = question.semanticRoot.getLemma();
            }
            TypedDependency c = question.dependencies.stream().filter(d -> d.reln().toString().equalsIgnoreCase("nmod:of") &&
                    question.wordList.get(d.gov().index() - 1).getLemma().equalsIgnoreCase("number")).findFirst().orElse(null);
            String countingObjectIndex = Integer.toString(c.dep().index());
            String countingObject = question.wordList.get(Integer.parseInt(countingObjectIndex) - 1).getLemma();

            rules.add(getReferencedPredicates(direction, countingObject, countingObjectIndex, referencedObject, referencedIndex));

        } else if (question.semanticRoot.getPOSTag().equalsIgnoreCase("JJ") &&
                question.semanticRoot.getLemma().matches("left|right")
                && !question.semanticRoot.getRelationMap().getOrDefault("nmod:of", new ArrayList<>()).isEmpty()) {
            String direction = question.semanticRoot.getLemma();
            String referencedIndex = Integer.toString(question.semanticRoot.getRelationMap().get("nmod:of").get(0).getWordIndex());
            String referencedObject = question.semanticRoot.getRelationMap().get("nmod:of").get(0).getLemma();
            String countingObjectIndex = null;
            String countingObject = null;

            if(question.dependencies.stream().filter(d -> d.reln().toString().equalsIgnoreCase("nmod:of")
                    && d.gov().value().equalsIgnoreCase("number")).findFirst().isPresent()){
                TypedDependency c = question.dependencies.stream().filter(d -> d.reln().toString().equalsIgnoreCase("nmod:of") && d.gov().value().equalsIgnoreCase("number")).findFirst().get();
                countingObjectIndex = Integer.toString(c.dep().index());
                countingObject = question.wordList.get(Integer.parseInt(countingObjectIndex) - 1).getLemma();
            }
            else if (!question.semanticRoot.getRelationMap().getOrDefault("nsubjpass", new ArrayList<>()).isEmpty()) {
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

    private static Rule getReferencedPredicates(String direction, String countingObject, String
            countingObjectIndex, String referencedObject, String referencedIndex) {
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
        Word object = null;
        if (question.information.questionType == QuestionType.WHAT) {
            TypedDependency c = question.dependencies.stream().filter(d -> d.reln().toString().equalsIgnoreCase("nmod:of") &&
                    question.wordList.get(d.gov().index() - 1).getLemma().equalsIgnoreCase("number")).findFirst().get();
            object = question.wordList.get(c.dep().index() - 1);
        } else {
            object = question.semanticRoot.getRelationMap().get("nsubj").get(0);
        }
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
