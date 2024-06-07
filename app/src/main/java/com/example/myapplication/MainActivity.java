package com.example.myapplication;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.opencv.android.OpenCVLoader;

public class MainActivity extends AppCompatActivity {

    static{
       if(OpenCVLoader.initDebug()){
           Log.d("MainActivity","OpenCV is Loaded");
       }
       else{
           Log.d("MainActivity", "OpenCV failed to load");
       }
    }

    private Button camera_button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        camera_button=findViewById(R.id.camera_button);
        camera_button.setOnClickListener(v -> startActivity(new Intent(MainActivity.this,CameraActivity.class).addFlags(
                Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP)));
    }
}