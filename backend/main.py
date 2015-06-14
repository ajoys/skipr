from flask import Flask, request, send_from_directory, jsonify
from getSessionName import getRoomName
import spotify
from spotify import Spotify
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


@app.route('/room', methods=['POST', 'PUT'])
def createRoom():

    userId = request.form['userId']
    token = request.form['token']

    roomName = getRoomName()

    sp = Spotify(secrets.CLIENT_ID, secrets.CLIENT_SECRET, token)
    tracks, error = sp.getPlaylistTracks('spotify', sp.PLAYLIST_ID_TOP_50)
    
    if error == None:
        # Persist room in db
        resp = app.db.post(params={
            'createdAt' : int(time.time() * 1000),
            'name':roomName,
            'owner':userId,
            'token':token,
            'users':[userId],
            'tracks':tracks
            })

        roomId = resp.json()['id']
        return jsonify(id=roomId, name=roomName)
    else:
        return jsonify(error), error.get('status', 500)


@app.route('/room/<roomId>/join', methods=['PUT'])
def joinRoomById(roomId=None):

    userId = request.form['userId']
    response = addUserToRoom(roomId, userId)
    return response

@app.route('/join', methods=['PUT'])
def joinRoomByName():

    # params to join room with pretty name
    userId = request.form['userId']
    roomName = request.form['roomName'].upper()

    # design doc
    doc = app.db.design('query')
    # creating index to search database
    index = doc.search('s')
    # search database for room name
    doc = index.get(params={'query':'name:'+roomName})

    # check to see if valid room is passed
    if len(doc.json()['rows']) == 0:
        return jsonify({'error':'ERROR: ROOM NOT FOUND'}),404
    else:
        return addUserToRoom(doc.json()['rows'][0]['id'], userId)
    
def addUserToRoom(roomId, userId):

    listOfUsers = app.db.document(roomId).get().json()
    listOfUsers = listOfUsers['users']
   

    listOfUsers.append(userId)

    # insert user into database
    resp = app.db.document(roomId).merge({'users':listOfUsers})
    return jsonify(resp.json())


@app.route('/room/<roomId>/tracks', methods=['GET'])
def getTracksForRoom(roomId=None):
    room = app.db.document(roomId).get().json()
    if 'tracks' in room:
        return json.dumps(room['tracks']) # Dirty hack
        #return jsonify({'tracks':room['tracks']})
    else:
        return jsonify(room), 404

@app.route('/room/<roomId>/next', methods=['POST'])
def getHighestVotedTrack(roomId=None):
    room = app.db.document(roomId).get().json()
    if 'tracks' in room:

        if len(room['tracks']) == 0:
            return jsonify({'error':'Empty playlist'}), 400

        highestVotes = 0
        highestVotesIndex = 0
        i = -1
        for track in room['tracks']:
            i += 1
            if track['votes'] > highestVotes:
                highestVotesIndex = i
                highestVotes = track['votes']

        topTrack = room['tracks'].pop(highestVotesIndex)

        resp = app.db.document(roomId).merge({'tracks': room['tracks']})

        if resp.status_code == 201:
            return jsonify({'topTrack':topTrack})
        else:
            return jsonify(resp.json()), resp.status_code
    else:
        return jsonify(room), 404



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
