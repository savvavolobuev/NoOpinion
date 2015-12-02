package com.noopinion.haste.noopinion.ui.activity;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

import com.noopinion.haste.noopinion.R;
import com.noopinion.haste.noopinion.ui.fragment.NewsFragment;
import com.squareup.picasso.Picasso;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by haste on 01.12.15.
 */
public class ImageActivity extends AppCompatActivity {

    public static final String TRANSITION_IMAGE_NAME = "img";

    @Bind(R.id.image)
    ImageView mImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);
        ButterKnife.bind(this);

        ViewCompat.setTransitionName(mImageView, TRANSITION_IMAGE_NAME);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Drawable mTintedErrorDrawable = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_sad_face, getTheme());
        if (mTintedErrorDrawable == null) {
            mTintedErrorDrawable = new ColorDrawable(Color.TRANSPARENT);
        }
        Picasso.with(this)
               .load(getIntent().getStringExtra(NewsFragment.INTENT_IMAGE))
               .error(mTintedErrorDrawable)
               .into(mImageView);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ButterKnife.unbind(this);
    }
}
