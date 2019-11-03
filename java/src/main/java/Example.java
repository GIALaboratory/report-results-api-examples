import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.net.UnknownHostException;

// Google's Gson to parse the JSON document
// https://github.com/google/gson
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

// Apache HTTPComponents to make the HTTP request
// https://hc.apache.org/
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

public class Example {
    // Report number to look up. Can be provided on command line
    public static final String DEFAULT_REPORT_NUMBER = "1206489210";
    // Location of the graphql query. This file should be moved to your resources directory.
    public static final String GRAPHQL_QUERY_FILE = "../graphql_query/report_results.graphql";

    public static void main(String[] args) {
        
        // Get parameters from environmental variables. Do not store secrets in code!
        String url = System.getenv("REPORT_RESULTS_API_ENDPOINT");
        String key = System.getenv("REPORT_RESULTS_API_KEY");

        // Confirm that url and key are available
        if (url == null || url.isEmpty()) {
            System.out.println("You must provide environment variable REPORT_RESULTS_API_ENDPOINT.");
            System.exit(1);
        }
        if (key == null || key.isEmpty()) {
            System.out.println("You must provide environment variable REPORT_RESULTS_API_KEY.");
            System.exit(1);
        }

        // Load the query from a file
        String query = "";
        try {
            query = new String(Files.readAllBytes(Paths.get(GRAPHQL_QUERY_FILE)));
        } catch (NoSuchFileException e) {
            System.out.println("Cannot find the graphql file at " + GRAPHQL_QUERY_FILE);
            System.exit(1);
        }
        catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

        // Set the report number to lookup
        String reportNumber;
        if (args.length == 0) {
            reportNumber = DEFAULT_REPORT_NUMBER;
        } else {
            reportNumber = args[0];
        }
        System.out.println("Looking up report number: " + reportNumber + "\n");

        // Construct the payload to be POSTed to the graphql server
        Map<String, String> queryVariables = new HashMap<String, String>();
        queryVariables.put("ReportNumber", reportNumber);
        Map<String, Object> payload = new HashMap<String, Object>();
        payload.put("query", query);
        payload.put("variables", queryVariables);

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

        // This Map will hold all the JSON elements in a flat structure
        Map<String, String> dict = new HashMap<String, String>();

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

            // Check for any HTTP errors
            if (response.getStatusLine().getStatusCode() != 200) {
                System.out.println("HTTP error returned by the API: " + response.getStatusLine());
                System.exit(1);
            }

            // Parse the response (a string) into a JsonDocument so we can
            // traverse the structure
            JsonElement rootElement = JsonParser.parseString(jsonResult);

            // Save all elements to a Map
            flattenJsonDoc(rootElement, dict, "");

            // Check for errors in the response
            if (dict.containsKey("/errors/0/message")) {
                System.out.print("Error processing request: ");
                System.out.println(dict.get("/errors/0/message") + "\n");
            }
            
        } catch (UnknownHostException e) {
            System.out.println("Unknown host: " + url);
            System.out.println("Check your network connection and the API endpoint URL.");
            System.exit(1);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }

        // Write all data to the console
        System.out.println("PARSED REPORT RESULTS");
        System.out.println("---------------------");

        for (Entry<String, String> entry : dict.entrySet()) {
            System.out.println(entry.getKey() + ": " + entry.getValue());
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
            dict.put(path, "null");
        }

        // All others are JSON primitives
        if (element.isJsonPrimitive()) {
            dict.put(path, element.getAsString());
        }
    }
}