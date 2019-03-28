import edu.stanford.nlp.trees.TypedDependency;
import javafx.util.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SemanticRelationsGeneration {

    private static List<String> posFacts = new ArrayList<>();
    private static List<String> dependenciesFacts = new ArrayList<>();
    private static List<String> sementicRelations = new ArrayList<>();
    public static void generateSemanticRelations(Sentence sentence){

        String regexPattern = "^[a-zA-Z0-9-]*$";
        for (Word word: sentence.wordList) {
            //String w = word.getWord();
            String w = word.getLemma();
            if(w.matches(regexPattern)){
                String pos = word.getPOSTag().toLowerCase().replaceAll("\\$","_po");
                if(pos.contains("-")) pos = pos.split("-")[0];
                String s = "_pos("+w+","+pos+").";
                //System.out.println(s);
                posFacts.add(s);
            }


        }
        Map<Integer,String> indexLemmaMap = sentence.wordList.stream().collect(Collectors.toMap(Word :: getWordIndex, Word:: getLemma));
        Map<String,String> lemmaPosMap = sentence.wordList.stream().collect(Collectors.toMap(Word :: getLemma, Word:: getPOSTag, (pos1, pos2)-> pos1 ));
        for (TypedDependency dependency : sentence.dependencies){
            String relation = dependency.reln().toString().replace(':','_');
            if(relation.equalsIgnoreCase("root")
                    || relation.equalsIgnoreCase("punct")) continue;


            //String gov = dependency.gov().backingLabel().value();
           // String dep = dependency.dep().backingLabel().value();
            String gov = indexLemmaMap.get(dependency.gov().index());
            String dep = indexLemmaMap.get(dependency.dep().index());
            String s = "_"+relation + "(" + gov + "," + dep + ").";
            //System.out.println(s);
            dependenciesFacts.add(s);



            if(relation.equalsIgnoreCase("compound")
                    && lemmaPosMap.get(gov).equalsIgnoreCase("nn")
                    && lemmaPosMap.get(dep).equalsIgnoreCase("nn")){
                 sementicRelations.add(getIsARule(gov,dep));
            }


            if(relation.equalsIgnoreCase("compound")
                    && lemmaPosMap.get(gov).equalsIgnoreCase("nnp")
                    && lemmaPosMap.get(dep).equalsIgnoreCase("nnp")){
                sementicRelations.add(getSynonymRule(gov,dep));
            }
        }

    }

    public static List<String> getPosFacts() {
        return posFacts;
    }

    public static List<String> getDependenciesFacts() {
        return dependenciesFacts;
    }

    public static List<String> getSementicRelations() {
        return sementicRelations;
    }


    private static String getIsARule(String gov, String dep) {
        return "is_a("+dep+"_"+gov+","+gov+").";
    }


    private static String getSynonymRule(String gov, String dep) {
        return "synonym("+gov+","+dep+"_"+gov+").\nsynonym("+dep+","+dep+"_"+gov+").";
    }

}
