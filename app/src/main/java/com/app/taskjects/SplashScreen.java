package com.app.taskjects;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

public class SplashScreen extends Activity {


    //Duracion del spl
    private final int SPLASH_DISPLAY_LENGTH = 2000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_screen_taskjects_layout);

        new Handler().postDelayed(() -> {
            Intent loginScreen = new Intent(SplashScreen.this,LoginActivity.class);
            SplashScreen.this.startActivity(loginScreen);
            SplashScreen.this.finish();
        },SPLASH_DISPLAY_LENGTH);

    }
}
