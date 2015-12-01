package com.noopinion.haste.noopinion.ui.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.noopinion.haste.noopinion.R;
import com.noopinion.haste.noopinion.ui.adapter.NewsAdapter;

/**
 * Created by haste on 29.11.15.
 */
public class MainFragment extends Fragment {

    private NewsAdapter mAdapter;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.main_fragment, null);
        return view;
    }

    @Override
    public void onViewCreated(final View view, final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mAdapter = new NewsAdapter(getActivity(),null);
        //mAdapter.
    }
}
