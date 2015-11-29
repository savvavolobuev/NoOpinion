package com.noopinion.haste.noopinion.ui.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import com.hannesdorfmann.adapterdelegates.AbsAdapterDelegate;
import com.noopinion.haste.noopinion.model.News;

import java.util.List;

/**
 * Created by haste on 29.11.15.
 */
public class NewsAdapter extends RecyclerView.Adapter {
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(final ViewGroup parent, final int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }

    final class NewsFullDelegate extends AbsAdapterDelegate<List<News>> {

        public NewsFullDelegate(final int viewType) {
            super(viewType);
        }

        @Override
        public boolean isForViewType(final List<News> items, final int position) {
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

    final class NewsLessDelegate extends AbsAdapterDelegate<List<News>>{

        public NewsLessDelegate(final int viewType) {
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
}
