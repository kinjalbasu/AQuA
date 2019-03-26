import edu.stanford.nlp.trees.TypedDependency;
import javafx.util.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SemanticRelationsGeneration {

    public static Pair<List<String>,List<String>> generateSemanticRelations(Sentence sentence){
        List<String> posFacts = new ArrayList<>();
        List<String> dependenciesFacts = new ArrayList<>();
        String regexPattern = "^[a-zA-Z0-9-]*$";
        for (Word word: sentence.wordList) {
            //String w = word.getWord();
            String w = word.getLemma();
            if(w.matches(regexPattern)){
                String pos = word.getPOSTag().toLowerCase().replaceAll("\\$","_po");
                if(pos.contains("-")) pos = pos.split("-")[0];
                String s = "pos("+w+","+pos+").";
                //System.out.println(s);
                posFacts.add(s);
            }


        }
        Map<Integer,String> indexLemmaMap = sentence.wordList.stream().collect(Collectors.toMap(Word :: getWordIndex, Word:: getLemma));
        for (TypedDependency dependency : sentence.dependencies){
            String relation = dependency.reln().toString().replace(':','_');
            if(relation.equalsIgnoreCase("root")
                    || relation.equalsIgnoreCase("punct")) continue;


            //String gov = dependency.gov().backingLabel().value();
           // String dep = dependency.dep().backingLabel().value();
            String gov = indexLemmaMap.get(dependency.gov().index());
            String dep = indexLemmaMap.get(dependency.dep().index());
            String s = relation + "(" + gov + "," + dep + ").";
            //System.out.println(s);
            dependenciesFacts.add(s);
        }

        return new Pair<>(posFacts,dependenciesFacts);
    }
}
