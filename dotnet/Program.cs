using System;
using System.Net;
using System.Collections.Generic;
using System.Text.Json;
using System.IO;

namespace rrapi_dotnet
{

    class Program
    {
        public const string DEFAULT_REPORT_NUMBER = "1206489210";
        public const string GRAPHQL_QUERY_FILE = "../graphql_query/report_results.graphql";
        
        static void Main(string[] args)
        {
            // Get parameters from environmental variables. Do not store secrets in code!
            string url = System.Environment.GetEnvironmentVariable("REPORT_RESULTS_API_ENDPOINT");
            string key = System.Environment.GetEnvironmentVariable("REPORT_RESULTS_API_KEY");

            // Confirm that url and key are available
            if (string.IsNullOrEmpty(url) | string.IsNullOrEmpty(key))
            {
                Console.WriteLine("You must provide environment variables REPORT_RESULTS_API_ENDPOINT and REPORT_RESULTS_API_KEY.");
                System.Environment.Exit(1);
            }

            // Load the query from a file
            string query = "";
            try
            {
                query = File.ReadAllText(GRAPHQL_QUERY_FILE);
            }
            catch (System.IO.FileNotFoundException e)
            {
                Console.WriteLine(e.Message);
                System.Environment.Exit(1);
            }
            catch (Exception e)
            {
                Console.WriteLine(e.Message);
                Console.WriteLine(e.StackTrace);
            }

            // Set the report number to lookup
            string reportNumber;
            if (args.Length == 0)
            {
                reportNumber = DEFAULT_REPORT_NUMBER;
            }
            else
            {
                reportNumber = args[0];
            }
            Console.WriteLine("Looking up report number: " + reportNumber + "\n");

            // Construct the payload to be POSTed to the graphql server
            var query_variables = new Dictionary<string, string> {
                { "ReportNumber", reportNumber}
            };
            var payload = new Dictionary<string, object> {
                { "query", query },
                { "variables", query_variables }
            };

            // Pretty-print the JSON for readability
            JsonSerializerOptions options = new JsonSerializerOptions
            {
                WriteIndented = true
            };
            // Convert the payload to JSON
            string json = JsonSerializer.Serialize(payload, options);

            // Write the payload to the console
            Console.WriteLine("JSON PAYLOAD TO BE POSTED TO THE SERVER");
            Console.WriteLine("---------------------------------------");
            Console.WriteLine(json + "\n");

            // The results will be saved in this dictionary
            Dictionary<string, string> reportResults = new Dictionary<string, string>();

            using (var client = new WebClient())
            {
                // Set headers for the api key and content-type
                client.Headers.Add(HttpRequestHeader.Authorization, key);
                client.Headers.Add(HttpRequestHeader.ContentType, "application/json");

                string response = "";
                try
                {
                    // Send the payload as a JSON to the endpoint
                    response = client.UploadString(url, json);
                }
                catch (System.Net.WebException e)
                {
                    Console.Write("Error accessing " + url + ": ");
                    Console.WriteLine(e.Message);
                    System.Environment.Exit(1);
                }

                Console.WriteLine("JSON RESPONSE RECEIVED FROM THE API");
                Console.WriteLine("-----------------------------------");
                Console.WriteLine(response + "\n");

                // Parse the response (a string) into a JsonDocument so we can
                // traverse the fields
                JsonDocument document = JsonDocument.Parse(response);

                // Recursively flatten the JSON document into a dictionary
                flattenJsonDoc(document.RootElement, reportResults);
            }

            // Check for errors in the response
            if (reportResults.ContainsKey("/errors/0/message")) {
                Console.Write("Error processing request: ");
                Console.WriteLine(reportResults["/errors/0/message"] + "\n");
            }

            // Write all data to the console
            Console.WriteLine("PARSED REPORT RESULTS");
            Console.WriteLine("---------------------");
            foreach (KeyValuePair<string, string> entry in reportResults)
            {
                Console.WriteLine(entry.Key + ": " + entry.Value);
            }

            void flattenJsonDoc(JsonElement element, Dictionary<string, string> dict, string path = "")
            // This method flattens a JSON document into a dictionary of strings
            // element: The root element of the JSON to be flattened
            // dict: The dictionary to receive the flattened elements
            {
                // JSON has two structures: a collection and an ordered list. All other elements are leaf nodes on the tree.
                switch (element.ValueKind)
                {
                    // A JSON object has properties that must be enumerated. 
                    case JsonValueKind.Object:
                        {
                            foreach (JsonProperty property in element.EnumerateObject())
                            {
                                flattenJsonDoc(property.Value, dict, path + "/" + property.Name);
                            }
                            break;
                        }

                    // A JSON array holds a number of JSON elements
                    case JsonValueKind.Array:
                        {
                            int index = 0;
                            foreach (JsonElement child in element.EnumerateArray())
                            {
                                flattenJsonDoc(child, dict, path + "/" + index);
                                index++;
                            }
                            break;
                        }

                    // The other elements are leaf nodes that can be added to the dictionary with their paths
                    case JsonValueKind.String:
                        {
                            dict.Add(path, element.ToString());
                            break;
                        }
                    case JsonValueKind.Number:
                        {
                            dict.Add(path, element.ToString());
                            break;
                        }
                    case JsonValueKind.Null:
                        {
                            dict.Add(path, "null");

                            break;
                        }
                    case JsonValueKind.False:
                        {
                            dict.Add(path, "false");
                            break;
                        }
                    case JsonValueKind.True:
                        {
                            dict.Add(path, "true");
                            break;
                        }
                    case JsonValueKind.Undefined:
                        {
                            dict.Add(path, "undefined");
                            break;
                        }
                    default:
                        {
                            dict.Add(path, element.ToString());
                            break;
                        }
                }
            }
        }
    }
}
