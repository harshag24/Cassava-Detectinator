package com.example.se_ui;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.utils.ColorTemplate;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;

import static com.google.android.gms.tasks.Tasks.await;
import static java.util.concurrent.CompletableFuture.completedFuture;

public class Analytics extends AppCompatActivity {
    BottomNavigationView navigation;
    DatabaseReference  databaseReference;
    float cbb,cbsd,cgm,cmd,healthy;
    ArrayList<BarEntry> entries;
    BarChart barChart;
    BarData data;
    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analytics);

        navigation = findViewById(R.id.bottomNavigationView_analytics);
        navigation.getMenu().findItem(R.id.bottomnav_analytics).setChecked(true);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        barChart = findViewById(R.id.barchart);
        entries = new ArrayList<>();
        BarDataSet bardataset = new BarDataSet(entries, "Diseases");
        ArrayList<String> labels = new ArrayList<String>();
        labels.add("CBB");
        labels.add("CBSD");
        labels.add("CGM");
        labels.add("CMD");
        labels.add("Healthy");
        data = new BarData(labels, bardataset);
        barChart.setDescription("Cassava Disease count");
        barChart.setDescriptionColor(16777215);
        barChart.setData(data);
        bardataset.setColors(ColorTemplate.COLORFUL_COLORS);
        XAxis xAxis = barChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        barChart.getAxisLeft().setAxisMinValue(0);
        barChart.getAxisLeft().setAxisMaxValue(200);
        barChart.animateY(1500);
        barChart.notifyDataSetChanged();
    }

    @Override
    protected void onResume() {
        super.onResume();

        databaseReference = FirebaseDatabase.getInstance().getReference().child("Analytics");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                entries.clear();

                cbb = Float.parseFloat(snapshot.child("Cassava Bacterial Blight (CBB)").getValue().toString());
                cbsd = Float.parseFloat(snapshot.child("Cassava Brown Streak Disease (CBSD)").getValue().toString());
                cgm = Float.parseFloat(snapshot.child("Cassava Green Mottle (CGM)").getValue().toString());
                cmd = Float.parseFloat(snapshot.child("Cassava Mosaic Disease (CMD)").getValue().toString());
                healthy = Float.parseFloat(snapshot.child("Healthy").getValue().toString());

                entries.add(new BarEntry(cbb , 0));
                entries.add(new BarEntry(cbsd, 1));
                entries.add(new BarEntry(cgm, 2));
                entries.add(new BarEntry(cmd, 3));
                entries.add(new BarEntry(healthy, 4));

                barChart.notifyDataSetChanged();
                barChart.invalidate();

                System.out.println(entries.get(0));
                Log.v("Val", String.valueOf(cbb));
                Log.v("Val", String.valueOf(cgm));
                Log.v("Val", String.valueOf(cbsd));
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
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