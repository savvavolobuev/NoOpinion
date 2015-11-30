package com.noopinion.haste.noopinion.ui.activity;

import android.app.Activity;
import android.os.Bundle;

import com.noopinion.haste.noopinion.R;
import com.noopinion.haste.noopinion.ui.fragment.NewsFragment;


public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction().add(R.id.content, NewsFragment.create(), NewsFragment.TAG).commit();
        }
    }
}
