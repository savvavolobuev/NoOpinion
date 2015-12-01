package com.noopinion.haste.noopinion.model;

import android.database.DataSetObserver;
import android.support.annotation.NonNull;

import java.io.Closeable;

/**
 * Created by Ivan Gusev on 01.12.2015.
 */
public interface NewsCursor extends Closeable {

    long getId();

    @NonNull
    String getText();

    @NonNull
    String getLink();

    @NonNull
    String getImage();

    void close();

    int getCount();

    boolean moveToPosition(int position);

    void unregisterDataSetObserver(@NonNull DataSetObserver observer);

    void registerDataSetObserver(@NonNull DataSetObserver observer);
}
