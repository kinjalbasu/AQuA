import edu.stanford.nlp.trees.TypedDependency;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class ClevrQueryGeneration {


    public static List<Rule> getClevrQuery(Question question) {
        List<Rule> rules = new ArrayList<>();
        if (question.information.answerType == AnswerType.BOOLEAN || question.information.questionType == QuestionType.TRUE_FALSE) {
            rules = ClevrQuestionBoolean.getBooleanRules(question);
        }
        else if (question.information.answerType == AnswerType.QUANTITY){
            rules = ClevrQuestionQuantity.getQuantityRules(question);
        }
        return rules;
    }



/*

    private static Map<Word, List<Word>> getProperties(Question question, List<Word> nouns) {
        Map<Word, List<Word>> properties = new HashMap<>();

        for (Word noun : nouns) {
            List<Word> mods = getModifier(noun);
            properties.put(noun, mods);
        }

        return properties;
    }
*/

/*
    private static List<Word> getModifier(Word noun) {
        List<Word> modifiers = new ArrayList<>();
        if (noun.getRelationMap().containsKey("amod")) {
            modifiers.addAll(noun.getRelationMap().get("amod"));
        }

        return modifiers;
    }
*/


}
