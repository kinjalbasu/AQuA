import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class HttpRequest {

    public static String getKnowledge(String keyWord) throws IOException {
        String url = "https://en.wikipedia.org/api/rest_v1/page/summary/" + URLEncoder.encode(keyWord, "UTF-8");

        URL urlForGetRequest = new URL(url);
        String readLine = null;
        HttpURLConnection conection = (HttpURLConnection) urlForGetRequest.openConnection();
        conection.setRequestMethod("GET");
        conection.setRequestProperty("redirect", "true"); // set userId its a sample here
        int responseCode = conection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(conection.getInputStream()));
            StringBuffer response = new StringBuffer();
            while ((readLine = in .readLine()) != null) {
                response.append(readLine);
            } in .close();
            // print result
            //System.out.println("JSON String Result " + response.toString());
            return response.toString();
            //GetAndPost.POSTRequest(response.toString());
        } else {
            return "GET NOT WORKED";
        }
    }
}
