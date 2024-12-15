package com.example.modeltest;

import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class SettingActivity extends AppCompatActivity {

    private Switch switchGPS;
    private Switch switchBackgroundRun;
    private Switch switchMicrophone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting_page);


        TextView btnGoBack = findViewById(R.id.btnGoBack);

        btnGoBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SettingActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

        // Switches
        switchGPS = findViewById(R.id.switch_gps);
        switchBackgroundRun = findViewById(R.id.switch_background_run);
        switchMicrophone = findViewById(R.id.switch_microphone);

        // GPS Switch Listener
        switchGPS.setOnCheckedChangeListener((CompoundButton buttonView, boolean isChecked) -> {
            if (isChecked) {
                // Enable GPS
                enableGPS();
            } else {
                // Disable GPS
                disableGPS();
            }
        });

        // Background Run Switch Listener
        switchBackgroundRun.setOnCheckedChangeListener((CompoundButton buttonView, boolean isChecked) -> {
            if (isChecked) {
                enableBackgroundRun();
            } else {
                disableBackgroundRun();
            }
        });

        // Microphone Switch Listener
        switchMicrophone.setOnCheckedChangeListener((CompoundButton buttonView, boolean isChecked) -> {
            if (isChecked) {
                enableMicrophone();
            } else {
                disableMicrophone();
            }
        });
    }

    private void enableGPS() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(intent);
            Toast.makeText(this, "Redirecting to GPS settings to enable GPS", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "GPS is already enabled", Toast.LENGTH_SHORT).show();
        }
    }

    private void disableGPS() {
        // Directly disabling GPS programmatically is not allowed for security reasons.
        Toast.makeText(this, "Please manually disable GPS from settings", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        startActivity(intent);
    }

    private void enableBackgroundRun() {
        // Simulate enabling background run functionality
        Toast.makeText(this, "Background Run Enabled", Toast.LENGTH_SHORT).show();
        // Add your logic here for enabling background functionality (e.g., background services)
    }

    private void disableBackgroundRun() {
        // Simulate disabling background run functionality
        Toast.makeText(this, "Background Run Disabled", Toast.LENGTH_SHORT).show();
        // Add your logic here for disabling background functionality
    }

    private void enableMicrophone() {
        // Simulate enabling microphone access
        Toast.makeText(this, "Microphone Enabled", Toast.LENGTH_SHORT).show();
        // Add your logic here for enabling microphone
    }

    private void disableMicrophone() {
        // Simulate disabling microphone access
        Toast.makeText(this, "Microphone Disabled", Toast.LENGTH_SHORT).show();
        // Add your logic here for disabling microphone
    }
}