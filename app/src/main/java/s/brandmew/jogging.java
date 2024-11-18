package s.brandmew;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.SystemClock;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class jogging extends AppCompatActivity implements SensorEventListener {
    private static final int REQUEST_CODE_ACTIVITY_RECOGNITION = 100;
    private SensorManager sensorManager;
    private Sensor stepCounterSensor;
    private boolean isPermissionGranted = false;

    private TextView stepCountTextView, timerTextView, caloriesBurnedTextView;
    private Button startTimerButton, stopTimerButton, resetButton;

    private boolean isJogging = false;
    private long startTime, elapsedTime;
    private int initialStepCount = 0;
    private int stepCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_jogging);
        // Initialize Views
        stepCountTextView = findViewById(R.id.stepCountTextView);
        timerTextView = findViewById(R.id.timerTextView);
        caloriesBurnedTextView = findViewById(R.id.caloriesBurnedTextView);
        startTimerButton = findViewById(R.id.startTimerButton);
        stopTimerButton = findViewById(R.id.stopTimerButton);
        resetButton = findViewById(R.id.resetButton);

        // Setup Sensor Manager
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        // Check Permissions for Android 10 and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACTIVITY_RECOGNITION}, REQUEST_CODE_ACTIVITY_RECOGNITION);
            } else {
                isPermissionGranted = true;
                initStepCounter();
            }
        } else {
            // Permission is not required for versions below Android 10
            isPermissionGranted = true;
            initStepCounter();
        }

        // Set up Buttons
        startTimerButton.setOnClickListener(v -> startJogging());
        stopTimerButton.setOnClickListener(v -> stopJogging());
        resetButton.setOnClickListener(v -> resetJogging());
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

    private void startJogging() {
        if (!isJogging) {
            isJogging = true;
            startTime = SystemClock.elapsedRealtime();
            initialStepCount = stepCount;
            Toast.makeText(this, "Jogging Started!", Toast.LENGTH_SHORT).show();
        }
    }

    private void stopJogging() {
        if (isJogging) {
            isJogging = false;
            elapsedTime = SystemClock.elapsedRealtime() - startTime;
            calculateCaloriesBurned();
            Toast.makeText(this, "Jogging Stopped!", Toast.LENGTH_SHORT).show();
        }
    }

    private void resetJogging() {
        isJogging = false;
        stepCount = 0;
        initialStepCount = 0;
        elapsedTime = 0;
        timerTextView.setText("Time: 00:00");
        stepCountTextView.setText("Steps: 0");
        caloriesBurnedTextView.setText("Calories: 0");
        Toast.makeText(this, "Jogging Reset!", Toast.LENGTH_SHORT).show();
    }

    private void calculateCaloriesBurned() {
        int stepsTaken = stepCount - initialStepCount;
        double caloriesBurned = stepsTaken * 0.05; // Approximate calories burned per step
        caloriesBurnedTextView.setText(String.format("Calories: %.2f", caloriesBurned));
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_STEP_COUNTER) {
            stepCount = (int) event.values[0];
            stepCountTextView.setText("Steps: " + (stepCount - initialStepCount));
            if (isJogging) {
                elapsedTime = SystemClock.elapsedRealtime() - startTime;
                int seconds = (int) (elapsedTime / 1000) % 60;
                int minutes = (int) (elapsedTime / 1000) / 60;
                timerTextView.setText(String.format("Time: %02d:%02d", minutes, seconds));
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isPermissionGranted && stepCounterSensor != null) {
            sensorManager.registerListener(this, stepCounterSensor, SensorManager.SENSOR_DELAY_UI);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
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