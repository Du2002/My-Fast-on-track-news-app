package com.example.my_fast_on_track_news_app;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.*;

public class UserProfileActivity extends AppCompatActivity {

    private TextView tvUsername, tvEmail;
    private Button btnEdit, btnSignOut;

    private DatabaseReference usersRef;
    private String loggedInUsername;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        tvUsername = findViewById(R.id.tvUsername);
        tvEmail = findViewById(R.id.tvEmail);
        btnEdit = findViewById(R.id.btnEdit);
        btnSignOut = findViewById(R.id.btnSignOut);

        loggedInUsername = getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
                .getString("loggedInUsername", null);

        if (loggedInUsername == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        usersRef = FirebaseDatabase.getInstance().getReference("users");

        loadUserData();

        btnEdit.setOnClickListener(v -> showEditDialog());
        btnSignOut.setOnClickListener(v -> showSignOutDialog());
    }

    private void loadUserData() {
        usersRef.child(loggedInUsername).addListenerForSingleValueEvent(new ValueEventListener() {
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

    private void showEditDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_edit_profile, null);
        EditText etUsername = dialogView.findViewById(R.id.etUsername);
        EditText etEmail = dialogView.findViewById(R.id.etEmail);

        usersRef.child(loggedInUsername).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                String currentUsername = snapshot.child("username").getValue(String.class);
                String currentEmail = snapshot.child("email").getValue(String.class);
                String currentPassword = snapshot.child("password").getValue(String.class); // Keep password as-is

                etUsername.setText(currentUsername);
                etEmail.setText(currentEmail);

                new AlertDialog.Builder(UserProfileActivity.this)
                        .setTitle("Edit your profile")
                        .setView(dialogView)
                        .setPositiveButton("Update", (dialog, which) -> {
                            String newUsername = etUsername.getText().toString().trim();
                            String newEmail = etEmail.getText().toString().trim();

                            if (newUsername.isEmpty() || newEmail.isEmpty()) {
                                Toast.makeText(UserProfileActivity.this, "Fields can't be empty", Toast.LENGTH_SHORT).show();
                                return;
                            }

                            if (!newUsername.equals(loggedInUsername)) {
                                // Move user data to new username key
                                DatabaseReference oldRef = usersRef.child(loggedInUsername);
                                DatabaseReference newRef = usersRef.child(newUsername);

                                // Copy user data
                                User user = new User(newUsername, newEmail, currentPassword);
                                newRef.setValue(user).addOnSuccessListener(aVoid -> {
                                    oldRef.removeValue(); // Remove old node
                                    // Update shared pref
                                    getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
                                            .edit()
                                            .putString("loggedInUsername", newUsername)
                                            .apply();
                                    loggedInUsername = newUsername;
                                    loadUserData();
                                    Toast.makeText(UserProfileActivity.this, "Profile updated!", Toast.LENGTH_SHORT).show();
                                });
                            } else {
                                // Only email update
                                usersRef.child(loggedInUsername).child("email").setValue(newEmail)
                                        .addOnSuccessListener(aVoid -> {
                                            loadUserData();
                                            Toast.makeText(UserProfileActivity.this, "Email updated!", Toast.LENGTH_SHORT).show();
                                        });
                            }
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            }

            @Override
            public void onCancelled(DatabaseError error) { }
        });
    }

    private void showSignOutDialog() {
        new AlertDialog.Builder(this)
                .setMessage("Really want to sign out?")
                .setPositiveButton("Ok", (dialog, which) -> {
                    getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
                            .edit()
                            .remove("loggedInUsername")
                            .apply();
                    Intent intent = new Intent(this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    // Helper User class to move user data
    public static class User {
        public String username;
        public String email;
        public String password;

        public User() {}

        public User(String username, String email, String password) {
            this.username = username;
            this.email = email;
            this.password = password;
        }
    }
}
