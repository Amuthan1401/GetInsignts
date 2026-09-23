package com.example.automotivenewsapp;

import android.content.Context;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.NewsViewHolder> {

    private final ArrayList<News> newsList;
    private final OnNewsItemInteractionListener listener;
    private boolean isAdmin;

    public NewsAdapter(ArrayList<News> newsList, OnNewsItemInteractionListener listener) {
        this.newsList = newsList;
        this.listener = listener;
    }

    public void setAdmin(boolean isAdmin) {
        this.isAdmin = isAdmin;
    }

    @NonNull
    @Override
    public NewsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.news_item, parent, false);
        return new NewsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NewsViewHolder holder, int position) {
        News news = newsList.get(position);

        holder.titleTextView.setText(news.getTitle());
        holder.newsTextView.setText(news.getContent());

        // Get timestamp
        long timestamp = news.getTimestamp();
        Calendar newsCalendar = Calendar.getInstance();
        newsCalendar.setTimeInMillis(timestamp);

        Calendar now = Calendar.getInstance();
        String formattedDate = formatTimestamp(holder.itemView.getContext(), timestamp, newsCalendar, now);

        holder.timestampTextView.setText(formattedDate);

        if (isAdmin) {
            holder.editButton.setVisibility(View.VISIBLE);
            holder.deleteButton.setVisibility(View.VISIBLE);

            holder.editButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEditNews(news);
                }
            });

            holder.deleteButton.setOnClickListener(v -> {
                if (listener != null && news.getKey() != null) {
                    listener.onDeleteNews(news.getKey());
                }
            });
        } else {
            holder.editButton.setVisibility(View.GONE);
            holder.deleteButton.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return newsList.size();
    }

    private String formatTimestamp(Context context, long timestamp, Calendar newsCalendar, Calendar now) {
        String formattedDate;
        if (DateUtils.isToday(timestamp)) {
            // Same day: Show time only
            formattedDate = new SimpleDateFormat("h:mm a", Locale.getDefault()).format(timestamp);
        } else if (now.get(Calendar.YEAR) == newsCalendar.get(Calendar.YEAR)) {
            // Same year: Show date
            formattedDate = new SimpleDateFormat("MMM dd", Locale.getDefault()).format(timestamp);
        } else {
            // Different year: Show date with year
            formattedDate = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(timestamp);
        }
        return formattedDate;
    }

    public interface OnNewsItemInteractionListener {
        void onEditNews(News news);
        void onDeleteNews(String key);
    }

    static class NewsViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView, newsTextView, timestampTextView;
        ImageButton editButton, deleteButton;

        NewsViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.titleTextView);
            newsTextView = itemView.findViewById(R.id.newsTextView);
            timestampTextView = itemView.findViewById(R.id.timestampTextView);
            editButton = itemView.findViewById(R.id.editButton);
            deleteButton = itemView.findViewById(R.id.deleteButton);
        }
    }
}