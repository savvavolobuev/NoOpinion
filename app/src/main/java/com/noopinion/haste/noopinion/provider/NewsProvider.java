package com.noopinion.haste.noopinion.provider;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.noopinion.haste.noopinion.BuildConfig;
import com.noopinion.haste.noopinion.model.News;
import com.noopinion.haste.noopinion.model.NewsApiResponse;
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

    void getNews(@Nullable Callback callback);

    interface Callback {
        void onNewsReceived(@NonNull List<News> news, @ErrorCode int errorCode);
    }

    int ERROR_NONE                = 0;
    int ERROR_UNKNOWN             = 1;
    int ERROR_NETWORK_UNAVAILABLE = 2;

    @IntDef(value = {ERROR_NONE, ERROR_UNKNOWN, ERROR_NETWORK_UNAVAILABLE})
    @interface ErrorCode {}
}

final class NewsProviderImpl implements NewsProvider {

    private final ExecutorService mExecutorService;

    private final NewsApi           mApi;
    private final SharedPreferences mPreferences;

    public NewsProviderImpl(@NonNull final Context context) {
        mExecutorService = Executors.newSingleThreadExecutor();
        mApi = new Retrofit.Builder().baseUrl(BuildConfig.BASE_URL).addConverterFactory(GsonConverterFactory.create()).build().create(NewsApi.class);
        mPreferences = context.getSharedPreferences("news_cache", Context.MODE_PRIVATE);
    }

    @Override
    public void getNews(@Nullable final Callback callback) {
        mExecutorService.submit(
                new Runnable() {
                    @Override
                    public void run() {
                        try {
                            List<News> news = null;
                            int errorCode = ERROR_NONE;
                            try {
                                final Response<NewsApiResponse> response = mApi.getNews().execute();
                                if (response.isSuccess()) {
                                    news = response.body().getNews();
                                } else {
                                    errorCode = ERROR_UNKNOWN;
                                }
                            } catch (IOException e) {
                                errorCode = ERROR_NETWORK_UNAVAILABLE;
                            }

                            if (news == null) {
                                news = loadFromCache();
                            } else {
                                writeToCache(news);
                            }

                            if (callback != null) {
                                callback.onNewsReceived(news, errorCode);
                            }
                        } catch (Throwable t) {
                            if (callback != null) {
                                callback.onNewsReceived(new ArrayList<News>(0), ERROR_UNKNOWN);
                            }
                        }
                    }
                }
        );
    }

    @VisibleForTesting
    void writeToCache(@NonNull final List<News> news) {
        mPreferences.edit().putString("news", new Gson().toJson(news)).apply();
    }

    @VisibleForTesting
    @NonNull
    List<News> loadFromCache() {
        List<News> news = new Gson().fromJson(mPreferences.getString("news", null), new TypeToken<List<News>>() {}.getType());
        if (news == null) {
            news = new ArrayList<>(0);
        }
        return news;
    }
}

final class MockNewsProvider implements NewsProvider {

    private final ExecutorService mExecutorService = Executors.newSingleThreadExecutor();

    @Override
    public void getNews(@Nullable final Callback callback) {
        mExecutorService.submit(
                new Runnable() {
                    @Override
                    public void run() {
                        final List<News> news = new ArrayList<>();

                        News n;
                        for (int i = 0; i < 10; i++) {
                            n = new News();
                            n.setId(i + 1);
                            n.setText("Хуйнанэ номер " + (i + 1));
                            news.add(n);
                        }

                        if (callback != null) {
                            callback.onNewsReceived(news, ERROR_NONE);
                        }
                    }
                }
        );
    }
}
