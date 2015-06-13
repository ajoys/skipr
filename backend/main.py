from flask import Flask
import os
import requests
import secrets


app = Flask(__name__)

@app.route('/')
def hello():
    return 'Hello World! Skipr Test :D'

port = os.getenv('VCAP_APP_PORT', '5000')
if __name__ == "__main__":
    app.run(host='0.0.0.0', port=int(port))