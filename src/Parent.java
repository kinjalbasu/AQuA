import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.jpl7.Query;
import org.jpl7.Term;

import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.logging.RedwoodConfiguration;

import static java.lang.Runtime.getRuntime;

public class Parent {
    public static final String KNOWLEDGE_PATH = "resources/clevr/clevrKnowledge.lp";
    public static final String QUESTION_PATH = "resources/clevr/clevrQuery.lp";
    public static final String COMMON_FACTS_PATH = "resources/clevr/clevrCommonFacts.lp";
    public static final String RULE_PATH = "resources/clevr/clevrRules.lp";
    public static final String SEMANTIC_PATH = "resources/clevr/clevrSemanticRules.lp";
   // public static final String RULE_PATH = "resources/Rules.lp";

    public static void main(String[] args) throws IOException, InterruptedException {
        RedwoodConfiguration.current().clear().apply();
        Scanner scan = new Scanner(System.in);
        //String content = "Nikola Tesla (10 July 1856 – 7 January 1943) was a Serbian American inventor, electrical engineer, mechanical engineer, physicist, and futurist best known for his contributions to the design of the modern alternating current (AC) electricity supply system.";
        //String content = "The Matrix is a 1999 science fiction action film written and directed by The Wachowskis, starring Keanu Reeves, Laurence Fishburne, CarrieAnne Moss, Hugo Weaving, and Joe Pantoliano. It depicts a dystopian future in which reality as perceived by most humans is actually a simulated reality called \"the Matrix\", created by sentient machines to subdue the human population, while their bodies heat and electrical activity are used as an energy source. Computer programmer \"Neo\" learns this truth and is drawn into a rebellion against the machines, which involves other people who have been freed from the \"dream world\".";

        //String content = "A reusable launch system (RLS, or reusable launch vehicle, RLV) is a launch system which is capable of launching a payload into space more than once. This contrasts with expendable launch systems, where each launch vehicle is launched once and then discarded. No completely reusable orbital launch system has ever been created. Two partially reusable launch systems were developed, the Space Shuttle and Falcon 9. The Space Shuttle was partially reusable: the orbiter (which included the Space Shuttle main engines and the Orbital Maneuvering System engines), and the two solid rocket boosters were reused after several months of refitting work for each launch. The external tank was discarded after each flight.";
        //String content = "Oxygen is a chemical element with symbol O and atomic number 8. It is a member of the chalcogen group on the periodic table and is a highly reactive nonmetal and oxidizing agent that readily forms compounds (notably oxides) with most elements. By mass, oxygen is the third most abundant element in the universe, after hydrogen and helium. At standard temperature and pressure, two atoms of the element bind to form dioxygen, a colorless and odorless diatomic gas with the formula O. Diatomic oxygen gas constitutes 20 percent of the Earth's atmosphere. However, monitoring of atmospheric oxygen levels show a global downward trend, because of fossil fuel burning. Oxygen is the most abundant element by mass in the Earth's crust as part of oxide compounds such as silicon dioxide, making up almost half of the crust's mass.";
        //String content ="John and Marry own a car. They were traveling from Dallas to Houston with the car. The engine of the car broke down in the middle of the road. The car was repaired by John's brother.";

        //String content = "John went to the park yesterday because he saw hot air balloons taking off from there";
        //String question = "Since what year did ABC stylize abc's logo, as abc ?";
        //String question = "what company owns the american_broadcasting_company ?";
        //String question = "what company owns the american_broadcasting_company ?";
        //String question = "In what borough of New_York_City is ABC headquartered?";
        //String question = "since what year did abc stylize abc s logo , as abc ?";
        //String question = "when did tesla die ?";
        //String question = "when was nikola_tesla born ?";
        //String question = "which scientist was a computer engineer?";
        //String content = "There are two forks, two spoons and three bananas with four apples on the table.";


        //paths
        String semanticPath = new File(SEMANTIC_PATH).getCanonicalPath();
        String knowledgePath = new File(KNOWLEDGE_PATH).getCanonicalPath();
        String rulesPath = new File(RULE_PATH).getCanonicalPath();

        String queryPath = new File(QUESTION_PATH).getCanonicalPath();


        //Create question.lp
        File question_output = new File(QUESTION_PATH);
        /*
        //Create knowledge.lp
        File knowledge_ouput = new File(KNOWLEDGE_PATH);
        FileWriter fw1 = new FileWriter(knowledge_ouput);
        BufferedWriter bw1 = new BufferedWriter(fw1);

        //Create semanticRelations.lp
        File parser_ouput = new File(SEMANTIC_PATH);
        FileWriter fw2 = new FileWriter(parser_ouput);
        BufferedWriter bw2 = new BufferedWriter(fw2);




        //Print in Knowledge File
        bw1.write("#include 'CommonRules.lp'.\n#include 'question.lp'." +
                "\n#include 'semanticRelations.lp'.\n");
        Scasp_passage.printPassage(content, bw1, bw2);
        bw1.close();



        BufferedReader knowledgee_br = new BufferedReader(new FileReader(knowledgePath));
        List<String> events = new ArrayList<>();

        String p = knowledgee_br.readLine();
        while(p != null){
            if(p.startsWith("event") && !p.contains(":-"))
                events.add(p);
            p = knowledgee_br.readLine();
        }

        bw2.newLine();
        bw2.newLine();

        for (String s : events
             ) {
                bw2.write(s);
                bw2.newLine();
        }
        bw2.close();

        Runtime rt = Runtime.getRuntime();
        Process proc = rt.exec("gringo "  + semanticPath + " " + rulesPath + " -t");
        BufferedReader stdInput = new BufferedReader(new
                InputStreamReader(proc.getInputStream()));

        BufferedReader stdError = new BufferedReader(new
                InputStreamReader(proc.getErrorStream()));
        BufferedWriter rel_bw = new BufferedWriter(new FileWriter(semanticPath, true));
        rel_bw.newLine();
        String s =null;
        while((s = stdInput.readLine()) != null){
            if(s.startsWith("#") || s.startsWith("_")) continue;
            rel_bw.write(s);
            rel_bw.newLine();
        }
        rel_bw.close();
        String err = null;
        while((err = stdError.readLine()) != null){
            //System.out.println(err);
        }*/
        //-------------For Demo-------------------
        String questionFlag = "y";
        // ExecutorService executor = Executors.newCachedThreadPool();


        do {
            //System.out.print("Question = ");
            //String question = scan.nextLine();
            String question = "Is the large red object made of the same material as the blue cylinder ?";
            FileWriter fw3 = new FileWriter(question_output);
            BufferedWriter bw3 = new BufferedWriter(fw3);
            bw3.write(":- include('clevrKnowledge.pl').\n" +
                    ":- include('clevrRules.pl').\n" +
                    ":- include('clevrSemanticRules.pl').\n" +
                    ":- include('clevrCommonFacts.pl').");
            bw3.newLine();

            Scasp_question.printQuestion(question, bw3);
            bw3.close();
            generatePrologCompatibleCode();
            //bw3.write("?- query(Q,A).");
            //bw3.close();
            /*Runtime rt = getRuntime();
            System.out.println("Sasp invoked");
            Process proc1 = rt.exec("sasp " + queryPath);


            //Process proc1 = future.get(10, TimeUnit.SECONDS);


            BufferedReader stdInput1 = new BufferedReader(new
                    InputStreamReader(proc1.getInputStream()));

            BufferedReader stdError1 = new BufferedReader(new
                    InputStreamReader(proc1.getErrorStream()));
            String s1 = null;
            List<String> output = new ArrayList<>();


            while ((s1 = stdInput1.readLine()) != null) {
                output.add(s1);
            }
            if (2 < output.size()) {
                System.out.println(output.get(2));
            } else {
                System.out.println("No Answer");
            }
            //System.out.println(output.get(3));
            while ((s1 = stdError1.readLine()) != null) {
                // System.out.println(s1);
            }*/

            String t = "consult('resources/clevrProlog/clevrQuery.pl')";
            Query q = new Query(t);
            if(!q.hasSolution()){
                System.out.println("Fails");
            }
            else{
                //t = "get_all_id(Ids),filter_all([[shape,cube],[color,blue]],Ids,L1),filter_all([[material,nonmetal]],Ids,L2).";
                q = new Query("query(Q,A).");
                Map<String, Term>[] map = q.allSolutions();
                System.out.println("Question : " + map[0].get("Q"));
                System.out.println("Answer : " + map[0].get("A"));
            }
            //System.out.println("No Answer (Time Out)");
            System.out.print("\nDO YOU HAVA ANYMORE QUESTION? (y/n) ");
            questionFlag = scan.nextLine();

        } while (!questionFlag.toLowerCase().equals("n"));
    }

    private static void generatePrologCompatibleCode() throws IOException {
        List<String> clevrFiles = Files.walk(Paths.get("resources/clevr"))
                .filter(Files::isRegularFile)
                .map(f -> f.toString()).collect(Collectors.toList());

        String clevrPrologDirPath = "resources/clevrProlog";
        for(String f : clevrFiles){
            String newFileName = f.substring(f.lastIndexOf('\\')+1,f.indexOf('.')) + ".pl";
            FileWriter fw = new FileWriter(new File(clevrPrologDirPath + File.separator + newFileName));
            BufferedWriter bw = new BufferedWriter(fw);

            FileReader fr = new FileReader(new File(f));
            BufferedReader br = new BufferedReader(fr);
            String s = br.readLine();
            while(s != null){
                if ( s.contains("not ")){
                    String[] sARR = s.split("not ");
                    String s2 = sARR[0];
                    for(int i = 1; i < sARR.length; i++){
                        s2 += "not(" + sARR[i].replaceFirst("\\)","))");

                    }
                    bw.write(s2);
                    bw.newLine();
                }
                else if (s.startsWith("_")){
                    bw.write(s.replaceFirst("_",""));
                    bw.newLine();
                }
                else {
                    bw.write(s);
                    bw.newLine();
                }
                s = br.readLine();
            }
            br.close();
            bw.close();
        }
    }

   /* private static Process runSasp(String queryPath) throws IOException {
        Runtime rt = getRuntime();
        Process proc1 = rt.exec("sasp " + queryPath);
        return proc1;
    }*/



}
