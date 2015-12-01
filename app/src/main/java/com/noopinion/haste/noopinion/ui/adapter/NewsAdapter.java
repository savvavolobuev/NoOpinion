package com.noopinion.haste.noopinion.ui.adapter;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.hannesdorfmann.adapterdelegates.AbsAdapterDelegate;
import com.hannesdorfmann.adapterdelegates.ListDelegationAdapter;
import com.noopinion.haste.noopinion.R;
import com.noopinion.haste.noopinion.model.News;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by haste on 29.11.15.
 */
public final class NewsAdapter extends ListDelegationAdapter<List<News>> implements DelegationAdapter {

    public interface Listener {
        void onLinkClick(@NonNull String link);
    }

    private final Listener mListener;

    private final Set<News> mBucket = new HashSet<>();

    public NewsAdapter(@NonNull final Activity activity, @Nullable final Listener listener) {
        mListener = listener;
        setItems(new ArrayList<News>());
        setHasStableIds(true);

        delegatesManager.addDelegate(new NewsBucketDelegate(activity, this, 0));
        delegatesManager.addDelegate(new FullNewsDelegate(activity, this, 1));
        delegatesManager.addDelegate(new LessNewsDelegate(activity, this, 2));
    }

    public void addToBucket(@NonNull final News news) {
        final boolean hadBucket = hasBucket();

        mBucket.add(news);

        if (hadBucket) {
            notifyItemChanged(0);
        } else {
            notifyItemInserted(0);
        }
    }

    @Override
    public void onLinkClick(final int adapterPosition) {
        if (mListener != null) {
            mListener.onLinkClick(items.get(adapterPosition).getLink());
        }
    }

    @Override
    public boolean hasBucket() {
        return !mBucket.isEmpty();
    }

    @Override
    public int getBucketSize() {
        return mBucket.size();
    }

    @Override
    public void flushBucket() {

    }

    @Override
    public long getItemId(final int position) {
        if (hasBucket() && position == 0) {
            return -1;
        }

        return items.get(hasBucket() ? position - 1 : position).getId();
    }

    @Override
    public void setItems(@NonNull final List<News> items) {
        final List<News> existing = getItems();
        if (existing == null) {
            super.setItems(items);
        } else {
            existing.clear();
            existing.addAll(items);
            notifyDataSetChanged();
        }
    }
}

interface DelegationAdapter {
    void onLinkClick(final int adapterPosition);

    boolean hasBucket();

    int getBucketSize();

    void flushBucket();
}

abstract class BaseAdapterDelegate extends AbsAdapterDelegate<List<News>> {

    final DelegationAdapter mDelegationAdapter;
    final LayoutInflater    mInflater;

    BaseAdapterDelegate(@NonNull final Activity activity, @NonNull final DelegationAdapter delegationAdapter, final int viewType) {
        super(viewType);
        mDelegationAdapter = delegationAdapter;
        mInflater = activity.getLayoutInflater();
    }
}

final class NewsBucketDelegate extends BaseAdapterDelegate {

    NewsBucketDelegate(@NonNull final Activity activity, @NonNull final DelegationAdapter delegationAdapter, final int viewType) {
        super(activity, delegationAdapter, viewType);
    }

    @Override
    public boolean isForViewType(@NonNull final List<News> items, final int position) {
        return mDelegationAdapter.hasBucket() && position == 0;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(final ViewGroup parent) {
        return new NewsBucketViewHolder(mInflater.inflate(R.layout.item_news_bucket, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull final List<News> items, final int position, @NonNull final RecyclerView.ViewHolder holder) {
        NewsBucketViewHolder vh = (NewsBucketViewHolder) holder;
        vh.mBucketLabel.setText(vh.mBucketLabel.getResources().getString(R.string.open_news_bucket, mDelegationAdapter.getBucketSize()));
    }

    static class NewsBucketViewHolder extends RecyclerView.ViewHolder {

        @Bind(R.id.bucket_label)
        TextView mBucketLabel;

        public NewsBucketViewHolder(final View itemView) {
            super(itemView);

            ButterKnife.bind(this, itemView);
        }
    }
}

class LessNewsDelegate extends BaseAdapterDelegate {

    LessNewsDelegate(@NonNull final Activity activity, @NonNull final DelegationAdapter delegationAdapter, final int viewType) {
        super(activity, delegationAdapter, viewType);
    }

    @Override
    public boolean isForViewType(@NonNull final List<News> items, final int position) {
        return TextUtils.isEmpty((mDelegationAdapter.hasBucket() ? items.get(position - 1) : items.get(position)).getImage());
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(final ViewGroup parent) {
        return new NewsLessViewHolder(mInflater.inflate(R.layout.item_news_less, parent, false), mDelegationAdapter);
    }

    @Override
    public void onBindViewHolder(@NonNull final List<News> items, final int position, @NonNull final RecyclerView.ViewHolder holder) {
        redraw((NewsLessViewHolder) holder, items.get(position));
    }

    protected void redraw(@NonNull final NewsLessViewHolder vh, @NonNull final News n) {
        vh.mText.setText(n.getText());
        vh.mLink.setVisibility(TextUtils.isEmpty(n.getLink()) ? View.GONE : View.VISIBLE);
        vh.mLink.setTag(n.getLink());
    }

    static class NewsLessViewHolder extends RecyclerView.ViewHolder {

        @Bind(R.id.text)
        TextView  mText;
        @Bind(R.id.link)
        ImageView mLink;

        protected final DelegationAdapter mDelegationAdapter;

        public NewsLessViewHolder(View itemView, final DelegationAdapter delegationAdapter) {
            super(itemView);
            mDelegationAdapter = delegationAdapter;
            ButterKnife.bind(this, itemView);
        }

        @OnClick(R.id.link)
        public void onLinkClick() {
            mDelegationAdapter.onLinkClick(getAdapterPosition());
        }
    }
}

final class FullNewsDelegate extends LessNewsDelegate {

    private final Drawable mTintedErrorDrawable;

    FullNewsDelegate(@NonNull final Activity activity, @NonNull final DelegationAdapter delegationAdapter,
                     final int viewType) {
        super(activity, delegationAdapter, viewType);

        mTintedErrorDrawable = activity.getResources().getDrawable(R.drawable.ic_sad_face, activity.getTheme());
        if (mTintedErrorDrawable != null) {
            mTintedErrorDrawable.setTint(activity.getResources().getColor(R.color.primary));
        }
    }

    @Override
    public boolean isForViewType(@NonNull final List<News> news, final int position) {
        return !super.isForViewType(news, position);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(final ViewGroup parent) {
        return new NewsFullViewHolder(mInflater.inflate(R.layout.item_news_full, parent, false), mDelegationAdapter);
    }

    @Override
    protected void redraw(@NonNull final NewsLessViewHolder vh, @NonNull final News n) {
        super.redraw(vh, n);

        Picasso.with(vh.itemView.getContext()).load(n.getImage()).error(mTintedErrorDrawable).into(((NewsFullViewHolder) vh).mImage);
    }

    static final class NewsFullViewHolder extends NewsLessViewHolder {

        @Bind(R.id.image)
        ImageView mImage;

        public NewsFullViewHolder(final View itemView, final DelegationAdapter delegationAdapter) {
            super(itemView, delegationAdapter);
        }
    }
}
