package com.example.ocrdemo;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;

import java.io.File;
import java.util.List;

public class MainActivity extends AppCompatActivity {


    Button captureButt, detectButt;
    ImageView cameraDisp;
    Bitmap imageBitmap=null; //imageToShow
    LinearLayout buttonLayout;
    LinearLayout imageLayout;
    public static List<FirebaseVisionText.TextBlock> blocks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        captureButt = findViewById(R.id.captureButt);
        detectButt = findViewById(R.id.detectButt);

        cameraDisp = findViewById(R.id.camera);
        buttonLayout=findViewById(R.id.buttonLayout);
        captureButt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*Get permission to save to disk and use camera*/
                if (ContextCompat.checkSelfPermission(MainActivity.this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {

                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            1);
                }

                if (ContextCompat.checkSelfPermission(MainActivity.this,
                        Manifest.permission.CAMERA)
                        != PackageManager.PERMISSION_GRANTED) {

                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{Manifest.permission.CAMERA},
                            1);
                }
                else{
                    LinearLayout.LayoutParams layoutParams=new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT,4);

                    buttonLayout.setLayoutParams(layoutParams);
                    dispatchTakePictureIntent(); //start camera process
                }
            }
        });

        detectButt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(imageBitmap==null) {
                    Toast.makeText(MainActivity.this,"Please click a picture first!",Toast.LENGTH_LONG).show();
                }
                else
                  detectText(); //get text from image
            }
        });

    }

    static final int REQUEST_IMAGE_CAPTURE = 1;
    private Uri mImageUri; //uri of image
    private void dispatchTakePictureIntent() {

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File photo;
        try
        {
            photo = this.createTemporaryFile("picture", ".jpg"); //where to save file taken from camera
            photo.delete();
        }
        catch(Exception e)
        {
            Log.v("PHOTO ERROR : ", "Can't create file to take picture!");
            Toast.makeText(MainActivity.this, "Couldn't write to SD Card, please check SD Card", Toast.LENGTH_SHORT).show();
            return;
        }
        mImageUri = Uri.fromFile(photo);

        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, mImageUri);

        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }


    }

    private File createTemporaryFile(String picture, String s)throws Exception {
        File tempDir= Environment.getExternalStorageDirectory();
        tempDir=new File(tempDir.getAbsolutePath()+"/.temp/");
        if(!tempDir.exists())
        {
            tempDir.mkdirs();
        }
        return File.createTempFile(picture, s, tempDir);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {


        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            this.getContentResolver().notifyChange(mImageUri, null);
            ContentResolver cr = this.getContentResolver();

            try
            {
                imageBitmap = android.provider.MediaStore.Images.Media.getBitmap(cr, mImageUri);
                cameraDisp.setImageBitmap(imageBitmap);
            }
            catch (Exception e)
            {
                Toast.makeText(this, "Failed to load", Toast.LENGTH_SHORT).show();
                Log.d("ERROR", "Failed to load", e);
            }
        }
    }

    private void detectText()
    {


        FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(imageBitmap);
        FirebaseVisionTextRecognizer detector = FirebaseVision.getInstance()
                .getOnDeviceTextRecognizer();

        Task<FirebaseVisionText> result =
                detector.processImage(image)
                        .addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
                            @Override
                            public void onSuccess(FirebaseVisionText firebaseVisionText) {
                                processText(firebaseVisionText);
                            }
                        })
                        .addOnFailureListener(
                                new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {

                                    }
                                });



    }
    private void processText(FirebaseVisionText firebaseVisionText)
    {
        blocks = firebaseVisionText.getTextBlocks();

        if(blocks.size()==0)
        {
            Toast.makeText(MainActivity.this,"NO TEXT FOUND, PLEASE TRY TAKING ANOTHER PICTURE",Toast.LENGTH_LONG).show();
            return;
        }

        Intent outputActivity=new Intent(MainActivity.this,Output.class);
        startActivity(outputActivity);
    }
}
