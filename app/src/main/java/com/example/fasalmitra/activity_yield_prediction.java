package com.example.fasalmitra;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.FirebaseApp;

public class activity_yield_prediction extends AppCompatActivity {

    private Spinner spinnerGrowthStage, spinnerSoilQuality, spinnerWaterLevel;
    private Button btnPredictYield, btnUploadToFirebase;
    private TextView tvYieldPredictionResult;

    // Firebase Database reference
    private DatabaseReference databaseReference;
    private String predictedYield;

    // Set the Firebase Realtime Database URL manually
    FirebaseDatabase database = FirebaseDatabase.getInstance("https://fasalmitra-815ac-default-rtdb.asia-southeast1.firebasedatabase.app");

    // Now, you can use this database instance to interact with Firebase Realtime Database
    DatabaseReference yieldRef = database.getReference("yield_predictions");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_yield_prediction);

        // Initialize Firebase
        FirebaseApp.initializeApp(this);

        // Initialize Firebase Database
        databaseReference = yieldRef;

        // Initialize views
        spinnerGrowthStage = findViewById(R.id.spinnerGrowthStage);
        spinnerSoilQuality = findViewById(R.id.spinnerSoilQuality);
        spinnerWaterLevel = findViewById(R.id.spinnerWaterLevel);
        btnPredictYield = findViewById(R.id.btnPredictYield);
        btnUploadToFirebase = findViewById(R.id.btnUploadToFirebase);
        tvYieldPredictionResult = findViewById(R.id.tvYieldPredictionResult);

        setupSpinners();

        // Predict yield when button is clicked
        btnPredictYield.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String growthStage = spinnerGrowthStage.getSelectedItem().toString();
                String soilQuality = spinnerSoilQuality.getSelectedItem().toString();
                String waterLevel = spinnerWaterLevel.getSelectedItem().toString();

                getPrediction(growthStage, soilQuality, waterLevel);
            }
        });

        // Upload prediction to Firebase when button is clicked
        btnUploadToFirebase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (predictedYield != null && !predictedYield.isEmpty()) {
                    uploadPredictionToFirebase(predictedYield);
                } else {
                    Toast.makeText(activity_yield_prediction.this, "Please predict the yield first.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void setupSpinners() {
        String[] growthStages = {"Seedling", "Vegetative", "Flowering", "Maturity"};
        String[] soilQualities = {"Poor", "Average", "Good", "Excellent"};
        String[] waterLevels = {"Low", "Medium", "High"};

        ArrayAdapter<String> growthStageAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, growthStages);
        growthStageAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGrowthStage.setAdapter(growthStageAdapter);

        ArrayAdapter<String> soilQualityAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, soilQualities);
        soilQualityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSoilQuality.setAdapter(soilQualityAdapter);

        ArrayAdapter<String> waterLevelAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, waterLevels);
        waterLevelAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerWaterLevel.setAdapter(waterLevelAdapter);
    }

    private void getPrediction(String growthStage, String soilQuality, String waterLevel) {
        String simulatedPrediction = "Predicted Yield: ";

        // Logic for simulated prediction
        if (growthStage.equals("Maturity") && soilQuality.equals("Excellent") && waterLevel.equals("High")) {
            simulatedPrediction += "2000 kg/ha (High Yield)";
        } else if (growthStage.equals("Flowering") && soilQuality.equals("Good") && waterLevel.equals("Medium")) {
            simulatedPrediction += "1500 kg/ha (Moderate Yield)";
        } else {
            simulatedPrediction += "1000 kg/ha (Low Yield)";
        }

        predictedYield = simulatedPrediction;
        tvYieldPredictionResult.setText(predictedYield);

        // Enable the upload button once prediction is available
        btnUploadToFirebase.setEnabled(true);
    }

    private void uploadPredictionToFirebase(String prediction) {
        String predictionId = databaseReference.push().getKey();
        if (predictionId != null) {
            // Firebase upload logic
            databaseReference.child(predictionId).setValue(prediction)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            // Success
                            Toast.makeText(activity_yield_prediction.this, "Prediction uploaded successfully!", Toast.LENGTH_SHORT).show();
                        } else {
                            // Failure
                            Toast.makeText(activity_yield_prediction.this, "Failed to upload prediction. Try again.", Toast.LENGTH_SHORT).show();
                            Log.e("FirebaseUpload", "Error: " + task.getException());
                        }
                    });
        } else {
            Toast.makeText(activity_yield_prediction.this, "Error generating prediction ID.", Toast.LENGTH_SHORT).show();
        }
    }
}
