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
        return null;
    }
}
