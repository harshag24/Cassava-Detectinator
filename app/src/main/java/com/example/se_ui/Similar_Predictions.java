package com.example.se_ui;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

public class Similar_Predictions extends AppCompatActivity {
    RecyclerView recyclerView;
    myadapter adapter;
    FirebaseUser user;
    String pred_class;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_similar__predictions);

        recyclerView = findViewById(R.id.similar_recview);

        Bundle bundle = getIntent().getExtras();
        assert bundle != null;
        pred_class =  bundle.getString("Prediction");
        user = FirebaseAuth.getInstance().getCurrentUser();

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        FirebaseRecyclerOptions<model> options_sim =
                new FirebaseRecyclerOptions.Builder<model>()
                        .setQuery(FirebaseDatabase.getInstance().getReference().child("Prediction").child(user.getUid()).orderByChild("prediction").equalTo(pred_class), model.class)
                        .build();

        adapter=new myadapter(options_sim);
        recyclerView.setAdapter(adapter);


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
}