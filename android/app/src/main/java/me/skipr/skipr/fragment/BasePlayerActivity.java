package me.skipr.skipr.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.spotify.sdk.android.player.Config;
import com.spotify.sdk.android.player.ConnectionStateCallback;
import com.spotify.sdk.android.player.Player;
import com.spotify.sdk.android.player.PlayerNotificationCallback;
import com.spotify.sdk.android.player.PlayerState;
import com.spotify.sdk.android.player.Spotify;

import org.json.JSONException;
import org.json.JSONObject;

import me.skipr.skipr.R;
import me.skipr.skipr.api.Constants;
import me.skipr.skipr.api.SkiprApi;
import me.skipr.skipr.api.TopTrack;
import me.skipr.skipr.api.Track;
import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.android.AndroidLog;
import retrofit.client.Response;

/**
 * Created by Navjot on 6/14/2015.
 */
public class BasePlayerActivity extends Fragment implements
        PlayerNotificationCallback, ConnectionStateCallback {

    protected SkiprApi skiprApi;

    protected String mRoomId;
    protected String mRoomName;

    private Handler mHandler;
    private Player mPlayer;
    private String mToken;
    private Track mCurrentSong;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        RestAdapter restAdapter;
        restAdapter = new RestAdapter.Builder()
                .setEndpoint(Constants.URL)  //call your base url
                .setLogLevel(RestAdapter.LogLevel.FULL).setLog(new AndroidLog("YOUR_LOG_TAG"))
                .build();
        skiprApi = restAdapter.create(SkiprApi.class);
        mHandler = new Handler();
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    protected void startPlaying(String token){
        mToken = token;
        requestSong();
    }

    private void requestSong(){
        skiprApi.getFreshestTrack(mRoomId, new Callback<TopTrack>() {
            @Override
            public void success(TopTrack topTrack, Response response) {
                Track track = topTrack.topTrack;
                mCurrentSong = track;
                playSong(mCurrentSong.id);
                mHandler.postDelayed(updateSong, 10 * 1000);
            }

            @Override
            public void failure(RetrofitError error) {
                return;
            }
        });
    }

    private void playSong(final String songId){
        Config playerConfig = new Config(getActivity(), mToken, getActivity().getResources().getString(R.string.client_id));
        mPlayer = Spotify.getPlayer(playerConfig, this, new Player.InitializationObserver() {
            @Override
            public void onInitialized(Player player) {
                mPlayer.addConnectionStateCallback(BasePlayerActivity.this);
                mPlayer.addPlayerNotificationCallback(BasePlayerActivity.this);
                mPlayer.play("spotify:track:" + songId);
            }

            @Override
            public void onError(Throwable throwable) {
                Log.e("MainActivity", "Could not initialize player: " + throwable.getMessage());
            }
        });
    }

    private void deleteSong(String trackId){
        skiprApi.deleteTrack(mRoomId, trackId, new Callback<Response>() {
            @Override
            public void success(Response response, Response response2) {
                return;
            }

            @Override
            public void failure(RetrofitError error) {
                return;
            }
        });
    }

    private Runnable updateSong = new Runnable() {
        @Override
        public void run() {
            skiprApi.getFreshestTrack(mRoomId, new Callback<TopTrack>() {
                @Override
                public void success(TopTrack topTrack, Response response) {
                    final Track track = topTrack.topTrack;
                    if (!track.id.equals(mCurrentSong.id)) {
                        deleteSong(mCurrentSong.id);
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                switchSong(track.id);
                            }
                        });
                        mCurrentSong = track;
                    }
                    mHandler.postDelayed(updateSong, 10 * 1000);
                }

                @Override
                public void failure(RetrofitError error) {
                    mHandler.postDelayed(updateSong, 10*1000);
                    return;
                }
            });
        }
    };

    private void switchSong(String songId){
        mPlayer.pause();
        mPlayer.clearQueue();
        mPlayer.queue("spotify:track:" + songId);
        mPlayer.resume();
    }

    @Override
    public void onLoggedIn() {

    }

    @Override
    public void onLoggedOut() {

    }

    @Override
    public void onLoginFailed(Throwable throwable) {

    }

    @Override
    public void onTemporaryError() {

        return;
    }

    @Override
    public void onConnectionMessage(String s) {

    }

    @Override
    public void onPlaybackEvent(EventType eventType, PlayerState playerState) {
        Log.d("MainActivity", "Playback event received: " + eventType.name());
    }

    @Override
    public void onPlaybackError(ErrorType errorType, String s) {
        Log.d("MainActivity", "Playback error received: " + errorType.name());
    }

    @Override
    public void onDestroy() {
        Spotify.destroyPlayer(this);
        super.onDestroy();
    }
}
