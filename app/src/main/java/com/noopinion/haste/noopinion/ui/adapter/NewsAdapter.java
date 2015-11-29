package com.noopinion.haste.noopinion.ui.adapter;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import com.hannesdorfmann.adapterdelegates.AdapterDelegatesManager;
import com.noopinion.haste.noopinion.model.News;
import com.noopinion.haste.noopinion.ui.adapter.Delegate.NewsFullDelegate;
import com.noopinion.haste.noopinion.ui.adapter.Delegate.NewsLessDelegate;

import java.util.List;

/**
 * Created by haste on 29.11.15.
 */
public class NewsAdapter extends RecyclerView.Adapter {

    private AdapterDelegatesManager<List<News>> delegatesManager;
    private List<News> items;

    public NewsAdapter(Activity activity, List<News> items){
        this.items = items;
        delegatesManager = new AdapterDelegatesManager<>();
        delegatesManager.addDelegate(new NewsFullDelegate(activity, 0));
        delegatesManager.addDelegate(new NewsLessDelegate(activity, 1));
    }

    @Override
    public int getItemViewType(int position) {
        return delegatesManager.getItemViewType(items, position);
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(final ViewGroup parent, final int viewType) {
        return delegatesManager.onCreateViewHolder(parent, viewType);
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        delegatesManager.onBindViewHolder(items, position, holder);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }
}
