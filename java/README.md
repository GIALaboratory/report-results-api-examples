
# GIA Report Result API Code Example in Java

### Requirements
Requires Java 1.8+ and Maven

### Configure environmental variables
```
set REPORT_RESULTS_API_KEY=INSERT_YOUR_API_KEY_HERE
set REPORT_RESULTS_API_ENDPOINT=INSERT_THE_API_ENDPOINT_HERE

mvn clean package
java -jar target/java-0.0.2-jar-with-dependencies.jar
```

### Output

```
Looking up report number: 2141438172

JSON PAYLOAD TO BE POSTED TO THE SERVER
---------------------------------------
{
  "variables": {
    "ReportNumber": "2141438172"
  },
  "query": "query ReportQuery($ReportNumber: String!) {\r\n    getReport(report_number: $ReportNumber){\r\n        report_number\r\n        report_date\r\n        report_type\r\n        results {\r\n            __typename\r\n            ... on DiamondGradingReportResults {\r\n                shape_and_cutting_style\r\n                carat_weight\r\n                clarity_grade\r\n                color_grade\r\n            }\r\n        }\r\n        quota {\r\n            remaining\r\n        }\r\n    }\r\n}"
}

JSON RESPONSE RECEIVED FROM THE API
-----------------------------------
{"data":{"getReport":{"report_number":"2141438172","report_date":"September 01, 2019","report_type":"Diamond Dossier","results":{"__typename":"DiamondGradingReportResults","shape_and_cutting_style":"Round Brilliant","carat_weight":"1.01 carat","clarity_grade":"SI2","color_grade":"G"},"quota":{"remaining":-1}}}}

PARSED REPORT RESULTS
---------------------
/data/getReport/report_type: Diamond Dossier
/data/getReport/results/__typename: DiamondGradingReportResults
/data/getReport/results/carat_weight: 1.01 carat
/data/getReport/quota/remaining: -1
/data/getReport/report_number: 2141438172
/data/getReport/results/shape_and_cutting_style: Round Brilliant
/data/getReport/report_date: September 01, 2019
/data/getReport/results/clarity_grade: SI2
/data/getReport/results/color_grade: G

```