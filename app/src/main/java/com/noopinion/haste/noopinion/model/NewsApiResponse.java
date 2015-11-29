package com.noopinion.haste.noopinion.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by Ivan Gusev on 30.11.2015.
 */
public final class NewsApiResponse {
    @SerializedName("count")
    int        mCount;
    @SerializedName("html")
    List<News> mNews;

    public int getCount() {
        return mCount;
    }

    public void setCount(final int count) {
        mCount = count;
    }

    public List<News> getNews() {
        return mNews;
    }

    public void setNews(final List<News> news) {
        mNews = news;
    }
}
