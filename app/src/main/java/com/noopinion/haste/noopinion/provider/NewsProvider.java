package com.noopinion.haste.noopinion.provider;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.database.MatrixCursor;
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
import com.noopinion.haste.noopinion.model.NewsCursor;
import com.noopinion.haste.noopinion.model.db.NewsContentProvider;
import com.noopinion.haste.noopinion.provider.api.NewsApi;

import java.io.IOException;
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
        void onNewsReceived(@NonNull NewsCursor cursor, int downloaded, @ErrorCode int errorCode);
    }

    int ERROR_NONE = 0;
    int ERROR_UNKNOWN = 1;
    int ERROR_NETWORK_UNAVAILABLE = 2;

    @IntDef(value = {ERROR_NONE, ERROR_UNKNOWN, ERROR_NETWORK_UNAVAILABLE})
    @interface ErrorCode {
    }
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
                        loadNews(start, limit, true, callback);
                    }
                }
        );
    }

    @SuppressLint("LongLogTag")
    void loadNews(final int start, final int limit, final boolean recursionAllowed, @Nullable final Callback callback) {
        int errorCode = ERROR_NONE;
        int downloaded = 0;
        long maxLocalId = mLocalProvider.getMaxId();
        try {
            final Response<NewsApiResponse> response = mApi.getNews(start, limit).execute();
            if (response.isSuccess()) {
                final List<News> downloadedNews = response.body().getNews();
                downloaded = downloadedNews.size();
                if (downloaded != 0 && maxLocalId != -1) {
                    long minRemoteId = downloadedNews.get(downloaded - 1).getId();
                    int diff = (int) (minRemoteId - maxLocalId);
                    if (diff > 1) {
                        int newLimit = limit + diff;
                        if (recursionAllowed) {
                            loadNews(start, newLimit, false, callback);
                            return;
                        }
                    }
                }
                mLocalProvider.cacheNews(downloadedNews);
            } else {
                errorCode = ERROR_UNKNOWN;
            }
        } catch (IOException e) {
            errorCode = ERROR_NETWORK_UNAVAILABLE;
        } catch (Throwable t) {
            Log.e("NewsRemoteCachingProvider", t.getMessage(), t);
        } finally {
            if (callback != null) {
                callback.onNewsReceived(mLocalProvider.loadNews(), downloaded, errorCode);
            }
        }
    }
}

final class NewsLocalProvider {

    private final ContentResolver mContentResolver;

    public NewsLocalProvider(@NonNull final Context context) {
        mContentResolver = context.getContentResolver();
    }

    @VisibleForTesting
    @NonNull
    NewsCursor loadNews() {
        final Cursor cursor = mContentResolver.query(NewsContentProvider.CONTENT_URI, null, null, null, BaseColumns._ID + " DESC");

        if (cursor != null && cursor.moveToFirst()) {
            return new NewsCursorImpl(cursor);
        }
        if (cursor != null) {
            cursor.close();
        }

        return new NewsCursorImpl(new MatrixCursor(new String[]{BaseColumns._ID, "txt", "link", "image"}, 0));
    }

    @VisibleForTesting
    long getMaxId() {
        NewsCursor cursor = loadNews();
        if (cursor.getCount() == 0) {
            return -1;
        }
        return cursor.getId();
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

    static final class NewsCursorImpl implements NewsCursor {

        private final Cursor mCursor;

        public NewsCursorImpl(@NonNull final Cursor cursor) {
            mCursor = cursor;
        }

        @Override
        public long getId() {
            return mCursor.getLong(mCursor.getColumnIndex(BaseColumns._ID));
        }

        @NonNull
        @Override
        public String getText() {
            return mCursor.getString(mCursor.getColumnIndex("txt"));
        }

        @NonNull
        @Override
        public String getLink() {
            return mCursor.getString(mCursor.getColumnIndex("link"));
        }

        @NonNull
        @Override
        public String getImage() {
            return mCursor.getString(mCursor.getColumnIndex("image"));
        }

        @Override
        public void close() {
            mCursor.close();
        }

        @Override
        public int getCount() {
            return mCursor.getCount();
        }

        @Override
        public boolean moveToPosition(final int position) {
            return mCursor.moveToPosition(position);
        }

        @Override
        public void registerDataSetObserver(@NonNull final DataSetObserver observer) {
            mCursor.registerDataSetObserver(observer);
        }

        @Override
        public void unregisterDataSetObserver(@NonNull final DataSetObserver observer) {
            mCursor.unregisterDataSetObserver(observer);
        }
    }
}
