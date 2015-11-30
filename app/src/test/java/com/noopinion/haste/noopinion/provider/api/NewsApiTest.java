package com.noopinion.haste.noopinion.provider.api;

import com.noopinion.haste.noopinion.BuildConfig;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;

import java.io.IOException;

import retrofit.GsonConverterFactory;
import retrofit.Retrofit;

/**
 * Created by Ivan Gusev on 29.11.2015.
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public class NewsApiTest {

    @Test
    public void checkGetNewsResponseIsSuccessful() {
        try {
            if (!new Retrofit.Builder().baseUrl(BuildConfig.BASE_URL).addConverterFactory(GsonConverterFactory.create()).build()
                                       .create(NewsApi.class).getNews().execute().isSuccess()) {
                throw new RuntimeException("Api call was not successful");
            }
        } catch (IOException e) {
            throw new RuntimeException("No internet connection");
        }
    }
}
