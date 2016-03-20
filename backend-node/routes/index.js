var express = require('express');
var roomnameutil = require('./room-name-util.js');
var spotify = require('./spotify.js');
var Schema = require('./schema.js');
var Track = Schema.Track;
var Room = Schema.Room;

var router = express.Router();

/* create new room POST */
router.post('/room', function (req, res, next) {
    var userId = req.body.userId;
    var token = req.body.token;
    createRoom(userId, token, res);
});

/* create new room PUT */
router.put('/room', function (req, res, next) {
    var userId = req.body.userId;
    var token = req.body.token;
    createRoom(userId, token, res);
});

/* join room by roomId */
router.put('/room/:roomId/join', function (req, res, next) {
    var roomId = req.params.roomId;
    var userId = req.body.userid;
    addUserToRoom(roomId, userId, function(room){
        res.send(room);
    });
});

/* join room by name - todo: test this route*/
router.put('/join', function (req, res, next) {
    var userId = req.body.userId;
    var roomName = req.body.roomName.toUpperCase();
    
    Room.findOne({name: roomName}, function(err, room){
        if(err){
            console.log(err);
        } else if(room){
            addUserToRoom(room.id, userId);
        } else {
            res.status(404).send("Error: cannot find room");
        }
    });
});

/* get room id from name */
router.get('/room/:roomName', function (req, res, next) {
    var roomName = req.params.roomName.toUpperCase();
    Room.findOne({name: roomName}, function(err, room){
        if(err){
            console.log(err);
        } else if(room){
            res.send(room.id);
        } else {
            res.status(404).send("Error: cannot find room");
        }
    });
});

/* get tracks for room */
router.get('/room/:roomId/track', function (req, res, next) {
    var roomId = req.params.roomId;
    var userId = req.body.userId;
    Room.findById(roomId, function(err, room){
        if(err){
            console.log(err);
        } else if(room){
            var tracks = [];
            if(userId){
                // todo: test this case
                for(var i = 0; i < room.tracks.length; i++){
                    if(room.tracks[i].voters.indexOf(userId) > -1){
                        tracks.push(room.tracks[i]);
                    }
                }
            } else{
                tracks = room.tracks;
            }
            res.send(room.tracks);
        } else {
            res.status(404).send("Error: cannot find room");
        }
    });
});

//TODO: test this
/* get highest voted tracks for room */
router.get('/room/:roomId/tracks/sorted', function (req, res, next) {
    var roomId = req.params.roomId;
    Room.findById(roomId, function(err, room){
        if(err){
            console.log(err);
        } else if(room){
            if(room.tracks && room.tracks.length > 0){

                // todo: perf? maybe extract this as a func with callback
                room.tracks.sort(function(a, b) {
                    return b.votes - a.votes;
                });
                res.send(room.tracks);

            } else {
                res.status(400).send("Error: empty playlist");
            }
            res.send(room.tracks);
        } else {
            res.status(404).send("Error: cannot find room");
        }
    });
});



var createRoom = function (userId, token, res) {
    var roomName = roomnameutil.getRoomName();
    var spotifyAuth = {token : token};
    spotify.getPlaylistTrack(spotifyAuth, "spotify", spotify.PLAYLIST_ID_TOP_50, function (tracks) {

        var room = new Room({
            name: roomName,
            owner : userId,
            token :token,
            users :[userId],
            tracks :tracks
        });

        room.save(function (err) {
            if (err) {
                console.log(err);
            } else {
                var roomId = room.id;
                res.send({id: roomId, name: roomName});
            }
        });

    }, console.log);
};


var addUserToRoom = function (roomId, userId, next) {
    Room.findByIdAndUpdate(
        roomId,
        {$push: {users: userId}},
        {safe: true, upsert: true, new : true},
        function(err, room) {
            if (err) {
                console.log(err);
            } else {
                next(room);
            }
        }
    );
};

module.exports = router;
