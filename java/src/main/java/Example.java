import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

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

        // Use Gson builder for pretty-printing, otherwise use: Gson gson = new Gson();
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        String json = gson.toJson(payload);

        // Write the payload to the console
        System.out.println("JSON PAYLOAD TO BE POSTED TO THE SERVER");
        System.out.println("---------------------------------------");
        System.out.println(json + "\n");

        // Create http client and the post request
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(url);
        
        // Set headers for the api key and content-type
        httpPost.setHeader("Authorization", key);
        httpPost.setHeader("Content-type", "application/json");
        
        try {

            // The payload is the JSON we created earlier
            httpPost.setEntity(new StringEntity(json));

            // Send the request to the server
            CloseableHttpResponse response = httpclient.execute(httpPost);

            // Retrieve the JSON result from the response
            HttpEntity result = response.getEntity();
            String jsonResult = EntityUtils.toString(result);


            System.out.println("JSON RESPONSE RECEIVED FROM THE API");
            System.out.println("-----------------------------------");
            System.out.println(jsonResult + "\n");

            // Parse the response (a string) into a JsonDocument so we can
            // traverse the structure
            JsonElement rootElement = JsonParser.parseString(jsonResult);

            // Save all elements to a Map
            Map<String, String> dict = new HashMap<String, String>();
            flattenJsonDoc(rootElement, dict, "");

            // Write all data to the console
            System.out.println("PARSED REPORT RESULTS");
            System.out.println("---------------------");

            for (Entry<String, String> entry : dict.entrySet()) {
                System.out.println(entry.getKey() + ": " + entry.getValue());
            }
        
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    public static void flattenJsonDoc(JsonElement element, Map<String, String> dict, String path) {
        // This method flattens a JSON document into a dictionary of strings
        // element: The root element of the JSON to be flattened
        // dict: The HashMap to receive the flattened elements

        // A JSON object has properties that must be enumerated
        if (element.isJsonObject()) {
            for (Entry<String, JsonElement> el : element.getAsJsonObject().entrySet()) {
                flattenJsonDoc(el.getValue(), dict, path + "/" + el.getKey());   
            }
        }

        // A JSON Array holds a number of JSON elements
        if (element.isJsonArray()) {
            int index = 0;
            for (JsonElement child : element.getAsJsonArray()) {
                flattenJsonDoc(child, dict, path + "/" + index);
                index++;
            }
        }

        // Check for null values
        if (element.isJsonNull()) {
            dict.put(path, element.getAsString());
        }

        // All others are JSON primitives
        if (element.isJsonPrimitive()) {
            dict.put(path, element.getAsString());
        }
    }
}