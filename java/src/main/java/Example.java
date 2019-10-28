import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

public class Example {
    public static void main(String[] args) {
        
        // Get parameters from environmental variables. Do not store secrets in code!
        String url = System.getenv("REPORT_CHECK_API_ENDPOINT");
        String key = System.getenv("REPORT_CHECK_API_KEY");

        // Confirm that url and key are available
        if (url == null || url.isEmpty()) {
            System.out.println("You must provide environment variable REPORT_CHECK_API_ENDPOINT.");
            System.exit(1);
        }
        if (key == null || key.isEmpty()) {
            System.out.println("You must provide environment variable REPORT_CHECK_API_KEY.");
            System.exit(1);
        }

        // Load the query from a file
        String query_file = "../graphql_query/report_results.graphql";
        String query = "";
        try {
            query = new String(Files.readAllBytes(Paths.get(query_file)));
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

        // Set the report number to lookup
        String reportNumber = "1206489210";
        System.out.println("Looking up report number: " + reportNumber + "\n");

        // Construct the payload to be POSTed to the graphql server
        Map<String, String> query_variables = new HashMap<String, String>();
        query_variables.put("ReportNumber", reportNumber);
        Map<String, Object> payload = new HashMap<String, Object>();
        payload.put("query", query);
        payload.put("variables", query_variables);

        // Use Gson builder for pretty-printing
        //Gson gson = new Gson();
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        String json = gson.toJson(payload);

        // Write the payload to the console
        System.out.println("JSON PAYLOAD TO BE POSTED TO THE SERVER");
        System.out.println("---------------------------------------");
        System.out.println(json + "\n");

        HttpPost httpPost = new HttpPost(url);
        
        httpPost.setHeader("Authorization", key);
        httpPost.setHeader("Content-type", "application/json");
        
        CloseableHttpClient httpclient = HttpClients.createDefault();
        
        try {
            httpPost.setEntity(new StringEntity(json));
            CloseableHttpResponse response2 = httpclient.execute(httpPost);
            System.out.println(response2.getStatusLine());
            HttpEntity entity2 = response2.getEntity();
            // do something useful with the response body
            // and ensure it is fully consumed
            System.out.println(EntityUtils.toString(entity2));
        
        } catch (Exception e) {
            System.out.println("Error");
            System.out.println(e);
        }


    }
}