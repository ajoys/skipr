from flask import Flask, request, send_from_directory, jsonify
from flaskutils import crossdomain
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
@crossdomain(origin='*')
def joinRoomByName():

    # params to join room with pretty name
    userId = request.form['userId']
    roomName = request.form['roomName'].upper()

    # design doc
    doc = app.db.design('query')
    # creating index to search database
    index = doc.search('searchRoom')
    # search database for room name
    doc = index.get(params={'query':'name:'+roomName})

    # check to see if valid room is passed
    if len(doc.json()['rows']) == 0:
        return jsonify({'error':'ERROR: ROOM NOT FOUND'}),404
    else:
        return addUserToRoom(doc.json()['rows'][0]['id'], userId)

@app.route('/room/<roomName>', methods=['GET'])
@crossdomain(origin='*')
def getRoomIdFromName(roomName=None):

    # params to join room with pretty name
    if roomName is not None:
        roomName = roomName.upper()

    # design doc
    doc = app.db.design('query')
    # creating index to search database
    index = doc.search('searchRoom')
    # search database for room name
    doc = index.get(params={'query':'name:'+roomName})

    # check to see if valid room is passed
    if len(doc.json()['rows']) == 0:
        return jsonify({'error':'ERROR: ROOM NOT FOUND'}),404
    else:
        return doc.json()['rows'][0]['id']
    
def addUserToRoom(roomId, userId):

    listOfUsers = app.db.document(roomId).get().json()
    listOfUsers = listOfUsers['users']
   

    listOfUsers.append(userId)

    # insert user into database
    resp = app.db.document(roomId).merge({'users':listOfUsers})
    return jsonify(resp.json())


@app.route('/room/<roomId>/tracks', methods=['GET'])
def getTracksForRoom(roomId=None):

    # Get the optional userId parameter
    userId = request.args.get('userId')

    room = app.db.document(roomId).get().json()
    if 'tracks' in room:

        # Filter by voter if userId was provided
        if userId != None:
            room['tracks'] = [x for x in room['tracks'] if not userId in x['voters']]

        return json.dumps(room['tracks']) # Dirty hack
        #return jsonify({'tracks':room['tracks']})
    else:
        return jsonify(room), 404


@app.route('/room/<roomId>/tracks/sorted', methods=['GET'])
@crossdomain(origin='*')
def getHighestVotedTracks(roomId=None):
    room = app.db.document(roomId).get().json()
    if 'tracks' in room:

        if len(room['tracks']) == 0:
            return jsonify({'error':'Empty playlist'}), 400

        room['tracks'].sort(key=lambda x: x['votes'], reverse=True)

        return jsonify({'tracks':room['tracks']})
    else:
        return jsonify(room), 404


@app.route('/room/<roomId>/next', methods=['GET'])
def getHighestVotedTrack(roomId=None):
    room = app.db.document(roomId).get().json()
    if 'tracks' in room:

        if len(room['tracks']) == 0:
            return jsonify({'error':'Empty playlist'}), 400

        room['tracks'].sort(key=lambda x: x['votes'], reverse=True)

        # Read the top track
        topTrack = room['tracks'][0]
        return jsonify({'topTrack':topTrack})

    else:
        return jsonify(room), 404

@app.route('/room/<roomId>/tracks/<trackId>', methods=['DELETE'])
def deleteTrack(roomId=None, trackId=None):
    room = app.db.document(roomId).get().json()
    if 'tracks' in room:

        # Find the track index
        index = next((i for i, track in enumerate(room['tracks']) if track['id'] == trackId), -1)

        if index == -1:
            return jsonify({'warning':'Track not found'}), 200

        # Remove the specified track
        room['tracks'].pop(index)
        resp = app.db.document(roomId).merge({'tracks': room['tracks']})
        
        if resp.status_code == 201:
            return jsonify(resp.json())
        else:
            return jsonify(resp.json()), resp.status_code

    else:
        return jsonify(room), 404

@app.route('/room/<roomId>/vote', methods=['PUT'])
def voteForTracks(roomId=None):

    # Get parameters from raw json
    if not request.json:
        return jsonify({'error':'Missing parameters'}), 400

    userId = request.json['nameValuePairs']['userId']
    ratedTracks = request.json['nameValuePairs']['tracks']['nameValuePairs']


    room = app.db.document(roomId).get().json()
    if 'tracks' in room:

        # Loop over playlist and vote for tracks
        changed = 0
        for track in room['tracks']:
            if track['id'] in ratedTracks:
                if userId not in track['voters']:
                    track['votes'] += ratedTracks[track['id']]
                    track['voters'].append(userId)
                    changed += 1

        # Save updated tracks
        resp = app.db.document(roomId).merge({'tracks': room['tracks']})
        data = resp.json()
        data['votedFor'] = changed

        return jsonify(data), resp.status_code

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
