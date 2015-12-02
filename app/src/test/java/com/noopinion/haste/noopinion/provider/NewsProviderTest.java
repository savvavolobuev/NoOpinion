package com.noopinion.haste.noopinion.provider;

import com.noopinion.haste.noopinion.BuildConfig;
import com.noopinion.haste.noopinion.model.News;

import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.List;

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
}
