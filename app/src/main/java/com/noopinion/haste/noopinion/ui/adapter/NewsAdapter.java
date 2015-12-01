package com.noopinion.haste.noopinion.ui.adapter;

import android.app.Activity;
import android.database.DataSetObserver;
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
import com.hannesdorfmann.adapterdelegates.AbsDelegationAdapter;
import com.noopinion.haste.noopinion.R;
import com.noopinion.haste.noopinion.model.News;
import com.noopinion.haste.noopinion.model.NewsCursor;
import com.squareup.picasso.Picasso;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by haste on 29.11.15.
 */
public final class NewsAdapter extends AbsDelegationAdapter<NewsCursor> implements DelegationAdapter {

    public interface Listener {
        void onLinkClick(@NonNull String link);
    }

    private final Listener mListener;

    private boolean         mDataValid;
    private DataSetObserver mDataSetObserver;

    public NewsAdapter(@NonNull final Activity activity, @Nullable final Listener listener) {
        mListener = listener;
        mDataSetObserver = new NotifyingDataSetObserver();

        setHasStableIds(true);

        delegatesManager.addDelegate(new FullNewsDelegate(activity, 0, this));
        delegatesManager.addDelegate(new LessNewsDelegate(activity, 1, this));
    }

    @Override
    public void onLinkClick(final int adapterPosition) {
        if (mDataValid && items != null && items.moveToPosition(adapterPosition)) {
            if (mListener != null) {
                mListener.onLinkClick(items.getLink());
            }
        }
    }

    @Override
    public int getItemCount() {
        if (mDataValid && items != null) {
            return items.getCount();
        }
        return 0;
    }

    @Override
    public long getItemId(int position) {
        if (mDataValid && items != null && items.moveToPosition(position)) {
            return items.getId();
        }
        return 0;
    }

    /**
     * Change the underlying cursor to a new cursor. If there is an existing cursor it will be closed.
     */
    public void changeCursor(@Nullable final NewsCursor cursor) {
        final NewsCursor old = swapCursor(cursor);
        if (old != null) {
            old.close();
        }
    }

    /**
     * Swap in a new Cursor, returning the old Cursor.  Unlike {@link #changeCursor(NewsCursor)}, the returned old Cursor is <em>not</em> closed.
     */
    @Nullable
    public NewsCursor swapCursor(@Nullable final NewsCursor newCursor) {
        if (newCursor == items) {
            return null;
        }

        final NewsCursor oldCursor = items;
        if (oldCursor != null && mDataSetObserver != null) {
            oldCursor.unregisterDataSetObserver(mDataSetObserver);
        }
        items = newCursor;
        if (items != null) {
            if (mDataSetObserver != null) {
                items.registerDataSetObserver(mDataSetObserver);
            }
            mDataValid = true;
            notifyDataSetChanged();
        } else {
            mDataValid = false;
            //There is no notifyDataSetInvalidated() method in RecyclerView.Adapter
            notifyDataSetChanged();
        }

        return oldCursor;
    }

    private class NotifyingDataSetObserver extends DataSetObserver {
        @Override
        public void onChanged() {
            super.onChanged();
            mDataValid = true;
            notifyDataSetChanged();
        }

        @Override
        public void onInvalidated() {
            super.onInvalidated();
            mDataValid = false;
            //There is no notifyDataSetInvalidated() method in RecyclerView.Adapter
            notifyDataSetChanged();
        }
    }
}

interface DelegationAdapter {
    void onLinkClick(final int adapterPosition);
}

final class NewsBucketDelegate extends AbsAdapterDelegate<List<News>> {

    public NewsBucketDelegate(final int viewType) {
        super(viewType);
    }

    @Override
    public boolean isForViewType(@NonNull final List<News> items, final int position) {
        return false;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(final ViewGroup parent) {
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull final List<News> items, final int position, @NonNull final RecyclerView.ViewHolder holder) {

    }
}

class LessNewsDelegate extends AbsAdapterDelegate<NewsCursor> {

    final DelegationAdapter mDelegationAdapter;
    final LayoutInflater    mInflater;

    public LessNewsDelegate(final Activity activity, final int viewType, final DelegationAdapter delegationAdapter) {
        super(viewType);
        mDelegationAdapter = delegationAdapter;
        mInflater = activity.getLayoutInflater();
    }

    @Override
    public boolean isForViewType(@NonNull final NewsCursor items, final int position) {
        return items.moveToPosition(position) && TextUtils.isEmpty(items.getImage());
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(final ViewGroup parent) {
        return new NewsLessViewHolder(mInflater.inflate(R.layout.item_news_less, parent, false), mDelegationAdapter);
    }

    @Override
    public void onBindViewHolder(@NonNull final NewsCursor items, final int position, @NonNull final RecyclerView.ViewHolder holder) {
        if (items.moveToPosition(position)) {
            redraw((NewsLessViewHolder) holder, items);
        }
    }

    protected void redraw(@NonNull final NewsLessViewHolder vh, @NonNull final NewsCursor cursor) {
        vh.mText.setText(cursor.getText());
        vh.mLink.setVisibility(TextUtils.isEmpty(cursor.getLink()) ? View.GONE : View.VISIBLE);
        vh.mLink.setTag(cursor.getLink());
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
    public boolean isForViewType(@NonNull final NewsCursor cursor, final int position) {
        return !super.isForViewType(cursor, position);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(final ViewGroup parent) {
        return new NewsFullViewHolder(mInflater.inflate(R.layout.item_news_full, parent, false), mDelegationAdapter);
    }

    @Override
    protected void redraw(@NonNull final NewsLessViewHolder vh, @NonNull final NewsCursor cursor) {
        super.redraw(vh, cursor);

        Picasso.with(vh.itemView.getContext()).load(cursor.getImage()).error(mTintedErrorDrawable).into(((NewsFullViewHolder) vh).mImage);
    }

    static final class NewsFullViewHolder extends NewsLessViewHolder {

        @Bind(R.id.image)
        ImageView mImage;

        public NewsFullViewHolder(final View itemView, final DelegationAdapter delegationAdapter) {
            super(itemView, delegationAdapter);
        }
    }
}
