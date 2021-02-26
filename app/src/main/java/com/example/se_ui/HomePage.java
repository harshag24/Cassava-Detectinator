package com.example.se_ui;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

public class HomePage extends AppCompatActivity {
    ImageButton logout;
    BottomNavigationView navigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);

        logout = findViewById(R.id.logout_button);
        navigation = findViewById(R.id.bottomNavigationView_profile);
        navigation.getMenu().findItem(R.id.bottomnav_home).setChecked(true);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        logout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(HomePage.this , Login.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            finish();
            startActivity(intent);
        });
    }

    private final BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = item -> {
                switch (item.getItemId()) {
                    case R.id.bottomnav_home:
                        break;
                    case R.id.bottomnav_profile:
                        Intent intent = new Intent(HomePage.this , UserProfile.class);
                        finish();
                        startActivity(intent);
                        break;
                    case R.id.bottomnav_prevpred:
                        Intent intent1 = new Intent(HomePage.this , Prev_pred.class);
                        startActivity(intent1);
                        break;
                    case R.id.bottomnav_analytics:
                        Intent intent2 = new Intent(HomePage.this , Analytics.class);
                        startActivity(intent2);
                        break;
                }
                return false;
            };
}