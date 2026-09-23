package com.example.automotivenewsapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.Collections;

public class NewsActivity extends AppCompatActivity implements NewsAdapter.OnNewsItemInteractionListener {

    private DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("news");
    private LinearLayout enterNewsLayout;
    private EditText titleEditText, newsEditText;
    private Button addNewsButton, logoutButtonInMenu;
    private RecyclerView recyclerView;
    private NewsAdapter newsAdapter;
    private ArrayList<News> newsList;

    private boolean isAdmin = false;
    private boolean isEditing = false;
    private String editingKey = null;

    private DrawerLayout drawerLayout;  // DrawerLayout for sliding menu
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news);

        // Initialize views
        drawerLayout = findViewById(R.id.drawerLayout);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);  // Set the toolbar as the ActionBar

        // Enable hamburger icon using ActionBarDrawerToggle
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close
        );
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState(); // Sync the toggle state with the drawer

        enterNewsLayout = findViewById(R.id.enterNewsLayout);
        titleEditText = findViewById(R.id.titleEditText);
        newsEditText = findViewById(R.id.newsEditText);
        addNewsButton = findViewById(R.id.addNewsButton);
        recyclerView = findViewById(R.id.recyclerView);
        logoutButtonInMenu = findViewById(R.id.logoutButtonInMenu);

        newsList = new ArrayList<>();
        newsAdapter = new NewsAdapter(newsList, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(newsAdapter);

        // Set up Firebase
        mAuth = FirebaseAuth.getInstance();
        String uid = mAuth.getCurrentUser().getUid();
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users").child(uid);

        usersRef.child("isAdmin").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Boolean adminStatus = dataSnapshot.getValue(Boolean.class);
                isAdmin = adminStatus != null && adminStatus;
                enterNewsLayout.setVisibility(isAdmin ? View.VISIBLE : View.GONE);
                newsAdapter.setAdmin(isAdmin);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(NewsActivity.this, "Error fetching user role", Toast.LENGTH_SHORT).show();
            }
        });

        addNewsButton.setOnClickListener(v -> {
            String newsTitle = titleEditText.getText().toString().trim();
            String newsContent = newsEditText.getText().toString().trim();

            if (!newsTitle.isEmpty() && !newsContent.isEmpty()) {
                if (isEditing) {
                    databaseReference.child(editingKey).child("title").setValue(newsTitle);
                    databaseReference.child(editingKey).child("content").setValue(newsContent);
                    isEditing = false;
                    editingKey = null;
                    Toast.makeText(this, "News updated", Toast.LENGTH_SHORT).show();
                } else {
                    String id = databaseReference.push().getKey();
                    if (id != null) {
                        long timestamp = System.currentTimeMillis(); // Get current time
                        News newNews = new News(newsTitle, newsContent, timestamp);
                        newNews.setKey(id);
                        databaseReference.child(id).setValue(newNews);
                        Toast.makeText(this, "News added", Toast.LENGTH_SHORT).show();
                    }
                }
                titleEditText.setText("");
                newsEditText.setText("");
            } else {
                Toast.makeText(this, "Both title and content are required", Toast.LENGTH_SHORT).show();
            }
        });

        logoutButtonInMenu.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(NewsActivity.this, LoginActivity.class));
            finish();
        });

        // Fetch the latest news first (sort by timestamp in descending order)
        databaseReference.orderByChild("timestamp").limitToLast(100).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                newsList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    News newsItem = dataSnapshot.getValue(News.class);
                    if (newsItem != null) {
                        newsItem.setKey(dataSnapshot.getKey());
                        newsList.add(newsItem);
                    }
                }
                // Reverse the list to show latest news first
                Collections.reverse(newsList);
                newsAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(NewsActivity.this, "Failed to fetch news", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onEditNews(News news) {
        titleEditText.setText(news.getTitle());
        newsEditText.setText(news.getContent());
        isEditing = true;
        editingKey = news.getKey();
    }

    @Override
    public void onDeleteNews(String key) {
        if (key != null) {
            databaseReference.child(key).removeValue()
                    .addOnSuccessListener(aVoid -> Toast.makeText(NewsActivity.this, "News deleted", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e -> Toast.makeText(NewsActivity.this, "Failed to delete news", Toast.LENGTH_SHORT).show());
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }
}