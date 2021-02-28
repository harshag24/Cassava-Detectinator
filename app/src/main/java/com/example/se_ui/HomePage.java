package com.example.se_ui;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class HomePage extends AppCompatActivity {
    ImageButton logout;
    BottomNavigationView navigation;
    Button choosepic  , next;
    ImageView selectedpic;
    ProgressDialog progressDialog;
    StorageReference mStorageRef;
    Uri selectedImage;
    FirebaseUser user;
    DatabaseReference databaseReference ;
    StorageReference Ref;
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

        mStorageRef= FirebaseStorage.getInstance().getReference("Images");
        progressDialog = new ProgressDialog(this);
        selectedpic = findViewById(R.id.selectedpic_imageview);
        choosepic = findViewById(R.id.selectpic_button);
        next = findViewById(R.id.nextTopage3_button);
        user = FirebaseAuth.getInstance().getCurrentUser();
        mStorageRef=FirebaseStorage.getInstance().getReference();

        choosepic.setOnClickListener(v -> {
            Intent galleryintent = new Intent(Intent.ACTION_PICK , MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(galleryintent , 1 );

            next.setOnClickListener(v1 -> {
                progressDialog.setMessage("Uploading...");
                progressDialog.show();
                Fileuploader();
            });

        });
    }

    private String getExtension(Uri uri)
    {
        ContentResolver cr = getContentResolver();
        MimeTypeMap mimeTypeMap= MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(cr.getType(uri));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode ==1 &&resultCode== RESULT_OK && data!=null)
        {
            selectedImage = data.getData();
            selectedpic.setImageURI(selectedImage);
        }
    }

    public void Fileuploader()
    {
        Ref = mStorageRef.child(System.currentTimeMillis()+"."+getExtension(selectedImage));
        Ref.putFile(selectedImage)
                .addOnSuccessListener(taskSnapshot -> {
//                    Toast.makeText(HomePage.this,"Successfully Uploaded",Toast.LENGTH_LONG).show();
                    Log.e("TAG", "onClick: "+user.getUid() );
//                  pred();
                    String[] url = addPrediction();
                    progressDialog.dismiss();
                    Intent intent = new Intent(HomePage.this , Result.class);
                    intent.putExtra("URL", url[0]);
                    finish();
                    startActivity(intent);
                })
                .addOnFailureListener(exception -> Toast.makeText(HomePage.this,"Upload Unsuccessful!! Please Try Again ",Toast.LENGTH_LONG).show());
    }

    public String[] addPrediction()
    {
        final String[] url_main = new String[1];
        Ref.getDownloadUrl().addOnSuccessListener(uri -> {
            Log.d("TAG", String.valueOf(uri));

            String url = String.valueOf(uri);
            url_main[0] = url;
            String  current_date;
            Calendar calendar = Calendar.getInstance();
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd-HH:mm:ss:SSS");
            current_date = simpleDateFormat.format(calendar.getTime());

            HashMap<String, Object> add_product = new HashMap<>();
//          add_product.put("prediction" , prediction);
            add_product.put("url" , url);

            databaseReference = FirebaseDatabase.getInstance().getReference().child("Prediction").child(user.getUid()).child(current_date);
            databaseReference.updateChildren(add_product);
        });
        return url_main;
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