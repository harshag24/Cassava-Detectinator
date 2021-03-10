package com.example.se_ui;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.support.common.FileUtil;
import org.tensorflow.lite.support.common.TensorOperator;
import org.tensorflow.lite.support.common.TensorProcessor;
import org.tensorflow.lite.support.common.ops.DequantizeOp;
import org.tensorflow.lite.support.common.ops.NormalizeOp;
import org.tensorflow.lite.support.image.ImageProcessor;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.image.ops.ResizeOp;
import org.tensorflow.lite.support.image.ops.ResizeWithCropOrPadOp;
import org.tensorflow.lite.support.label.TensorLabel;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class Test extends AppCompatActivity {

    float prediction_confidence;
    ImageButton logout;
    BottomNavigationView navigation;
    Button choosepic  , submit_pic, about;
    ImageView selectedpic;
    ProgressDialog progressDialog;
    StorageReference mStorageRef, Ref;
    Uri imageuri;
    FirebaseUser user;
    DatabaseReference databaseReference, dR_analytics, dR_Welcome;
    protected Interpreter tflite;
    private TensorImage inputImageBuffer;
    private Bitmap bitmap;
    String prediction = "";
    int val=0;
    TextView welcome;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);

        welcome = findViewById(R.id.welcometext);
        logout = findViewById(R.id.logout_button);
        about = findViewById(R.id.about_cassava);
        navigation = findViewById(R.id.bottomNavigationView_profile);
        navigation.getMenu().findItem(R.id.bottomnav_home).setChecked(true);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        mStorageRef= FirebaseStorage.getInstance().getReference("Images");
        progressDialog = new ProgressDialog(this);
        selectedpic = findViewById(R.id.input_image);
        choosepic = findViewById(R.id.selectpic_button);
        submit_pic = findViewById(R.id.submit_img);
        user = FirebaseAuth.getInstance().getCurrentUser();
        mStorageRef=FirebaseStorage.getInstance().getReference();

        user = FirebaseAuth.getInstance().getCurrentUser();
        dR_Welcome = FirebaseDatabase.getInstance().getReference().child("Users").child(user.getUid());
        dR_Welcome.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String dispname = Objects.requireNonNull(dataSnapshot.child("name").getValue()).toString().trim();
                String[] first_name = dispname.split(" ");
                welcome.setText(first_name[0]);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

        //About Cassava Button
        about.setOnClickListener(v -> {
            Intent intent = new Intent(Test.this, About_Cassava.class);
            startActivity(intent);
        });

        //Logout Button
        logout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(Test.this , Login.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            Toast.makeText(Test.this, "Successfully Logged Out", Toast.LENGTH_LONG).show();
            finish();
            startActivity(intent);
        });

        //Choose Pic Button
        choosepic.setOnClickListener(v -> {
            Intent galleryintent = new Intent(Intent.ACTION_PICK , MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(galleryintent , 1 );
        });


        //Submit Button
        submit_pic.setOnClickListener(v -> {
            progressDialog.setMessage("Processing...");
            progressDialog.show();
            prediction();
//            updateAnalytics();
            Fileuploader();

        });
    }

    public void prediction(){
//        int imageTensorIndex = 0;
//
//        DataType imageDataType = tflite.getInputTensor(imageTensorIndex).dataType();
//
//        int probabilityTensorIndex = 0;
//
//        DataType probabilityDataType = tflite.getOutputTensor(probabilityTensorIndex).dataType();
        ImageProcessor imageProcessor =
                new ImageProcessor.Builder()
                        .add(new ResizeOp(380, 380, ResizeOp.ResizeMethod.BILINEAR))
                        .build();
        inputImageBuffer = new TensorImage(DataType.FLOAT32);
        inputImageBuffer.load(bitmap);
        inputImageBuffer = imageProcessor.process(inputImageBuffer);
//        DataType probabilityDataType = tflite.getOutputTensor(0).dataType();
        TensorBuffer probabilityBuffer =
                TensorBuffer.createFixedSize(new int[]{1, 5}, DataType.FLOAT32);
        try{
            MappedByteBuffer tfliteModel
                    = FileUtil.loadMappedFile(Test.this,
                    "model.tflite");
            Interpreter tflite = new Interpreter(tfliteModel);
        } catch (IOException e){
            Log.e("tfliteSupport", "Error reading model", e);
        }

// Running inference
        if(null != tflite) {
            tflite.run(inputImageBuffer.getBuffer(), probabilityBuffer.getBuffer());
        }

        final String ASSOCIATED_AXIS_LABELS = "labels.txt";
        List<String> associatedAxisLabels = null;

        try {
            associatedAxisLabels = FileUtil.loadLabels(this, ASSOCIATED_AXIS_LABELS);
        } catch (IOException e) {
            Log.e("tfliteSupport", "Error reading label file", e);
        }

        TensorProcessor probabilityProcessor =
                new TensorProcessor.Builder().add(new NormalizeOp(0, 255)).build();

        if (null != associatedAxisLabels) {
            // Map of labels and their corresponding probability
            TensorLabel labels = new TensorLabel(associatedAxisLabels,
                    probabilityProcessor.process(probabilityBuffer));

            // Create a map to access the result based on label
            Map<String, Float> floatMap = labels.getMapWithFloatValue();

            float maxValueInMap =(Collections.max(floatMap.values()));
            Log.v("Preds", String.valueOf(maxValueInMap)) ;
            for (Map.Entry<String, Float> entry : floatMap.entrySet()) {
                Log.v("Preds1", String.valueOf(entry.getValue())) ;
                if (entry.getValue() == maxValueInMap) {
                    prediction = entry.getKey();
                    prediction_confidence = maxValueInMap;
                }
            }
        }
    }


    public void updateAnalytics(){
//        dR_analytics = (DatabaseReference) FirebaseDatabase.getInstance().getReference("Analytics").orderByChild(prediction);

        dR_analytics =  FirebaseDatabase.getInstance().getReference("Analytics").child(prediction);

        dR_analytics.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                val = Integer.parseInt(snapshot.getValue().toString());
                Log.v("Val", String.valueOf(snapshot.getValue()));
                val++;
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        dR_analytics.setValue(val);
    }

    private String getExtension(Uri uri)
    {
        ContentResolver cr = getContentResolver();
        MimeTypeMap mimeTypeMap= MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(cr.getType(uri));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==1 && resultCode==RESULT_OK && data!=null) {
            imageuri = data.getData();
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageuri);
                selectedpic.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void Fileuploader()
    {
        Ref = mStorageRef.child(System.currentTimeMillis()+"."+getExtension(imageuri));
        Ref.putFile(imageuri)
                .addOnSuccessListener(taskSnapshot -> {
//                    Toast.makeText(HomePage.this,"Successfully Uploaded",Toast.LENGTH_LONG).show();
                    Log.e("TAG", "onClick: "+user.getUid() );
                    addData();
                    progressDialog.dismiss();
                    Intent intent = new Intent(Test.this , Result.class);
                    Log.v("TF", Float.toString(prediction_confidence));
                    intent.putExtra("Prediction", prediction);
                    intent.putExtra("Confidence", Float.toString(prediction_confidence));
                    intent.putExtra("ImageUri", String.valueOf(imageuri));
                    startActivity(intent);
                })
                .addOnFailureListener(exception -> Toast.makeText(Test.this,"Upload Unsuccessful!! Please Try Again ",Toast.LENGTH_LONG).show());
    }

    public void addData()
    {
        Ref.getDownloadUrl().addOnSuccessListener(uri -> {
            Log.d("TAG", String.valueOf(uri));

            String url = String.valueOf(uri);
            String  current_date;
            Calendar calendar = Calendar.getInstance();
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd-HH:mm:ss:SSS");
            current_date = simpleDateFormat.format(calendar.getTime());

            HashMap<String, Object> add_product = new HashMap<>();
            add_product.put("prediction" , prediction);
            add_product.put("url" , url);

            databaseReference = FirebaseDatabase.getInstance().getReference().child("Prediction").child(user.getUid()).child(current_date);
            databaseReference.updateChildren(add_product);
        });
    }

    private final BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = item -> {
        switch (item.getItemId()) {
            case R.id.bottomnav_home:
                break;
            case R.id.bottomnav_profile:
                Intent intent = new Intent(Test.this , UserProfile.class);
                finish();
                startActivity(intent);
                break;
            case R.id.bottomnav_prevpred:
                Intent intent1 = new Intent(Test.this , Prev_pred.class);
                startActivity(intent1);
                break;
            case R.id.bottomnav_analytics:
                Intent intent2 = new Intent(Test.this , Analytics.class);
                startActivity(intent2);
                break;
        }
        return false;
    };


}