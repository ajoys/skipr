var mongoose = require('mongoose');
var Schema = mongoose.Schema;

var TrackSchema = new Schema({
    name: String,
    album: String,
    'image': String,
    'artist': String,
    'artist_id': String,
    'popularity': Number,
    'id': String,
    'votes': 0,
    'voters': [String]
});

var RoomSchema = new Schema({
        createdAt: { type: Date, default: Date.now },
        name: String,
        owner: String,
        token: String,
        users: [Number],
        tracks: [TrackSchema]
    }
);

RoomSchema.pre('save', function(next){
    this.name = this.name.toUpperCase();
    next();
});

exports.Track = mongoose.model('Track', TrackSchema);
exports.Room = mongoose.model('Room', RoomSchema);