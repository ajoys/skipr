package me.skipr.skipr.fragment;

import android.app.Fragment;
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
import com.andtinder.view.SimpleCardStackAdapter;

import org.w3c.dom.Text;

import me.skipr.skipr.R;
import me.skipr.skipr.model.SongCard;

/**
 * Created by Navjot on 6/13/2015.
 */
public class MainFragment extends Fragment{

    private String mRoomId;
    private String mRoomName;
    private CardContainer mCardContainer;
    private TextView mRoomNameTextview;
    private SimpleCardStackAdapter mCardAdapter;

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
        mCardAdapter = new SimpleCardStackAdapter(getActivity());
        addSongToQueue("1", "Title1", "Description goes here", "");
        addSongToQueue("2", "Title2", "Description goes here", "");
        addSongToQueue("3", "Title3", "Description goes here", "");
        mCardContainer.setAdapter(mCardAdapter);

        mRoomNameTextview = (TextView) rootView.findViewById(R.id.room_name);
        mRoomNameTextview.setText(mRoomName);


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
        final SongCard newCard = new SongCard(uniqueId, songTitle, songAuthor, ResourcesCompat.getDrawable(getResources(), R.drawable.picture1, null));
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
        mCardAdapter.notifyDataSetChanged();
    }

    private void reportDislike(String uniqueId) {
        //network call
    }

    private void reportLike(String uniqueId){
        //network call
    }

    private void requestSongs(){
        //network call
    }
}
