package com.example.fasalmitra;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.UUID;

public class activity_disease_detection extends AppCompatActivity {

    private ImageView imageView;
    private Button btnCaptureImage, btnSelectImage, btnAnalyze;
    private Uri imageUri;
    private FirebaseStorage storage;
    private StorageReference storageReference;

    // ActivityResultLauncher for camera and gallery
    private ActivityResultLauncher<Intent> activityResultLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                            // Check if the result is from the gallery
                            if (result.getData().getData() != null) {
                                imageUri = result.getData().getData();
                                loadImagePreview(imageUri);
                            } else {
                                // If camera, get the bitmap and convert to Uri
                                Bundle extras = result.getData().getExtras();
                                Bitmap imageBitmap = (Bitmap) extras.get("data");
                                imageUri = getImageUriFromBitmap(imageBitmap);
                                loadImagePreview(imageUri);
                            }
                        }
                    }
            );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_disease_detection);

        imageView = findViewById(R.id.ivImagePreview);
        btnCaptureImage = findViewById(R.id.btnCapture);
        btnSelectImage = findViewById(R.id.btnUpload);
        btnAnalyze = findViewById(R.id.btnAnalyze);

        // Initialize Firebase Storage
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        // Capture image from camera
        btnCaptureImage.setOnClickListener(v -> {
            Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            activityResultLauncher.launch(cameraIntent);
        });

        // Select image from gallery
        btnSelectImage.setOnClickListener(v -> {
            Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            activityResultLauncher.launch(galleryIntent);
        });
        
        // Analyze button click to open floweridentifier with imageUri
        btnAnalyze.setOnClickListener(v -> {
            if (imageUri != null) {
                Intent intent = new Intent(activity_disease_detection.this, floweridentifier.class);
                intent.putExtra("imageUri", imageUri.toString());
                startActivity(intent);
            } else {
                Toast.makeText(this, "Please select or capture an image first", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void loadImagePreview(Uri uri) {
        Glide.with(activity_disease_detection.this)
                .load(uri)
                .into(imageView);
    }

    private Uri getImageUriFromBitmap(Bitmap bitmap) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, "CapturedImage", null);
        return Uri.parse(path);
    }

    private void uploadImageToFirebase() {
        String fileName = "images/" + UUID.randomUUID().toString();
        StorageReference fileRef = storageReference.child(fileName);

        fileRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    Toast.makeText(activity_disease_detection.this, "Image uploaded successfully", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(activity_disease_detection.this, "Failed to upload image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
