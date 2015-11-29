package com.noopinion.haste.noopinion.ui.adapter.Delegate;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.hannesdorfmann.adapterdelegates.AbsAdapterDelegate;
import com.noopinion.haste.noopinion.R;
import com.noopinion.haste.noopinion.model.News;

import java.util.List;

/**
 * Created by haste on 29.11.15.
 */
public class NewsFullDelegate extends AbsAdapterDelegate<List<News>> {

    private LayoutInflater inflater;

    public NewsFullDelegate(Activity activity,final int viewType) {
        super(viewType);
        inflater = activity.getLayoutInflater();
    }

    @Override
    public boolean isForViewType(final List<News> items, final int position) {
        return items.get(position) instanceof News; //todo picture !=null ;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(final ViewGroup parent) {
        return new NewsFullViewHolder(inflater.inflate(R.layout.item_news_full, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull final List<News> items, final int position, @NonNull final RecyclerView.ViewHolder holder) {
        NewsFullViewHolder vh = (NewsFullViewHolder) holder;
        News news =  items.get(position);
    }

    static class NewsFullViewHolder extends RecyclerView.ViewHolder {

        public TextView name;

        public NewsFullViewHolder(View itemView) {
            super(itemView);
            //name = (TextView) itemView.findViewById(R.id.name);
        }
    }
}
