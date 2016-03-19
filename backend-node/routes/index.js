var express = require('express');
var roomnameutil = require('./room-name-util.js');
var spotify = require('./spotify.js');
var router = express.Router();

/* create new room POST */
router.post('/room', function (req, res, next) {
    var userId = req.body.userId;
    var token = req.body.token;
    createRoom(userId, token);
});

/* create new room PUT */
router.put('/room', function (req, res, next) {
    var userId = req.body.userId;
    var token = req.body.token;
    createRoom(userId, token);
});

var createRoom = function (userId, token) {
    var roomName = roomnameutil.getRoomName();
    var spotifyAuth = {token: token};
    spotify.getPlaylistTrack(spotifyAuth, "spotify", spotify.PLAYLIST_ID_TOP_50, function () {
        // add room to db
        // return id of room
        res.send("todo");
    }, console.log);
};

module.exports = router;
