package com.example.my_fast_on_track_news_app;
import android.widget.EditText;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;
import com.google.firebase.storage.*;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;

import de.hdodenhof.circleimageview.CircleImageView;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.bumptech.glide.Glide;


public class UserProfileActivity extends AppCompatActivity {

    private static final int PICK_IMAGE = 1;
    private CircleImageView profileImage;
    private TextView tvUsername, tvEmail;
    private Button btnEdit, btnSignOut;
    private Uri selectedImageUri;

    // Firebase
    private FirebaseAuth mAuth;
    private DatabaseReference userRef;
    private StorageReference storageRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        profileImage = findViewById(R.id.profileImage);
        tvUsername = findViewById(R.id.tvUsername);
        tvEmail = findViewById(R.id.tvEmail);
        btnEdit = findViewById(R.id.btnEdit);
        btnSignOut = findViewById(R.id.btnSignOut);

        // Firebase init
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            // Not logged in
            finish();
            return;
        }
        userRef = FirebaseDatabase.getInstance().getReference("users").child(user.getUid());
        storageRef = FirebaseStorage.getInstance().getReference("profile_images").child(user.getUid() + ".jpg");

        // Load user data
        loadUserData();

        // Load profile photo
        storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
            Glide.with(this).load(uri).into(profileImage);
        }).addOnFailureListener(e -> {
            // Use default image
            profileImage.setImageResource(R.drawable.default_profile);
        });

        profileImage.setOnClickListener(v -> pickImage());

        btnEdit.setOnClickListener(v -> showEditDialog());
        btnSignOut.setOnClickListener(v -> showSignOutDialog());
    }

    private void loadUserData() {
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                String username = snapshot.child("username").getValue(String.class);
                String email = snapshot.child("email").getValue(String.class);
                tvUsername.setText("Username : " + (username != null ? username : ""));
                tvEmail.setText("Email : " + (email != null ? email : ""));
            }
            @Override
            public void onCancelled(DatabaseError error) { }
        });
    }

    private void pickImage() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && data != null) {
            selectedImageUri = data.getData();
            profileImage.setImageURI(selectedImageUri);
            uploadImage();
        }
    }

    private void uploadImage() {
        if (selectedImageUri != null) {
            storageRef.putFile(selectedImageUri)
                    .addOnSuccessListener(taskSnapshot -> {
                        Toast.makeText(this, "Profile photo updated.", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Photo upload failed.", Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void showSignOutDialog() {
        new AlertDialog.Builder(this)
                .setMessage("Really want to sign out?")
                .setPositiveButton("Ok", (dialog, which) -> {
                    mAuth.signOut();
                    Intent intent = new Intent(this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showEditDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_edit_profile, null);
        EditText etUsername = dialogView.findViewById(R.id.etUsername);
        EditText etEmail = dialogView.findViewById(R.id.etEmail);

        // Pre-fill current values
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                String currentUsername = snapshot.child("username").getValue(String.class);
                String currentEmail = snapshot.child("email").getValue(String.class);
                etUsername.setText(currentUsername);
                etEmail.setText(currentEmail);
            }
            @Override
            public void onCancelled(DatabaseError error) { }
        });

        new AlertDialog.Builder(this)
                .setTitle("Edit your profile")
                .setView(dialogView)
                .setPositiveButton("Ok", (dialog, which) -> {
                    String newUsername = etUsername.getText().toString().trim();
                    String newEmail = etEmail.getText().toString().trim();

                    userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot snapshot) {
                            String oldUsername = snapshot.child("username").getValue(String.class);
                            String oldEmail = snapshot.child("email").getValue(String.class);
                            String uid = mAuth.getCurrentUser().getUid();
                            DatabaseReference usernamesRef = FirebaseDatabase.getInstance().getReference("usernames");

                            // 1. Remove old username entry
                            if (oldUsername != null && !oldUsername.equals(newUsername)) {
                                usernamesRef.child(oldUsername).removeValue();
                            }
                            // 2. Update /users/<uid>
                            userRef.child("username").setValue(newUsername);
                            userRef.child("email").setValue(newEmail);

                            // 3. Add new username entry
                            usernamesRef.child(newUsername).setValue(uid);

                            // (Optional) Update email in FirebaseAuth
                            // Only do this if you want to allow login with new email
                        /*
                        mAuth.getCurrentUser().updateEmail(newEmail)
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    Toast.makeText(UserProfileActivity.this, "Email updated", Toast.LENGTH_SHORT).show();
                                }
                            });
                        */

                            tvUsername.setText("Username : " + newUsername);
                            tvEmail.setText("Email : " + newEmail);
                            Toast.makeText(UserProfileActivity.this, "Profile updated!", Toast.LENGTH_SHORT).show();
                        }
                        @Override
                        public void onCancelled(DatabaseError error) { }
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

}