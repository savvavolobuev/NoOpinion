package com.noopinion.haste.noopinion.provider;

import android.support.annotation.NonNull;

import com.noopinion.haste.noopinion.BuildConfig;
import com.noopinion.haste.noopinion.model.News;
import com.noopinion.haste.noopinion.model.NewsCursor;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Ivan Gusev on 29.11.2015.
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public class NewsProviderTest {

    private static final List<News> FAKE_NEWS = new ArrayList<>();

    static {
        News n;
        for (int i = 0; i < 10; i++) {
            n = new News();
            n.setId(i + 1);
            n.setText("Test text");
            n.setImage("http://image");
            n.setLink("http://link");
            FAKE_NEWS.add(n);
        }
    }

    @Test
    public void checkCache() {
        final NewsProvider provider = Providers.createNewsProvider(RuntimeEnvironment.application);
        ((NewsProviderImpl) provider).writeToCache(FAKE_NEWS);

        Assert.assertEquals(FAKE_NEWS, ((NewsProviderImpl) provider).loadFromCache());
    }

    @Test
    public void checkErrorCodeIsNone() {
        final NewsProvider provider = Providers.createNewsProvider(RuntimeEnvironment.application);

        final CountDownLatch latch = new CountDownLatch(1);

        final AtomicInteger value = new AtomicInteger(NewsProvider.ERROR_NONE);

        provider.getNews(
                new NewsProvider.Callback() {
                    @Override
                    public void onNewsReceived(@NonNull final NewsCursor news, @NewsProvider.ErrorCode final int errorCode) {
                        value.set(errorCode);
                        latch.countDown();
                    }
                }
        );

        try {
            latch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException("Latch has been passed by InterruptedException");
        }

        if (value.get() != NewsProvider.ERROR_NONE) {
            throw new RuntimeException("Error code is " + value.get());
        }
    }
}
