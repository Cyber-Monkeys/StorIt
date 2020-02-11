package com.example.storit;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;

import gr.net.maroulis.library.EasySplashScreen;

public class SplashScreenActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //splash screen
        EasySplashScreen config = new EasySplashScreen(SplashScreenActivity.this)
                .withFullScreen()
                .withTargetActivity(Login.class)
                .withSplashTimeOut(2000)
                .withBackgroundColor(Color.parseColor("#ffffff"))
                .withAfterLogoText("StorIT")
                .withLogo(R.drawable.storit_logo);

        config.getLogo().getLayoutParams().height = 600;
        config.getLogo().getLayoutParams().width = 600;

        View splashScreen = config.create();
        setContentView(splashScreen);
    }
}
