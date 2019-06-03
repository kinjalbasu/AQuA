import java.util.*;

/**
 * Created by dhruv on 10/4/2017.
 */
public class Question extends Sentence {

    QuestionInformation information = null;

    public Question(String sentence) {
        super(sentence);
        information = ExtractInformation();
    }

    private QuestionInformation ExtractInformation() {
        QuestionInformation information = new QuestionInformation();
        Word questionWord = null;
        for(Word word : this.wordList){
            if(word.IsQuestionWord()){
                if(word.getWord().equalsIgnoreCase("how")
                        && this.wordList.get(word.getWordIndex()).getWord().matches("many|much"))
                {
                    String newWordString = word.getWord()+"_"+this.wordList.get(word.getWordIndex()).getWord();
                    information.questionType = GetQuestionType(newWordString);
                    questionWord = word;
                }
                else {
                    questionWord = word;
                    information.questionType = GetQuestionType(word.getWord());
                }
                break;
            }
        }

        information.questionWord = questionWord;
        //information.questionType = GetQuestionType(questionWord);
        Word answerKind = GetAnswerKind(questionWord);
        information.answerKind = answerKind;
        information.answerType = GetAnswerType(answerKind, information.questionType);

        StringBuilder builder = new StringBuilder();
        builder.append("\n\nQuestion Information : \n");
        String value = information.questionWord == null ? "null":information.questionWord.toString();
        builder.append("Question Word : " + value + "\n");
        builder.append("Question Type : " + information.questionType.toString() + "\n");
        value = information.answerKind == null ? "null":information.answerKind.toString();
        builder.append("Answer Word : " + value + "\n");
        builder.append("Answer Type : " + information.answerType.toString() + "\n");
        //System.out.println(builder.toString());
        return information;
    }

    private AnswerType GetAnswerType(Word answerKind, QuestionType questionType) {
        if(questionType == QuestionType.HOW_MANY || questionType == QuestionType.HOW_MUCH){
            return  AnswerType.QUANTITY;
        }
        if(questionType == QuestionType.WHEN){
            return AnswerType.TIME;
        }
        if(questionType == QuestionType.WHAT) {
            if(answerKind == null) return AnswerType.UNKNOWN;
            switch (answerKind.getLemma().toLowerCase()) {
                case "year":
                    return AnswerType.YEAR;
                case "time":
                    return AnswerType.TIME;
                case "day":
                    return AnswerType.DAY;
                case "month":
                    return AnswerType.MONTH;
            }

            for (Word word : this.wordList) {
                if (word.IsVerb()) {
                    AnswerType type = word.GetAnswerType(answerKind);
                    if (type != AnswerType.UNKNOWN) return type;
                }
            }
        }

        return AnswerType.UNKNOWN;
    }

    private Word GetAnswerKind(Word questionWord) {
        for(Word word : wordList){
            List<Word> determiners = word.GetDeterminers();
            for(Word determiner : determiners){
                if(determiner == questionWord){
                    return word;
                }
            }
        }

        return null;
    }

    private QuestionType GetQuestionType(String questionWord) {
        switch(questionWord.toLowerCase()){
            case "what": return QuestionType.WHAT;
            case "where": return QuestionType.WHERE;
            case "who": return QuestionType.WHO;
            case "when": return QuestionType.WHEN;
            case "how_many": return QuestionType.HOW_MANY;
            case "how_much": return QuestionType.HOW_MUCH;
        }

        return QuestionType.UNKNOWN;
    }

    public List<Rule> GenerateRules() {
        List<Rule> rules = new ArrayList<>();
        List<Rule> constraints = this.preProcessRules;
        List<Rule> eventQueries = new ArrayList<>();

        for(Word word : this.wordList){
            if(word.getPOSTag().equalsIgnoreCase(",")) continue;
            if(word.IsVerb()){
                eventQueries.addAll(word.GenerateVerbQuestionRules(this.information));
            }
            else if(word.IsNoun() || word.IsAdjective()){
                constraints.addAll(word.GenerateNounConstraintRules(this.information));
            }
        }

        List<Rule> finalConstraints = Word.GenerateQuestionConstraintRules(this.information, this.dependencies, this.wordList);
        List<Rule> combinedConstraints = new ArrayList<>();
        Rule allConstraints = Rule.AggregateAllRules(constraints);
        for(Rule constraint : finalConstraints) {
            Rule rule = Rule.ApplyConstraint(constraint, allConstraints);
            combinedConstraints.add(rule);
        }

        if(finalConstraints.size() == 0) {
            combinedConstraints.add(allConstraints);
        }

        for(Rule eventQuery : eventQueries){
            for(Rule combinedConstraint : combinedConstraints){
                Rule rule = Rule.ApplyConstraint(eventQuery, combinedConstraint);
                rules.add(rule);
            }
        }

        List<Rule> specialRules = GenerateSpecialRules(this.information);
        for(Rule specialRule : specialRules){
            for(Rule finalConstraint : finalConstraints){
                Rule rule = Rule.ApplyConstraint(specialRule, finalConstraint);
                rules.add(rule);
            }
        }

        if(rules.size() != 0) return rules;
        rules.addAll(combinedConstraints);

        return rules;
    }

    private List<Rule> GenerateSpecialRules(QuestionInformation information) {
        List<Rule> rules = new ArrayList<>();

        // Adding special rules for birth date and death date
        if(information.questionType == QuestionType.WHEN){
            Word verb = GetVerb(this.wordList, "bear");
            if(verb != null) {
                /*Word complement = verb.HasClausalComplement(this.wordList, verb);
                if(complement == null) return rules;
                if(Word.IsVerbAndQuestionWordConnected(complement, information.questionWord)){
                    List<Rule> birthRules = Word.GenerateBirthRule(verb, information);
                    rules.addAll(birthRules);
                }*/
                if(Word.IsVerbAndQuestionWordConnected(verb, information.questionWord)){
                    List<Rule> birthRules = Word.GenerateBirthRule(verb, information);
                    rules.addAll(birthRules);
                }

                return rules;
            }

            verb = GetVerb(this.wordList, "die");
            if(verb != null) {
                Word complement = verb.HasClausalComplement(this.wordList, verb);
                if(complement == null) return rules;
                if(Word.IsVerbAndQuestionWordConnected(complement, information.questionWord)){
                    List<Rule> deathRules = Word.GenerateDeathRule(verb, information);
                    rules.addAll(deathRules);
                }

                return rules;
            }
        }

        return rules;
    }

    private Word GetVerb(List<Word> wordList, String verbLemma) {
        for(Word word : wordList){
            if(word.getLemma().equalsIgnoreCase(verbLemma)){
                return word;
            }
        }

        return null;
    }

    public List<Rule> GenerateAllRules() {
        TreeSet<Rule> rulesSet = new TreeSet<>(new Comparator<Rule>() {
            @Override
            public int compare(Rule rule, Rule anotherRule) {
                return rule.toString().compareTo(anotherRule.toString());
            }
        });

        List<Rule> unconstraintRules = GenerateRules();
        rulesSet.addAll(unconstraintRules);

        Set<Rule> factConstraintRules = FilterRules(unconstraintRules, LiteralType.CONSTRAINT_QUERY);
        rulesSet.addAll(factConstraintRules);

        Set<Rule> variableConstraintRules = FilterRules(unconstraintRules, LiteralType.ANSWER_QUERY);
        rulesSet.addAll(variableConstraintRules);

        Set<Rule> weakConstraintRules = FilterRules(unconstraintRules, LiteralType.BASE_CONSTRAINT);
        rulesSet.addAll(weakConstraintRules);

        for(Rule rule : rulesSet){
            String sentence = this.sentenceString.replaceAll("'", "");
            rule.SetQuery(sentence, this.information);
        }

        List<Rule> rulesList = new ArrayList<>(rulesSet);
        Collections.sort(rulesList, new Comparator<Rule>() {
            @Override
            public int compare(Rule rule, Rule anotherRule) {
                int ruleQuality = rule.maxRuleQuality.ordinal();
                int anotherRuleQuality = anotherRule.maxRuleQuality.ordinal();
                return Integer.compare(ruleQuality, anotherRuleQuality);
            }
        });

        return rulesList;
    }

    private TreeSet<Rule> FilterRules(List<Rule> inputRules, LiteralType maxLiteralType) {
        TreeSet<Rule> rules = new TreeSet<>();
        for(Rule inputRule : inputRules){
            Rule rule = Rule.FilterRule(inputRule, maxLiteralType);
            if(rule != null && rule.toString().length() != 0) rules.add(rule);
        }

        return rules;
    }
}
