package com.example.se_ui;

import androidx.appcompat.app.AppCompatActivity;

import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class Result extends AppCompatActivity {
    ImageView input;
    Button similar;
    TextView pred;
    String pred_class;
    float confi;
    Uri img_uri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        input = findViewById(R.id.input_image);
        similar = findViewById(R.id.sim_pred);
        pred = findViewById(R.id.prediction);

        Bundle bundle = getIntent().getExtras();
        assert bundle != null;
        pred_class =  bundle.getString("Prediction");
        confi = Float.parseFloat(bundle.getString("Confidence"));
        img_uri = Uri.parse(bundle.getString("ImageUri"));

        input.setImageURI(img_uri);
        confi = confi*100;
        String str = "Prediction - "+pred_class+" ("+confi+")";
        pred.setText(str);
        //
    }
}