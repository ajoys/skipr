package me.skipr.skipr.activity;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;

import org.json.JSONException;
import org.json.JSONObject;

import io.branch.referral.Branch;
import io.branch.referral.BranchError;
import me.skipr.skipr.R;
import me.skipr.skipr.api.Constants;
import me.skipr.skipr.api.RoomCreateResponse;
import me.skipr.skipr.api.RoomJoinResponse;
import me.skipr.skipr.api.SkiprApi;
import me.skipr.skipr.util.UserUtil;
import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by Navjot on 6/13/2015.
 */
public class SplashActivity extends Activity {

    private EditText mJoinEditText;
    private Button mJoinButton;
    private Button mCreateButton;
    private ProgressBar mProgressBar;

    private SkiprApi skiprApi;

    private static final int REQUEST_CODE_SPOTIFY = 1337;
    private static final String REDIRECT_URI = "skipr://callback";

    @Override
    protected void onStart() {
        super.onStart();
        setContentView(R.layout.activity_splash);

        UserUtil.generateUserId(this);

        Branch branch = Branch.getInstance(getApplicationContext());
        branch.initSession(new Branch.BranchReferralInitListener() {
            @Override
            public void onInitFinished(JSONObject referringParams, BranchError error) {

                if (error == null) {
                    //go straight to the main activity if theres a room already selected
                    if (referringParams.has("room_name")) {
                        try {
                            String roomName = referringParams.getString("room_name");
                            if (!roomName.isEmpty()) {
                                handleAutoJoinRoom(roomName);
                            }
                        } catch (JSONException e) {
                            //do nothing
                        }
                    }
                    // params are the deep linked params associated with the link that the user clicked before showing up
                    Log.i("BranchConfigTest", "deep link data: " + referringParams.toString());
                }
            }
        }, this.getIntent().getData(), this);

        mJoinButton = (Button) findViewById(R.id.splash_join_btn);
        mJoinEditText = (EditText) findViewById(R.id.splash_join_field);
        mCreateButton = (Button) findViewById(R.id.splash_create_btn);
        mProgressBar = (ProgressBar) findViewById(R.id.splash_loading);

        mJoinButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleJoinButtonClick();
            }
        });

        mJoinEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if(event.getAction() == EditorInfo.IME_ACTION_DONE){
                    handleJoinButtonClick();
                    return true;
                }
                return false;
            }
        });

        mCreateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //get spotify token
                showConnecting();
                AuthenticationRequest.Builder builder = new AuthenticationRequest.Builder(getResources().getString(R.string.client_id), AuthenticationResponse.Type.TOKEN, REDIRECT_URI);

                builder.setScopes(new String[]{"streaming"});
                AuthenticationRequest request = builder.build();

                AuthenticationClient.openLoginActivity(SplashActivity.this, REQUEST_CODE_SPOTIFY, request);
            }
        });

        RestAdapter restAdapter;
        restAdapter = new RestAdapter.Builder()
                .setEndpoint(Constants.URL)  //call your base url
                .build();
        skiprApi = restAdapter.create(SkiprApi.class);
    }

    private void handleAutoJoinRoom(final String roomName) {
        showConnecting();
        attemptToJoinRoom(roomName, new Callback<RoomJoinResponse>() {
            @Override
            public void success(RoomJoinResponse roomJoinResponse, Response response) {

                hideConnecting();
                launchMain(roomJoinResponse.id, roomName);
            }

            @Override
            public void failure(RetrofitError error) {

                //stop the auto join, and enable the UI so the user can join a room themselves
                hideConnecting();
            }
        });

    }

    private void handleJoinButtonClick(){
        final String roomName = mJoinEditText.getText().toString();

        if(roomName.equals("SwagDebug")){
            launchMain("1", "SwagDebug");
            return;
        }

        if(roomName.isEmpty()){
            mJoinEditText.setError("Please enter a valid room name");
            return;
        }
        showConnecting();
        attemptToJoinRoom(roomName, new Callback<RoomJoinResponse>() {
            @Override
            public void success(RoomJoinResponse roomJoinResponse, Response response) {

                hideConnecting();
                launchMain(roomJoinResponse.id, roomName);
            }

            @Override
            public void failure(RetrofitError error) {

                hideConnecting();
                mJoinEditText.setError("Unable to join room");
            }
        });
    }

    private void showConnecting(){
        mJoinEditText.setEnabled(false);
        mProgressBar.setVisibility(View.VISIBLE);
    }

    private void hideConnecting(){
        mJoinEditText.setEnabled(true);
        mProgressBar.setVisibility(View.GONE);
    }

    private void attemptToJoinRoom(String roomName, Callback<RoomJoinResponse> onRoomJoin) {
        skiprApi.join(UserUtil.getUserId(), roomName, onRoomJoin);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        hideConnecting();
        // Check if result comes from the correct activity
        if (requestCode == REQUEST_CODE_SPOTIFY) {
            AuthenticationResponse response = AuthenticationClient.getResponse(resultCode, intent);

            switch (response.getType()) {
                // Response was successful and contains auth token
                case TOKEN:
                    createRoom(response.getAccessToken());
                    break;

                // Auth flow returned an error
                case ERROR:
                    // Handle error response
                    break;

                // Most likely auth flow was cancelled
                default:
                    // Handle other cases
            }
        }
    }

    private void createRoom(final String token){
        skiprApi.create(UserUtil.getUserId(), token, new Callback<RoomCreateResponse>() {
            @Override
            public void success(RoomCreateResponse roomCreateResponse, Response response) {
                hideConnecting();
                launchMain(roomCreateResponse.id, roomCreateResponse.name, token);
            }

            @Override
            public void failure(RetrofitError error) {
                hideConnecting();
                Toast.makeText(SplashActivity.this, "Error: could not create room", Toast.LENGTH_LONG).show();
            }
        });
//        skiprApi.create(UserUtil.getUserId(), token, new Callback<String>() {
//            @Override
//            public void success(String s, Response response) {
//                hideConnecting();
//                launchMain(s);
//            }
//
//            @Override
//            public void failure(RetrofitError error) {
//                hideConnecting();
//                Toast.makeText(SplashActivity.this, "Error: could not create room", Toast.LENGTH_LONG).show();
//            }
//        });
    }

    private void launchMain(String roomId, String roomName){
        Intent launchMainIntent = new Intent(SplashActivity.this, MainActivity.class);
        launchMainIntent.putExtra("room_id", roomId);
        launchMainIntent.putExtra("room_name", roomName);
        launchMainIntent.putExtra("isPlayer", false);
        startActivity(launchMainIntent);
    }

    private void launchMain(String roomId, String roomName, String token){
        Intent launchMainIntent = new Intent(SplashActivity.this, MainActivity.class);
        launchMainIntent.putExtra("room_id", roomId);
        launchMainIntent.putExtra("room_name", roomName);
        launchMainIntent.putExtra("token", token);
        launchMainIntent.putExtra("isPlayer", true);
        startActivity(launchMainIntent);
    }

    @Override
    protected void onStop() {
        super.onStop();
        Branch.getInstance(getApplicationContext()).closeSession();
    }
}
