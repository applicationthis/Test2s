package s.brandmew;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity implements SensorEventListener {
    private static final int REQUEST_CODE_ACTIVITY_RECOGNITION = 1;
    private SensorManager sensorManager;
    private Sensor stepCounterSensor;
    private TextView stepCountTextView;
    private boolean isPermissionGranted = false;
    private Button joggingButton;
    Button jogging1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        joggingButton = findViewById(R.id.joggingButton);
        jogging1=findViewById(R.id.joggingButton1);
        //
        jogging1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, pushups.class);
                startActivity(intent);
            }
        });
        //
        joggingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, jogging.class);
                startActivity(intent);
            }
        });
        // Initialize UI elements

        stepCountTextView = findViewById(R.id.stepCountTextView);
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        // Check for permission if API level >= 29 (Android 10+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACTIVITY_RECOGNITION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACTIVITY_RECOGNITION}, REQUEST_CODE_ACTIVITY_RECOGNITION);
            } else {
                isPermissionGranted = true;
                initStepCounter();
            }
        } else {
            // Permission not required below Android 10
            isPermissionGranted = true;
            initStepCounter();
        }
    }

    private void initStepCounter() {
        if (isPermissionGranted) {
            stepCounterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
            if (stepCounterSensor != null) {
                sensorManager.registerListener(this, stepCounterSensor, SensorManager.SENSOR_DELAY_UI);
            } else {
                Toast.makeText(this, "Step counter sensor not available!", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_STEP_COUNTER) {
            int steps = (int) event.values[0];
            stepCountTextView.setText("Steps: " + steps);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        if (sensor.getType() == Sensor.TYPE_STEP_COUNTER) {
            switch (accuracy) {
                case SensorManager.SENSOR_STATUS_UNRELIABLE:
                    Toast.makeText(this, "Step counter accuracy is unreliable!", Toast.LENGTH_SHORT).show();
                    break;

                case SensorManager.SENSOR_STATUS_ACCURACY_LOW:
                    Toast.makeText(this, "Step counter accuracy is low. Try recalibrating.", Toast.LENGTH_SHORT).show();
                    break;

                case SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM:
                    Toast.makeText(this, "Step counter accuracy is medium. Working fine.", Toast.LENGTH_SHORT).show();
                    break;

                case SensorManager.SENSOR_STATUS_ACCURACY_HIGH:
                    Toast.makeText(this, "Step counter accuracy is high. All good!", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
        // No action needed for accuracy changes in this context
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Re-register the listener if permission is granted
        if (isPermissionGranted && stepCounterSensor != null) {
            sensorManager.registerListener(this, stepCounterSensor, SensorManager.SENSOR_DELAY_UI);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Unregister the listener to save battery
        if (isPermissionGranted) {
            sensorManager.unregisterListener(this);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_ACTIVITY_RECOGNITION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                isPermissionGranted = true;
                initStepCounter();
            } else {
                Toast.makeText(this, "Permission denied to access step counter", Toast.LENGTH_SHORT).show();
            }
        }
    }
}