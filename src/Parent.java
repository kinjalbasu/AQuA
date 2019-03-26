import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.logging.RedwoodConfiguration;
public class Parent {
    public static void main(String[] args) throws IOException {
        RedwoodConfiguration.current().clear().apply();
        Scanner scan = new Scanner(System.in);
        //System.out.println("PLEASE PROVIDE ME SOME KNOWLEDGE - ");
        //String content = scan.nextLine();

        String content = "Nikola Tesla (10 July 1856 – 7 January 1943) was a Serbian American inventor, electrical engineer, mechanical engineer, physicist, and futurist best known for his contributions to the design of the modern alternating current (AC) electricity supply system.";
     //String content ="John and Marry own a car. They were traveling from Dallas to Houston with the car. The engine of the car broke down in the middle of the road. The car was repaired by John's brother.";

 //       String content = "John went to the park yesterday because he saw hot air balloons taking off from there";
        //String question = "Since what year did ABC stylize abc's logo, as abc ?";
        //String question = "what company owns the american_broadcasting_company ?";
        //String question = "what company owns the american_broadcasting_company ?";
        //String question = "In what borough of New_York_City is ABC headquartered?";
        //String question = "since what year did abc stylize abc s logo , as abc ?";
        //String question = "when did tesla die ?";
        //String question = "when was nikola_tesla born ?";
        //String question = "which scientist was a computer engineer?";




        //Create knowledge.lp
        File knowledge_ouput = new File("resources/knowledge.lp");
        FileWriter fw1 = new FileWriter(knowledge_ouput);
        BufferedWriter bw1 = new BufferedWriter(fw1);

        //Create knowledge.lp
        File parser_ouput = new File("resources/semanticRelations.lp");
        FileWriter fw2 = new FileWriter(parser_ouput);
        BufferedWriter bw2 = new BufferedWriter(fw2);


        //Create question.lp
        File question_output = new File("resources/question.lp");





        //Print in Knowledge File
        bw1.write("#include 'CommonRules.lp'.\n#include 'question.lp'." +
                "\n#include 'Rules.lp'.\n#include 'semanticRelations.lp'.\n");
        Scasp_passage.printPassage(content, bw1, bw2);
        bw1.close();
        bw2.close();


        //System.out.println("I AM READY TO ANSWER YOUR QUESTIONS....");
        String questionFlag = "y";
        do {
            System.out.print("Question = ");
            String question = scan.nextLine();
            FileWriter fw3 = new FileWriter(question_output);
            BufferedWriter bw3 = new BufferedWriter(fw3);
            Scasp_question.printQuestion(question, bw3);
            bw3.write("?- query(1,Question,answer( Answer, Confidence_Level)).");
            bw3.close();

            //Running CMD

            //String path = knowledge_ouput.getAbsolutePath();
            //String path = "E:\\college\\UTD\\UTD_Projects\\TestAgent1\\resources\\knowledge.lp";
            String path = "C:\\Users\\kxb170730\\IdeaProjects\\CommonSenseQA\\resources\\knowledge.lp";
            Runtime rt = Runtime.getRuntime();
            Process proc = rt.exec("sasp " + path);

            BufferedReader stdInput = new BufferedReader(new
                    InputStreamReader(proc.getInputStream()));

            BufferedReader stdError = new BufferedReader(new
                    InputStreamReader(proc.getErrorStream()));

            // read the output from the command
            //System.out.println("Here is the standard output of the command:\n");
            String s = null;
            List<String> output = new ArrayList<>();
            while ((s = stdInput.readLine()) != null) {
                output.add(s);
            }
            //System.out.println(output.get(1));
            System.out.println(output.get(2));
            System.out.println(output.get(3));
            // read any errors from the attempted command
            //System.out.println("Here is the standard error of the command (if any):\n");
            while ((s = stdError.readLine()) != null) {
                System.out.println(s);
            }
            System.out.print("\nDO YOU HAVA ANYMORE QUESTION? (y/n) ");
            questionFlag = scan.nextLine();
        }while(questionFlag.toLowerCase().equals("y"));
    }


}
