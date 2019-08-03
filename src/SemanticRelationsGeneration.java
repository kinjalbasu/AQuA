import com.sun.javafx.fxml.builder.URLBuilder;
import edu.stanford.nlp.trees.TypedDependency;
import edu.stanford.nlp.util.StringUtils;
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
    public static void initializeLists(){
        posFacts = new ArrayList<>();
        dependenciesFacts = new ArrayList<>();
        sementicRelations = new ArrayList<>();
        conceptualRelations = new ArrayList<>();
    }
    public static void generateSemanticRelations(Sentence sentence){

        String regexPattern = "^[a-zA-Z0-9-]*$";
        for (Word word : sentence.wordList) {
            //String w = word.getWord();
            String w = word.getLemma();
            if (w.matches(regexPattern)) {
                String pos = word.getPOSTag().toLowerCase().replaceAll("\\$", "_po");
                if (pos.contains("-")) pos = pos.split("-")[0];
                String s =null;
                if(StringUtils.isNumeric(w)){
                    s = "_pos(" + w + "," + pos + ").";
                }
                else{
                    s = "_pos(" + w+ "_"+word.getWordIndex() + "," + pos + ").";
                }
                //System.out.println(s);
                posFacts.add(s);


                //check 'anything' or not for quantification 1 (needed for existential queries). e.g. "is there anything....."

                if(w.equalsIgnoreCase("anything") && word.getPOSTag().equalsIgnoreCase("nn")){
                    sementicRelations.add("quantification(1,"+w+"_"+word.getWordIndex()+").");
                }
                if(w.equalsIgnoreCase("thing")
                        && sentence.wordList.get(word.getWordIndex() - 2).getLemma().equalsIgnoreCase("other")){
                    sementicRelations.add("quantification(1,"+w+"_"+word.getWordIndex()+").");

                }

                //~~~~~Adding ConceptNet
               /* if (pos.matches("nn|nnp|nns|nnps")) {
                    List<String> conceptList = null;
                    conceptList = getConcept(w);

                    if (!conceptList.isEmpty()) {
                        for (String concept : conceptList) {
                            conceptualRelations.add(getConceptualRule(w, concept));
                        }

                    }
                }*/
            }


        }
        Map<Integer, String> indexLemmaMap = sentence.wordList.stream().collect(Collectors.toMap(Word::getWordIndex, Word::getLemma));
        Map<String, String> lemmaPosMap = sentence.wordList.stream().collect(Collectors.toMap(Word::getLemma, Word::getPOSTag, (pos1, pos2) -> pos1));
        for (TypedDependency dependency : sentence.dependencies) {
            String relation = dependency.reln().toString().replace(':', '_');
            if (relation.equalsIgnoreCase("root")
                    || relation.equalsIgnoreCase("punct")) continue;


            String gov = indexLemmaMap.get(dependency.gov().index());
            String dep = indexLemmaMap.get(dependency.dep().index());

            //TODO Dependency Corrections
            if(relation.equalsIgnoreCase("compound") && dep.equalsIgnoreCase("metal")){
                relation = "amod";
            }
            else if(relation.equalsIgnoreCase("compound") && dep.equalsIgnoreCase("rubber")){
                relation = "amod";
            }
            else if(relation.equalsIgnoreCase("nsubj") && dep.equalsIgnoreCase("rubber")){
                relation = "amod";
            }
            else if(relation.equalsIgnoreCase("compound") && dep.equalsIgnoreCase("matte")){
                relation = "amod";
            }
            else if(relation.equalsIgnoreCase("compound") && dep.equalsIgnoreCase("cyan")){
                relation = "amod";
            }
            else if(relation.equalsIgnoreCase("nsubjpass") && gov.equalsIgnoreCase("left")){
                relation = "nsubj";
            }
            else if(relation.equalsIgnoreCase("auxpass") && gov.equalsIgnoreCase("left")){
                relation = "cop";
            }
            else if(relation.equalsIgnoreCase("acl_relcl") && dep.equalsIgnoreCase("block")
                    && sentence.dependencies.stream().filter( d -> d.reln().toString().equalsIgnoreCase("mark") &&
                    indexLemmaMap.get(d.gov().index()).equalsIgnoreCase(dep) && indexLemmaMap.get(d.dep().index()).equalsIgnoreCase("as")).findFirst().isPresent()){
                relation = "nmod_as";
            }


            //----------------------
            String s = null;
            if(!StringUtils.isNumeric(gov) && !StringUtils.isNumeric(dep)) {
                s = "_" + relation + "(" + gov + "_" + dependency.gov().index() + "," + dep + "_" + dependency.dep().index() + ").";
            }
            else if(StringUtils.isNumeric(gov) && !StringUtils.isNumeric(dep)) {
                s = "_" + relation + "(" + gov + "," + dep + "_" + dependency.dep().index() + ").";
            }
            else if(!StringUtils.isNumeric(gov) && StringUtils.isNumeric(dep)) {
                s = "_" + relation + "(" + gov + "_" + dependency.gov().index() + "," + dep + ").";
            }
            else{
                s = "_" + relation + "(" + gov + "," + dep + ").";
            }
            dependenciesFacts.add(s);


            if (relation.equalsIgnoreCase("compound")
                    && lemmaPosMap.get(gov).equalsIgnoreCase("nn")
                    && lemmaPosMap.get(dep).equalsIgnoreCase("nn")) {
                sementicRelations.add(getIsARule(gov, dep));
            }


            if (relation.equalsIgnoreCase("compound")
                    && lemmaPosMap.get(gov).equalsIgnoreCase("nnp")
                    && lemmaPosMap.get(dep).equalsIgnoreCase("nnp")) {
                sementicRelations.add(getSynonymRule(gov, dep));
            }

            if(relation.equalsIgnoreCase("det") && dep.toLowerCase().matches("a|an|any")){
                sementicRelations.add(getQuantificationRule(dependency,sentence.wordList));
            }

        }

    }

    private static String getQuantificationRule(TypedDependency dependency, List<Word> wordList) {
       // return "quantification(1,"+ dependency.gov().backingLabel().toString().replace("-","_") + ").";
        return "quantification(1,"+ wordList.get(dependency.gov().index()-1).getLemma() + "_" + dependency.gov().index() + ").";
    }

    public static List<String> getConcept(String w) {
        List<String> conceptList = new ArrayList<>();
        try {
            String concept = "";

            StringBuilder response = new StringBuilder();

            URL url = new URIBuilder("https://concept.research.microsoft.com/api/Concept/ScoreByProb")
                    .addParameter("instance", w)
                    .addParameter("topK", "5")
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


            Iterator<String> keys = result.keys();
            while (keys.hasNext()) {
                conceptList.add(keys.next());
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return conceptList;

    }

    public static List<String> getPosFacts() {
        return posFacts;
    }

    public static List<String> getDependenciesFacts() {
        return dependenciesFacts;
    }

    public static List<String> getSementicRelations()
    {
        return sementicRelations;
    }

    public static List<String> getConceptualRelations() {
        return conceptualRelations;
    }


    private static String getIsARule(String gov, String dep) {
        return "is_a(" + dep + "_" + gov + "," + gov + ").";
    }


    private static String getSynonymRule(String gov, String dep) {
        return "synonym(" + gov + "," + dep + "_" + gov + ").\nsynonym(" + dep + "," + dep + "_" + gov + ").";
    }

    private static String getConceptualRule(String word, String concept) {
        return "is_a(" + word + "," + getCompoundWord(concept) + ").";
    }

    private static String getCompoundWord(String w) {
        return w.replaceAll("\\s+", "_");
    }
}
