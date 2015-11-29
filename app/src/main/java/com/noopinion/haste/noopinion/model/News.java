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

    private News() {
    }

    public int getId() {
        return mId;
    }

    public void setId(final int id) {
        mId = id;
    }

    public String getText() {
        return mText;
    }

    public void setText(final String text) {
        mText = text;
    }

    public String getLink() {
        return mLink;
    }

    public void setLink(final String link) {
        mLink = link;
    }

    public String getImage() {
        return mImage;
    }

    public void setImage(final String image) {
        mImage = image;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final News news = (News) o;

        if (mId != news.mId) return false;
        if (mText != null ? !mText.equals(news.mText) : news.mText != null) return false;
        if (mLink != null ? !mLink.equals(news.mLink) : news.mLink != null) return false;
        return !(mImage != null ? !mImage.equals(news.mImage) : news.mImage != null);

    }

    @Override
    public int hashCode() {
        int result = mId;
        result = 31 * result + (mText != null ? mText.hashCode() : 0);
        result = 31 * result + (mLink != null ? mLink.hashCode() : 0);
        result = 31 * result + (mImage != null ? mImage.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "News{" +
                "mId=" + mId +
                ", mText='" + mText + '\'' +
                ", mLink='" + mLink + '\'' +
                ", mImage='" + mImage + '\'' +
                '}';
    }

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
