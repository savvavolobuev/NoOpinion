package com.noopinion.haste.noopinion.provider.api;

import com.noopinion.haste.noopinion.model.NewsApiResponse;

import retrofit.Call;
import retrofit.http.GET;

/**
 * Created by Ivan Gusev on 29.11.2015.
 */
public interface NewsApi {
    @GET("/api.php")
    Call<NewsApiResponse> getNews();
}
