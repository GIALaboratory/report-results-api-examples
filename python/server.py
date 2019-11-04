from __future__ import print_function
import json
import os
import requests
import sys
from flask import Flask
from flask import render_template

app = Flask(__name__)

# Remember to set the environmental variables before running this example
REPORT_CHECK_API_KEY = os.getenv('REPORT_RESULTS_API_KEY')
REPORT_CHECK_API_ENDPOINT = os.getenv('REPORT_RESULTS_API_ENDPOINT')

if not REPORT_CHECK_API_KEY or not REPORT_CHECK_API_ENDPOINT:
    print("You must set the REPORT_CHECK_API_KEY and REPORT_CHECK_API_ENDPOINT environmental variables.")
    sys.exit(1)

# Read the graphql file
with open('report_check_query.graphql', 'r') as query_file:
    query = query_file.read()

# You must pass your API key in the authorization header
headers = {'Authorization': REPORT_CHECK_API_KEY}

@app.route('/')
def index():
    return render_template('index.html')

@app.route('/reports/<report_number>')
def show_report(report_number):
    variables = {'ReportNumber': report_number}
    try:
        # Pass a POST request with the query and variable payload
        r = requests.post(url=REPORT_CHECK_API_ENDPOINT, json={'query' : query, 'variables': variables}, headers=headers)
    except requests.exceptions.RequestException as e:
        print(e)
        return str(e)
        
    raw_data = json.dumps(r.json(), sort_keys=False, indent=2)
    print(raw_data)
    print("Returned in %0.3f" % r.elapsed.total_seconds())
    return render_template('report.html', data=r.json(), raw_data=raw_data)

if __name__ == '__main__':
    app.run()
