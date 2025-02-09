package com.example.eventplanner;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ImageButton btnProfile, fabAddEvent;
    private FirebaseFirestore db;
    private String userRole;
    private String userId;
    private boolean roleChecked = false;
    private RecyclerView organizerRecyclerView, allEventsRecyclerView;
    private EventAdapter eventAdapter, organizerAdapter;
    private List<Event> eventList = new ArrayList<>();
    private List<Event> organizerEvents = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize views
        btnProfile = findViewById(R.id.btnProfile);
        fabAddEvent = findViewById(R.id.fabAddEvent);
        organizerRecyclerView = findViewById(R.id.organizerEventsRecyclerView);
        allEventsRecyclerView = findViewById(R.id.allEventsRecyclerView);

        // Initialize Firestore and FirebaseAuth
        db = FirebaseFirestore.getInstance();
        FirebaseAuth auth = FirebaseAuth.getInstance();

        // Check if the user is logged in
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            // If no user is logged in, redirect to the login activity
            Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(loginIntent);
            finish(); // Close this activity
            return;
        }

        // Get the user ID
        userId = currentUser.getUid();

        // Check user role
        checkUserRole();

        // Set up RecyclerViews
        organizerRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        allEventsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        organizerAdapter = new EventAdapter(organizerEvents);
        eventAdapter = new EventAdapter(eventList);

        organizerRecyclerView.setAdapter(organizerAdapter);
        allEventsRecyclerView.setAdapter(eventAdapter);

        fetchEvents(); // Fetch events from Firestore

        // Profile button click listener
        btnProfile.setOnClickListener(v -> {
            Intent profileIntent = new Intent(MainActivity.this, ProfileActivity.class);
            startActivity(profileIntent);
        });

        // Add event button click listener (only visible for organizers)
        fabAddEvent.setOnClickListener(v -> {
            if (roleChecked) {
                if (userRole != null && userRole.equals("organizer")) {
                    Intent addEventIntent = new Intent(MainActivity.this, AddActivity.class);
                    startActivity(addEventIntent);
                } else {
                    Toast.makeText(MainActivity.this, "You must be an organizer to add an event.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(MainActivity.this, "Please wait while we check your role.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkUserRole() {
        // Check the user role (attendee or organizer) in Firestore
        db.collection("attendees").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        userRole = "attendee";
                        fabAddEvent.setVisibility(View.GONE); // Hide the Add Event button for attendees
                    } else {
                        db.collection("organizers").document(userId).get()
                                .addOnSuccessListener(doc -> {
                                    if (doc.exists()) {
                                        userRole = "organizer";
                                        fabAddEvent.setVisibility(View.VISIBLE); // Show the Add Event button for organizers
                                    }
                                }).addOnFailureListener(e -> {
                                    Toast.makeText(MainActivity.this, "Failed to retrieve user role.", Toast.LENGTH_SHORT).show();
                                });
                    }
                    roleChecked = true; // Set the flag after role check completes
                }).addOnFailureListener(e -> {
                    Toast.makeText(MainActivity.this, "Failed to retrieve user role.", Toast.LENGTH_SHORT).show();
                    roleChecked = true; // Ensure that the flag is updated
                });
    }

    private void fetchEvents() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String currentUserEmail = currentUser.getEmail(); // Get the logged-in user's email
            db.collection("events").get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        eventList.clear();
                        organizerEvents.clear(); // Clear both lists to avoid duplication

                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            Event event = document.toObject(Event.class);
                            // Separate the events into organizer events and all events
                            if (event.getOrganizerEmail() != null && event.getOrganizerEmail().equals(currentUserEmail)) {
                                organizerEvents.add(event); // This event belongs to the logged-in user
                            } else {
                                eventList.add(event); // This is a general event
                            }
                        }

                        // Update the RecyclerViews
                        organizerAdapter.notifyDataSetChanged();
                        eventAdapter.notifyDataSetChanged();

                        // Set visibility for organizer section
                        if (!organizerEvents.isEmpty()) {
                            findViewById(R.id.organizerHeader).setVisibility(View.VISIBLE);
                        } else {
                            findViewById(R.id.organizerHeader).setVisibility(View.GONE);
                        }

                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(MainActivity.this, "Failed to load events", Toast.LENGTH_SHORT).show();
                    });
        }
    }


}
