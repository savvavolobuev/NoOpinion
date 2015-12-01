package com.noopinion.haste.noopinion.ui.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.noopinion.haste.noopinion.R;
import com.noopinion.haste.noopinion.ui.fragment.NewsFragment;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().add(R.id.content, NewsFragment.create(), NewsFragment.TAG).commit();
        }
    }
}
