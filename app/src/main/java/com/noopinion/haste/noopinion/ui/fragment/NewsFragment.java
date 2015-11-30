package com.noopinion.haste.noopinion.ui.fragment;

import android.app.Fragment;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ViewAnimator;

import com.noopinion.haste.noopinion.R;
import com.noopinion.haste.noopinion.model.News;
import com.noopinion.haste.noopinion.provider.NewsProvider;
import com.noopinion.haste.noopinion.provider.Providers;
import com.noopinion.haste.noopinion.ui.adapter.NewsAdapter;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import icepick.Icepick;

/**
 * Created by Ivan Gusev on 30.11.2015.
 */
public final class NewsFragment extends Fragment implements NewsAdapter.Listener {

    public static final String TAG = NewsFragment.class.getName();

    @NonNull
    public static NewsFragment create() {
        return new NewsFragment();
    }

    private NewsProvider mNewsProvider;
    private NewsAdapter  mNewsAdapter;

    /**
     * View's state:
     */
    private static final int STATE_LOADING = 0;
    private static final int STATE_CONTENT = 1;
    private static final int STATE_ERROR   = 2;

    @IntDef(value = {STATE_LOADING, STATE_CONTENT, STATE_ERROR})
    @interface State {}

    @State
    int mViewState = STATE_LOADING;
    @State
    ArrayList<News> mNews;

    /**
     * NoView state variables:
     */
    boolean mSyncViewStateRequired;

    /**
     * Android Views:
     */
    @Bind(R.id.content_animator)
    ViewAnimator mContentAnimator;

    @Bind(R.id.recycler)
    RecyclerView mRecyclerView;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        mNewsProvider = Providers.createNewsProvider(getActivity());
        mNewsAdapter = new NewsAdapter(getActivity(), this);
    }

    @Override
    public void onSaveInstanceState(final Bundle outState) {
        Icepick.saveInstanceState(this, outState);

        super.onSaveInstanceState(outState);
    }

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        Icepick.restoreInstanceState(this, savedInstanceState);
        return inflater.inflate(R.layout.fragment_news, container, false);
    }

    @Override
    public void onViewCreated(final View view, final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ButterKnife.bind(this, view);

        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.setAdapter(mNewsAdapter);

        mNewsAdapter.setItems(mNews);
        mNewsAdapter.notifyDataSetChanged();

        syncViewState();
    }

    @Override
    public void onStart() {
        super.onStart();

        mNewsProvider.getNews(
                new NewsProvider.Callback() {
                    @Override
                    public void onNewsReceived(@NonNull final List<News> news, @NewsProvider.ErrorCode final int errorCode) {
                        getActivity().runOnUiThread(
                                new Runnable() {
                                    @Override
                                    public void run() {
                                        mNews = new ArrayList<>(news);

                                        mNewsAdapter.setItems(mNews);
                                        mNewsAdapter.notifyDataSetChanged();

                                        mViewState = STATE_CONTENT;
                                        syncViewState();
                                    }
                                }
                        );
                    }
                }
        );
    }

    @Override
    public void onDestroyView() {
        ButterKnife.unbind(this);

        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        mNewsAdapter = null;
        mNewsProvider = null;

        super.onDestroy();
    }

    @Override
    public void onLinkClick(@NonNull final String link) {
        final Intent browseIntent = new Intent(Intent.ACTION_VIEW).setData(Uri.parse(link));
        if (getActivity().getPackageManager().resolveActivity(browseIntent, 0) != null) {
            startActivity(browseIntent);
        }
    }

    private void syncViewState() {
        if (getView() == null) {
            mSyncViewStateRequired = true;
        } else {
            mContentAnimator.setDisplayedChild(mViewState);
        }
    }
}
