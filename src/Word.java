import edu.stanford.nlp.trees.GrammaticalRelation;
import edu.stanford.nlp.trees.TypedDependency;
import javafx.util.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by dhruv on 9/24/2017.
 */
public class Word {

    private String word;
    private int wordIndex;
    private String POSTag;
    private String lemma;
    private String id;
    private String value;
    private NamedEntityTagger.NamedEntityTags NERTag;
    private HashMap<String, List<Word>> relationMap;
    public boolean isVariable = false;
    public static int eventId = 1;

    Word(String word, boolean isVariable) {
        this.isVariable = isVariable;
        this.wordIndex = 0;
        this.word = word.toLowerCase();
        if (isVariable) this.word = word;
        this.POSTag = "NN";
        this.lemma = word.toLowerCase();
        if (isVariable) this.lemma = word;
        this.relationMap = new HashMap<>();
        this.NERTag = NamedEntityTagger.GetEntityTag("O");
        this.id = "";
        this.value = word.toLowerCase();
    }

    Word(int wordIndex, String word, String lemma, String POSTag, String NERTag, boolean isPreProcessed) {
        this.wordIndex = wordIndex;
        this.word = isPreProcessed ? word.toLowerCase() : word;
        this.value = this.getWord();
        this.POSTag = POSTag;
        this.lemma = isPreProcessed ? lemma.toLowerCase() : lemma;
        this.NERTag = NamedEntityTagger.GetEntityTag(NERTag);
        this.relationMap = new HashMap<>();
        this.id = this.IsVerb() ? String.valueOf(this.eventId++) : "";
    }

  /*  Word(int wordIndex, String word, String lemma, String POSTag, String NERTag, String value, boolean isPreProcessed) {
        this.wordIndex = wordIndex;
        this.word = isPreProcessed ? word.toLowerCase() : word;
        this.value = value == null ? this.getWord() : value;
        ;
        this.POSTag = POSTag;
        this.lemma = isPreProcessed ? lemma.toLowerCase() : lemma;
        this.NERTag = NamedEntityTagger.GetEntityTag(NERTag);
        this.relationMap = new HashMap<>();
        this.id = this.IsVerb() ? String.valueOf(this.eventId++) : "";
    }*/


    Word(Word word) {
        this.wordIndex = word.wordIndex;
        this.word = word.getWord();
        this.POSTag = word.getPOSTag();
        this.lemma = word.getLemma();
        this.NERTag = word.getNERTag();
        this.relationMap = new HashMap<>();
        this.id = word.id;
        this.value = word.getWord();
    }

    @Override
    public String toString() {
        return String.format("%s-%s", this.word, this.wordIndex);
    }

    public void AddDependency(Word dependentWord, GrammaticalRelation relation) {
        if (dependentWord == null) {
            return;
        }

        // Misinterpreting Adjective as a OtherTag(X)
        String relationName = relation.getShortName();
        if (relationName.equalsIgnoreCase("amod") && !dependentWord.getPOSTag().equals("JJ")) {
            dependentWord.POSTag = String.format("JJ-%s", dependentWord.POSTag);
        }

        // Misinterpreting Verb as Noun
        if (relationName.equalsIgnoreCase("dobj") ||
                (relationName.equalsIgnoreCase("nmod") && relation.getSpecific() != null &&
                        relation.getSpecific().equalsIgnoreCase("agent"))) {
            if (this.getPOSTag().startsWith("NN")) {
                this.POSTag = String.format("VB-%s", this.POSTag);
                this.id = String.valueOf(this.eventId++);
            }
        }

        String specific = relation.getSpecific();
        if (relationName.equals("nmod") && specific != null) {
            relationName = String.format("%s:%s", relationName, specific);
        }

        if (!this.relationMap.containsKey(relationName)) {
            this.relationMap.put(relationName, new ArrayList<>());
        }

        List<Word> dependencies = this.relationMap.get(relationName);
        dependencies.add(dependentWord);
        this.relationMap.put(relationName, dependencies);
    }

    public String getPOSTag() {
        return this.POSTag;
    }

    public NamedEntityTagger.NamedEntityTags getNERTag() {
        return this.NERTag;
    }

    public String getLemma() {
        return lemma;
    }

    public String getWord() {
        return word;
    }

    public int getWordIndex() {
        return wordIndex;
    }

    public String getValue() {
        return value;
    }


    public void setWord(String word) {
        this.word = word;
    }

    public void setLemma(String lemma) {
        this.lemma = lemma;
    }

    public List<Rule> GenerateRules() {
        if (this.IsVerb()) {
            return GenerateRulesForVerb();
        } else if (this.IsNoun() || this.IsAdjective()) {
            return GenerateRulesForNouns();
        }

        return new ArrayList<>();
    }

    public List<Rule> GenerateVerbQuestionRules(QuestionInformation information) {
        List<Rule> rules = new ArrayList<>();
        Rule normalQuery = GenerateNormalQuery(information);
        Rule agentQuery = GenerateAgentQuery(information);
        Rule clausalQuery = GenerateClausalQuery(information);

        List<Rule> propertyConstraints = GeneratePropertyConstraints(information);
        Rule constraint = Rule.AggregateAllRules(propertyConstraints);

        if (normalQuery != null) rules.add(Rule.ApplyConstraint(normalQuery, constraint));
        if (agentQuery != null) rules.add(Rule.ApplyConstraint(agentQuery, constraint));
        if (clausalQuery != null) rules.add(Rule.ApplyConstraint(clausalQuery, constraint));
        return rules;
    }

    private Rule GenerateClausalQuery(QuestionInformation information) {
        List<Rule> rules = new ArrayList<>();
        Word eventWord = new Word("event", false);
        String subjectFormat = information.answerType == AnswerType.SUBJECT ? "X%s" : "S%s";

        List<Word> subjects = this.GetSubjects();
        if (subjects.size() == 0) return null;

        List<Literal> terms = new ArrayList<>();
        terms.add(new Literal(new Word(String.format("E%s", this.id), true)));
        terms.add(new Literal(new Word(this.getLemma(), false)));
        terms.add(new Literal(new Word("_", false)));
        terms.add(new Literal(new Word("_", false)));
        Literal queryEvent = new Literal(eventWord, terms);
        Rule rule = new Rule(queryEvent, null, true);
        rules.add(rule);

        Word clausalSubjectPredicate = new Word("_relation", false);
        Word similarPredicate = new Word("_similar", false);
        for (Word subject : subjects) {
            if (information.answerKind != subject) {
                terms = new ArrayList<>();
                terms.add(new Literal(subject));
                terms.add(new Literal(new Word(String.format(subjectFormat, this.id), true)));
                Literal similarQuery = new Literal(similarPredicate, terms);
                rule = new Rule(similarQuery, null, true);
                rules.add(rule);
            }

            terms = new ArrayList<>();
            terms.add(new Literal(new Word(String.format(subjectFormat, this.id), true)));
            terms.add(new Literal(new Word(String.format("E%s", this.id), true)));
            terms.add(new Literal(new Word("_clause", false)));
            Literal clausalQuery = new Literal(clausalSubjectPredicate, terms);
            rule = new Rule(clausalQuery, null, true);
            rules.add(rule);
        }

        if (rules.size() == 0) return null;
        rule = Rule.AggregateAllRules(rules);
        return rule;
    }

    private List<Rule> GeneratePropertyConstraints(QuestionInformation information) {
        List<Rule> rules = new ArrayList<>();
        Word predicate = new Word("_property", false);
        List<Pair<Word, Word>> modifierPairs = this.GetNominalModifiers();

        for (Pair<Word, Word> modifier : modifierPairs) {
            List<Literal> bodyList = new ArrayList<>();
            Literal concept = new Literal(this);
            Word preposition = modifier.getValue();
            if (this.id.length() > 0) {
                bodyList.add(new Literal(new Word(String.format("E%s", this.id), true)));
            }

            bodyList.add(concept);

            if (preposition == null || preposition.getWord().endsWith("mod")) {
                preposition = new Word("_", false);
            }

            bodyList.add(new Literal(preposition));
            Word modifierWord = modifier.getKey();
            if (modifierWord == information.answerKind) {
                bodyList.add(new Literal(new Word(String.format("X%s", modifierWord.id), true)));
            } else {
                bodyList.add(new Literal(modifierWord));
            }

            Literal head = new Literal(predicate, bodyList);
            Rule rule = new Rule(head, null, false);
            rules.add(rule);
        }

        return rules;
    }

    public boolean IsAdjective() {
        return this.getPOSTag().startsWith("JJ");
    }

    private List<Rule> GenerateRulesForNouns() {
        List<Rule> rules = new ArrayList<>();
        rules.addAll(this.GenerateAdjectiveRules());
        rules.addAll(this.GenerateNERRules());
        rules.addAll(this.GenerateNominalModifierRules());
        rules.addAll(this.GenerateAdjectiveClauseRules());
        rules.addAll(this.GenerateCopulaRules());
        rules.addAll(this.GenerateAppositionalModifierRules());
        rules.addAll(this.GeneratePossessiveRules());
        return rules;
    }

    private List<Rule> GeneratePossessiveRules() {
        List<Rule> rules = new ArrayList<>();
        Word bePredicate = new Word("_possess", false);

        List<Word> modifiers = GetPossessiveModifiers();
        for (Word modifier : modifiers) {
            List<Literal> terms = new ArrayList<>();
            terms.add(new Literal(modifier));
            terms.add(new Literal(this));
            Literal head = new Literal(bePredicate, terms);
            Rule rule = new Rule(head, null, false);
            rules.add(rule);

            List<Word> appositionalModifier = this.GetAppositionalModifiers();
            for (Word appos : appositionalModifier) {
                terms = new ArrayList<>();
                terms.add(new Literal(modifier));
                terms.add(new Literal(appos));
                head = new Literal(bePredicate, terms);
                rule = new Rule(head, null, false);
                rules.add(rule);
            }
        }

        return rules;
    }

    private List<Word> GetPossessiveModifiers() {
        List<Word> modifiers = new ArrayList<>();
        if (this.relationMap.containsKey("nmod:poss")) modifiers.addAll(this.relationMap.get("nmod:poss"));
        return modifiers;
    }

    private List<Rule> GenerateAppositionalModifierRules() {
        List<Rule> rules = new ArrayList<>();
        Word bePredicate = new Word("_is", false);

        List<Word> modifiers = GetAppositionalModifiers();
        for (Word modifier : modifiers) {
            List<Literal> terms = new ArrayList<>();
            terms.add(new Literal(this));
            terms.add(new Literal(modifier));
            Literal head = new Literal(bePredicate, terms);
            Rule rule = new Rule(head, null, false);
            rules.add(rule);

            terms = new ArrayList<>();

            if (WordNet.IsDictionaryWord(this)) {
                terms.add(new Literal(modifier));
                head = new Literal(this, terms);
                rule = new Rule(head, null, false);
                rules.add(rule);
            } else if (WordNet.IsDictionaryWord(modifier)) {
                terms.add(new Literal(this));
                head = new Literal(modifier, terms);
                rule = new Rule(head, null, false);
                rules.add(rule);
            }
        }

        return rules;
    }

    private List<Word> GetAppositionalModifiers() {
        List<Word> modifiers = new ArrayList<>();
        if (this.relationMap.containsKey("appos")) modifiers.addAll(this.relationMap.get("appos"));
        return modifiers;
    }

    private List<Rule> GenerateAdjectiveClauseRules() {
        List<Rule> rules = new ArrayList<>();
        List<Word> clauses = this.GetAdjectiveClause();
        Word relationWord = new Word("_relation", false);
        for (Word clause : clauses) {
            if (!clause.IsVerb()) continue;
            List<Literal> bodyList = new ArrayList<>();
            bodyList.add(new Literal(this));
            bodyList.add(new Literal(new Word(String.valueOf(clause.id), false)));
            bodyList.add(new Literal(new Word("_clause", false)));

            Literal head = new Literal(relationWord, bodyList);
            rules.add(new Rule(head, null, false));
        }

        return rules;
    }

    private List<Word> GetAdjectiveClause() {
        List<Word> adjectiveClaussRoot = new ArrayList<>();
        if (this.relationMap.containsKey("acl")) adjectiveClaussRoot.addAll(this.relationMap.get("acl"));
        return adjectiveClaussRoot;
    }

    private List<Rule> GenerateNominalModifierRules() {
        List<Rule> rules = new ArrayList<>();
        Word predicate = new Word("_property", false);
        List<Pair<Word, Word>> modifierPairs = this.GetNominalModifiers();

        for (Pair<Word, Word> modifier : modifierPairs) {
            List<Literal> bodyList = new ArrayList<>();
            Literal concept = new Literal(this);
            List<Word> numModifiers = modifier.getKey().GetNumericalModifiers();
            numModifiers.add(modifier.getKey());
            Word modifiedWord = CreateCompoundWord(numModifiers);
            Word preposition = modifier.getValue();
            if (this.id.length() > 0) {
                bodyList.add(new Literal(new Word(this.id, false)));
            }

            bodyList.add(concept);

            if (preposition == null) {
                preposition = new Word("null", false);
            }

            bodyList.add(new Literal(preposition));
            bodyList.add(new Literal(modifiedWord));

            Literal head = new Literal(predicate, bodyList);
            Rule rule = new Rule(head, null, false);
            rules.add(rule);
        }

        List<Word> modifiers = this.GetSpecialNominalModifiers("agent");
        for (Word modifier : modifiers) {
            Literal concept = new Literal(this);
            List<Literal> bodyList = new ArrayList<>();
            if (this.id.length() != 0) {
                bodyList.add(new Literal(new Word(this.id, false)));
            }
            bodyList.add(concept);
            bodyList.add(new Literal(new Word("_by", false)));
            bodyList.add(new Literal(modifier));
            Literal head = new Literal(predicate, bodyList);
            Rule rule = new Rule(head, null, false);
            rules.add(rule);
        }

        rules.addAll(GenerateSuchAsRules());
        return rules;
    }

    private List<Rule> GenerateSuchAsRules() {
        List<Rule> rules = new ArrayList<>();
        List<Word> modifiers = this.GetSpecialNominalModifiers("such_as");
        for (Word modifier : modifiers) {
            List<Word> adjectives = this.GetAdjectives();
            List<Word> modifierAdjectives = modifier.GetAdjectives();
            List<Literal> bodyList = new ArrayList<>();
            bodyList.add(new Literal(this));
            bodyList.add(new Literal(modifier));
            Literal head = new Literal(new Word("_is", false), bodyList);
            Rule rule = new Rule(head, null, false);
            rules.add(rule);

            adjectives.add(this);
            Word adjModifier = Word.CreateCompoundWord(adjectives);
            bodyList = new ArrayList<>();
            bodyList.add(new Literal(adjModifier));
            bodyList.add(new Literal(modifier));
            head = new Literal(new Word("_is", false), bodyList);
            rule = new Rule(head, null, false);
            rules.add(rule);

            modifierAdjectives.add(modifier);
            Word otherAdjModifier = Word.CreateCompoundWord(modifierAdjectives);
            bodyList = new ArrayList<>();
            bodyList.add(new Literal(this));
            bodyList.add(new Literal(otherAdjModifier));
            head = new Literal(new Word("_is", false), bodyList);
            rule = new Rule(head, null, false);
            rules.add(rule);

            bodyList = new ArrayList<>();
            bodyList.add(new Literal(adjModifier));
            bodyList.add(new Literal(otherAdjModifier));
            head = new Literal(new Word("_is", false), bodyList);
            rule = new Rule(head, null, false);
            rules.add(rule);

            if (WordNet.IsDictionaryWord(this)) {
                List<Literal> terms = new ArrayList<>();
                terms.add(new Literal(modifier));
                head = new Literal(this, terms);
                rule = new Rule(head, null, false);
                rules.add(rule);

                terms = new ArrayList<>();
                terms.add(new Literal(otherAdjModifier));
                head = new Literal(this, terms);
                rule = new Rule(head, null, false);
                rules.add(rule);
            }
        }

        return rules;
    }

    private List<Word> GetSpecialNominalModifiers(String specialSpecific) {
        List<Word> specialModifiers = new ArrayList<>();
        String relationLong = String.format("nmod:%s", specialSpecific);
        if (this.relationMap.containsKey(relationLong)) specialModifiers.addAll(this.relationMap.get(relationLong));
        return specialModifiers;
    }

    private List<Rule> GenerateAdjectiveRules() {
        List<Rule> rules = new ArrayList<>();
        if (!this.IsNoun()) return rules;

        Word predicate = new Word("_mod", false);
        List<Word> adjectives = this.GetAdjectives();
        adjectives.addAll(this.GetNumericalModifiers());
        for (Word adjective : adjectives) {
            List<Literal> bodyList = new ArrayList<>();
            Literal concept = new Literal(new Word(this.lemma, false));
            Literal adj = new Literal(new Word(adjective.word, false));
            bodyList.add(concept);
            bodyList.add(adj);

            Literal head = new Literal(predicate, bodyList);
            Rule rule = new Rule(head, null, false);
            rules.add(rule);
        }

        return rules;
    }

    private List<Word> GetAdjectives() {
        List<Word> adjectives = new ArrayList<>();
        if (this.relationMap.containsKey("amod")) adjectives.addAll(this.relationMap.get("amod"));
        return adjectives;
    }

    private List<Word> GetNumericalModifiers() {
        List<Word> numericalModifiers = new ArrayList<>();
        if (this.relationMap.containsKey("nummod")) numericalModifiers.addAll(this.relationMap.get("nummod"));
        return numericalModifiers;
    }

    private List<Pair<Word, Word>> GetNominalModifiers() {
        List<Pair<Word, Word>> nominalModifierPairs = new ArrayList<>();
        List<String> relations = new ArrayList<>(this.relationMap.keySet());
        relations.removeIf(x -> !x.startsWith("nmod"));

        for (String relation : relations) {
            String prepString = relation.contains(":") ? relation.split(":")[1] : "";
            if (ShouldExcludeNominalModifierPreps(prepString)) continue;
            Word preposition = null;
            if (!prepString.equals("")) preposition = new Word(prepString, false);
            List<Word> modifiers = this.relationMap.get(relation);
            for (Word modifier : modifiers) {
                nominalModifierPairs.add(new Pair<>(modifier, preposition));
            }
        }

        return nominalModifierPairs;
    }

    private boolean ShouldExcludeNominalModifierPreps(String prepString) {
        if (prepString.equals("poss")) return true;
        if (prepString.equals("agent")) return true;
        if (prepString.equals("such_as")) return true;
        return false;
    }

    private List<Rule> GenerateNERRules() {
        List<Rule> rules = new ArrayList<>();
        if (!this.IsNoun()) return rules;

        Word predicate = this.GenerateNERPredicate();
        if (predicate == null) return rules;

        List<Literal> bodyList = new ArrayList<>();
        Literal concept = new Literal(new Word(this.lemma, false));
        bodyList.add(concept);

        Literal head = new Literal(predicate, bodyList);
        Rule rule = new Rule(head, null, false);
        rules.add(rule);
        return rules;
    }

    private Word GenerateNERPredicate() {
        switch (this.NERTag) {
            case DATE:
                return new Word("time", false);
            case ORGANIZATION:
                return new Word("company", false);
        }

        return null;
    }

    public boolean IsNoun() {
        return this.POSTag.startsWith("NN");
    }

    private List<Rule> GenerateRulesForVerb() {
        List<Rule> rules = new ArrayList<>();
        Word eventWord = new Word("event", false);
        List<Word> subjects = this.GetSubjects();
        List<Word> modifiers = this.GetModifiers();

        if (this.getLemma().equals("call") && this.hasAuxillaryVerbBe()) {
            rules.addAll(GenerateIsRule(subjects, modifiers));
        } else {
            for (Word subject : subjects) {
                for (Word modifier : modifiers) {
                    List<Literal> bodyList = new ArrayList<>();
                    bodyList.add(new Literal(new Word(this.id, true)));
                    bodyList.add(new Literal(new Word(this.lemma, false)));
                    bodyList.add(new Literal(new Word(subject.lemma, false)));
                    bodyList.add(new Literal(new Word(modifier.lemma, false)));

                    Literal head = new Literal(eventWord, bodyList);
                    rules.add(new Rule(head, null, false));
                    rules.addAll(GenerateAppositionalEventRules(this, subject, modifier));
                }
            }

            if (subjects.size() == 0) {
                for (Word modifier : modifiers) {
                    List<Literal> bodyList = new ArrayList<>();
                    bodyList.add(new Literal(new Word(String.valueOf(this.id), false)));
                    bodyList.add(new Literal(new Word(this.lemma, false)));
                    bodyList.add(new Literal(new Word("null", false)));
                    bodyList.add(new Literal(new Word(modifier.lemma, false)));

                    Literal head = new Literal(eventWord, bodyList);
                    rules.add(new Rule(head, null, false));
                    rules.addAll(GenerateAppositionalEventRules(this, null, modifier));
                }
            }

            if (modifiers.size() == 0) {
                for (Word subject : subjects) {
                    List<Literal> bodyList = new ArrayList<>();
                    bodyList.add(new Literal(new Word(String.valueOf(this.id), false)));
                    bodyList.add(new Literal(new Word(this.lemma, false)));
                    bodyList.add(new Literal(new Word(subject.lemma, false)));
                    bodyList.add(new Literal(new Word("null", false)));

                    Literal head = new Literal(eventWord, bodyList);
                    rules.add(new Rule(head, null, false));
                    rules.addAll(GenerateAppositionalEventRules(this, subject, null));
                }
            }

            if (subjects.size() == 0 && modifiers.size() == 0) {
                List<Literal> bodyList = new ArrayList<>();
                bodyList.add(new Literal(new Word(String.valueOf(this.id), false)));
                bodyList.add(new Literal(new Word(this.lemma, false)));
                bodyList.add(new Literal(new Word("null", false)));
                bodyList.add(new Literal(new Word("null", false)));

                Literal head = new Literal(eventWord, bodyList);
                rules.add(new Rule(head, null, false));
            }

            rules.addAll(GenerateClausalComplementRules());
            rules.addAll(GenerateClausalRules());
            rules.addAll(GenerateAdverbRules());
            rules.addAll(GenerateNominalModifierRules());
            rules.addAll(GenerateConjunctionRules());
        }

        return rules;
    }

    private Rule GenerateAgentQuery(QuestionInformation questionInformation) {
        List<Rule> rules = new ArrayList<>();
        Word eventWord = new Word("event", false);
        String subjectFormat = questionInformation.answerType == AnswerType.SUBJECT ? "X%s" : "S%s";
        String objectFormat = questionInformation.answerType == AnswerType.OBJECT ? "X%s" : "O%s";

        List<Word> subjects = this.GetSubjects();
        List<Word> modifiers = this.GetModifiers();
        if (subjects.size() == 0 && modifiers.size() == 0) return null;

        List<Literal> terms = new ArrayList<>();
        terms.add(new Literal(new Word(String.format("E%s", this.id), true)));
        terms.add(new Literal(new Word(this.getLemma(), false)));
        terms.add(new Literal(new Word("_", false)));
        terms.add(new Literal(new Word(String.format(objectFormat, this.id), true)));
        Literal queryEvent = new Literal(eventWord, terms);
        Rule rule = new Rule(queryEvent, null, true);
        rules.add(rule);

        Word similarWord = new Word("_similar", false);
        for (Word subject : subjects) {
            if (questionInformation.answerType == AnswerType.SUBJECT && questionInformation.answerKind == subject)
                continue;
            terms = new ArrayList<>();
            terms.add(new Literal(subject));
            terms.add(new Literal(new Word(String.format(subjectFormat, this.id), true)));
            Literal similarQuery = new Literal(similarWord, terms);
            rule = new Rule(similarQuery, null, true);
            rules.add(rule);
        }

        for (Word modifier : modifiers) {
            if (questionInformation.answerType == AnswerType.OBJECT && questionInformation.answerKind == modifier)
                continue;
            terms = new ArrayList<>();
            terms.add(new Literal(modifier));
            terms.add(new Literal(new Word(String.format(objectFormat, this.id), true)));
            Literal similarQuery = new Literal(similarWord, terms);
            rule = new Rule(similarQuery, null, true);
            rules.add(rule);
        }

        Word propertyWord = new Word("_property", false);
        terms = new ArrayList<>();
        terms.add(new Literal(new Word(String.format("E%s", this.id), true)));
        terms.add(new Literal(new Word(this.getLemma(), false)));
        terms.add(new Literal(new Word("_by", false)));
        terms.add(new Literal(new Word(String.format(subjectFormat, this.id), true)));
        Literal agentQuery = new Literal(propertyWord, terms);
        rule = new Rule(agentQuery, null, true);
        rules.add(rule);

        if (rules.size() == 0) return null;
        rule = Rule.AggregateAllRules(rules);
        return rule;
    }

    private Rule GenerateNormalQuery(QuestionInformation information) {
        List<Rule> rules = new ArrayList<>();
        Word eventWord = new Word("event", false);
        String subjectFormat = information.answerType == AnswerType.SUBJECT ? "X%s" : "S%s";
        String objectFormat = information.answerType == AnswerType.OBJECT ? "X%s" : "O%s";
        List<Word> subjects = this.GetSubjects();
        List<Word> modifiers = this.GetModifiers();

        if (subjects.size() == 0 && modifiers.size() == 0) return null;

        List<Literal> terms = new ArrayList<>();
        terms.add(new Literal(new Word(String.format("E%s", this.id), true)));
        terms.add(new Literal(new Word(this.getLemma(), false)));
        terms.add(new Literal(new Word(String.format(subjectFormat, this.id), true)));
        terms.add(new Literal(new Word(String.format("O%s", this.id), true)));
        Literal queryEvent = new Literal(eventWord, terms);
        Rule rule = new Rule(queryEvent, null, true);
        rules.add(rule);

        Word similarWord = new Word("_similar", false);
        for (Word subject : subjects) {
            if (information.answerType == AnswerType.SUBJECT && information.answerKind == subject) continue;
            terms = new ArrayList<>();
            terms.add(new Literal(subject));
            terms.add(new Literal(new Word(String.format(subjectFormat, this.id), true)));
            Literal similarQuery = new Literal(similarWord, terms);
            rule = new Rule(similarQuery, null, true);
            rules.add(rule);
        }

        for (Word modifier : modifiers) {
            if (information.answerType == AnswerType.OBJECT && information.answerKind == modifier) continue;
            terms = new ArrayList<>();
            terms.add(new Literal(modifier));
            terms.add(new Literal(new Word(String.format(objectFormat, this.id), true)));
            Literal similarQuery = new Literal(similarWord, terms);
            rule = new Rule(similarQuery, null, true);
            rules.add(rule);
        }

        if (rules.size() == 0) return null;
        rule = Rule.AggregateAllRules(rules);
        return rule;
    }

    private static List<Rule> GenerateAppositionalEventRules(Word actionWord, Word subject, Word object) {
        List<Rule> rules = new ArrayList<>();
        List<Word> subjectApposList = subject != null ? subject.GetAppositionalModifiers() : new ArrayList<>();
        List<Word> objectApposList = object != null ? object.GetAppositionalModifiers() : new ArrayList<>();
        Word eventWord = new Word("event", false);
        for (Word subjectAppos : subjectApposList) {
            for (Word objectAppos : objectApposList) {
                List<Literal> bodyList = new ArrayList<>();
                bodyList.add(new Literal(new Word(actionWord.id, false)));
                bodyList.add(new Literal(new Word(actionWord.lemma, false)));
                bodyList.add(new Literal(subjectAppos));
                bodyList.add(new Literal(objectAppos));
                Literal head = new Literal(eventWord, bodyList);
                Rule rule = new Rule(head, null, false);
                rules.add(rule);
            }
        }

        if (subjectApposList.size() != 0 && objectApposList.size() != 0) return rules;

        for (Word subjectAppos : subjectApposList) {
            List<Literal> bodyList = new ArrayList<>();
            bodyList.add(new Literal(new Word(actionWord.id, false)));
            bodyList.add(new Literal(new Word(actionWord.lemma, false)));
            bodyList.add(new Literal(subjectAppos));
            bodyList.add(new Literal(new Word("null", false)));
            Literal head = new Literal(eventWord, bodyList);
            Rule rule = new Rule(head, null, false);
            rules.add(rule);
        }

        if (subjectApposList.size() != 0) return rules;

        for (Word objectAppos : objectApposList) {
            List<Literal> bodyList = new ArrayList<>();
            bodyList.add(new Literal(new Word(actionWord.id, false)));
            bodyList.add(new Literal(new Word(actionWord.lemma, false)));
            bodyList.add(new Literal(new Word("null", false)));
            bodyList.add(new Literal(objectAppos));
            Literal head = new Literal(eventWord, bodyList);
            Rule rule = new Rule(head, null, false);
            rules.add(rule);
        }

        return rules;
    }

    private List<Rule> GenerateConjunctionRules() {
        List<Rule> rules = new ArrayList<>();
        List<Word> conjunctions = this.GetConjunctionRelations();
        Word relationWord = new Word("_relation", false);
        for (Word conjunction : conjunctions) {
            List<Literal> bodyList = new ArrayList<>();
            bodyList.add(new Literal(new Word(String.valueOf(this.id), false)));
            if (conjunction.IsVerb()) {
                bodyList.add(new Literal(new Word(String.valueOf(conjunction.id), false)));
            } else if (conjunction.IsNoun() || conjunction.IsAdjective()) {
                bodyList.add(new Literal(conjunction));
            } else continue;

            bodyList.add(new Literal(new Word("_conj", false)));
            Literal head = new Literal(relationWord, bodyList);
            rules.add(new Rule(head, null, false));
        }
        return rules;
    }

    private List<Word> GetConjunctionRelations() {
        List<Word> conjunctions = new ArrayList<>();
        if (this.relationMap.containsKey("conj")) conjunctions.addAll(this.relationMap.get("conj"));
        return conjunctions;
    }

    private List<Rule> GenerateClausalRules() {
        List<Rule> rules = new ArrayList<>();
        List<Word> clauses = this.GetAdverbClause();
        Word relationWord = new Word("_relation", false);
        for (Word clause : clauses) {
            if (!clause.IsVerb()) continue;
            List<Literal> bodyList = new ArrayList<>();
            bodyList.add(new Literal(new Word(String.valueOf(this.id), false)));
            bodyList.add(new Literal(new Word(String.valueOf(clause.id), false)));
            bodyList.add(new Literal(new Word("_clause", false)));

            Literal head = new Literal(relationWord, bodyList);
            rules.add(new Rule(head, null, false));
        }

        return rules;
    }

    private List<Word> GetAdverbClause() {
        List<Word> clauses = new ArrayList<>();
        if (this.relationMap.containsKey("advcl")) clauses.addAll(this.relationMap.get("advcl"));
        return clauses;
    }

    private List<Rule> GenerateClausalComplementRules() {
        List<Rule> rules = new ArrayList<>();
        List<Word> clausalComplements = this.GetClausalComplements();
        Word relationWord = new Word("_relation", false);
        for (Word clause : clausalComplements) {
            if (!clause.IsVerb()) continue;
            List<Literal> bodyList = new ArrayList<>();
            bodyList.add(new Literal(new Word(String.valueOf(this.id), false)));
            bodyList.add(new Literal(new Word(String.valueOf(clause.id), false)));
            bodyList.add(new Literal(new Word("_clcomplement", false)));

            Literal head = new Literal(relationWord, bodyList);
            rules.add(new Rule(head, null, false));
        }

        return rules;
    }

    private List<Rule> GenerateAdverbRules() {
        List<Rule> rules = new ArrayList<>();
        List<Word> adverbs = GetAdverbs();
        for (Word adverb : adverbs) {
            Word predicate = new Word("_mod", false);
            List<Literal> terms = new ArrayList<>();
            terms.add(new Literal(this));
            terms.add(new Literal(adverb));
            Literal head = new Literal(predicate, terms);
            Rule rule = new Rule(head, null, false);
            rules.add(rule);
        }

        return rules;
    }

    private List<Word> GetAdverbs() {
        List<Word> adverbs = new ArrayList<>();
        if (this.relationMap.containsKey("advmod")) adverbs.addAll(this.relationMap.get("advmod"));
        return adverbs;
    }

    private List<Rule> GenerateIsRule(List<Word> subjects, List<Word> modifiers) {
        List<Rule> rules = new ArrayList<>();
        Word isWord = new Word("_is", false);
        for (Word subject : subjects) {
            for (Word modifier : modifiers) {
                List<Literal> bodyList = new ArrayList<>();
                boolean isVariable = false;
                bodyList.add(new Literal(new Word(subject.lemma, isVariable)));
                isVariable = false;
                bodyList.add(new Literal(new Word(modifier.lemma, isVariable)));
                Literal head = new Literal(isWord, bodyList);
                rules.add(new Rule(head, null, false));
            }
        }
        return rules;
    }

    private List<Word> GetClausalComplements() {
        List<Word> clauses = new ArrayList<>();
        if (this.relationMap.containsKey("xcomp")) clauses.addAll(this.relationMap.get("xcomp"));
        if (this.relationMap.containsKey("ccomp")) clauses.addAll(this.relationMap.get("ccomp"));
        return clauses;
    }

    private List<Word> GetModifiers() {
        List<Word> modifiers = new ArrayList<>();
        modifiers.addAll(GetPassiveSubjects());
        modifiers.addAll(GetDirectObjects());

        modifiers = FilterCardinalNumbers(modifiers);
        return modifiers;
    }

    private List<Word> GetPassiveSubjects() {
        List<Word> passiveSubjects = new ArrayList<>();
        if (this.relationMap.containsKey("nsubjpass")) passiveSubjects.addAll(this.relationMap.get("nsubjpass"));
        return passiveSubjects;
    }

    private List<Word> FilterCardinalNumbers(List<Word> modifiers) {
        List<Word> filtered = new ArrayList<>();
        for (Word modifier : modifiers) {
            if (modifier.getPOSTag().equals("CD")) continue;
            filtered.add(modifier);
        }
        return filtered;
    }

    private List<Word> GetDirectObjects() {
        List<Word> directObjects = new ArrayList<>();
        if (this.relationMap.containsKey("dobj")) {
            directObjects.addAll(this.relationMap.get("dobj"));
            List<Word> supplimentaryDirectObjects = new ArrayList<>();
            List<Word> adjectiveDirectObjects = GetSupplimentaryFromAdjectives(directObjects);
            directObjects.addAll(adjectiveDirectObjects);
            for (Word directObject : directObjects) {
                List<Pair<Word, Word>> nmods = directObject.GetNominalModifiers();
                if (nmods.size() == 0) continue;
                for (Pair<Word, Word> nmod : nmods) {
                    Word preposition = nmod.getValue();
                    if (preposition == null) continue;
                    List<Word> wordCollection = new ArrayList<>();
                    wordCollection.add(directObject);
                    wordCollection.add(preposition);
                    wordCollection.add(nmod.getKey());
                    Word compound = CreateCompoundWord(wordCollection);
                    supplimentaryDirectObjects.add(compound);
                }
            }

            directObjects.addAll(supplimentaryDirectObjects);
        }

        return directObjects;
    }

    private List<Word> GetSubjects() {
        List<Word> subjects = new ArrayList<>();
        if (this.relationMap.containsKey("nsubj")) subjects.addAll(this.relationMap.get("nsubj"));
        if (this.relationMap.containsKey("nsubj:xsubj")) subjects.addAll(this.relationMap.get("nsubj:xsubj"));
        subjects.addAll(GetIndirectObjects());

        List<Word> adjectiveSubjects = GetSupplimentaryFromAdjectives(subjects);
        subjects.addAll(adjectiveSubjects);
        return subjects;
    }

    private List<Word> GetIndirectObjects() {
        List<Word> indirectObjects = new ArrayList<>();
        if (this.relationMap.containsKey("iobj")) indirectObjects.addAll(this.relationMap.get("iobj"));
        return indirectObjects;
    }

    private List<Word> GetSupplimentaryFromAdjectives(List<Word> subjects) {
        List<Word> compounds = new ArrayList<>();
        for (Word subject : subjects) {
            List<Word> adjectives = subject.GetAdjectives();
            if (adjectives.size() == 0) continue;
            List<Word> wordCollection = new ArrayList<>();
            wordCollection.addAll(adjectives);
            wordCollection.add(subject);
            Word compound = CreateCompoundWord(wordCollection);
            compounds.add(compound);
        }

        return compounds;
    }

    public boolean IsVerb() {
        return this.POSTag.startsWith("VB");
    }

    public boolean hasAuxillaryVerbBe() {
        if (this.relationMap.containsKey("auxpass")) {
            List<Word> auxWords = this.relationMap.get("auxpass");
            for (Word aux : auxWords) {
                if (aux.getLemma().equals("be")) {
                    return true;
                }
            }
        }
        return false;
    }

    public List<Rule> GenerateCopulaRules() {
        if (!this.IsNoun() && !this.IsAdjective()) return new ArrayList<>();
        List<Rule> rules = new ArrayList<>();
        Word bePredicate = new Word("_is", false);

        Word copula = GetToBeCopula();
        if (copula == null) return new ArrayList<>();
        List<Word> subjects = this.GetSubjects();
        List<Word> adjectives = this.GetAdjectives();
        for (Word subject : subjects) {
            List<Literal> terms = new ArrayList<>();
            terms.add(new Literal(subject));
            terms.add(new Literal(this));
            Literal head = new Literal(bePredicate, terms);
            Rule rule = new Rule(head, null, false);
            rules.add(rule);

            if (WordNet.IsDictionaryWord(this)) {
                terms = new ArrayList<>();
                terms.add(new Literal(subject));
                head = new Literal(this, terms);
                rule = new Rule(head, null, false);
                rules.add(rule);
            }

            if (adjectives.size() != 0) {
                terms = new ArrayList<>();
                terms.add(new Literal(subject));
                List<Word> wordCollection = new ArrayList<>();
                wordCollection.addAll(adjectives);
                wordCollection.add(this);
                Word compundWord = CreateCompoundWord(wordCollection);
                terms.add(new Literal(compundWord));
                head = new Literal(bePredicate, terms);
                rule = new Rule(head, null, false);
                rules.add(rule);
            }
        }

        for (Word subject : subjects) {
            List<Word> conjunctions = GetConjunctionRelations();
            for (Word conjunction : conjunctions) {
                if (!conjunction.IsNoun() && !conjunction.IsAdjective()) continue;
                adjectives = conjunction.GetAdjectives();
                List<Literal> terms = new ArrayList<>();
                terms.add(new Literal(subject));
                terms.add(new Literal(conjunction));
                Literal head = new Literal(bePredicate, terms);
                Rule rule = new Rule(head, null, false);
                rules.add(rule);

                if (WordNet.IsDictionaryWord(conjunction)) {
                    terms = new ArrayList<>();
                    terms.add(new Literal(subject));
                    head = new Literal(conjunction, terms);
                    rule = new Rule(head, null, false);
                    rules.add(rule);
                }

                if (adjectives.size() != 0) {
                    terms = new ArrayList<>();
                    terms.add(new Literal(subject));
                    List<Word> wordCollection = new ArrayList<>();
                    wordCollection.addAll(adjectives);
                    wordCollection.add(conjunction);
                    Word compoundWord = CreateCompoundWord(wordCollection);
                    terms.add(new Literal(compoundWord));
                    head = new Literal(bePredicate, terms);
                    rule = new Rule(head, null, false);
                    rules.add(rule);
                }
            }
        }

        return rules;
    }

    private Word GetToBeCopula() {
        if (this.relationMap.containsKey("cop")) {
            List<Word> copulas = new ArrayList<>();
            copulas.addAll(this.relationMap.get("cop"));
            for (Word copula : copulas) {
                if (copula.lemma.equals("be")) return copula;
            }
        }
        ;

        return null;
    }

    public static List<Word> ExtractVerbs(List<Word> wordList) {
        List<Word> verbs = new ArrayList<>();
        for (Word word : wordList) {
            if (word.relationMap.size() == 0) continue;
            if (word.IsVerb()) {
                verbs.add(word);
            }
        }

        return verbs;
    }

    public static Word CreateCompoundWord(List<Word> wordCollection) {
        StringBuilder builder = new StringBuilder();
        for (Word word : wordCollection) {
            String wordString = word.IsAdjective() ? word.getWord() : word.getLemma();
            builder.append(wordString);
            builder.append(" ");
        }

        String compoundWord = builder.toString().trim();
        compoundWord = compoundWord.replaceAll(" ", "_");
        return new Word(compoundWord, false);
    }

    public void SetNERTag(NamedEntityTagger.NamedEntityTags tag) {
        this.NERTag = tag;
    }

    public List<Rule> GenerateAlternateCopulaRules(List<Word> wordList) {
        List<Rule> rules = new ArrayList<>();
        Word copula = GetToBeCopula();
        if (copula == null) return rules;
        List<Word> subjects = this.GetSubjects();
        if (subjects.size() != 0) return rules;
        Word bePredicate = new Word("_is", false);
        for (Word word : wordList) {
            List<Word> conjunctions = word.GetConjunctionRelations();
            for (Word conjunction : conjunctions) {
                if (conjunction != this) continue;
                subjects = word.GetSubjects();

                List<Word> adjectives = this.GetAdjectives();
                for (Word subject : subjects) {
                    List<Literal> terms = new ArrayList<>();
                    terms.add(new Literal(subject));
                    terms.add(new Literal(this));
                    Literal head = new Literal(bePredicate, terms);
                    Rule rule = new Rule(head, null, false);
                    rules.add(rule);

                    if (adjectives.size() != 0) {
                        terms = new ArrayList<>();
                        terms.add(new Literal(subject));
                        List<Word> wordCollection = new ArrayList<>();
                        wordCollection.addAll(adjectives);
                        wordCollection.add(this);
                        Word compundWord = CreateCompoundWord(wordCollection);
                        terms.add(new Literal(compundWord));
                        head = new Literal(bePredicate, terms);
                        rule = new Rule(head, null, false);
                        rules.add(rule);
                    }
                }
            }
        }
        return rules;
    }

    public boolean IsNumber() {
        if (this.getPOSTag().equals("CD")) return true;
        return false;
    }

    public boolean IsDay() {
        if (this.IsNumber() && this.getNERTag() == NamedEntityTagger.NamedEntityTags.DATE) {
            try {
                int day = Integer.parseInt(this.getWord());
                if (day > 0 && day < 32) return true;
            } catch (Exception ex) {
                return false;
            }
        }

        return false;
    }

    public boolean IsYear() {
        if (this.IsNumber() && this.getNERTag() == NamedEntityTagger.NamedEntityTags.DATE) {
            if (this.getWord().length() == 4)
                return true;
        }

        return false;
    }

    public boolean IsMonth() {
        String month = this.getWord().toLowerCase();
        switch (month) {
            case "jan":
            case "january":
            case "feb":
            case "february":
            case "mar":
            case "march":
            case "apr":
            case "april":
            case "may":
            case "jun":
            case "june":
            case "jul":
            case "july":
            case "aug":
            case "august":
            case "sep":
            case "sept":
            case "september":
            case "oct":
            case "october":
            case "nov":
            case "november":
            case "dec":
            case "december":
                return true;
        }

        return false;
    }

    public static void SetWordIds(List<Word> wordsList) {
        List<Word> verbs = Word.ExtractVerbs(wordsList);
        for (Word word : wordsList) {
            if (verbs.contains(word)) {
                SetWordIds(word, word.id);
            } else {
                Word toBeCopula = word.GetToBeCopula();
                if (toBeCopula == null) continue;
                if (word.id.length() == 0) word.id = toBeCopula.id;
                SetWordIds(word, toBeCopula.id);
            }
        }
    }

    private static void SetWordIds(Word word, String eventId) {
        HashMap<String, List<Word>> relations = word.relationMap;
        for (String relation : relations.keySet()) {
            List<Word> dependantWords = relations.get(relation);
            for (Word dependantWord : dependantWords) {
                if (dependantWord.id.length() > 0) continue;
                dependantWord.id = eventId;
                SetWordIds(dependantWord, eventId);
            }
        }
    }

    public List<Word> GetDeterminers() {
        List<Word> determiners = new ArrayList<>();
        if (this.relationMap.containsKey("det")) determiners.addAll(this.relationMap.get("det"));
        return determiners;
    }

    private Word GetPreposition() {
        Word preposition = null;
        if (this.relationMap.containsKey("case")) preposition = this.relationMap.get("case").get(0);
        return preposition;
    }

    public boolean IsQuestionWord() {
        if (this.getPOSTag().startsWith("W")) {
            return true;
        }

        return false;
    }

    public AnswerType GetAnswerType(Word answerKind) {
        List<Word> subjects = this.GetSubjects();
        if (subjects.contains(answerKind)) return AnswerType.SUBJECT;
        List<Word> modifiers = this.GetModifiers();
        if (modifiers.contains(answerKind)) return AnswerType.OBJECT;
        return AnswerType.UNKNOWN;
    }

    public static List<Rule> GenerateQuestionConstraintRules(QuestionInformation information, List<TypedDependency> dependencies, List<Word> wordList) {
        List<Rule> rules = new ArrayList<>();
        if (information.questionType == QuestionType.WHAT && information.answerKind == null) return rules;

        Word yearPredicate = new Word("year", false);
        Word timePredicate = new Word("time", false);
        Word quantityPredicate = new Word("total_element", false);
        switch (information.answerType) {
            case TIME:
                Literal timeVariable = new Literal(new Word(String.format("X%s", information.questionWord.id), true),
                        LiteralType.BASE_CONSTRAINT);
                List<Literal> terms = new ArrayList<>();
                terms.add(timeVariable);
                Literal timeLiteral = new Literal(timePredicate, terms);
                Rule rule = new Rule(timeLiteral, null, true);
                rules.add(rule);
                return rules;

            case YEAR:
                timeVariable = new Literal(new Word(String.format("T%s", information.answerKind.id), true),
                        LiteralType.BASE_CONSTRAINT);
                terms = new ArrayList<>();
                terms.add(timeVariable);
                terms.add(new Literal(new Word(String.format("X%s", information.answerKind.id), true),
                        LiteralType.BASE_CONSTRAINT));
                Literal yearLiteral = new Literal(yearPredicate, terms);
                rule = new Rule(yearLiteral, null, true);
                rules.add(rule);

                terms = new ArrayList<>();
                terms.add(timeVariable);
                timeLiteral = new Literal(timePredicate, terms);
                rule = new Rule(timeLiteral, null, true);
                rules.add(rule);

                rule = Rule.AggregateAllRules(rules);
                rules = new ArrayList<>();
                rules.add(rule);
                return rules;

            case QUANTITY:


                TypedDependency dependency = dependencies.stream().filter(p -> p.reln().getShortName().equalsIgnoreCase("amod")
                        && p.dep().value().equalsIgnoreCase("many"))
                        .findFirst()
                        .orElse(null);
                String keyWord = wordList.stream().filter(p -> p.getWordIndex() == dependency.gov().index()).findFirst().get().getLemma();


                Literal quantityVariable = new Literal(new Word(String.format("X%s", information.questionWord.id), true),
                        LiteralType.BASE_CONSTRAINT);
                terms = new ArrayList<>();

                terms.add(new Literal(new Word(keyWord, false)));
                terms.add(quantityVariable);
                Literal quantityLiteral = new Literal(quantityPredicate, terms);
                rule = new Rule(quantityLiteral, null, true);
                rules.add(rule);
                return rules;

            default:
                Word basePredicate = information.answerKind;
                terms = new ArrayList<>();
                terms.add(new Literal(new Word(String.format("X%s", information.questionWord.id), true), LiteralType.BASE_CONSTRAINT));
                terms.add(new Literal(new Word("_", false)));
                Literal baseLiteral = new Literal(basePredicate, terms);
                rule = new Rule(baseLiteral, null, true);
                rules.add(rule);

                Word modPredicate = new Word("_mod", false);
                terms = new ArrayList<>();
                terms.add(new Literal(information.answerKind));
                terms.add(new Literal(new Word(String.format("X%s", information.questionWord.id), true), LiteralType.BASE_CONSTRAINT));
                Literal modLiteral = new Literal(modPredicate, terms);
                rule = new Rule(modLiteral, null, true);
                rules.add(rule);
        }

        return rules;
    }

    public List<Rule> GenerateNounConstraintRules(QuestionInformation information) {
        List<Rule> constraints = new ArrayList<>();
        List<Rule> propertyConstraints = GeneratePropertyConstraints(information);
        List<Rule> copulaConstraints = GenerateCopulaConstraints();
        List<Rule> possessiveConstraints = GeneratePossessiveConstraints();
        List<Rule> adjectiveConstraints = GenerateAdjectiveConstraints();
        List<Rule> adjClauseConstraints = GenerateAdjClauseConstraints(information);

        constraints.addAll(propertyConstraints);
        constraints.addAll(copulaConstraints);
        constraints.addAll(possessiveConstraints);
        constraints.addAll(adjectiveConstraints);
        constraints.addAll(adjClauseConstraints);
        return constraints;
    }

    private List<Rule> GenerateAdjClauseConstraints(QuestionInformation information) {
        List<Rule> rules = new ArrayList<>();
        Word propertyPredicate = new Word("_property", false);

        List<Word> modifiers = GetAdjectiveClause();
        for (Word modifier : modifiers) {
            Word preposition = modifier.GetPreposition();
            if (preposition == null) return rules;
            List<Literal> terms = new ArrayList<>();
            terms.add(new Literal(new Word(String.format("E%s", this.id), true)));
            terms.add(new Literal(this));
            terms.add(new Literal(preposition));
            if (modifier == information.questionWord)
                terms.add(new Literal(new Word(String.format("X%s", modifier.id), true)));
            else terms.add(new Literal(modifier));
            Literal queryEvent = new Literal(propertyPredicate, terms);
            Rule rule = new Rule(queryEvent, null, true);
            rules.add(rule);
        }

        return rules;
    }

    private List<Rule> GenerateAdjectiveConstraints() {
        List<Rule> rules = new ArrayList<>();
        Word adjPredicate = new Word("_mod", false);

        List<Word> modifiers = GetAdjectives();
        for (Word modifier : modifiers) {
            List<Literal> terms = new ArrayList<>();
            terms.add(new Literal(this));
            terms.add(new Literal(modifier));
            Literal head = new Literal(adjPredicate, terms);
            Rule rule = new Rule(head, null, false);
            rules.add(rule);
        }

        return rules;
    }

    private List<Rule> GeneratePossessiveConstraints() {
        List<Rule> rules = new ArrayList<>();
        Word bePredicate = new Word("_possess", false);

        List<Word> modifiers = GetPossessiveModifiers();
        for (Word modifier : modifiers) {
            List<Literal> terms = new ArrayList<>();
            terms.add(new Literal(modifier));
            terms.add(new Literal(this));
            Literal head = new Literal(bePredicate, terms);
            Rule rule = new Rule(head, null, false);
            rules.add(rule);
        }

        return rules;
    }

    private List<Rule> GenerateCopulaConstraints() {
        List<Rule> rules = new ArrayList<>();
        Word bePredicate = new Word("_is", false);
        Word copula = this.GetToBeCopula();
        if (copula == null) return new ArrayList<>();

        List<Word> subjects = this.GetSubjects();
        for (Word subject : subjects) {
            List<Literal> terms = new ArrayList<>();
            terms.add(new Literal(subject));
            terms.add(new Literal(this));
            Literal head = new Literal(bePredicate, terms);
            Rule rule = new Rule(head, null, false);
            rules.add(rule);
        }

        return rules;
    }

    public String GetAnswerID() {
        return String.format("X%s", this.id);
    }

    public static boolean IsVerbAndQuestionWordConnected(Word verb, Word questionWord) {
        if (verb.relationMap != null && verb.relationMap.containsKey("advmod")) {
            List<Word> modifiers = verb.relationMap.get("advmod");
            if (modifiers.contains(questionWord)) return true;
        }

        return false;
    }

    public static List<Rule> GenerateBirthRule(Word verb, QuestionInformation information) {
        String subjectFormat = "S%s";
        String answerFormat = "X%s";
        Word predicateWord = new Word("_start_date", false);
        List<Rule> rules = new ArrayList<>();
        List<Word> subjects = verb.GetSubjects();
        if (subjects.size() == 0) return rules;
        Word similarPredicate = new Word("_similar", false);

        for (Word subject : subjects) {
            List<Literal> terms = new ArrayList<>();
            terms.add(new Literal(subject));
            terms.add(new Literal(new Word(String.format(subjectFormat, subject.id), true)));
            Literal similarLiteral = new Literal(similarPredicate, terms);

            terms = new ArrayList<>();
            terms.add(new Literal(new Word(String.format(subjectFormat, subject.id), true)));
            terms.add(new Literal(new Word(String.format(answerFormat, information.questionWord.id), true)));
            Literal startDateLiteral = new Literal(predicateWord, terms);

            terms = new ArrayList<>();
            terms.add(startDateLiteral);
            terms.add(similarLiteral);
            Rule rule = new Rule(null, terms, false);
            rules.add(rule);
        }

        return rules;
    }

    public static List<Rule> GenerateDeathRule(Word verb, QuestionInformation information) {
        String subjectFormat = "S%s";
        String answerFormat = "X%s";
        Word predicateWord = new Word("_end_date", false);
        List<Rule> rules = new ArrayList<>();
        List<Word> subjects = verb.GetSubjects();
        if (subjects.size() == 0) return rules;
        Word similarPredicate = new Word("_similar", false);

        for (Word subject : subjects) {
            List<Literal> terms = new ArrayList<>();
            terms.add(new Literal(subject));
            terms.add(new Literal(new Word(String.format(subjectFormat, subject.id), true)));
            Literal similarLiteral = new Literal(similarPredicate, terms);

            terms = new ArrayList<>();
            terms.add(new Literal(new Word(String.format(subjectFormat, subject.id), true)));
            terms.add(new Literal(new Word(String.format(answerFormat, information.questionWord.id), true)));
            Literal startDateLiteral = new Literal(predicateWord, terms);

            terms = new ArrayList<>();
            terms.add(startDateLiteral);
            terms.add(similarLiteral);
            Rule rule = new Rule(null, terms, false);
            rules.add(rule);
        }

        return rules;
    }

    public Word HasClausalComplement(List<Word> words, Word dependentWord) {
        for (Word word : words) {
            if (word.relationMap.containsKey("ccomp")) {
                List<Word> clausalComplements = word.relationMap.get("ccomp");
                for (Word complement : clausalComplements) {
                    if (complement == dependentWord) return word;
                }
            }
        }

        return null;
    }
}
