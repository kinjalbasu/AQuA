import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by dhruv on 11/8/2017.
 */
public class StorageManager {
    public static final String ONTOLOGY_FOLDER = "ontology";
    private String ontologyBasePath;
    private String outputFilePath;
    private String storyFilePath;

    public StorageManager(){
        this.ontologyBasePath = "E:\\college\\UTD\\UTD Projects\\dhruv2\\sasp_files\\ontology";
        this.storyFilePath = "E:\\college\\UTD\\UTD Projects\\dhruv2\\sasp_files\\story";
        this.outputFilePath = "E:\\college\\UTD\\UTD Projects\\dhruv2\\sasp_files\\outputfile";
    }

    public StorageManager(String outputFilePath, String ontologyBasePath, String storyFilePath){
        this.outputFilePath = outputFilePath;
        this.ontologyBasePath = ontologyBasePath + "\\" + ONTOLOGY_FOLDER;
        this.storyFilePath = storyFilePath;

        File ontologyBase = new File(this.ontologyBasePath);
        if(!ontologyBase.exists()){
            ontologyBase.mkdir();
        }
    }

    public String ReadFileContent() {
        String line;
        StringBuilder builder = new StringBuilder();
        try {
            FileReader fileReader = new FileReader(storyFilePath);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            while((line = bufferedReader.readLine()) != null) {
                builder.append(line);
            }

            bufferedReader.close();
        }
        catch(Exception ex) {
            System.out.println("Exception occured " + ex.getMessage());
        }
        return builder.toString();
    }

    public void WriteConceptToFile(String aspCode, String concept, List<String> headers, boolean shouldWriteToFile) throws IOException {
        String conceptBasePath = String.format("%s\\%s.lp", ontologyBasePath, concept);
        WriteASPCode(conceptBasePath, aspCode, headers, shouldWriteToFile);
    }

    public void WriteStoryToFile(String aspCode, List<String> headers, boolean shouldWriteToFile) throws IOException {
        WriteASPCode(outputFilePath, aspCode, headers, shouldWriteToFile);
    }

    public static void WriteASPCode(String outputFilePath, String aspCode, List<String> headerFiles, boolean shouldWriteToFile) throws IOException {
        if(!shouldWriteToFile){
            System.out.print(aspCode);
            return;
        }

        StringBuilder builder = new StringBuilder();
        if(headerFiles != null) {
            for (String headerFile : headerFiles) {
                String headerFilesCode = String.format("#include('%s.lp').\n", headerFile.toLowerCase());
                builder.append(headerFilesCode);
            }
        }

        String headers = builder.toString();
        String code = String.format("%s \n %s", headers, aspCode);
        if(headers.equals("")){
            code = String.format("%s", aspCode);
        }

        FileWriter fileWriter = new FileWriter(outputFilePath);
        BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
        bufferedWriter.write(code);
        bufferedWriter.close();

    }
}
