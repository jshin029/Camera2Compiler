package com.jshin029.camera2compiler;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;

import java.io.IOException;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private Button snapBtn;
    ImageView imageView;
    TextView textView;
    Bitmap bitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imageView = (ImageView)findViewById(R.id.imageView);
        textView = (TextView)findViewById(R.id.textView);
    }

    public void detect(View v)
    {
        if(bitmap==null)
        {
            Toast.makeText(getApplicationContext(),"Bitmap is null",Toast.LENGTH_LONG).show();
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
        {
            for(FirebaseVisionText.TextBlock block:firebaseVisionText.getTextBlocks())
            {
                String text = block.getText();
                textView.setText(text);
            }
        }
    }

    public void pick_image(View v)
    {
        Intent i = new Intent(Intent.ACTION_PICK,MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        startActivityForResult(i,1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 1 && resultCode == RESULT_OK)
        {
            Uri uri = data.getData();
            try
            {
                bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(),uri);
                imageView.setImageBitmap(bitmap);
            }catch(IOException e)
            {
                e.printStackTrace();
            }


        }
    }
}
