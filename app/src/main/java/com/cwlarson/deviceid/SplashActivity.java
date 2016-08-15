package com.cwlarson.deviceid;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onPostResume() {
        super.onPostResume();
        startActivity(new Intent(SplashActivity.this, MainActivity.class));
    }
}
