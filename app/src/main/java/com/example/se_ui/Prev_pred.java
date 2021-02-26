package com.example.se_ui;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class Prev_pred extends AppCompatActivity {
    BottomNavigationView navigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prev_pred);

        navigation = findViewById(R.id.bottomNavigationView_pred);
        navigation.getMenu().findItem(R.id.bottomnav_prevpred).setChecked(true);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
    }

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = item -> {
        switch (item.getItemId()) {
            case R.id.bottomnav_home:
                Intent intent = new Intent(Prev_pred.this , HomePage.class);
                finish();
                startActivity(intent);
                break;
            case R.id.bottomnav_profile:
                Intent intent1 = new Intent(Prev_pred.this , UserProfile.class);
                startActivity(intent1);
                break;
            case R.id.bottomnav_prevpred:
                break;
            case R.id.bottomnav_analytics:
                Intent intent2 = new Intent(Prev_pred.this , Analytics.class);
                startActivity(intent2);
                break;
        }
        return false;
    };
}