package com.noopinion.haste.noopinion.provider;

import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.noopinion.haste.noopinion.model.News;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Ivan Gusev on 29.11.2015.
 */
public interface NewsProvider {

    void getNews(@Nullable Callback callback);

    interface Callback {
        void onNewsReceived(@NonNull List<News> news);

        void onError(@ErrorCode int errorCode);
    }

    int ERROR_UNKNOWN             = 1;
    int ERROR_NETWORK_UNAVAILABLE = 2;

    @IntDef(value = {ERROR_UNKNOWN, ERROR_NETWORK_UNAVAILABLE})
    @interface ErrorCode {}
}

final class MockNewsProvider implements NewsProvider {

    private final ExecutorService mExecutorService = Executors.newSingleThreadExecutor();

    @Override
    public void getNews(@Nullable final Callback callback) {
        if (!mExecutorService.isTerminated()) {
            return;
        }

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
                            callback.onNewsReceived(news);
                        }
                    }
                }
        );
    }
}
