package com.example.se_ui;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class Analytics extends AppCompatActivity {
    BottomNavigationView navigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analytics);

        navigation = findViewById(R.id.bottomNavigationView_analytics);
        navigation.getMenu().findItem(R.id.bottomnav_analytics).setChecked(true);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
    }

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = item -> {
        switch (item.getItemId()) {
            case R.id.bottomnav_home:
                Intent intent = new Intent(Analytics.this , HomePage.class);
                finish();
                startActivity(intent);
                break;
            case R.id.bottomnav_profile:
                Intent intent1 = new Intent(Analytics.this , UserProfile.class);
                startActivity(intent1);
                break;
            case R.id.bottomnav_prevpred:
                Intent intent2 = new Intent(Analytics.this , Prev_pred.class);
                startActivity(intent2);
                break;
            case R.id.bottomnav_analytics:
                break;
        }
        return false;
    };
}