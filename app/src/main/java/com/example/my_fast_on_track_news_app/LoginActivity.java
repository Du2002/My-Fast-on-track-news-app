package com.example.my_fast_on_track_news_app;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.*;

public class LoginActivity extends AppCompatActivity {

    private EditText etUsername, etPassword;
    private Button btnLogin;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etUsername = findViewById(R.id.etEmail); // Used for username
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);

        TextView tvSignUp = findViewById(R.id.tvSignUp);
        tvSignUp.setOnClickListener(view -> {
            Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
            startActivity(intent);
        });

        progressDialog = new ProgressDialog(this);

        btnLogin.setOnClickListener(view -> {
            String username = etUsername.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (TextUtils.isEmpty(username)) {
                etUsername.setError("Enter username");
                return;
            }
            if (TextUtils.isEmpty(password)) {
                etPassword.setError("Enter password");
                return;
            }

            progressDialog.setMessage("Logging in...");
            progressDialog.show();

            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(username);

            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    progressDialog.dismiss();
                    if (snapshot.exists()) {
                        String dbPassword = snapshot.child("password").getValue(String.class);
                        if (dbPassword != null && dbPassword.equals(password)) {
                            // Save logged-in username for next activities
                            getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
                                    .edit()
                                    .putString("loggedInUsername", username)
                                    .apply();

                            Toast.makeText(LoginActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(LoginActivity.this, NewsActivity.class));
                            finish();
                        } else {
                            Toast.makeText(LoginActivity.this, "Incorrect password.", Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Toast.makeText(LoginActivity.this, "Username not found.", Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    progressDialog.dismiss();
                    Toast.makeText(LoginActivity.this, "Database Error: " + error.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        });
    }
}
