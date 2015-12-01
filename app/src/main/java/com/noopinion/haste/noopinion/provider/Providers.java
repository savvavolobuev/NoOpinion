package com.noopinion.haste.noopinion.provider;

import android.content.Context;
import android.support.annotation.NonNull;

/**
 * Created by Ivan Gusev on 29.11.2015.
 */
public final class Providers {

    @NonNull
    public static NewsProvider createNewsProvider(@NonNull final Context context) {
        return new NewsRemoteCachingProvider(context);
    }

    private Providers() {
    }
}
