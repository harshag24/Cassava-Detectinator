package com.example.se_ui;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.*;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Login extends AppCompatActivity {
    Button login, signup;
    EditText username, passw;
    String email, password;
    FirebaseAuth mAuth;
    ProgressDialog progressDialog;
    DatabaseReference databaseReference;
    FirebaseUser user;
    TextView forget_pass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        progressDialog = new ProgressDialog(this);
        username = findViewById(R.id.Username_editText);
        passw = findViewById(R.id.Pass_editText);
        login = findViewById(R.id.buttonLogin);
        signup = findViewById(R.id.buttonSignUp);
        forget_pass = findViewById(R.id.forget_pass);

        login.setOnClickListener(view -> {

            email = username.getText().toString().trim();
            password = passw.getText().toString().trim();
            if (validateEmail() && validatePassword()) {
                progressDialog.setMessage("Logging In...");
                progressDialog.show();
                setLogin();

            }
        });


        signup.setOnClickListener(view -> {
            Intent intent = new Intent(Login.this, Signup.class);
            startActivity(intent);
        });

        forget_pass.setOnClickListener(v -> {
            reset_pass_dialog exampleDialog = new reset_pass_dialog();
            exampleDialog.show(getSupportFragmentManager(), "reset dialog");
            //Toast.makeText(Login_Page.this , "Reset link sent!!" , Toast.LENGTH_SHORT);
        });
    }

    public void setLogin() {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Log.w("MSG", "onComplete: Success");

                        Intent intent = new Intent(Login.this, HomePage.class);
                        progressDialog.dismiss();
                        Toast.makeText(Login.this, "Successfully Logged In", Toast.LENGTH_LONG).show();
                        finish();
                        startActivity(intent);

                    } else {
                        Log.w("MSG", "signInWithEmail:failure", task.getException());
                        progressDialog.dismiss();
                        Toast.makeText(Login.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public boolean validateEmail() {
        if (email.isEmpty()) {
            username.setError("Field can't be empty");
            return false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            username.setError("Please enter a valid email address");
            return false;

        } else {
            return true;
        }
    }

//    public void getUserName() {
//        user = FirebaseAuth.getInstance().getCurrentUser();
//        databaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(user.getUid());
//        databaseReference.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                String name = dataSnapshot.child("name").getValue().toString();
//
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//                Toast.makeText(Login.this, "Client doesn't have permission to read from that database reference.", Toast.LENGTH_LONG).show();
//            }
//        });
//
//    }

    public boolean validatePassword() {

        if (password.isEmpty()) {
            passw.setError("Field can't be empty");
            return false;
        } else if (password.length() < 6) {
            passw.setError("Password Should be more than 6 digits");
            return false;
        } else {
            return true;
        }
    }

}