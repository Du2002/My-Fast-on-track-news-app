package com.example.my_fast_on_track_news_app;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide; // Use this if you load image from URL

public class NewsDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news_detail);

        ImageView backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> {
            finish(); // This will close the detail activity and return to the previous screen
        });


        ImageView ivNewsImage = findViewById(R.id.ivNewsImage);
        TextView tvTitle = findViewById(R.id.tvNewsTitle);
        TextView tvDesc = findViewById(R.id.tvNewsDesc);
        TextView tvDate = findViewById(R.id.tvNewsDate);

        // Get data from Intent
        String title = getIntent().getStringExtra("title");
        String desc = getIntent().getStringExtra("desc");
        String imageUrl = getIntent().getStringExtra("imageUrl");
        String date = getIntent().getStringExtra("date");

        tvTitle.setText(title);
        tvDesc.setText(desc);
        tvDate.setText(date);

        // If your image is a URL, use Glide
        Glide.with(this).load(imageUrl).into(ivNewsImage);

        // If your image is base64 or a drawable, let me know, I’ll update code.
    }
}

