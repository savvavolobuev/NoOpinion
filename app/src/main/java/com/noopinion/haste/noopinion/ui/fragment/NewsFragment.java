package com.noopinion.haste.noopinion.ui.fragment;

import android.app.ActivityOptions;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ViewAnimator;

import com.noopinion.haste.noopinion.R;
import com.noopinion.haste.noopinion.model.NewsCursor;
import com.noopinion.haste.noopinion.provider.NewsProvider;
import com.noopinion.haste.noopinion.provider.Providers;
import com.noopinion.haste.noopinion.ui.activity.ImageActivity;
import com.noopinion.haste.noopinion.ui.adapter.NewsAdapter;

import butterknife.Bind;
import butterknife.ButterKnife;
import icepick.Icepick;
import icepick.State;

/**
 * Created by Ivan Gusev on 30.11.2015.
 */
public final class NewsFragment extends Fragment implements NewsAdapter.Listener, SwipeRefreshLayout.OnRefreshListener {

    public static final String TAG = NewsFragment.class.getName();
    public static final String INTENT_IMAGE = "intent_image";
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
    private static final int STATE_ERROR   = 3;

    @IntDef(value = {STATE_LOADING, STATE_CONTENT, STATE_ERROR})
    @interface ViewState {}

    @ViewState
    @State
    int     mViewState = STATE_LOADING;
    @State
    int     mStart     = 0;
    @State
    boolean mLoading   = false;

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

    @Bind(R.id.swipe_refresh_layout)
    SwipeRefreshLayout mSwipeRefreshLayout;
    @Bind(R.id.recycler)
    RecyclerView       mRecyclerView;

    LinearLayoutManager mLayoutManager;

    private final RecyclerView.OnScrollListener mOnScrollListener = new RecyclerView.OnScrollListener() {

        @Override
        public void onScrolled(final RecyclerView recyclerView, final int dx, final int dy) {
            if (dy > 0) {
                final int visibleItemCount = mLayoutManager.getChildCount();
                final int totalItemCount = mLayoutManager.getItemCount();
                final int pastVisibleItems = mLayoutManager.findFirstVisibleItemPosition();

                if (!mLoading) {
                    if ((visibleItemCount + pastVisibleItems) >= totalItemCount) {
                        loadNews(mStart);
                    }
                }
            }
        }
    };

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        mNewsProvider = Providers.createNewsProvider(getActivity());

        mLayoutManager = new LinearLayoutManager(getActivity());
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
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mNewsAdapter);
        mRecyclerView.addOnScrollListener(mOnScrollListener);

        mSwipeRefreshLayout.setOnRefreshListener(this);

        syncViewState();
    }

    @Override
    public void onStart() {
        super.onStart();

        loadNews(0);
    }

    @Override
    public void onDestroyView() {
        mSwipeRefreshLayout.setOnRefreshListener(null);

        mRecyclerView.removeOnScrollListener(mOnScrollListener);

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
    public void onLinkClick(@NonNull final View view,@NonNull final String link) {
        final Intent browseIntent = new Intent(Intent.ACTION_VIEW).setData(Uri.parse(link));
        if (getActivity().getPackageManager().resolveActivity(browseIntent, 0) != null) {
            startActivity(browseIntent);
        }
    }

    @Override
    public void onImageClick(@NonNull final View view, @NonNull final String image) {
        Intent animIntent = new Intent(getActivity(),ImageActivity.class);
        animIntent.putExtra(INTENT_IMAGE, image);
        ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(getActivity(),view,"img");
        ActivityCompat.startActivity(getActivity(), animIntent, options.toBundle());
    }

    @Override
    public void onRefresh() {
        loadNews(0);
    }

    private void syncViewState() {
        if (getView() != null) {
            mContentAnimator.setDisplayedChild(mViewState);
            if (mSwipeRefreshLayout.isRefreshing()) {
                mSwipeRefreshLayout.setRefreshing(false);
            }
        }
    }

    private void loadNews(final int start) {
        mLoading = true;
        mNewsProvider.loadNews(
                start, 10, new NewsProvider.Callback() {
                    @Override
                    public void onNewsReceived(@NonNull final NewsCursor cursor, final int downloaded,
                                               @NewsProvider.ErrorCode final int errorCode) {
                        getActivity().runOnUiThread(
                                new Runnable() {
                                    @Override
                                    public void run() {
                                        mLoading = false;
                                        mNewsAdapter.changeCursor(cursor);

                                        if (downloaded == 0) {
                                            mNewsAdapter.disableProgress();
                                        } else {
                                            mNewsAdapter.enableProgress();
                                        }

                                        if (mStart == 0) {
                                            mStart = cursor.getCount();
                                        } else {
                                            mStart += downloaded;
                                        }

                                        mViewState = STATE_CONTENT;
                                        syncViewState();
                                    }
                                }
                        );
                    }
                }
        );
    }
}
