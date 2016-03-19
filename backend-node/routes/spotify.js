var https = require('https');

exports.PLAYLIST_ID_TOP_50 = "5FJXhjdILmRA2z5bvz4nzf";
exports.getPlaylistTrack = function (spotifyAuth, userId, playlistId, next, err) {
    var routeUri = "/v1/users/" + userId + "/playlists/" + playlistId;
    var options = {
        host: "api.spotify.com",
        method: "GET",
        path: routeUri,
        headers: {
            "Authorization": "Bearer " + spotifyAuth.token,
            "Accept-Encoding": "utf8"
        }
    };

    https.get(options, function (resp) {
        var str = '';
        resp.setEncoding('utf8');
        resp.on('data', function (data) {
            str += data;

        });
        resp.on('end', function () {
            var dataJson = JSON.parse(str);
            if (dataJson.tracks.items[0].track.name.indexOf("Intro") > -1) {
                dataJson.tracks.items.remove(0);
            }
            var tracks = [];
            console.log(dataJson.tracks.items);
            dataJson.tracks.items.forEach(
                function(playlistItem) {
                    tracks.push({
                        'name': playlistItem.track.name,
                        'album': playlistItem.track.album.name,
                        'image': playlistItem.track.album.images[0].url,
                        'artist': playlistItem.track.artists[0].name,
                        'artist_id': playlistItem.track.artists[0].id,
                        'popularity': playlistItem.track.popularity,
                        'id': playlistItem.track.id,
                        'votes': 0,
                        'voters': []
                    });
                }
            );
            next(tracks);
        });
    }).on("error", function (e) {
        err(e);
    });
};