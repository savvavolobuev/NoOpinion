package com.noopinion.haste.noopinion.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.hannesdorfmann.parcelableplease.annotation.ParcelablePlease;

/**
 * Created by Ivan Gusev on 29.11.2015.
 */
@ParcelablePlease
public final class News implements Parcelable {

    int    mId;
    String mText;
    String mLink;
    String mImage;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(final Parcel dest, final int flags) {
        NewsParcelablePlease.writeToParcel(this, dest, flags);
    }

    public static final Creator<News> CREATOR = new Creator<News>() {
        @Override
        public News createFromParcel(final Parcel source) {
            final News news = new News();
            NewsParcelablePlease.readFromParcel(news, source);
            return news;
        }

        @Override
        public News[] newArray(final int size) {
            return new News[size];
        }
    };
}
