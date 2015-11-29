package com.noopinion.haste.noopinion.provider;

import com.noopinion.haste.noopinion.model.News;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ivan Gusev on 29.11.2015.
 */
public interface NewsProvider {
    List<News> getNews();
}

final class MockNewsProvider implements NewsProvider {

    @Override
    public List<News> getNews() {
        final List<News> news = new ArrayList<>();

        News n;
        for (int i = 0; i < 10; i++) {
            n = new News();
            n.setId(i + 1);
            n.setText("Хуйнанэ номер " + (i + 1));
            news.add(n);
        }
        return news;
    }
}
