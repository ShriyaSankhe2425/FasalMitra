package com.example.fasalmitra;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import java.io.ByteArrayOutputStream;

public class MainActivity extends AppCompatActivity {

    private DatabaseReference databaseReference;
    private StorageReference storageReference;


    private Button btnDiseaseDetection;
    private Button btnWeatherRecommendation;
    private Button btnYieldPrediction;

    private ActivityResultLauncher<Intent> activityResultLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);  // Ensure your layout file matches this name
        FirebaseApp.initializeApp(this);

        // Initialize Firebase references
        databaseReference = FirebaseDatabase.getInstance().getReference("images");
        storageReference = FirebaseStorage.getInstance().getReference("uploads");

        // Initialize buttons
        btnDiseaseDetection = findViewById(R.id.btnDiseaseDetection);
        btnWeatherRecommendation = findViewById(R.id.btnWeatherRecommendation);
        btnYieldPrediction = findViewById(R.id.btnYieldPrediction);

        // Initialize ActivityResultLauncher
        activityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        if (imageUri == null && result.getData().getExtras() != null) {
                            Bitmap bitmap = (Bitmap) result.getData().getExtras().get("data");
                            uploadImageToFirebase(bitmap);
                        } else {
                            uploadImageToFirebase(imageUri);
                        }
                    }
                }
        );

        // Set up listeners to navigate to other activities
        btnDiseaseDetection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent diseaseDetectionIntent = new Intent(MainActivity.this, activity_disease_detection.class);
                startActivity(diseaseDetectionIntent);
            }
        });

        btnWeatherRecommendation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent weatherRecommendationIntent = new Intent(MainActivity.this, activity_weather_recommendation.class);
                startActivity(weatherRecommendationIntent);
            }
        });

        btnYieldPrediction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent yieldPredictionIntent = new Intent(MainActivity.this, activity_yield_prediction.class);
                startActivity(yieldPredictionIntent);
            }
        });
    }

    // Upload image from Uri (gallery selection)
    private void uploadImageToFirebase(Uri imageUri) {
        StorageReference fileRef = storageReference.child(System.currentTimeMillis() + ".jpg");
        fileRef.putFile(imageUri).addOnSuccessListener(taskSnapshot -> {
            fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                String imageUrl = uri.toString();
                String uploadId = databaseReference.push().getKey();
                databaseReference.child(uploadId).setValue(imageUrl);
                Toast.makeText(MainActivity.this, "Image Uploaded", Toast.LENGTH_SHORT).show();
            });
        }).addOnFailureListener(e -> Toast.makeText(MainActivity.this, "Upload Failed", Toast.LENGTH_SHORT).show());
    }

    // Upload image from Bitmap (camera capture)
    private void uploadImageToFirebase(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();

        StorageReference fileRef = storageReference.child(System.currentTimeMillis() + ".jpg");
        fileRef.putBytes(data).addOnSuccessListener(taskSnapshot -> {
            fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                String imageUrl = uri.toString();
                String uploadId = databaseReference.push().getKey();
                databaseReference.child(uploadId).setValue(imageUrl);
                Toast.makeText(MainActivity.this, "Image Uploaded", Toast.LENGTH_SHORT).show();
            });
        }).addOnFailureListener(e -> Toast.makeText(MainActivity.this, "Upload Failed", Toast.LENGTH_SHORT).show());
    }
}
