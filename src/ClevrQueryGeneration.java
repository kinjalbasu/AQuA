import org.json.JSONException;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ClevrQueryGeneration {


    public static List<Rule> getClevrQuery(Question question) {
        List<Rule> rules = new ArrayList<>();
        if(question.information.answerType == AnswerType.BOOLEAN || question.information.questionType == QuestionType.TRUE_FALSE){
            rules = getBooleanRules(question);
        }
        return rules;

    }

    private static List<Rule> getBooleanRules(Question question) {
        List<Rule> rules = new ArrayList<>();
        List<Word> nouns = question.wordList.stream().filter(w -> w.getPOSTag().toLowerCase().matches("nn|nnp|nns|nnps")).collect(Collectors.toList());
        Map<String,List<String>> commonSenseConceptsMap = nouns.stream().collect(Collectors.toMap(n -> n.getWord(), n-> SemanticRelationsGeneration.getConcept(n.getWord())));
        return rules;
    }
}
