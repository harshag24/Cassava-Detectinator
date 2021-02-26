package com.example.se_ui;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.Objects;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class UserProfile extends AppCompatActivity {
    BottomNavigationView navigation;
    EditText name , email , phone;
    Button submit , edit;
    TextView reset;
    FirebaseAuth firebaseAuth;
    FirebaseUser user;
    DatabaseReference databaseReference ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        edit = findViewById(R.id.edit_button);
        submit = findViewById(R.id.submit_profile);
        reset = findViewById(R.id.reset_pass_field);
        name = findViewById(R.id.profile_name_field);
        email = findViewById(R.id.profile_email_field);
        phone = findViewById(R.id.profile_phone_field);

        navigation = findViewById(R.id.bottomNavigationView_profile);
        navigation.getMenu().findItem(R.id.bottomnav_profile).setChecked(true);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        user = FirebaseAuth.getInstance().getCurrentUser();
        databaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(user.getUid());
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String username = Objects.requireNonNull(dataSnapshot.child("name").getValue()).toString();
                String userphone = Objects.requireNonNull(dataSnapshot.child("phone_no").getValue()).toString();
                String useremail = user.getEmail();
                name.setText(username);
                email.setText(useremail);
                phone.setText(userphone);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(UserProfile.this, "Client doesn't have permission to read from that database.", Toast.LENGTH_LONG).show();
            }
        });

        name.setFocusable(false);
        name.setCursorVisible(false);

        email.setFocusable(false);
        email.setCursorVisible(false);

        phone.setFocusable(false);
        phone.setCursorVisible(false);

        edit.setOnClickListener(v -> {
            name.setFocusableInTouchMode(true);
            name.setCursorVisible(true);
            phone.setFocusableInTouchMode(true);
            phone.setCursorVisible(true);
            submit.setVisibility(View.VISIBLE);
            edit.setVisibility(View.GONE);
        });

        submit.setOnClickListener(v -> {
            String username = name.getText().toString();
            String userphone = phone.getText().toString();
            Model_Class modelClass = new Model_Class(username,userphone);
            databaseReference.setValue(modelClass)
                    .addOnCompleteListener(task -> {
                        if(task.isSuccessful())
                        { Toast.makeText(UserProfile.this , "Profile Updated!!" , Toast.LENGTH_LONG).show();
                            recreate();
                        }
                        else
                            Toast.makeText(UserProfile.this , "Error in updating profile!" , Toast.LENGTH_SHORT).show();
                    });
        });

//        Log.d("TAG", "onSuccess:Updated in Users ")
        reset.setOnClickListener(v -> {
            firebaseAuth = FirebaseAuth.getInstance();
            firebaseAuth.sendPasswordResetEmail(Objects.requireNonNull(user.getEmail()))
                    .addOnCompleteListener(task -> {
                        if(task.isSuccessful())
                        { Toast.makeText(UserProfile.this , "Reset link sent to "+user.getEmail() , Toast.LENGTH_SHORT).show();
                            finish();
                        }
                        else
                            Toast.makeText(UserProfile.this , "Unable to send reset link" , Toast.LENGTH_SHORT).show();
                    });
        });

    }

    private final BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
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