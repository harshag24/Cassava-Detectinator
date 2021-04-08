package com.example.se_ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import android.media.MediaPlayer;
import java.util.Objects;

public class SplashScreen extends AppCompatActivity {
    FirebaseUser firebaseUser;
    DatabaseReference databaseReference;
    MediaPlayer music;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        getSupportActionBar().hide();
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        int SPLASH_TIME_OUT = 800;
        new Handler().postDelayed(() -> {
            if (firebaseUser != null) {
//                    music=MediaPlayer.create(SplashScreen.this,R.raw.detectinaotr);
//                    music.start();
                getUserName();
            } else {
                Intent intent = new Intent(SplashScreen.this, Login.class);
//                    music.release();
                finish();
                startActivity(intent);
            }
        }, SPLASH_TIME_OUT);
    }

    public void getUserName() {

        databaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(firebaseUser.getUid());
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Intent intent = new Intent(SplashScreen.this, HomePage.class);
                finish();
                startActivity(intent);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
//                Toast.makeText(SplashScreen.this, "Client doesn't have permission to read from that database reference.", Toast.LENGTH_LONG).show();
            }
        });

    }
}