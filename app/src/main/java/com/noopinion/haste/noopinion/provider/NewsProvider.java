package com.noopinion.haste.noopinion.provider;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.util.Log;

import com.noopinion.haste.noopinion.BuildConfig;
import com.noopinion.haste.noopinion.model.News;
import com.noopinion.haste.noopinion.model.NewsApiResponse;
import com.noopinion.haste.noopinion.model.db.NewsContentProvider;
import com.noopinion.haste.noopinion.provider.api.NewsApi;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit.GsonConverterFactory;
import retrofit.Response;
import retrofit.Retrofit;

/**
 * Created by Ivan Gusev on 29.11.2015.
 */
public interface NewsProvider {

    void loadNews(int start, int limit, @Nullable Callback callback);

    interface Callback {
        void onNewsReceived(@NonNull List<News> news, @ErrorCode int errorCode);
    }

    int ERROR_NONE                = 0;
    int ERROR_UNKNOWN             = 1;
    int ERROR_NETWORK_UNAVAILABLE = 2;

    @IntDef(value = {ERROR_NONE, ERROR_UNKNOWN, ERROR_NETWORK_UNAVAILABLE})
    @interface ErrorCode {}
}

final class NewsRemoteCachingProvider implements NewsProvider {

    private final NewsLocalProvider mLocalProvider;

    private final ExecutorService mExecutorService;

    private final NewsApi mApi;

    public NewsRemoteCachingProvider(@NonNull final Context context) {
        mLocalProvider = new NewsLocalProvider(context);

        mExecutorService = Executors.newSingleThreadExecutor();
        mApi = new Retrofit.Builder().baseUrl(BuildConfig.BASE_URL).addConverterFactory(GsonConverterFactory.create()).build().create(NewsApi.class);
    }

    @Override
    public void loadNews(final int start, final int limit, @Nullable final Callback callback) {
        mExecutorService.submit(
                new Runnable() {
                    @SuppressLint("LongLogTag")
                    @Override
                    public void run() {
                        int errorCode = ERROR_NONE;
                        try {
                            try {
                                final Response<NewsApiResponse> response = mApi.getNews(start, limit).execute();
                                if (response.isSuccess()) {
                                    mLocalProvider.cacheNews(response.body().getNews());
                                } else {
                                    errorCode = ERROR_UNKNOWN;
                                }
                            } catch (IOException e) {
                                errorCode = ERROR_NETWORK_UNAVAILABLE;
                            }
                        } catch (Throwable t) {
                            Log.e("NewsRemoteCachingProvider", t.getMessage(), t);
                        } finally {
                            if (callback != null) {
                                callback.onNewsReceived(mLocalProvider.loadNews(), errorCode);
                            }
                        }
                    }
                }
        );
    }
}

final class NewsLocalProvider {

    private final ContentResolver mContentResolver;

    public NewsLocalProvider(@NonNull final Context context) {
        mContentResolver = context.getContentResolver();
    }

    @VisibleForTesting
    @NonNull
    List<News> loadNews() {
        final List<News> news = new ArrayList<>();

        final Cursor cursor = mContentResolver.query(
                NewsContentProvider.CONTENT_URI,
                null, null, null, null
        );
        News n;
        if (cursor != null && cursor.moveToFirst()) {
            do {
                n = new News();
                n.setId(cursor.getInt(cursor.getColumnIndex(BaseColumns._ID)));
                n.setText(cursor.getString(cursor.getColumnIndex("txt")));
                n.setLink(cursor.getString(cursor.getColumnIndex("link")));
                n.setImage(cursor.getString(cursor.getColumnIndex("image")));

                news.add(n);
            } while (cursor.moveToNext());
        }
        if (cursor != null) {
            cursor.close();
        }

        return news;
    }

    @VisibleForTesting
    void cacheNews(@NonNull final List<News> news) {
        final ContentValues[] values = new ContentValues[news.size()];

        int counter = 0;
        ContentValues cv;
        for (News n : news) {
            cv = new ContentValues();
            cv.put(BaseColumns._ID, n.getId());
            cv.put("txt", n.getText());
            cv.put("link", n.getLink());
            cv.put("image", n.getImage());
            values[counter++] = cv;
        }

        mContentResolver.bulkInsert(Uri.parse("content://" + NewsContentProvider.AUTHORITY + "/news"), values);
    }
}
