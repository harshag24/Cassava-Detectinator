package com.example.se_ui;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;

import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

public class Prev_pred extends AppCompatActivity {
    BottomNavigationView navigation;
    RecyclerView recview;
    myadapter adapter;
    FirebaseUser user;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prev_pred);

        navigation = findViewById(R.id.bottomNavigationView_pred);
        navigation.getMenu().findItem(R.id.bottomnav_prevpred).setChecked(true);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        user = FirebaseAuth.getInstance().getCurrentUser();

        recview=(RecyclerView)findViewById(R.id.recview);
        recview.setLayoutManager(new LinearLayoutManager(this));

        FirebaseRecyclerOptions<model> options =
                new FirebaseRecyclerOptions.Builder<model>()
                        .setQuery(FirebaseDatabase.getInstance().getReference().child("Prediction").child(user.getUid()), model.class)
                        .build();

        adapter=new myadapter(options);
        recview.setAdapter(adapter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
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