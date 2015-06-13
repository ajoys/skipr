from flask import Flask, request, send_from_directory
import flask
import os
import json
import time
import requests
import secrets
import cloudant


app = Flask(__name__, static_url_path='')

@app.route('/')
def root():
    return app.send_static_file('index.html')

@app.route('/hello')
def hello():
    return 'Hello World! (Skipr Test)'


@app.route('/room', methods=['POST'])
def createRoom():

    # Persist room in db
    resp = app.db.post(params={
        'createdAt' : int(time.time() * 1000)
        })

    roomId = resp.json()['id']

    return flask.jsonify(id=roomId)


def setupDB(services):
    '''
    Setup a cloudant DB object
    '''
    dbUser = services['cloudantNoSQLDB'][0]['credentials']['username']
    dbPass = services['cloudantNoSQLDB'][0]['credentials']['password']
    account = cloudant.Account(dbUser)
    
    login = account.login(dbUser, dbPass)
    assert login.status_code == 200

    # Select the DB
    db = account.database('skipr')
    response = db.get().json()

    # Ensure DB exists
    if 'error' in response and response['error'] == 'not_found':
        response = db.put().json()
        assert login.status_code == 200

    print('Successfully connected to database')
    return db


port = os.getenv('VCAP_APP_PORT', '5000')
if __name__ == "__main__":
    # Load service parameters from env or secrets
    services = json.loads(os.getenv('VCAP_SERVICES', secrets.VCAP_SERVICES))
    app.db = setupDB(services)
    app.run(host='0.0.0.0', port=int(port), debug=True)
