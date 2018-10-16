package com.offerland.app;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;

import datamodels.User;

public class SplashActivity extends ActionBarActivity {
    private static final int SPLASH_DURATION = 2 * 1000;
    private Handler handler;
    private Runnable runnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // init splash handler and runnable
        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                finish();
            }
        };

        // check if there is a saved active user
        User user = AppController.getInstance(getApplicationContext()).getActiveUser();
        if (user == null) {
            // start splash
            handler.postDelayed(runnable, SPLASH_DURATION);
        } else {
            // check saved reg_id
            if (user.getRegId() == null) {
                // register user to GCM
                AppController.getInstance(getApplicationContext()).registerToGCM();
            }

            // start update location receiver to update location right now
            long when = System.currentTimeMillis();
            AppController.startLocationUpdater(this, when);

            // goto main activity
            Intent intent = new Intent(SplashActivity.this, MainActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.no_anim, R.anim.no_anim);
            finish();
        }
    }
}
