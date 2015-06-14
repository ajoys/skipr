package me.skipr.skipr;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.andtinder.model.CardModel;
import com.andtinder.view.CardStackAdapter;
import com.andtinder.view.SimpleCardStackAdapter;
import com.squareup.picasso.Picasso;

import me.skipr.skipr.model.SongCard;

/**
 * Created by Navjot on 6/14/2015.
 */
public class StackAdapter extends CardStackAdapter {

    public StackAdapter(Context mContext) {
        super(mContext);
    }

    @Override
    public View getCardView(int position, CardModel model, View convertView, ViewGroup parent) {
        if(convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.song_card, parent, false);
            assert convertView != null;
        }

        ((TextView) convertView.findViewById(R.id.title)).setText(model.getTitle());
        ((TextView) convertView.findViewById(R.id.description)).setText(model.getDescription());

        String url = ((SongCard) model).getmImageUrl();
        if(!TextUtils.isEmpty(url)){
            Picasso.with(getContext()).load(url).into(((ImageView) convertView.findViewById(R.id.image)));
        }

        return convertView;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        FrameLayout wrapper = (FrameLayout) convertView;
        FrameLayout innerWrapper;
        View cardView;
        View convertedCardView;
        if (wrapper == null) {
            wrapper = new FrameLayout(getContext());
            wrapper.setBackgroundResource(R.drawable.sharp_card);
            if (shouldFillCardBackground()) {
                innerWrapper = new FrameLayout(getContext());
                innerWrapper.setBackgroundColor(getContext().getResources().getColor(android.R.color.background_light));
                wrapper.addView(innerWrapper);
            } else {
                innerWrapper = wrapper;
            }
            cardView = getCardView(position, getCardModel(position), null, parent);
            innerWrapper.addView(cardView);
        } else {
            if (shouldFillCardBackground()) {
                innerWrapper = (FrameLayout) wrapper.getChildAt(0);
            } else {
                innerWrapper = wrapper;
            }
            cardView = innerWrapper.getChildAt(0);
            convertedCardView = getCardView(position, getCardModel(position), cardView, parent);
            if (convertedCardView != cardView) {
                wrapper.removeView(cardView);
                wrapper.addView(convertedCardView);
            }
        }

        return wrapper;
    }

}
