import edu.stanford.nlp.trees.TypedDependency;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class ClevrQuestionBoolean {


    public static List<Rule> getBooleanRules(Question question) {
        List<Rule> rules = new ArrayList<>();
        List<Word> nouns = question.wordList.stream().filter(w -> w.getPOSTag().toLowerCase().matches("nn|nnp|nns|nnps")).collect(Collectors.toList());
        //Map<String,List<String>> commonSenseConceptsMap = nouns.stream().collect(Collectors.toMap(n -> n.getWord(), n-> SemanticRelationsGeneration.getConcept(n.getWord())));

        ClevrQuestionCommonRules.generateFacts(question, nouns);
        //Is there a tiny cyan matte ball?
        if (nouns.size() == 1) {
            rules.addAll(getExistentialRule(nouns.get(0)));
        }
        //Is there a cylinder of the same color as the rubber object?
        else if ((question.semanticRoot.getPOSTag().toLowerCase().matches("vbz") ||
                question.semanticRoot.getPOSTag().toLowerCase().matches("vbp")) &&
                !question.semanticRoot.getRelationMap().getOrDefault("nsubj", new ArrayList<>()).isEmpty() &&
                !question.semanticRoot.getRelationMap().getOrDefault("expl", new ArrayList<>()).isEmpty() &&
                !question.wordList.stream().filter(w -> w.getPOSTag().matches("JJR|RBR|JJ-JJR|JJ-RBR")).findFirst().isPresent()) {
            rules.addAll(getComplexExistentialRule(question));
        }
        //Is the brown thing the same size as the red matte cylinder?
        else if (question.semanticRoot.getPOSTag().equalsIgnoreCase("nn") &&
                !question.semanticRoot.getRelationMap().getOrDefault("amod", new ArrayList<>()).isEmpty() &&
                !question.semanticRoot.getRelationMap().getOrDefault("nmod:as", new ArrayList<>()).isEmpty()) {
            rules.addAll(getComparisonRules(question));
        }

        //Is the size of the cyan thing the same as the red cylinder?
        else if (question.semanticRoot.getPOSTag().equalsIgnoreCase("jj") &&
                !question.semanticRoot.getRelationMap().getOrDefault("nsubj", new ArrayList<>()).isEmpty()) {
            rules.addAll(getComparisonRules(question));

        } else if (question.semanticRoot.getPOSTag().equalsIgnoreCase("vbz") &&
                !question.semanticRoot.getRelationMap().getOrDefault("nsubj", new ArrayList<>()).isEmpty() &&
                !question.semanticRoot.getRelationMap().get("nsubj").get(0).getRelationMap().getOrDefault("det", new ArrayList<>()).isEmpty() &&
                !question.semanticRoot.getRelationMap().get("nsubj").get(0).getLemma().equalsIgnoreCase("number")) {
            rules.addAll(getComparisonRules(question));
        } else if (question.semanticRoot.getPOSTag().equalsIgnoreCase("nn") &&
                !question.semanticRoot.getRelationMap().getOrDefault("det", new ArrayList<>()).isEmpty() &&
                (!question.semanticRoot.getRelationMap().getOrDefault("dep", new ArrayList<>()).isEmpty() ||
                        !question.semanticRoot.getRelationMap().getOrDefault("dobj", new ArrayList<>()).isEmpty())) {
            rules.addAll(getComparisonRules(question));
        } else if (question.semanticRoot.getPOSTag().equalsIgnoreCase("vbp") &&
                question.dependencies.stream().filter(d -> d.reln().getShortName().equalsIgnoreCase("conj")
                        && d.reln().getSpecific().equalsIgnoreCase("and")).findFirst().isPresent() &&
                question.dependencies.stream().filter(d -> d.reln().getShortName().equalsIgnoreCase("acl")).findFirst().isPresent()
        ) {
            rules.addAll(getComparisonRules(question));
        }
        //Is the number of things less than the number of big blue spheres?
        else if (question.semanticRoot.getPOSTag().toLowerCase().matches("vbz") &&
                !question.semanticRoot.getRelationMap().getOrDefault("nsubj", new ArrayList<>()).isEmpty() &&
                question.semanticRoot.getRelationMap().get("nsubj").get(0).getLemma().equalsIgnoreCase("number")) {
            rules.addAll(getArithmaticRules(question));
        }
        //Are there fewer cubes than shiny objects?
        else if (question.semanticRoot.getPOSTag().equalsIgnoreCase("vbp") &&
                !question.semanticRoot.getRelationMap().getOrDefault("nsubj", new ArrayList<>()).isEmpty() &&
                !question.semanticRoot.getRelationMap().getOrDefault("expl", new ArrayList<>()).isEmpty() &&
                question.wordList.stream().filter(w -> w.getPOSTag().matches("JJR|RBR|JJ-JJR|JJ-RBR")).findFirst().isPresent()) {
            rules.addAll(getArithmaticRules(question));
        }
        //Are there more cubes than rubber objects?
        else if (question.semanticRoot.getPOSTag().equalsIgnoreCase("vbp") &&
                !question.semanticRoot.getRelationMap().getOrDefault("advmod", new ArrayList<>()).isEmpty() &&
                question.semanticRoot.getRelationMap().get("advmod").get(0).getLemma().equalsIgnoreCase("there") &&
                question.wordList.stream().filter(w -> w.getPOSTag().matches("RBR|JJ-RBR")).findFirst().isPresent()) {
            rules.addAll(getArithmaticRules(question));
        }
        rules = ClevrQuestionCommonRules.modifyCommonQueryRules(rules, true);
        //return getCommonQueryRules(rules,true);
        rules.addAll(ClevrQuestionCommonRules.getCommonQueryRules(question, true));
        return rules;
    }

    private static List<Rule> getExistentialRule(Word word) {
        List<Rule> rules = new ArrayList<>();
        Literal head = null;
        List<Literal> body = new ArrayList<>();

        List<Literal> terms = new ArrayList<>();
        terms.add(new Literal(new Word(word.getLemma(), false)));
        terms.add(new Literal(new Word(Integer.toString(word.getWordIndex()), false)));
        terms.add(new Literal(new Word("L", true)));
        body.add(new Literal(new Word("find_all_filters", false), terms));

        terms = new ArrayList<>();
        terms.add(new Literal(new Word("L", true)));
        terms.add(new Literal(new Word("Ids", true)));
        body.add(new Literal(new Word("list_object", false), terms));

        terms = new ArrayList<>();
        terms.add(new Literal(new Word("Ids", true)));
        terms.add(new Literal(new Word("C", true)));
        body.add(new Literal(new Word("list_length", false), terms));

        terms = new ArrayList<>();
        terms.add(new Literal(new Word("N", true)));
        terms.add(new Literal(new Word(word.getLemma() + "_" + word.getWordIndex(), false)));
        body.add(new Literal(new Word("quantification", false), terms));

        terms = new ArrayList<>();
        terms.add(new Literal(new Word("C", true)));
        terms.add(new Literal(new Word("N", true)));
        body.add(new Literal(new Word("gte", false), terms));


        rules.add(new Rule(head, body, true));
        return rules;
    }


    private static List<Rule> getComplexExistentialRule(Question question) {
        List<Rule> rules = new ArrayList<>();
        if (!question.semanticRoot.getRelationMap().get("nsubj").get(0).getRelationMap()
                .getOrDefault("nmod:of", new ArrayList<>()).isEmpty() &&
                !question.semanticRoot.getRelationMap().get("nsubj").get(0).getRelationMap()
                        .getOrDefault("nmod:as", new ArrayList<>()).isEmpty()) {
            String exitentialItem = question.semanticRoot.getRelationMap().get("nsubj").get(0).getLemma();
            String existentialIndex = Integer.toString(question.semanticRoot.getRelationMap().get("nsubj").get(0).getWordIndex());
            TypedDependency c1 = question.dependencies.stream()
                    .filter(d -> d.reln().getShortName().equalsIgnoreCase("nmod")
                            && d.reln().getSpecific().equalsIgnoreCase("as")
                            && question.wordList.get(d.gov().index()-1).getLemma().equalsIgnoreCase(exitentialItem))
                    .findFirst().orElse(null);

            //String comparator = c1 != null ? c1.dep().value() : null;
            String comparator = c1 != null ? question.wordList.get(c1.dep().index()-1).getLemma() : null;
            String comparatorIndex = c1 != null ? Integer.toString(c1.dep().index()) : null;
            TypedDependency c2 = question.dependencies.stream()
                    .filter(d -> d.reln().getShortName().equalsIgnoreCase("nmod")
                            && d.reln().getSpecific().equalsIgnoreCase("of")
                            && question.wordList.get(d.gov().index()-1).getLemma().equalsIgnoreCase(exitentialItem))
                    .findFirst().orElse(null);

            //String comparisonAttribute = c2 != null ? c2.dep().value() : null;
            String comparisonAttribute = c2 != null ? question.wordList.get(c2.dep().index()-1).getLemma() : null;

            rules.add(getComplexExistentialPredicates(exitentialItem, existentialIndex, comparator, comparatorIndex, comparisonAttribute));
        } else if (!question.semanticRoot.getRelationMap().get("nsubj")
                .get(0).getRelationMap().getOrDefault("acl:relcl", new ArrayList<>()).isEmpty()) {


            String existentialItem = question.semanticRoot.getRelationMap().get("nsubj").get(0).getLemma();
            String existentialIndex = Integer.toString(question.semanticRoot.getRelationMap().get("nsubj").get(0).getWordIndex());
            Word w = question.semanticRoot.getRelationMap().get("nsubj").get(0).getRelationMap().get("acl:relcl").get(0);
            if (w.getRelationMap().getOrDefault("nmod:as", new ArrayList<>()).isEmpty()) {
                Word comparisonAttribute = w.getRelationMap().get("dobj").get(0);
                String comparator = comparisonAttribute.getRelationMap().get("nmod:as").get(0).getLemma();
                String comparatorIndex = Integer.toString(comparisonAttribute.getRelationMap().get("nmod:as").get(0).getWordIndex());
                rules.add(getComplexExistentialPredicates(existentialItem, existentialIndex, comparator, comparatorIndex, comparisonAttribute.getLemma()));
            } else if (!w.getRelationMap().getOrDefault("dobj", new ArrayList<>()).isEmpty()) {
                String comparisonAttribute = w.getRelationMap().get("dobj").get(0).getLemma();
                String comparator = w.getRelationMap().get("nmod:as").get(0).getLemma();
                String comparatorIndex = Integer.toString(w.getRelationMap().get("nmod:as").get(0).getWordIndex());
                rules.add(getComplexExistentialPredicates(existentialItem, existentialIndex, comparator, comparatorIndex, comparisonAttribute));
            } else {
                String comparisonAttribute = w.getLemma();
                String comparator = w.getRelationMap().get("nmod:as").get(0).getLemma();
                String comparatorIndex = Integer.toString(w.getRelationMap().get("nmod:as").get(0).getWordIndex());
                rules.add(getComplexExistentialPredicates(existentialItem, existentialIndex, comparator, comparatorIndex, comparisonAttribute));
            }
        } else if (!question.semanticRoot.getRelationMap().get("nsubj").get(0).getRelationMap().getOrDefault("acl", new ArrayList<>()).isEmpty()) {
            String existentialItem = question.semanticRoot.getRelationMap().get("nsubj").get(0).getLemma();
            String existentialIndex = Integer.toString(question.semanticRoot.getRelationMap().get("nsubj").get(0).getWordIndex());
            Word w = question.semanticRoot.getRelationMap().get("nsubj").get(0).getRelationMap().get("acl").get(0);
            String comparisonAttribute = w.getRelationMap().get("nmod:of").get(0).getLemma();
            String comparator = w.getRelationMap().get("nmod:as").get(0).getLemma();
            String comparatorIndex = Integer.toString(w.getRelationMap().get("nmod:as").get(0).getWordIndex());
            rules.add(getComplexExistentialPredicates(existentialItem, existentialIndex, comparator, comparatorIndex, comparisonAttribute));

        } else if (!question.semanticRoot.getRelationMap().get("nsubj").get(0).getRelationMap().entrySet()
                .stream()
                .filter(r -> r.getKey().equalsIgnoreCase("nmod:behind") || r.getKey().equalsIgnoreCase("nmod:in_front_of"))
                .collect(Collectors.toList())
                .isEmpty()) {


            String exitentialItem = question.semanticRoot.getRelationMap().get("nsubj").get(0).getLemma();
            String existentialIndex = Integer.toString(question.semanticRoot.getRelationMap().get("nsubj").get(0).getWordIndex());
            Literal head = null;
            List<Literal> body = new ArrayList<>();


            List<TypedDependency> depList = question.dependencies.stream()
                    .filter(d -> d.reln().getShortName().equalsIgnoreCase("nmod")
                            && (d.reln().getSpecific().equalsIgnoreCase("in_front_of") || d.reln().getSpecific().equalsIgnoreCase("behind")))
                    .collect(Collectors.toList());
            depList.sort(Comparator.comparing(d -> d.gov().index()));
            depList.sort(Comparator.reverseOrder());

            for (int i = 0; i <= depList.size(); i++) {
                if (i == 0) {
                    TypedDependency d = depList.get(i);
                    body.addAll(getTransitiveLiterals(d, i, question.wordList, false));
                } else if (i == depList.size()) {
                    TypedDependency d = depList.get(i - 1);
                    body.addAll(getTransitiveLiterals(d, i, question.wordList, true));
                } else {
                    TypedDependency d = depList.get(i - 1);
                    body.addAll(getTransitiveLiterals(d, i, question.wordList, false));
                }

            }
            List<Literal> terms = new ArrayList<>();
            terms.add(new Literal(new Word("Ids", true)));
            terms.add(new Literal(new Word("C", true)));
            body.add(new Literal(new Word("list_length", false), terms));

            terms = new ArrayList<>();
            terms.add(new Literal(new Word("N", true)));
            terms.add(new Literal(new Word(exitentialItem + "_" + existentialIndex, false)));
            body.add(new Literal(new Word("quantification", false), terms));

            terms = new ArrayList<>();
            terms.add(new Literal(new Word("C", true)));
            terms.add(new Literal(new Word("N", true)));
            body.add(new Literal(new Word("gte", false), terms));
            rules.add(new Rule(head, body, true));
        }

        return rules;

    }


    private static List<Literal> getTransitiveLiterals(TypedDependency d, int i, List<Word> wordList, boolean lastDependency) {
        String relation = d.reln().getSpecific();
        String gov = wordList.get(d.gov().index() -1).getLemma();
        String govIndex = Integer.toString(d.gov().index());
        String dep = wordList.get(d.dep().index() -1).getLemma();
        String depIndex = Integer.toString(d.dep().index());


        List<Literal> body = new ArrayList<>();

        List<Literal> terms = new ArrayList<>();
        if (i == 0) {
            terms.add(new Literal(new Word("L" + ((2 * i) + 1), true)));
            body.add(new Literal(new Word("get_all_id", false), terms));

        } else if (relation.equalsIgnoreCase("behind")) {
            terms.add(new Literal(new Word("H" + (i - 1), true)));
            terms.add(new Literal(new Word("L" + ((2 * i) + 1), true)));
            body.add(new Literal(new Word("get_behind_list", false), terms));
        } else if (relation.equalsIgnoreCase("in_front_of")) {
            terms.add(new Literal(new Word("H" + (i - 1), true)));
            terms.add(new Literal(new Word("L" + ((2 * i) + 1), true)));
            body.add(new Literal(new Word("get_front_list", false), terms));
        }


        terms = new ArrayList<>();
        if (i == 0) {
            terms.add(new Literal(new Word(dep, false)));
            terms.add(new Literal(new Word(depIndex, false)));
        } else {
            terms.add(new Literal(new Word(gov, false)));
            terms.add(new Literal(new Word(govIndex, false)));
        }
        terms.add(new Literal(new Word("L" + ((2 * i) + 2), true)));
        body.add(new Literal(new Word("find_all_filters", false), terms));

        terms = new ArrayList<>();

        terms.add(new Literal(new Word("L" + ((2 * i) + 2), true)));
        terms.add(new Literal(new Word("L" + ((2 * i) + 1), true)));
        if (lastDependency) {
            terms.add(new Literal(new Word("Ids", true)));
        } else {
            terms.add(new Literal(new Word("[H" + i + "|T" + i + "]", true)));
        }
        body.add(new Literal(new Word("filter_all", false), terms));

        return body;

    }

    private static List<Rule> getComparisonRules(Question question) {
        List<Rule> rules = new ArrayList<>();
        if (!question.semanticRoot.getRelationMap().getOrDefault("amod", new ArrayList<>()).isEmpty() &&
                question.semanticRoot.getRelationMap().get("amod").get(0).getLemma().equalsIgnoreCase("same")) {
            String comparisonAttribute = question.semanticRoot.getLemma();
            TypedDependency c1 = question.dependencies.stream()
                    .filter(d -> d.reln().getShortName().equalsIgnoreCase("nsubj")
                            && question.wordList.get(d.gov().index()-1).getLemma().equalsIgnoreCase(question.semanticRoot.getLemma()))
                    .findFirst().orElse(null);
            //String comparator1 = c1 != null ? c1.dep().value() : null;
            String comparator1 = c1 != null ? question.wordList.get(c1.dep().index()-1).getLemma() : null;
            String comparator1Index = Integer.toString(c1.dep().index());
            TypedDependency c2 = question.dependencies.stream()
                    .filter(d -> d.reln().getShortName().equalsIgnoreCase("nmod")
                            && question.wordList.get(d.gov().index()-1).getLemma().equalsIgnoreCase(question.semanticRoot.getLemma()))
                    .findFirst().orElse(null);
            //String comparator2 = c2 != null ? c2.dep().value() : null;
            String comparator2 = c2 != null ? question.wordList.get(c2.dep().index()-1).getLemma() : null;
            String comparator2Index = Integer.toString(c2.dep().index());
            rules.add(getEqualComparisonRules(comparisonAttribute, comparator1, comparator2, comparator1Index, comparator2Index));
        } else if (question.semanticRoot.getLemma().equalsIgnoreCase("same")) {
            String comparisonAttribute = question.semanticRoot.getRelationMap().getOrDefault("nsubj", new ArrayList<>()).get(0).getLemma();
            TypedDependency c1 = question.dependencies.stream().filter(d -> d.reln().getShortName().equalsIgnoreCase("nmod")
                    && question.wordList.get(d.gov().index()-1).getLemma().equalsIgnoreCase(comparisonAttribute))
                    .findFirst().orElse(null);
            //String comparator1 = c1 != null ? c1.dep().value() : null;
            String comparator1 = c1 != null ? question.wordList.get(c1.dep().index()-1).getLemma() : null;
            String comparator1Index = Integer.toString(c1.dep().index());
            TypedDependency c2 = question.dependencies.stream()
                    .filter(d -> d.reln().getShortName().equalsIgnoreCase("nmod")
                            && question.wordList.get(d.gov().index()-1).getLemma().equalsIgnoreCase(question.semanticRoot.getLemma()))
                    .findFirst().orElse(null);
            //String comparator2 = c2 != null ? c2.dep().value() : null;
            String comparator2 = c2 != null ? question.wordList.get(c2.dep().index()-1).getLemma() : null;
            String comparator2Index = Integer.toString(c2.dep().index());
            rules.add(getEqualComparisonRules(comparisonAttribute, comparator1, comparator2, comparator1Index, comparator2Index));
        } else if (!question.semanticRoot.getRelationMap().getOrDefault("nsubj", new ArrayList<>()).isEmpty() &&
                question.dependencies.stream().filter(d -> d.reln().getShortName().equalsIgnoreCase("dep")
                        && question.wordList.get(d.dep().index()-1).getLemma().equalsIgnoreCase("same"))
                        .findFirst().isPresent()) {
            String comparisonAttribute = question.semanticRoot.getRelationMap().get("nsubj").get(0).getLemma();
            TypedDependency c1 = question.dependencies.stream().filter(d -> d.reln().getShortName().equalsIgnoreCase("nmod")
                    && d.reln().getSpecific().equalsIgnoreCase("of")
                    && question.wordList.get(d.gov().index()-1).getLemma().equalsIgnoreCase(comparisonAttribute))
                    .findFirst().orElse(null);

            //String comparator1 = c1 != null ? c1.dep().value() : null;
            String comparator1 = c1 != null ? question.wordList.get(c1.dep().index()-1).getLemma() : null;
            String comparator1Index = Integer.toString(c1.dep().index());
            TypedDependency c2 = question.dependencies.stream().filter(d -> d.reln().getShortName().equalsIgnoreCase("nmod")
                    && d.reln().getSpecific().equalsIgnoreCase("as")
                    && question.wordList.get(d.gov().index()-1).getLemma().equalsIgnoreCase("same"))
                    .findFirst().orElse(null);
           // String comparator2 = c2 != null ? c2.dep().value() : null;
            String comparator2 = c2 != null ? question.wordList.get(c2.dep().index()-1).getLemma() : null;
            String comparator2Index = Integer.toString(c2.dep().index());
            rules.add(getEqualComparisonRules(comparisonAttribute, comparator1, comparator2, comparator1Index, comparator2Index));
        } else if (question.semanticRoot.getPOSTag().equalsIgnoreCase("nn") &&
                !question.semanticRoot.getRelationMap().getOrDefault("det", new ArrayList<>()).isEmpty() &&
                !question.semanticRoot.getRelationMap().getOrDefault("dep", new ArrayList<>()).isEmpty() &&
                !question.semanticRoot.getRelationMap().get("dep").get(0).getRelationMap().getOrDefault("amod", new ArrayList<>()).isEmpty() &&
                question.semanticRoot.getRelationMap().get("dep").get(0).getRelationMap().get("amod").get(0).getLemma().equalsIgnoreCase("same")) {
            String comparisonAttribute = question.semanticRoot.getRelationMap().get("dep").get(0).getLemma();
            String comparator1 = question.semanticRoot.getLemma();
            String comparator1Index = Integer.toString(question.semanticRoot.getWordIndex());
            TypedDependency c2 = question.dependencies.stream().filter(d -> d.reln().getShortName().equalsIgnoreCase("nmod")
                    && d.reln().getSpecific().equalsIgnoreCase("as")
                    && question.wordList.get(d.gov().index()-1).getLemma().equalsIgnoreCase(comparisonAttribute))
                    .findFirst().orElse(null);
            //String comparator2 = c2 != null ? c2.dep().value() : null;
            String comparator2 = c2 != null ? question.wordList.get(c2.dep().index()-1).getLemma() : null;
            String comparator2Index = Integer.toString(c2.dep().index());
            rules.add(getEqualComparisonRules(comparisonAttribute, comparator1, comparator2, comparator1Index, comparator2Index));
        } else if (question.semanticRoot.getPOSTag().equalsIgnoreCase("nn") &&
                !question.semanticRoot.getRelationMap().getOrDefault("det", new ArrayList<>()).isEmpty() &&
                !question.semanticRoot.getRelationMap().getOrDefault("dobj", new ArrayList<>()).isEmpty() &&
                !question.semanticRoot.getRelationMap().get("dobj").get(0).getRelationMap().getOrDefault("amod", new ArrayList<>()).isEmpty() &&
                question.semanticRoot.getRelationMap().get("dobj").get(0).getRelationMap().get("amod").get(0).getLemma().equalsIgnoreCase("same")) {
            String comparisonAttribute = question.semanticRoot.getRelationMap().get("dobj").get(0).getLemma();
            String comparator1 = question.semanticRoot.getLemma();
            String comparator1Index = Integer.toString(question.semanticRoot.getWordIndex());
            TypedDependency c2 = question.dependencies.stream().filter(d -> d.reln().getShortName().equalsIgnoreCase("nmod")
                    && d.reln().getSpecific().equalsIgnoreCase("as")
                    && question.wordList.get(d.gov().index()-1).getLemma().equalsIgnoreCase(comparisonAttribute))
                    .findFirst().orElse(null);
            //String comparator2 = c2 != null ? c2.dep().value() : null;
            String comparator2 = c2 != null ? question.wordList.get(c2.dep().index()-1).getLemma() : null;
            String comparator2Index = Integer.toString(c2.dep().index());
            rules.add(getEqualComparisonRules(comparisonAttribute, comparator1, comparator2, comparator1Index, comparator2Index));
        } else if (!question.semanticRoot.getRelationMap().get("nsubj").get(0).getRelationMap().getOrDefault("acl", new ArrayList<>()).isEmpty()) {
            String comparator1 = question.semanticRoot.getRelationMap().get("nsubj").get(0).getLemma();
            String comparator1Index = Integer.toString(question.semanticRoot.getRelationMap().get("nsubj").get(0).getWordIndex());
            Word w = question.semanticRoot.getRelationMap().get("nsubj").get(0).getRelationMap().get("acl").get(0);
            String comparisonAttribute = w.getRelationMap().get("nmod:of").get(0).getLemma();
            String comparator2 = w.getRelationMap().get("nmod:as").get(0).getLemma();
            String comparator2Index = Integer.toString(w.getRelationMap().get("nmod:as").get(0).getWordIndex());
            if (!w.getRelationMap().get("nmod:of").get(0).getRelationMap().getOrDefault("amod", new ArrayList<>()).isEmpty() &&
                    w.getRelationMap().get("nmod:of").get(0).getRelationMap().get("amod").get(0).getLemma().equalsIgnoreCase("same")) {
                rules.add(getEqualComparisonRules(comparisonAttribute, comparator1, comparator2, comparator1Index, comparator2Index));
            }
        } else if (question.dependencies.stream().filter(d -> d.reln().getShortName().equalsIgnoreCase("acl")).findFirst()
                .get().dep().value().equalsIgnoreCase("made") &&
                question.dependencies.stream().filter(d -> d.reln().getShortName().equalsIgnoreCase("nmod") &&
                        d.reln().getSpecific().equalsIgnoreCase("of")
                        && d.gov().value().equalsIgnoreCase("made")).findFirst().isPresent()) {
            TypedDependency c1 = question.dependencies.stream().filter(d -> d.reln().getShortName().equalsIgnoreCase("conj")
                    && d.reln().getSpecific().equalsIgnoreCase("and")).findFirst().get();

            String comparator1Index = Integer.toString(c1.gov().index());
            String comparator1 = question.wordList.get(c1.gov().index()-1).getLemma();
            String comparator2Index = Integer.toString(c1.dep().index());
            String comparator2 = question.wordList.get(c1.dep().index()-1).getLemma();
            int comparisonAttributeIndex = question.dependencies.stream().filter(d -> d.reln().getShortName().equalsIgnoreCase("nmod")
                    && d.reln().getSpecific().equalsIgnoreCase("of")
                    && d.gov().value().equalsIgnoreCase("made")).findFirst().get().dep().index();
            String comparisonAttribute = question.wordList.get(comparisonAttributeIndex-1).getLemma();
            rules.add(getEqualComparisonRules(comparisonAttribute, comparator1, comparator2, comparator1Index, comparator2Index));
        }
        return rules;
    }

    private static List<Rule> getArithmaticRules(Question question) {
        List<Rule> rules = new ArrayList<>();
        Optional<Word> comparatorWord = question.wordList.stream().filter(w -> w.getPOSTag().matches("JJR|RBR|JJ-JJR|JJ-RBR")).findFirst();
        if (comparatorWord.isPresent() && comparatorWord.get().getPOSTag().matches("JJR|JJ-JJR") &&
                comparatorWord.get().getLemma().matches("less|greater") &&
                !comparatorWord.get().getRelationMap().getOrDefault("nmod:than", new ArrayList<>()).isEmpty()) {

            TypedDependency c1 = question.dependencies.stream().filter(r -> r.reln().getShortName().equalsIgnoreCase("amod")
                    && r.dep().value().matches("less|greater")).findFirst().orElse(null);
            String object1Index = c1 != null ? Integer.toString(c1.gov().index()) : null;
            //String object1 = question.wordList.stream().filter(w -> w.getWordIndex() == Integer.parseInt(object1Index)).findFirst().get().getLemma();
            String object1 = question.wordList.get(Integer.parseInt(object1Index) - 1).getLemma();
            String object2 = comparatorWord.get().getRelationMap().get("nmod:than")
                    .get(0).getRelationMap().getOrDefault("nmod:of", new ArrayList<>()).get(0).getLemma();
            String object2Index = Integer.toString(comparatorWord.get().getRelationMap().get("nmod:than")
                    .get(0).getRelationMap().getOrDefault("nmod:of", new ArrayList<>()).get(0).getWordIndex());

            rules.add(getArithmaticRulesBody(object1, object1Index, object2, object2Index, comparatorWord.get().getLemma()));
        } else if (comparatorWord.isPresent() && comparatorWord.get().getPOSTag().matches("RBR|JJ-RBR")
                && comparatorWord.get().getLemma().matches("less|greater") &&
                !comparatorWord.get().getRelationMap().getOrDefault("nmod:than", new ArrayList<>()).isEmpty()) {

            TypedDependency c1 = question.dependencies.stream().filter(r -> r.reln().getShortName().equalsIgnoreCase("nmod")
                    && r.gov().value().equalsIgnoreCase("number")).findFirst().orElse(null);
            //String object1 = c1 != null ? c1.gov().value() : null;
            String object1Index = c1 != null ? Integer.toString(c1.dep().index()) : null;
            //String object1 = question.wordList.stream().filter(w -> w.getWordIndex() == Integer.parseInt(object1Index)).findFirst().get().getLemma();
            String object1 = question.wordList.get(Integer.parseInt(object1Index)-1).getLemma();
            String object2 = comparatorWord.get().getRelationMap().get("nmod:than")
                    .get(0).getRelationMap().getOrDefault("nmod:of", new ArrayList<>()).get(0).getLemma();
            String object2Index = Integer.toString(comparatorWord.get().getRelationMap().get("nmod:than")
                    .get(0).getRelationMap().getOrDefault("nmod:of", new ArrayList<>()).get(0).getWordIndex());

            rules.add(getArithmaticRulesBody(object1, object1Index, object2, object2Index, comparatorWord.get().getLemma()));

        } else if (comparatorWord.isPresent() && comparatorWord.get().getPOSTag().matches("JJR|JJ-JJR") &&
                comparatorWord.get().getLemma().matches("fewer") &&
                question.dependencies.stream()
                        .filter(d -> d.reln().getShortName().equalsIgnoreCase("nmod")
                                && d.reln().getSpecific().equalsIgnoreCase("than")).findFirst().isPresent()) {
            Word o1 = question.semanticRoot.getRelationMap().get("nsubj").get(0);
            String object1 = o1.getLemma();
            String object1Index = Integer.toString(o1.getWordIndex());
            String object2 = o1.getRelationMap().get("nmod:than").get(0).getLemma();
            String object2Index = Integer.toString(o1.getRelationMap().get("nmod:than").get(0).getWordIndex());

            rules.add(getArithmaticRulesBody(object1, object1Index, object2, object2Index, "less"));
        } else if (comparatorWord.isPresent() && comparatorWord.get().getPOSTag().matches("RBR|JJ-RBR") &&
                comparatorWord.get().getLemma().equalsIgnoreCase("more") &&
                !question.semanticRoot.getRelationMap().get("advmod").get(0).getRelationMap().getOrDefault("dep", new ArrayList<>()).isEmpty()) {
            Word o1 = question.semanticRoot.getRelationMap().get("advmod").get(0).getRelationMap().get("dep").get(0);
            String object1 = o1.getLemma();
            String object1Index = Integer.toString(o1.getWordIndex());
            String object2 = o1.getRelationMap().get("nmod:than").get(0).getLemma();
            String object2Index = Integer.toString(o1.getRelationMap().get("nmod:than").get(0).getWordIndex());
            rules.add(getArithmaticRulesBody(object1, object1Index, object2, object2Index, "greater"));
        }
        return rules;
    }

    private static Rule getArithmaticRulesBody(String object1, String object1Index, String object2, String object2Index, String comparatorWord) {
        Literal head = null;
        List<Literal> body = new ArrayList<>();
        List<Literal> terms = new ArrayList<>();

        terms.add(new Literal(new Word(object1, false)));
        terms.add(new Literal(new Word(object1Index, false)));
        terms.add(new Literal(new Word("L1", true)));
        body.add(new Literal(new Word("find_all_filters", false), terms));

        terms = new ArrayList<>();
        terms.add(new Literal(new Word("L1", true)));
        terms.add(new Literal(new Word("Ids1", true)));
        body.add(new Literal(new Word("list_object", false), terms));

        terms = new ArrayList<>();
        terms.add(new Literal(new Word("Ids1", true)));
        terms.add(new Literal(new Word("C1", true)));
        body.add(new Literal(new Word("list_length", false), terms));

        terms = new ArrayList<>();
        terms.add(new Literal(new Word(object2, false)));
        terms.add(new Literal(new Word(object2Index, false)));
        terms.add(new Literal(new Word("L2", true)));
        body.add(new Literal(new Word("find_all_filters", false), terms));

        terms = new ArrayList<>();
        terms.add(new Literal(new Word("L2", true)));
        terms.add(new Literal(new Word("Ids2", true)));
        body.add(new Literal(new Word("list_object", false), terms));

        terms = new ArrayList<>();
        terms.add(new Literal(new Word("Ids2", true)));
        terms.add(new Literal(new Word("C2", true)));
        body.add(new Literal(new Word("list_length", false), terms));

        terms = new ArrayList<>();
        terms.add(new Literal(new Word("C1", true)));
        terms.add(new Literal(new Word("C2", true)));
        if (comparatorWord.equalsIgnoreCase("less")) {
            body.add(new Literal(new Word("lt", false), terms));
        } else if (comparatorWord.equalsIgnoreCase("greater")) {
            body.add(new Literal(new Word("gt", false), terms));
        }
        return new Rule(head, body, true);
    }


    private static Rule getEqualComparisonRules(String comparisonAttribute, String comparator1, String comparator2, String comparator1Index, String comparator2Index) {
        Rule r = null;
        if (comparisonAttribute != null && comparator1 != null && comparator2 != null) {
            Literal head = null;
            List<Literal> body = new ArrayList<>();

            List<Literal> terms = new ArrayList<>();
            terms.add(new Literal(new Word(comparator1, false)));
            terms.add(new Literal(new Word(comparator1Index, false)));
            terms.add(new Literal(new Word("L1", true)));
            body.add(new Literal(new Word("find_all_filters", false), terms));


            terms = new ArrayList<>();
            terms.add(new Literal(new Word(comparator2, false)));
            terms.add(new Literal(new Word(comparator2Index, false)));
            terms.add(new Literal(new Word("L2", true)));
            body.add(new Literal(new Word("find_all_filters", false), terms));


            terms = new ArrayList<>();
            terms.add(new Literal(new Word("L1", true)));
            terms.add(new Literal(new Word("Ids1", true)));
            body.add(new Literal(new Word("list_object", false), terms));


            terms = new ArrayList<>();
            terms.add(new Literal(new Word("L2", true)));
            terms.add(new Literal(new Word("Ids2", true)));
            body.add(new Literal(new Word("list_object", false), terms));


            terms = new ArrayList<>();
            terms.add(new Literal(new Word("Ids1", true)));
            terms.add(new Literal(new Word(comparisonAttribute, false)));
            terms.add(new Literal(new Word("Val", true)));
            body.add(new Literal(new Word("get_att_val", false), terms));

            terms = new ArrayList<>();
            terms.add(new Literal(new Word("Ids2", true)));
            terms.add(new Literal(new Word(comparisonAttribute, false)));
            terms.add(new Literal(new Word("Val", true)));
            body.add(new Literal(new Word("get_att_val", false), terms));

            r = new Rule(head, body, true);
        }
        return r;
    }




    private static Rule getComplexExistentialPredicates(String exitentialItem, String existentialIndex, String comparator,
                                                        String comparatorIndex, String comparisonAttribute) {
        Rule rule = null;
        Literal head = null;
        List<Literal> body = new ArrayList<>();

        List<Literal> terms = new ArrayList<>();
        terms.add(new Literal(new Word(comparator, false)));
        terms.add(new Literal(new Word(comparatorIndex, false)));
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
        terms.add(new Literal(new Word(exitentialItem, false)));
        terms.add(new Literal(new Word(existentialIndex, false)));
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


        terms = new ArrayList<>();
        terms.add(new Literal(new Word("Ids2", true)));
        terms.add(new Literal(new Word("Ids1", true)));
        terms.add(new Literal(new Word("Ids", true)));
        body.add(new Literal(new Word("list_subtract", false), terms));

        terms = new ArrayList<>();
        terms.add(new Literal(new Word("Ids", true)));
        terms.add(new Literal(new Word("C", true)));
        body.add(new Literal(new Word("list_length", false), terms));

        terms = new ArrayList<>();
        terms.add(new Literal(new Word("N", true)));
        terms.add(new Literal(new Word(exitentialItem + "_" + existentialIndex, false)));
        body.add(new Literal(new Word("quantification", false), terms));

        terms = new ArrayList<>();
        terms.add(new Literal(new Word("C", true)));
        terms.add(new Literal(new Word("N", true)));
        body.add(new Literal(new Word("gte", false), terms));


        rule = new Rule(head, body, true);
        return rule;
    }

}
