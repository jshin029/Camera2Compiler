package com.jshin029.camera2compiler;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    Button cameraBtn, compileBtn;
    ImageView imageView;
    TextView textView;
    Bitmap bitmap;
    EditText et_name;
    ArrayList<String> detectedText = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imageView = findViewById(R.id.imageView);
        textView =  findViewById(R.id.textView);
        cameraBtn = findViewById(R.id.cameraBtn);
        et_name = findViewById(R.id.et_name);
        compileBtn = findViewById(R.id.compileBtn);
        compileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String onlineIDE = "https://www.codechef.com/ide";
                Uri web_address = Uri.parse(onlineIDE);

                Intent gotoCompiler = new Intent(Intent.ACTION_VIEW, web_address);
                if (gotoCompiler.resolveActivity(getPackageManager()) != null){
                    startActivity(gotoCompiler);
                }
            }
        });

        cameraBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchTakePictureIntent();
            }
        });

        if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1000);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case 1000:
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    Toast.makeText(this, "Permission granted!", Toast.LENGTH_SHORT).show();
                }
                else{
                    Toast.makeText(this,"Permission not granted!", Toast.LENGTH_SHORT).show();
                    finish();
                }
        }
    }

    public void save(View v)
    {
        String filename = et_name.getText().toString();
        String fileName = filename + ".txt";
        File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), fileName);


        try{
                        FileOutputStream fos = new FileOutputStream(file);
                        for (int i = 0; i <detectedText.size(); i++){
                            fos.write(detectedText.get(i).getBytes());
                        }
                        fos.close();
                        Toast.makeText(this, "Saved!", Toast.LENGTH_SHORT).show();
                } catch (FileNotFoundException e){
                    e.printStackTrace();
                    Toast.makeText(this, "File not found!", Toast.LENGTH_SHORT).show();
                } catch(IOException e){
                    e.printStackTrace();
                    Toast.makeText(this, "Error saving!", Toast.LENGTH_SHORT).show();
                }

    }
    public void detect(View v)
    {
        if(bitmap==null)
        {
            Toast.makeText(getApplicationContext(),"No image uploaded",Toast.LENGTH_LONG).show();
        }
        else
        {
            final FirebaseVisionImage firebaseVisionImage = FirebaseVisionImage.fromBitmap(bitmap);

            FirebaseVisionTextRecognizer TextRecognizer = FirebaseVision.getInstance().getOnDeviceTextRecognizer();

            TextRecognizer.processImage(firebaseVisionImage)
                    .addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
                        @Override
                        public void onSuccess(FirebaseVisionText firebaseVisionText) {
                            process_text(firebaseVisionText);
                        }
                    });
        }
    }

        private void process_text(FirebaseVisionText firebaseVisionText)
    {
        List<FirebaseVisionText.TextBlock> blocks = firebaseVisionText.getTextBlocks();
        if(blocks.size()==0)
        {
            Toast.makeText(getApplicationContext(),"No text detected",Toast.LENGTH_LONG).show();
        }
        else
        {   StringBuilder completeText = new StringBuilder();
            for(FirebaseVisionText.TextBlock block:firebaseVisionText.getTextBlocks())
            {
                String text = block.getText();
                detectedText.add(block.getText());
                completeText.append(text);
            }
            textView.setText(completeText);
        }
    }

    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int REQUEST_IMAGE_GALLERY = 2;

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, 2);
        }
    }


    public void pick_image(View v)
    {
        Intent imagePickIntent = new Intent(Intent.ACTION_PICK,MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        startActivityForResult(imagePickIntent,1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK)
        {

            Uri uri = null;
            if (data != null) {
                uri = data.getData();
            }
            try
            {
                bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(),uri);
                imageView.setImageBitmap(bitmap);
            }catch(IOException e)
            {
                e.printStackTrace();
            }
        }
        else if (requestCode == REQUEST_IMAGE_GALLERY && resultCode == RESULT_OK) {
            //assert data != null;
            Bundle extras;
            if (data != null) {
                extras = data.getExtras();
                if (extras != null) {
                    bitmap = (Bitmap) extras.get("data");
                }
                imageView.setImageBitmap(bitmap);
            }
        }
    }
}
