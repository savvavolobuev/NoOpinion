package com.noopinion.haste.noopinion.ui.fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ViewAnimator;

import com.noopinion.haste.noopinion.R;
import com.noopinion.haste.noopinion.model.News;
import com.noopinion.haste.noopinion.provider.NewsProvider;
import com.noopinion.haste.noopinion.provider.Providers;
import com.noopinion.haste.noopinion.ui.adapter.NewsAdapter;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import icepick.Icepick;

/**
 * Created by Ivan Gusev on 30.11.2015.
 */
public final class NewsFragment extends Fragment implements NewsAdapter.Listener, AppBarLayout.OnOffsetChangedListener {

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
    private static final int STATE_CONTENT = 0;
    private static final int STATE_LOADING = 1;
    private static final int STATE_ERROR   = 2;

    @IntDef(value = {STATE_LOADING, STATE_CONTENT, STATE_ERROR})
    @interface State {}

    @State
    int mViewState = STATE_LOADING;

    /**
     * NoView state variables:
     */
    boolean mSyncViewStateRequired;

    /**
     * Android Views:
     */
    @Bind(R.id.content_animator)
    ViewAnimator mContentAnimator;

    @Bind(R.id.coordinator)
    CoordinatorLayout mCoordinatorLayout;
    @Bind(R.id.appbar)
    AppBarLayout      mAppBarLayout;
    @Bind(R.id.title)
    TextView          mTitleView;

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

        mTitleView.setText(R.string.app_name);

        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.setAdapter(mNewsAdapter);

        syncViewState();
    }

    @Override
    public void onStart() {
        super.onStart();

        mNewsProvider.loadNews(
                0, 20, new NewsProvider.Callback() {
                    @Override
                    public void onNewsReceived(@NonNull final List<News> news, @NewsProvider.ErrorCode final int errorCode) {
                        getActivity().runOnUiThread(
                                new Runnable() {
                                    @Override
                                    public void run() {
                                        mNewsAdapter.setItems(news);
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
    public void onResume() {
        super.onResume();

        mAppBarLayout.addOnOffsetChangedListener(this);
    }

    @Override
    public void onPause() {
        mAppBarLayout.removeOnOffsetChangedListener(this);

        super.onPause();
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

    @Override
    public void onOffsetChanged(final AppBarLayout appBarLayout, final int verticalOffset) {
        Log.d(TAG, "");
    }

    private void syncViewState() {
        if (getView() == null) {
            mSyncViewStateRequired = true;
        } else {
            mContentAnimator.setDisplayedChild(mViewState);
        }
    }
}
