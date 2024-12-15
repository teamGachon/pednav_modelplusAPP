package com.example.modeltest;

import android.os.Bundle;
import android.widget.Switch;
import androidx.appcompat.app.AppCompatActivity;

public class SettingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting_page);

        // Switches
        Switch switchGPS = findViewById(R.id.switch_gps);
        Switch switchBackgroundRun = findViewById(R.id.switch_background_run);
        Switch switchAutoNoise = findViewById(R.id.switch_auto_noise);
        Switch switchMicrophone = findViewById(R.id.switch_microphone);
        Switch switchVehicleAlerm = findViewById(R.id.switch_vehicle_detection);

        // Listener for toggling states
        setToggleColor(switchGPS);
        setToggleColor(switchBackgroundRun);
        setToggleColor(switchAutoNoise);
        setToggleColor(switchMicrophone);
        setToggleColor(switchVehicleAlerm);
    }

    private void setToggleColor(Switch switchToggle) {
        switchToggle.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                switchToggle.setThumbTintList(getColorStateList(R.color.green));
                switchToggle.setTrackTintList(getColorStateList(R.color.green));
            } else {
                switchToggle.setThumbTintList(getColorStateList(R.color.gray));
                switchToggle.setTrackTintList(getColorStateList(R.color.gray));
            }
        });
    }
}
