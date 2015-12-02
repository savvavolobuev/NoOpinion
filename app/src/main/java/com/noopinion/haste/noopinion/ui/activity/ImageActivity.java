package com.noopinion.haste.noopinion.ui.activity;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
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

    @Bind(R.id.image)
    ImageView mImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);
        ButterKnife.bind(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Drawable mTintedErrorDrawable = getResources().getDrawable(R.drawable.ic_sad_face, getTheme());
        if (mTintedErrorDrawable != null) {
            mTintedErrorDrawable.setTint(getResources().getColor(R.color.primary));
        }
        Picasso.with(this).load(getIntent().getStringExtra(NewsFragment.INTENT_IMAGE))
                .error(mTintedErrorDrawable).into(mImageView);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ButterKnife.unbind(this);
    }
}
