package me.skipr.skipr.fragment;

import android.app.Fragment;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.content.res.ResourcesCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.andtinder.model.CardModel;
import com.andtinder.model.Orientations;
import com.andtinder.view.CardContainer;
import com.google.gson.Gson;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;
import java.util.List;

import me.skipr.skipr.R;
import me.skipr.skipr.StackAdapter;
import me.skipr.skipr.api.Constants;
import me.skipr.skipr.api.SkiprApi;
import me.skipr.skipr.api.Track;
import me.skipr.skipr.model.SongCard;
import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by Navjot on 6/13/2015.
 */
public class MainFragment extends Fragment{

    private String mRoomId;
    private String mRoomName;
    private CardContainer mCardContainer;
    private TextView mRoomNameTextview;
    private StackAdapter mCardAdapter;
    private SkiprApi skiprApi;
    private Drawable defaultDrawable;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        mRoomId = getArguments().getString("room_id", "");
        mRoomName = getArguments().getString("room_name", "");

        // at this point, the user should have selected a group to join
        if(TextUtils.isEmpty(mRoomId) || TextUtils.isEmpty(mRoomName)){
            Toast.makeText(getActivity(), "Error: not in a room", Toast.LENGTH_SHORT).show();
            getActivity().finish();
        }

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        mCardContainer = (CardContainer) rootView.findViewById(R.id.card_container);
        mCardContainer.setOrientation(Orientations.Orientation.Ordered);
        mCardAdapter = new StackAdapter(getActivity());

        mRoomNameTextview = (TextView) rootView.findViewById(R.id.room_name);
        mRoomNameTextview.setText(mRoomName);

        RestAdapter restAdapter;
        restAdapter = new RestAdapter.Builder()
                .setEndpoint(Constants.URL)  //call your base url
                .build();
        skiprApi = restAdapter.create(SkiprApi.class);

        defaultDrawable = ResourcesCompat.getDrawable(getResources(), R.drawable.picture1, null);

        requestSongs();
        return rootView;
    }

    public static MainFragment newInstance(String roomId, String roomName){
        MainFragment fragment = new MainFragment();
        Bundle args = new Bundle();
        args.putString("room_id", roomId);
        args.putString("room_name", roomName);
        Log.d("Skipr", roomId);
        fragment.setArguments(args);
        return fragment;
    }

    private void addSongToQueue(String uniqueId, String songTitle, String songAuthor, String image){
        final SongCard newCard = new SongCard(uniqueId, songTitle, songAuthor, defaultDrawable, image);
        newCard.setOnCardDimissedListener(new CardModel.OnCardDimissedListener() {
            @Override
            public void onLike() {
                Log.d("liked", newCard.getTitle());
                reportLike(newCard.getUniqueId());
            }

            @Override
            public void onDislike() {
                Log.d("disliked", newCard.getTitle());
                reportDislike(newCard.getUniqueId());
            }
        });
        mCardAdapter.add(newCard);
    }

    private void reportDislike(String uniqueId) {
        //network call
    }

    private void reportLike(String uniqueId){
        //network call
    }

    private void requestSongs(){
        final String roomId = mRoomId;
        skiprApi.tracks(roomId, new Callback<List<Track>>() {
            @Override
            public void success(List<Track> tracks, Response response) {
                for(Track track : tracks){
                    addSongToQueue(track.id, track.name, track.artist, track.image);
                }
                mCardContainer.setAdapter(mCardAdapter);
            }

            @Override
            public void failure(RetrofitError error) {
                return;
            }
        });
    }
}

class TrackDeserializer implements JsonDeserializer<Track>
{
    @Override
    public Track deserialize(JsonElement je, Type type, JsonDeserializationContext jdc)
            throws JsonParseException
    {
        // Get the "tracks" element from the parsed JSON
        JsonElement content = je.getAsJsonObject().get("tracks");

        // Deserialize it. You use a new instance of Gson to avoid infinite recursion
        // to this deserializer
        return new Gson().fromJson(content, Track.class);

    }
}