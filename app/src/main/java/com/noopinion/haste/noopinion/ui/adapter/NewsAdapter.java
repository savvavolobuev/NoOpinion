package com.noopinion.haste.noopinion.ui.adapter;

import android.app.Activity;
import android.database.DataSetObserver;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.hannesdorfmann.adapterdelegates.AbsAdapterDelegate;
import com.hannesdorfmann.adapterdelegates.AbsDelegationAdapter;
import com.noopinion.haste.noopinion.R;
import com.noopinion.haste.noopinion.model.NewsCursor;
import com.noopinion.haste.noopinion.utils.DateUtils;
import com.squareup.picasso.Picasso;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by haste on 29.11.15.
 */
public final class NewsAdapter extends AbsDelegationAdapter<NewsCursor> implements DelegationAdapter {

    public interface Listener {
        void onLinkClick(View view, @NonNull String link);

        void onImageClick(View view, @NonNull String image);
    }

    private final Listener mListener;

    private boolean mDataValid;
    private DataSetObserver mDataSetObserver;

    private boolean mProgressEnabled = true;

    public NewsAdapter(@NonNull final Activity activity, @Nullable final Listener listener) {
        mListener = listener;
        setHasStableIds(true);
        mDataSetObserver = new NotifyingDataSetObserver();

        delegatesManager.addDelegate(new FullNewsDelegate(activity, this, 0));
        delegatesManager.addDelegate(new LessNewsDelegate(activity, this, 1));
        delegatesManager.addDelegate(new ProgressDelegate(activity, this, 2));
    }

    @Override
    public int getItemViewType(final int position) {
        if (items == null) {
            return 2;
        }
        return super.getItemViewType(position);
    }

    public void enableProgress() {
        mProgressEnabled = true;

        if (mDataValid && items != null) {
            notifyDataSetChanged();
        }
    }

    public void disableProgress() {
        mProgressEnabled = false;

        if (mDataValid && items != null) {
            notifyDataSetChanged();
        }
    }

    private boolean hasProgress() {
        return mProgressEnabled;
    }

    @Override
    public void onLinkClick(@NonNull final View view, final int adapterPosition) {
        if (mDataValid && items != null && items.moveToPosition(adapterPosition)) {
            if (mListener != null) {
                mListener.onLinkClick(view, items.getLink());
            }
        }
    }

    @Override
    public void onImageClick(@NonNull final View view, final int adapterPosition) {
        if (mDataValid && items != null && items.moveToPosition(adapterPosition)) {
            if (mListener != null) {
                mListener.onImageClick(view, items.getImage());
            }
        }
    }

    @Override
    public int getItemCount() {
        int count = hasProgress() ? 1 : 0;
        if (mDataValid && items != null) {
            count += items.getCount();
        }

        return count;
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
    void onLinkClick(View view, final int adapterPosition);

    void onImageClick(View view, final int adapterPosition);
}

abstract class BaseAdapterDelegate extends AbsAdapterDelegate<NewsCursor> {

    final DelegationAdapter mDelegationAdapter;
    final LayoutInflater mInflater;

    BaseAdapterDelegate(@NonNull final Activity activity, @NonNull final DelegationAdapter delegationAdapter, final int viewType) {
        super(viewType);
        mDelegationAdapter = delegationAdapter;
        mInflater = activity.getLayoutInflater();
    }
}

abstract class NewsItemDelegate extends BaseAdapterDelegate {

    NewsItemDelegate(@NonNull final Activity activity, @NonNull final DelegationAdapter delegationAdapter, final int viewType) {
        super(activity, delegationAdapter, viewType);
    }
}

class LessNewsDelegate extends NewsItemDelegate {

    LessNewsDelegate(@NonNull final Activity activity, @NonNull final DelegationAdapter delegationAdapter, final int viewType) {
        super(activity, delegationAdapter, viewType);
    }

    @Override
    public boolean isForViewType(@NonNull final NewsCursor cursor, final int position) {
        if (cursor.moveToPosition(position)) {
            return TextUtils.isEmpty(cursor.getImage());
        }
        return false;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(final ViewGroup parent) {
        return new NewsLessViewHolder(mInflater.inflate(R.layout.item_news_less, parent, false), mDelegationAdapter);
    }

    @Override
    public void onBindViewHolder(@NonNull final NewsCursor cursor, final int position, @NonNull final RecyclerView.ViewHolder holder) {
        cursor.moveToPosition(position);
        redraw((NewsLessViewHolder) holder, cursor);
    }

    protected void redraw(@NonNull final NewsLessViewHolder vh, @NonNull final NewsCursor cursor) {
        vh.mText.setText(cursor.getText());
        vh.mLink.setVisibility(TextUtils.isEmpty(cursor.getLink()) ? View.GONE : View.VISIBLE);
        vh.mLink.setTag(cursor.getLink());

        String dateString = DateUtils.parseDate(cursor.getDate());
        vh.date.setText(dateString);
        vh.date.setVisibility(TextUtils.isEmpty(dateString) ? View.GONE : View.VISIBLE);
    }

    static class NewsLessViewHolder extends RecyclerView.ViewHolder {

        @Bind(R.id.text)
        TextView mText;
        @Bind(R.id.link)
        ImageView mLink;
        @Bind(R.id.date)
        TextView date;

        protected final DelegationAdapter mDelegationAdapter;

        public NewsLessViewHolder(View itemView, final DelegationAdapter delegationAdapter) {
            super(itemView);
            mDelegationAdapter = delegationAdapter;
            ButterKnife.bind(this, itemView);
        }

        @OnClick(R.id.link)
        public void onLinkClick() {
            mDelegationAdapter.onLinkClick(mLink, getAdapterPosition());
        }
    }
}

final class FullNewsDelegate extends LessNewsDelegate {

    private Drawable mTintedErrorDrawable;

    FullNewsDelegate(@NonNull final Activity activity, @NonNull final DelegationAdapter delegationAdapter,
                     final int viewType) {
        super(activity, delegationAdapter, viewType);

        mTintedErrorDrawable = ResourcesCompat.getDrawable(activity.getResources(), R.drawable.ic_sad_face, activity.getTheme());
        if (mTintedErrorDrawable != null) {
            mTintedErrorDrawable = new ColorDrawable(Color.TRANSPARENT);
        }
    }

    @Override
    public boolean isForViewType(@NonNull final NewsCursor cursor, final int position) {
        if (cursor.moveToPosition(position)) {
            return !TextUtils.isEmpty(cursor.getImage());
        }
        return false;

    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(final ViewGroup parent) {
        return new NewsFullViewHolder(mInflater.inflate(R.layout.item_news_full, parent, false), mDelegationAdapter);
    }

    @Override
    protected void redraw(@NonNull final NewsLessViewHolder vh, @NonNull final NewsCursor cursor) {
        super.redraw(vh, cursor);
        if (TextUtils.isEmpty(cursor.getLink()) && TextUtils.isEmpty(cursor.getText())) {
            ((NewsFullViewHolder) vh).line.setVisibility(View.GONE);
        } else {
            ((NewsFullViewHolder) vh).line.setVisibility(View.VISIBLE);
        }
        ((NewsFullViewHolder) vh).space.setVisibility(View.GONE);
        if (!TextUtils.isEmpty(cursor.getImage())) {
            if (!TextUtils.isEmpty(cursor.getLink()) || !TextUtils.isEmpty(cursor.getText())) {
                ((NewsFullViewHolder) vh).space.setVisibility(View.VISIBLE);
            }
        }
        Picasso.with(vh.itemView.getContext()).load(cursor.getImage()).error(mTintedErrorDrawable).into(((NewsFullViewHolder) vh).mImage);
        ((NewsFullViewHolder) vh).mProgressBar.setVisibility(View.VISIBLE);
    }

    static final class NewsFullViewHolder extends NewsLessViewHolder {

        @Bind(R.id.image)
        ImageView mImage;
        @Bind(R.id.line)
        LinearLayout line;
        @Bind(R.id.separator)
        View space;
        @Bind (R.id.imageProgress)
        ProgressBar mProgressBar;


        public NewsFullViewHolder(final View itemView, final DelegationAdapter delegationAdapter) {
            super(itemView, delegationAdapter);
        }

        @OnClick(R.id.image)
        public void onImageClick() {
            mProgressBar.setVisibility(View.INVISIBLE);
            mDelegationAdapter.onImageClick(mImage, getAdapterPosition());
        }
    }
}

final class ProgressDelegate extends BaseAdapterDelegate {

    ProgressDelegate(@NonNull final Activity activity, @NonNull final DelegationAdapter delegationAdapter, final int viewType) {
        super(activity, delegationAdapter, viewType);
    }

    @Override
    public boolean isForViewType(@NonNull final NewsCursor cursor, final int position) {
        return position == cursor.getCount();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(final ViewGroup parent) {
        return new ProgressViewHolder(mInflater.inflate(R.layout.item_progress, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull final NewsCursor cursor, final int position, @NonNull final RecyclerView.ViewHolder holder) {
    }

    static final class ProgressViewHolder extends RecyclerView.ViewHolder {

        public ProgressViewHolder(final View itemView) {
            super(itemView);
        }
    }
}
