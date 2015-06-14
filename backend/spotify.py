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

    def getPlaylist(self, userId, playlistId):
        url = 'https://api.spotify.com/v1/users/{0}/playlists/{1}'.format(userId, playlistId)
        resp = requests.get(url, headers=self.headers)
        return resp.text
        return resp.json()