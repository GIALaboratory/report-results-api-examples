### GIA Report Results API cURL Example

``` bash
# Configure environmental variables
set REPORT_RESULTS_API_KEY=INSERT_YOUR_API_KEY_HERE
set REPORT_RESULTS_API_ENDPOINT=INSERT_THE_API_ENDPOINT_HERE

curl -d "@request_payload.json" -H "Content-Type: application/json" -H "Authorization: %REPORT_RESULTS_API_KEY%" %REPORT_RESULTS_API_ENDPOINT%
```

``` bash
{"data":{"report1":{"report_number":"2141438171","report_date":"September 01, 2019","report_type":"Diamond Dossier","results":{"carat_weight":"0.51 carat","color_grade":"E","clarity_grade":"VS2","cut_grade":null}}}}
```