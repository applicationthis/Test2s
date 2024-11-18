package s.brandmew;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class pushups extends AppCompatActivity implements SensorEventListener {
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private boolean isCounting = false;
    private int pushUpCount = 0;

    private TextView pushUpCountTextView, timerTextView;
    private Button startButton, stopButton, resetButton;

    private float lastZ = 0;
    private boolean goingDown = false;

    private long startTime = 0L;
    private Handler timerHandler = new Handler();
    private Runnable timerRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pushups);
        pushUpCountTextView = findViewById(R.id.pushUpCountTextView);
        timerTextView = findViewById(R.id.timerTextView);
        startButton = findViewById(R.id.startButton);
        stopButton = findViewById(R.id.stopButton);
        resetButton = findViewById(R.id.resetButton);

        // Initialize Sensor Manager
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        // Start Button Listener
        startButton.setOnClickListener(v -> {
            if (!isCounting) {
                isCounting = true;
                pushUpCount = 0;
                startTime = SystemClock.elapsedRealtime();
                sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
                startTimer();
                Toast.makeText(this, "Push-up detection started!", Toast.LENGTH_SHORT).show();
            }
        });

        // Stop Button Listener
        stopButton.setOnClickListener(v -> {
            if (isCounting) {
                isCounting = false;
                sensorManager.unregisterListener(this);
                stopTimer();
                Toast.makeText(this, "Push-up detection stopped!", Toast.LENGTH_SHORT).show();
            }
        });

        // Reset Button Listener
        resetButton.setOnClickListener(v -> {
            isCounting = false;
            pushUpCount = 0;
            timerTextView.setText("Time: 00:00");
            pushUpCountTextView.setText("Push-ups: 0");
            sensorManager.unregisterListener(this);
            stopTimer();
            Toast.makeText(this, "Push-up counter reset!", Toast.LENGTH_SHORT).show();
        });

        // Timer Runnable
        timerRunnable = new Runnable() {
            @Override
            public void run() {
                long elapsedTime = SystemClock.elapsedRealtime() - startTime;
                int seconds = (int) (elapsedTime / 1000) % 60;
                int minutes = (int) (elapsedTime / 1000) / 60;
                timerTextView.setText(String.format("Time: %02d:%02d", minutes, seconds));
                timerHandler.postDelayed(this, 1000);
            }
        };
    }

    private void startTimer() {
        timerHandler.postDelayed(timerRunnable, 0);
    }

    private void stopTimer() {
        timerHandler.removeCallbacks(timerRunnable);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float z = event.values[2];

            if (goingDown && z > lastZ && z > 8.0) { // Threshold for "going up"
                pushUpCount++;
                pushUpCountTextView.setText("Push-ups: " + pushUpCount);
                goingDown = false;
            } else if (z < lastZ && z < 2.0) { // Threshold for "going down"
                goingDown = true;
            }

            lastZ = z;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Not used in this example
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isCounting) {
            sensorManager.unregisterListener(this);
            stopTimer();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isCounting) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
            startTimer();
        }
    }
}