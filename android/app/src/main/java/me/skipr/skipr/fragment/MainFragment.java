package me.skipr.skipr.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by Navjot on 6/13/2015.
 */
public class MainFragment extends Fragment{

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    public static MainFragment newInstance(){
        MainFragment fragment  = new MainFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public static MainFragment newInstance(String groupId){
        MainFragment fragment  = new MainFragment();
        Bundle args = new Bundle();
        args.putString("group_id", groupId);
        fragment.setArguments(args);
        return fragment;
    }
}
