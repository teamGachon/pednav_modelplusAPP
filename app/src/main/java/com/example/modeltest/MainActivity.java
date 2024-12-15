package com.example.modeltest;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Navigate to Detection Test Activity
        TextView btnGoDetectionTest = findViewById(R.id.btnGoDetectionTest);
        btnGoDetectionTest.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent intent = new Intent(MainActivity.this, ModelTestActivity.class);
                startActivity(intent);
            }
        });

        // Navigate to Navigation Test Activity
        TextView btnGoNavigationTest = findViewById(R.id.btnGoNavigationTest);
        btnGoNavigationTest.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent intent = new Intent(MainActivity.this, TMapAPIActivity.class);
                startActivity(intent);
            }
        });

        // Navigate to Setting Test Activity
        TextView btnGoSettingPageTest = findViewById(R.id.btnGoSettingPageTest);
        btnGoSettingPageTest.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent intent = new Intent(MainActivity.this, SettingActivity.class);
                startActivity(intent);
            }
        });
    }
}