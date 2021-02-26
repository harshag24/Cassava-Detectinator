package com.example.se_ui;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class UserProfile extends AppCompatActivity {
    BottomNavigationView navigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        navigation = findViewById(R.id.bottomNavigationView_profile);
        navigation.getMenu().findItem(R.id.bottomnav_profile).setChecked(true);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
    }


    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener

            = item -> {
                switch (item.getItemId()) {
                    case R.id.bottomnav_home:
                        Intent intent = new Intent(UserProfile.this , HomePage.class);
                        finish();
                        startActivity(intent);
                        break;
                    case R.id.bottomnav_profile:
                        break;
                    case R.id.bottomnav_prevpred:
                        Intent intent1 = new Intent(UserProfile.this , Prev_pred.class);
                        startActivity(intent1);
                        break;
                    case R.id.bottomnav_analytics:
                        Intent intent2 = new Intent(UserProfile.this , Analytics.class);
                        startActivity(intent2);
                        break;
                }
                return false;
            };
}