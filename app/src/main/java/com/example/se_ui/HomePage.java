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
import android.view.View;
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

public class HomePage extends AppCompatActivity {
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
    private  int imageSizeX, imageSizeY;
    private TensorBuffer outputProbabilityBuffer;
    private TensorProcessor probabilityProcessor;
    private static final float IMAGE_MEAN = 0.0f, IMAGE_STD = 1.0f, PROBABILITY_MEAN = 0.0f, PROBABILITY_STD = 255.0f;
    private Bitmap bitmap;
    private List<String> labels;
    String prediction = "";
    int imgcheck=0;
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
            Intent intent = new Intent(HomePage.this, About_Cassava.class);
            startActivity(intent);
        });

        //Logout Button
        logout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(HomePage.this , Login.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            Toast.makeText(HomePage.this, "Successfully Logged Out", Toast.LENGTH_LONG).show();
            finish();
            startActivity(intent);
        });

        //Choose Pic Button
        choosepic.setOnClickListener(v -> {
            Intent galleryintent = new Intent(Intent.ACTION_PICK , MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(galleryintent , 1 );
        });

        //tflite intialization
        try{
            tflite=new Interpreter(loadmodelfile(this));
        }catch (Exception e) {
            e.printStackTrace();
        }

        //Submit Button
        submit_pic.setOnClickListener(v -> {
            if(imgcheck==0){
                Toast.makeText(HomePage.this,"Please Select a Image",Toast.LENGTH_LONG).show();
            }
            else {
                progressDialog.setMessage("Processing...");
                progressDialog.show();
                prediction();
            updateAnalytics();
                Fileuploader();
            }
        });
    }

    public void prediction(){
        int imageTensorIndex = 0;
        int[] imageShape = tflite.getInputTensor(imageTensorIndex).shape(); // {1, height, width, 3}
        imageSizeY = imageShape[1];
        imageSizeX = imageShape[2];
        DataType imageDataType = tflite.getInputTensor(imageTensorIndex).dataType();

        int probabilityTensorIndex = 0;
        int[] probabilityShape =
                tflite.getOutputTensor(probabilityTensorIndex).shape(); // {1, NUM_CLASSES}
        DataType probabilityDataType = tflite.getOutputTensor(probabilityTensorIndex).dataType();

        inputImageBuffer = new TensorImage(imageDataType);
        outputProbabilityBuffer = TensorBuffer.createFixedSize(probabilityShape, probabilityDataType);
        probabilityProcessor = new TensorProcessor.Builder().add(getPostprocessNormalizeOp()).build();

        inputImageBuffer = loadImage(bitmap);

        tflite.run(inputImageBuffer.getBuffer(),outputProbabilityBuffer.getBuffer().rewind());

        showresult();
    }

    private TensorImage loadImage(final Bitmap bitmap) {
        // Loads bitmap into a TensorImage.
        inputImageBuffer.load(bitmap);
        // Creates processor for the TensorImage.
        int cropSize = Math.min(bitmap.getWidth(), bitmap.getHeight());
        // TODO(b/143564309): Fuse ops inside ImageProcessor.
        ImageProcessor imageProcessor =
                new ImageProcessor.Builder()
                        .add(new ResizeOp(380, 380, ResizeOp.ResizeMethod.NEAREST_NEIGHBOR))
                        .build();
        return imageProcessor.process(inputImageBuffer);
//                           .add(new ResizeWithCropOrPadOp(cropSize, cropSize))
//                                .add(getPreprocessNormalizeOp())

    }

    private MappedByteBuffer loadmodelfile(Activity activity) throws IOException {
        AssetFileDescriptor fileDescriptor=activity.getAssets().openFd("model.tflite");
        FileInputStream inputStream=new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel=inputStream.getChannel();
        long startoffset = fileDescriptor.getStartOffset();
        long declaredLength=fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY,startoffset,declaredLength);
    }

    private TensorOperator getPreprocessNormalizeOp() {
        return new NormalizeOp(IMAGE_MEAN, IMAGE_STD);
    }

    private TensorOperator getPostprocessNormalizeOp(){
        return new NormalizeOp(PROBABILITY_MEAN, PROBABILITY_STD);
    }

    private void showresult(){
        try{
            labels = FileUtil.loadLabels(this,"labels.txt");
        }catch (Exception e){
            e.printStackTrace();
        }
        Map<String, Float> labeledProbability =
                new TensorLabel(labels, probabilityProcessor.process(outputProbabilityBuffer))
                        .getMapWithFloatValue();
        float maxValueInMap =(Collections.max(labeledProbability.values()));

        for (Map.Entry<String, Float> entry : labeledProbability.entrySet()) {
            Log.v("Preds", String.valueOf(entry.getValue())) ;
            if (entry.getValue() == maxValueInMap) {
                prediction = entry.getKey();
                prediction_confidence = maxValueInMap;
            }
        }
    }

    public void updateAnalytics(){
//        dR_analytics = (DatabaseReference) FirebaseDatabase.getInstance().getReference("Analytics").orderByChild(prediction);
        dR_analytics =  FirebaseDatabase.getInstance().getReference("Analytics");

//        dR_analytics.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                 val = Integer.parseInt(Objects.requireNonNull(snapshot.getValue()).toString());
//                 Log.v("Val", String.valueOf(snapshot.getValue()));
//                val++;
//            }
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//
//            }
//        });
        dR_analytics.addListenerForSingleValueEvent(new ValueEventListener() {
            int val;
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for(DataSnapshot snapshot: dataSnapshot.getChildren()){
                    if(prediction.equals(snapshot.getKey())){
                        Log.v("Val", String.valueOf(snapshot.getKey()));
                        val = Integer.parseInt(snapshot.getValue().toString());
                        val += 1 ;
                        dR_analytics.child(prediction).setValue(val);
                        Log.v("Val", String.valueOf(val));
                        break;
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }

        });

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
                imgcheck=1;
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
                    Intent intent = new Intent(HomePage.this , Result.class);
                    Log.v("TF", Float.toString(prediction_confidence));
                    intent.putExtra("Prediction", prediction);
                    intent.putExtra("Confidence", Float.toString(prediction_confidence));
                    intent.putExtra("ImageUri", String.valueOf(imageuri));
                    startActivity(intent);
                })
                .addOnFailureListener(exception -> Toast.makeText(HomePage.this,"Upload Unsuccessful!! Please Try Again ",Toast.LENGTH_LONG).show());
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