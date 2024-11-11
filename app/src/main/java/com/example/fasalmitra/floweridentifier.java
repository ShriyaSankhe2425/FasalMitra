package com.example.fasalmitra;

import android.graphics.ImageDecoder;
import android.os.Build;
import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.ml.common.FirebaseMLException;
import com.google.firebase.ml.custom.FirebaseCustomLocalModel;
import com.google.firebase.ml.custom.FirebaseModelDataType;
import com.google.firebase.ml.custom.FirebaseModelInputOutputOptions;
import com.google.firebase.ml.custom.FirebaseModelInputs;
import com.google.firebase.ml.custom.FirebaseModelInterpreter;
import com.google.firebase.ml.custom.FirebaseModelInterpreterOptions;
import com.google.firebase.ml.custom.FirebaseModelOutputs;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.Map;

public class floweridentifier extends AppCompatActivity {

    private ImageView imageView;
    private TextView tvResult;
    private Uri imageUri;
    private FirebaseModelInterpreter interpreter;
    private FirebaseModelInputOutputOptions inputOutputOptions;
    private DatabaseReference databaseReference;
    private static final int IMAGE_SIZE = 224; // assuming the model expects 224x224 images

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_floweridentifier);

        // Set the Firebase Realtime Database URL manually
        FirebaseDatabase database = FirebaseDatabase.getInstance("https://fasalmitra-815ac-default-rtdb.asia-southeast1.firebasedatabase.app");

        // Now, you can use this database instance to interact with Firebase Realtime Database
        DatabaseReference myRef = database.getReference("message");

        imageView = findViewById(R.id.imageView);
        tvResult = findViewById(R.id.tvResult);

        // Initialize Firebase Database
        databaseReference = FirebaseDatabase.getInstance().getReference("classification_results");

        // Load the image URI passed from previous activity
        String uriString = getIntent().getStringExtra("imageUri");
        if (uriString != null) {
            imageUri = Uri.parse(uriString);
            loadImage(imageUri);
            classifyImage(imageUri);
        } else {
            Toast.makeText(this, "Image not found", Toast.LENGTH_SHORT).show();
        }

        // Initialize the ML model
        FirebaseCustomLocalModel localModel = new FirebaseCustomLocalModel.Builder()
                .setAssetFilePath("model_flower.tflite")
                .build();

        FirebaseModelInterpreterOptions options = new FirebaseModelInterpreterOptions.Builder(localModel).build();
        try {
            interpreter = FirebaseModelInterpreter.getInstance(options);
        } catch (FirebaseMLException e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to initialize interpreter", Toast.LENGTH_SHORT).show();
        }

        // Set input and output format for the model
        try {
            inputOutputOptions = new FirebaseModelInputOutputOptions.Builder()
                    .setInputFormat(0, FirebaseModelDataType.FLOAT32, new int[]{1, IMAGE_SIZE, IMAGE_SIZE, 3})  // Input shape: [1, 224, 224, 3]
                    .setOutputFormat(0, FirebaseModelDataType.FLOAT32, new int[]{1, 5}) // Output shape: e.g., 5 classes
                    .build();
        } catch (FirebaseMLException e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to set model options", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadImage(Uri uri) {
        imageView.setImageURI(uri);
    }

    private void classifyImage(Uri uri) {
        try {
            Bitmap bitmap;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                // For API 28 and above, use ImageDecoder
                bitmap = ImageDecoder.decodeBitmap(ImageDecoder.createSource(getContentResolver(), uri));
            } else {
                // For older versions, use the deprecated method
                bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
            }

            Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, IMAGE_SIZE, IMAGE_SIZE, true);
            ByteBuffer inputBuffer = convertBitmapToByteBuffer(resizedBitmap);

            // Wrap the inputBuffer addition in a try-catch block to handle FirebaseMLException
            try {
                FirebaseModelInputs inputs = new FirebaseModelInputs.Builder()
                        .add(inputBuffer)  // Add the image ByteBuffer as input
                        .build();

                interpreter.run(inputs, inputOutputOptions)
                        .addOnSuccessListener(this::displayResults)
                        .addOnFailureListener(e ->
                                Toast.makeText(floweridentifier.this, "Failed to process image", Toast.LENGTH_SHORT).show());

            } catch (FirebaseMLException e) {
                e.printStackTrace();
                Toast.makeText(this, "Error with model input: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error loading image", Toast.LENGTH_SHORT).show();
        }
    }

    private ByteBuffer convertBitmapToByteBuffer(Bitmap bitmap) {
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(IMAGE_SIZE * IMAGE_SIZE * 3 * 4); // 4 bytes per float
        byteBuffer.order(ByteOrder.nativeOrder());

        int[] intValues = new int[IMAGE_SIZE * IMAGE_SIZE];
        bitmap.getPixels(intValues, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());
        int pixel = 0;

        for (int i = 0; i < IMAGE_SIZE; ++i) {
            for (int j = 0; j < IMAGE_SIZE; ++j) {
                int pixelValue = intValues[pixel++];
                byteBuffer.putFloat(((pixelValue >> 16) & 0xFF) / 255.0f); // Red
                byteBuffer.putFloat(((pixelValue >> 8) & 0xFF) / 255.0f);  // Green
                byteBuffer.putFloat((pixelValue & 0xFF) / 255.0f);         // Blue
            }
        }
        return byteBuffer;
    }

    private void displayResults(FirebaseModelOutputs outputs) {
        // Cast the output to float[] correctly
        float[] probabilities = (float[]) outputs.getOutput(0);  // Casting to float[] here

        StringBuilder resultText = new StringBuilder();
        Map<String, Object> resultData = new HashMap<>();  // Data for Firebase

        // Assuming 5 classes; map the probabilities to labels manually
        String[] labels = {"Class A", "Class B", "Class C", "Class D", "Class E"};  // Replace with actual class names

        for (int i = 0; i < probabilities.length; i++) {
            resultText.append(labels[i])
                    .append(": ")
                    .append(String.format("%.2f", probabilities[i] * 100))
                    .append("%\n");

            // Add result to data map for Firebase upload
            resultData.put(labels[i], probabilities[i]);
        }

        tvResult.setText(resultText.toString());

        // Upload results to Firebase
        uploadResultsToFirebase(resultData);
    }

    private void uploadResultsToFirebase(Map<String, Object> resultData) {
        DatabaseReference resultsRef = databaseReference.child("flower_results");

        resultsRef.updateChildren(resultData)
                .addOnSuccessListener(aVoid ->
                        Toast.makeText(floweridentifier.this, "Results uploaded successfully", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e ->
                        Toast.makeText(floweridentifier.this, "Failed to upload results", Toast.LENGTH_SHORT).show());
    }
}
