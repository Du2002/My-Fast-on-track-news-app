package com.example.my_fast_on_track_news_app;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.DatabaseError;

public class SignUpActivity extends AppCompatActivity {

    private EditText etUsername, etPassword, etConfirmPassword, etEmail;
    private Button btnSignUp;
    private CheckBox chkRememberUsername;
    private SharedPreferences sharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        etEmail = findViewById(R.id.etEmail);
        btnSignUp = findViewById(R.id.btnSignUp);
        chkRememberUsername = findViewById(R.id.chkRememberUsername);

        sharedPref = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);

        // Restore username if remembered
        String savedUsername = sharedPref.getString("username", "");
        if (!TextUtils.isEmpty(savedUsername)) {
            etUsername.setText(savedUsername);
            chkRememberUsername.setChecked(true);
        }

        btnSignUp.setOnClickListener(v -> {
            String username = etUsername.getText().toString().trim();
            String usernameLower = username.toLowerCase();
            String password = etPassword.getText().toString().trim();
            String confirmPassword = etConfirmPassword.getText().toString().trim();
            String email = etEmail.getText().toString().trim();

            // Validation
            if (TextUtils.isEmpty(username)) {
                etUsername.setError("Enter username");
                return;
            }
            if (TextUtils.isEmpty(password)) {
                etPassword.setError("Enter password");
                return;
            }
            if (TextUtils.isEmpty(confirmPassword)) {
                etConfirmPassword.setError("Confirm your password");
                return;
            }
            if (!password.equals(confirmPassword)) {
                etConfirmPassword.setError("Passwords do not match");
                return;
            }
            if (TextUtils.isEmpty(email)) {
                etEmail.setError("Enter email");
                return;
            }

            // Remember Username
            if (chkRememberUsername.isChecked()) {
                sharedPref.edit().putString("username", username).apply();
            } else {
                sharedPref.edit().remove("username").apply();
            }

            // Check if username already exists in DB
            DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");
            usersRef.child(usernameLower).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        Toast.makeText(SignUpActivity.this, "Username already exists. Please choose another.", Toast.LENGTH_LONG).show();
                    } else {
                        // Create user in Firebase (Database only, not FirebaseAuth)
                        User newUser = new User(usernameLower, email, password);
                        usersRef.child(usernameLower).setValue(newUser)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(SignUpActivity.this, "Registration Successful!", Toast.LENGTH_SHORT).show();
                                    // Go back to login
                                    startActivity(new Intent(SignUpActivity.this, LoginActivity.class));
                                    finish();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(SignUpActivity.this, "Registration failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                });
                    }
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    Toast.makeText(SignUpActivity.this, "Database Error: " + error.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        });
    }

    // Helper class to match your snapshot structure
    public static class User {
        public String username;
        public String email;
        public String password;

        public User() { }

        public User(String username, String email, String password) {
            this.username = username;
            this.email = email;
            this.password = password;
        }
    }
}
