import edu.stanford.nlp.trees.TypedDependency;
import org.json.JSONException;

import java.io.*;
import java.net.URISyntaxException;
import java.util.*;
import java.util.stream.Collectors;

public class ClevrQueryGeneration {


    public static List<Rule> getClevrQuery(Question question) {
        List<Rule> rules = new ArrayList<>();
        if (question.information.answerType == AnswerType.BOOLEAN || question.information.questionType == QuestionType.TRUE_FALSE) {
            rules = getBooleanRules(question);
        }
        return rules;
    }

    private static List<Rule> getBooleanRules(Question question) {
        List<Rule> rules = new ArrayList<>();
        List<Word> nouns = question.wordList.stream().filter(w -> w.getPOSTag().toLowerCase().matches("nn|nnp|nns|nnps")).collect(Collectors.toList());
        //Map<String,List<String>> commonSenseConceptsMap = nouns.stream().collect(Collectors.toMap(n -> n.getWord(), n-> SemanticRelationsGeneration.getConcept(n.getWord())));

        generateFacts(question, nouns);
        //Is there a tiny cyan matte ball?
        if (nouns.size() == 1) {
            rules.addAll(getExistentialRule(nouns.get(0)));
        }
        //Is there a cylinder of the same color as the rubber object?
        else if (question.semanticRoot.getPOSTag().toLowerCase().matches("vbz") &&
                !question.semanticRoot.getRelationMap().getOrDefault("nsubj", new ArrayList<>()).isEmpty() &&
                !question.semanticRoot.getRelationMap().getOrDefault("expl", new ArrayList<>()).isEmpty()) {
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
        }
        //Is the number of things less than the number of big blue spheres?
        else if (question.semanticRoot.getPOSTag().toLowerCase().matches("vbz") &&
                !question.semanticRoot.getRelationMap().getOrDefault("nsubj", new ArrayList<>()).isEmpty() &&
                question.semanticRoot.getRelationMap().get("nsubj").get(0).getLemma().equalsIgnoreCase("number")) {
            rules.addAll(getArithmaticRules(question));
        }
        rules = modifyCommonQueryRules(rules, true);
        //return getCommonQueryRules(rules,true);
        rules.addAll(getCommonQueryRules(question, true));
        return rules;
    }


    private static List<Rule> getCommonQueryRules(Question question, boolean isBooleanQuestion) {
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

    private static List<Rule> modifyCommonQueryRules(List<Rule> rules, boolean isBooleanQuestion) {
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
                            && d.gov().value().equalsIgnoreCase(exitentialItem))
                    .findFirst().orElse(null);

            String comparator = c1 != null ? c1.dep().value() : null;
            String comparatorIndex = c1 != null ? Integer.toString(c1.dep().index()) : null;
            TypedDependency c2 = question.dependencies.stream()
                    .filter(d -> d.reln().getShortName().equalsIgnoreCase("nmod")
                            && d.reln().getSpecific().equalsIgnoreCase("of")
                            && d.gov().value().equalsIgnoreCase(exitentialItem))
                    .findFirst().orElse(null);

            String comparisonAttribute = c2 != null ? c2.dep().value() : null;
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
            } else {
                String comparisonAttribute = w.getRelationMap().get("dobj").get(0).getLemma();
                String comparator = w.getRelationMap().get("nmod:as").get(0).getLemma();
                String comparatorIndex = Integer.toString(w.getRelationMap().get("nmod:as").get(0).getWordIndex());
                rules.add(getComplexExistentialPredicates(existentialItem, existentialIndex, comparator, comparatorIndex, comparisonAttribute));

            }
        }

        //if(comparisonAttribute != null && exitentialItem != null && comparator != null){

        return rules;

    }

    private static List<Rule> getComparisonRules(Question question) {
        List<Rule> rules = new ArrayList<>();
        if (!question.semanticRoot.getRelationMap().getOrDefault("amod", new ArrayList<>()).isEmpty() &&
                question.semanticRoot.getRelationMap().get("amod").get(0).getLemma().equalsIgnoreCase("same")) {
            String comparisonAttribute = question.semanticRoot.getLemma();
            TypedDependency c1 = question.dependencies.stream()
                    .filter(d -> d.reln().getShortName().equalsIgnoreCase("nsubj")
                            && d.gov().value().equalsIgnoreCase(question.semanticRoot.getLemma()))
                    .findFirst().orElse(null);
            String comparator1 = c1 != null ? c1.dep().value() : null;
            String comparator1Index = Integer.toString(c1.dep().index());
            TypedDependency c2 = question.dependencies.stream()
                    .filter(d -> d.reln().getShortName().equalsIgnoreCase("nmod")
                            && d.gov().value().equalsIgnoreCase(question.semanticRoot.getLemma()))
                    .findFirst().orElse(null);
            String comparator2 = c2 != null ? c2.dep().value() : null;
            String comparator2Index = Integer.toString(c2.dep().index());
            rules.add(getEqualComparisonRules(comparisonAttribute, comparator1, comparator2, comparator1Index, comparator2Index));
        } else if (question.semanticRoot.getLemma().equalsIgnoreCase("same")) {
            String comparisonAttribute = question.semanticRoot.getRelationMap().getOrDefault("nsubj", new ArrayList<>()).get(0).getLemma();
            TypedDependency c1 = question.dependencies.stream().filter(d -> d.reln().getShortName().equalsIgnoreCase("nmod")
                    && d.gov().value().equalsIgnoreCase(comparisonAttribute))
                    .findFirst().orElse(null);
            String comparator1 = c1 != null ? c1.dep().value() : null;
            String comparator1Index = Integer.toString(c1.dep().index());
            TypedDependency c2 = question.dependencies.stream()
                    .filter(d -> d.reln().getShortName().equalsIgnoreCase("nmod")
                            && d.gov().value().equalsIgnoreCase(question.semanticRoot.getLemma()))
                    .findFirst().orElse(null);
            String comparator2 = c2 != null ? c2.dep().value() : null;
            String comparator2Index = Integer.toString(c2.dep().index());
            rules.add(getEqualComparisonRules(comparisonAttribute, comparator1, comparator2, comparator1Index, comparator2Index));
        } else if (!question.semanticRoot.getRelationMap().getOrDefault("nsubj", new ArrayList<>()).isEmpty() &&
                question.dependencies.stream().filter(d -> d.reln().getShortName().equalsIgnoreCase("dep")
                        && d.dep().value().equalsIgnoreCase("same"))
                        .findFirst().isPresent()) {
            String comparisonAttribute = question.semanticRoot.getRelationMap().get("nsubj").get(0).getLemma();
            TypedDependency c1 = question.dependencies.stream().filter(d -> d.reln().getShortName().equalsIgnoreCase("nmod")
                    && d.reln().getSpecific().equalsIgnoreCase("of")
                    && d.gov().value().equalsIgnoreCase(comparisonAttribute))
                    .findFirst().orElse(null);

            String comparator1 = c1 != null ? c1.dep().value() : null;
            String comparator1Index = Integer.toString(c1.dep().index());
            TypedDependency c2 = question.dependencies.stream().filter(d -> d.reln().getShortName().equalsIgnoreCase("nmod")
                    && d.reln().getSpecific().equalsIgnoreCase("as")
                    && d.gov().value().equalsIgnoreCase("same"))
                    .findFirst().orElse(null);
            String comparator2 = c2 != null ? c2.dep().value() : null;
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
                    && d.gov().value().equalsIgnoreCase(comparisonAttribute))
                    .findFirst().orElse(null);
            String comparator2 = c2 != null ? c2.dep().value() : null;
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
                    && d.gov().value().equalsIgnoreCase(comparisonAttribute))
                    .findFirst().orElse(null);
            String comparator2 = c2 != null ? c2.dep().value() : null;
            String comparator2Index = Integer.toString(c2.dep().index());
            rules.add(getEqualComparisonRules(comparisonAttribute, comparator1, comparator2, comparator1Index, comparator2Index));
        }


        return rules;
    }

    private static List<Rule> getArithmaticRules(Question question) {
        List<Rule> rules = new ArrayList<>();
        Optional<Word> comparatorWord = question.wordList.stream().filter(w -> w.getPOSTag().matches("JJR|JJ-JJR|JJ-RBR")).findFirst();
        if (comparatorWord.isPresent() && comparatorWord.get().getPOSTag().matches("JJR|JJ-JJR") &&
                comparatorWord.get().getLemma().matches("less|greater") &&
                !comparatorWord.get().getRelationMap().getOrDefault("nmod:than", new ArrayList<>()).isEmpty()) {

            TypedDependency c1 = question.dependencies.stream().filter(r -> r.reln().getShortName().equalsIgnoreCase("amod")
                    && r.dep().value().matches("less|greater")).findFirst().orElse(null);
            //String object1 = c1 != null ? c1.gov().value() : null;
            String object1Index = c1 != null ? Integer.toString(c1.gov().index()) : null;
            String object1 = question.wordList.stream().filter(w -> w.getWordIndex() == Integer.parseInt(object1Index)).findFirst().get().getLemma();
            String object2 = comparatorWord.get().getRelationMap().get("nmod:than")
                    .get(0).getRelationMap().getOrDefault("nmod:of", new ArrayList<>()).get(0).getLemma();
            String object2Index = Integer.toString(comparatorWord.get().getRelationMap().get("nmod:than")
                    .get(0).getRelationMap().getOrDefault("nmod:of", new ArrayList<>()).get(0).getWordIndex());

            rules.add(getArithmaticRulesBody(object1,object1Index,object2,object2Index,comparatorWord.get()));
        }
        else if (comparatorWord.isPresent() && comparatorWord.get().getPOSTag().matches("JJR|JJ-RBR")
                && comparatorWord.get().getLemma().matches("less|greater") &&
                !comparatorWord.get().getRelationMap().getOrDefault("nmod:than", new ArrayList<>()).isEmpty()) {

            TypedDependency c1 =question.dependencies.stream().filter(r -> r.reln().getShortName().equalsIgnoreCase("nmod")
                    && r.gov().value().equalsIgnoreCase("number")).findFirst().orElse(null);
            //String object1 = c1 != null ? c1.gov().value() : null;
            String object1Index = c1 != null ? Integer.toString(c1.dep().index()) : null;
            String object1 = question.wordList.stream().filter(w -> w.getWordIndex() == Integer.parseInt(object1Index)).findFirst().get().getLemma();
            String object2 = comparatorWord.get().getRelationMap().get("nmod:than")
                    .get(0).getRelationMap().getOrDefault("nmod:of", new ArrayList<>()).get(0).getLemma();
            String object2Index = Integer.toString(comparatorWord.get().getRelationMap().get("nmod:than")
                    .get(0).getRelationMap().getOrDefault("nmod:of", new ArrayList<>()).get(0).getWordIndex());

            rules.add(getArithmaticRulesBody(object1,object1Index,object2,object2Index,comparatorWord.get()));
        }
        return rules;
    }

    private static Rule getArithmaticRulesBody(String object1, String object1Index, String object2, String object2Index, Word word) {
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
        if(word.getLemma().equalsIgnoreCase("less")){
            body.add(new Literal(new Word("lt", false), terms));
        }
        else if(word.getLemma().equalsIgnoreCase("greater")){
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


    private static Map<Word, List<Word>> getProperties(Question question, List<Word> nouns) {
        Map<Word, List<Word>> properties = new HashMap<>();

        for (Word noun : nouns) {
            List<Word> mods = getModifier(noun);
            properties.put(noun, mods);
        }

        return properties;
    }

    private static List<Word> getModifier(Word noun) {
        List<Word> modifiers = new ArrayList<>();
        if (noun.getRelationMap().containsKey("amod")) {
            modifiers.addAll(noun.getRelationMap().get("amod"));
        }

        return modifiers;
    }

    private static void generateFacts(Question question, List<Word> nouns) {
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
