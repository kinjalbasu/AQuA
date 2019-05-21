import com.sun.javafx.fxml.builder.URLBuilder;
import edu.stanford.nlp.trees.TypedDependency;
import javafx.util.Pair;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.io.*;
import java.net.*;

import org.json.JSONException;
import org.json.JSONObject;
import org.apache.http.client.utils.URIBuilder;

public class SemanticRelationsGeneration {

    private static List<String> posFacts = new ArrayList<>();
    private static List<String> dependenciesFacts = new ArrayList<>();
    private static List<String> sementicRelations = new ArrayList<>();
    private static List<String> conceptualRelations = new ArrayList<>();
    public static void generateSemanticRelations(Sentence sentence) throws IOException {

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


                //~~~~~Adding ConceptNet
                if(pos.matches("nn|nnp|nns|nnps")){
                    List<String>  conceptList = null;
                    try {
                        conceptList = getConcept(w);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    } catch (URISyntaxException e) {
                        e.printStackTrace();
                    }
                    if(!conceptList.isEmpty()){
                        for (String concept: conceptList){
                            conceptualRelations.add(getConceptualRule(w,concept));
                        }

                    }
                }
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

    private static List<String> getConcept(String w) throws IOException, JSONException, URISyntaxException {
        String concept = "";
        StringBuilder response = new StringBuilder();
       /* URIBuilder urlBuilder = new URIBuilder("https://concept.research.microsoft.com/api/Concept/ScoreByProb");
        urlBuilder.addParameter("instance",w);
        urlBuilder.addParameter("topK","1");*/




        URL url = new URIBuilder("https://concept.research.microsoft.com/api/Concept/ScoreByProb")
                .addParameter("instance",w)
                .addParameter("topK","5")
                .build().toURL();
        //URL url = new URL("https://concept.research.microsoft.com/api/Concept/ScoreByProb?instance=apple&topK=1");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String line;
        while ((line = rd.readLine()) != null) {
            response.append(line);
        }
        rd.close();
       // return result.toString();
        JSONObject result = new JSONObject(response.toString());


        List<String> conceptList = new ArrayList<>();
        Iterator<String> keys = result.keys();
        while(keys.hasNext()) {
            conceptList.add(keys.next());
        }
        return conceptList;

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
    public static List<String> getConceptualRelations() {
        return conceptualRelations;
    }


    private static String getIsARule(String gov, String dep) {
        return "is_a("+dep+"_"+gov+","+gov+").";
    }


    private static String getSynonymRule(String gov, String dep) {
        return "synonym("+gov+","+dep+"_"+gov+").\nsynonym("+dep+","+dep+"_"+gov+").";
    }

    private static String getConceptualRule(String word, String concept) {
        return "is_a("+ word +","+getCompoundWord(concept)+").";
    }

    private static String getCompoundWord(String w){
        return w.replaceAll("\\s+","_");
    }
}
