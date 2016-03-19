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

/* for testing the new room route manually */
router.get('/test-room-create', function (req, res, next) {
    createRoom("1234", "{hardcoded token goes here}", res);
});

var createRoom = function (userId, token, res) {
    var roomName = roomnameutil.getRoomName();
    var spotifyAuth = {token: token};
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

module.exports = router;
