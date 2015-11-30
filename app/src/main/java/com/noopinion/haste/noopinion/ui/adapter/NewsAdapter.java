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

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by haste on 29.11.15.
 */
public class NewsAdapter extends ListDelegationAdapter<List<News>> implements DelegationAdapter {

    public interface Listener {
        void onLinkClick(@NonNull String link);
    }

    private final Listener mListener;

    public NewsAdapter(@NonNull final Activity activity, @Nullable final Listener listener) {
        mListener = listener;

        setHasStableIds(true);

        delegatesManager.addDelegate(new FullNewsDelegate(activity, 0, this));
        delegatesManager.addDelegate(new LessNewsDelegate(activity, 1, this));
    }

    @Override
    public long getItemId(final int position) {
        return items.get(position).getId();
    }

    @Override
    public void onLinkClick(final int adapterPosition) {
        if (mListener != null) {
            mListener.onLinkClick(items.get(adapterPosition).getLink());
        }
    }
}

interface DelegationAdapter {
    void onLinkClick(final int adapterPosition);
}

class LessNewsDelegate extends AbsAdapterDelegate<List<News>> {

    final DelegationAdapter mDelegationAdapter;
    final LayoutInflater    mInflater;

    public LessNewsDelegate(final Activity activity, final int viewType, final DelegationAdapter delegationAdapter) {
        super(viewType);
        mDelegationAdapter = delegationAdapter;
        mInflater = activity.getLayoutInflater();
    }

    @Override
    public boolean isForViewType(@NonNull final List<News> items, final int position) {
        return TextUtils.isEmpty(items.get(position).getImage());
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(final ViewGroup parent) {
        return new NewsLessViewHolder(mInflater.inflate(R.layout.item_news_less, parent, false), mDelegationAdapter);
    }

    @Override
    public final void onBindViewHolder(@NonNull final List<News> items, final int position, @NonNull final RecyclerView.ViewHolder holder) {
        final NewsLessViewHolder vh = (NewsLessViewHolder) holder;
        final News n = items.get(position);
        redraw(vh, n);
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

    public FullNewsDelegate(final Activity activity, final int viewType, final DelegationAdapter delegationAdapter) {
        super(activity, viewType, delegationAdapter);

        mTintedErrorDrawable = activity.getResources().getDrawable(R.drawable.ic_sad_face, activity.getTheme());
        if (mTintedErrorDrawable != null) {
            mTintedErrorDrawable.setTint(activity.getResources().getColor(R.color.primary));
        }
    }

    @Override
    public boolean isForViewType(@NonNull final List<News> items, final int position) {
        return !super.isForViewType(items, position);
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
