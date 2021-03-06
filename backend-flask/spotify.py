import requests


class Spotify:
    '''
    Thin wrapper around the Spotify REST API using python-requests
    '''

    PLAYLIST_ID_TOP_50 = '5FJXhjdILmRA2z5bvz4nzf'

    def __init__(self, id, secret, bearerToken):
        self.id = id
        self.secret = secret
        self.token = bearerToken
        self.headers = {
            'Authorization': 'Bearer {}'.format(bearerToken),
            'Accept-Encoding': 'gzip',
        }

    def getPlaylistTracks(self, userId, playlistId):
        '''
        Get a list of tracks from the specified playlist
        '''
        url = 'https://api.spotify.com/v1/users/{0}/playlists/{1}'.format(userId, playlistId)
        resp = requests.get(url, headers=self.headers)

        if resp.status_code == 200:

            resp = resp.json()

            # Remove any intro track if it exists
            if 'Intro' in resp['tracks']['items'][0]['track']['name']:
                resp['tracks']['items'].pop(0)

            # Only extract necessary information
            tracks = []
            for playlistItem in resp['tracks']['items']:
                tracks.append({
                    'name': playlistItem['track']['name'],
                    'album': playlistItem['track']['album']['name'],
                    'image': playlistItem['track']['album']['images'][0]['url'],
                    'artist': playlistItem['track']['artists'][0]['name'],
                    'artist_id': playlistItem['track']['artists'][0]['id'],
                    'popularity': playlistItem['track']['popularity'],
                    'id': playlistItem['track']['id'],
                    'votes': 0,
                    'voters': []
                })

            return tracks, None
        
        else:
            return [], resp.json()['error']
