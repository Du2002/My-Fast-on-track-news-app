package com.example.my_fast_on_track_news_app;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.my_fast_on_track_news_app.adapter.NewsAdapter;
import com.example.my_fast_on_track_news_app.model.NewsItem;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;



public class NewsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private NewsAdapter adapter;
    private List<NewsItem> newsList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news);

        recyclerView = findViewById(R.id.newsRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new NewsAdapter(this, newsList);
        recyclerView.setAdapter(adapter);

        // Add this code below your setup (but before any code that needs the menu button)
        findViewById(R.id.btnMenu).setOnClickListener(v -> {
            Toast.makeText(this, "Menu clicked", Toast.LENGTH_SHORT).show();
            showProfileMenu(v); // This calls your popup menu as usual
        });

        // Firebase Load
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("news");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                newsList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    NewsItem item = ds.getValue(NewsItem.class);
                    newsList.add(item);
                }
                adapter.notifyDataSetChanged();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });

        // MENU dropdowns
        findViewById(R.id.btnMenu).setOnClickListener(v -> showProfileMenu(v));
        findViewById(R.id.btnMore).setOnClickListener(v -> showMoreMenu(v));
    }

    private void showProfileMenu(View anchor) {
        PopupMenu menu = new PopupMenu(this, anchor);
        menu.getMenu().add("Go to author profile");
        menu.getMenu().add("Go to user profile");
        menu.setOnMenuItemClickListener(item -> {
            if (item.getTitle().equals("Go to author profile")) {
                startActivity(new Intent(this, AuthorProfileActivity.class));
            } else {
                startActivity(new Intent(this, UserProfileActivity.class));
            }
            return true;
        });
        menu.show();
    }

    private void showMoreMenu(View anchor) {
        PopupMenu menu = new PopupMenu(this, anchor);
        menu.getMenu().add("Help");
        menu.getMenu().add("Settings");
        menu.setOnMenuItemClickListener(item -> {
            // Add intent for Help, Settings
            return true;
        });
        menu.show();
    }

    private int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }
}
