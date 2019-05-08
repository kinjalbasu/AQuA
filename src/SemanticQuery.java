import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class SemanticQuery {

    public static List<Rule> getSemanticQueries(Question question) throws IOException {
        List<Rule> semanticQueries = new ArrayList<>();
        Runtime rt = Runtime.getRuntime();
        if(question.information.questionWord.getWord().equalsIgnoreCase("who") || true){
            String s = "sasp C:\\Users\\kxb170730\\IdeaProjects\\CommonSenseQA\\resources\\semanticRelations.lp -i";
            Process p = rt.exec(s);

            OutputStream stdin = p.getOutputStream ();
            InputStream stderr = p.getErrorStream ();
            InputStream stdout = p.getInputStream ();
            String line = null;

            line = "is_a(tesla,X).";
            stdin.write(line.getBytes() );
            stdin.flush();
            //stdin.write(";".getBytes());
            //stdin.flush();
            stdin.close();

            BufferedReader br = new BufferedReader(new InputStreamReader(stdout));
            String out = null;
            while((out=br.readLine()) != null){
                System.out.println(out);
            }
            BufferedReader brErr = new BufferedReader(new InputStreamReader(stderr));
            String err = null;
            while((err=brErr.readLine()) != null){
                System.out.println(err);
            }


        }


        return semanticQueries;

    }
}
