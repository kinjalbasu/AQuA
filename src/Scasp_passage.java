//package scasp;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.*;
import javafx.util.Pair;

public class Scasp_passage {
    public static void PrintRules(TreeSet<String> ruleString, BufferedWriter bw) throws IOException {
        for(String rule : ruleString){
            //System.out.println(String.format("%s.",rule));
            bw.write(String.format("%s.",rule));
            bw.newLine();
        }
    }
    public static void printPassage(String content, BufferedWriter bw1, BufferedWriter bw2) throws IOException {
        // TODO code application logic here
        // String content = "The American Broadcasting Company (ABC), stylized in the network's logo as abc since 1957, is " +
        //"an American commercial broadcast television network that is owned by the Disney_ABC_Television_Group, a subsidiary " +
        //"of Disney_Media_Networks division of The_Walt_Disney_Company. The ABC network is part of The_Big_Three television " +
        //"networks. The network is headquartered on Columbus_Avenue and West_66th_Street in Manhattan, with additional major " +
        //"offices and production facilities in New_York_City, Los_Angeles and Burbank in California.";
        
        //System.out.println("PLEASE WAIT!!! I AM LEARNING");
        StorageManager manager = new StorageManager();
        Pair<List<Rule>, List<Rule>> rulesPair = KnowledgeGeneration.RepresentKnowledge(manager, content, bw2);

        TreeSet<String> ruleString = new TreeSet<>();
        /*System.out.println("%-------------------------------------------------------%");
        System.out.println("%Story%");
        System.out.println("%-------------------------------------------------------%");*/
        for(Rule rule : rulesPair.getKey()){
            ruleString.add(rule.toString());
        }
        PrintRules(ruleString,bw1);
        //Assert.assertEquals(45, ruleString.size());

      /*  System.out.println("%%-------------------------------------------------------%%");
        System.out.println("%%Ontology%%");
        System.out.println("%%-------------------------------------------------------%%");*/
        ruleString = new TreeSet<>();
        for(Rule rule : rulesPair.getValue()){
            ruleString.add(rule.toString());
        }
        PrintRules(ruleString,bw1);
        //return ruleString;
    }
    
}
