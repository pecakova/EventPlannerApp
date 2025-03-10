package com.example.eventplanner;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {
    private List<Event> eventList;
    private String currentUserEmail;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();


    public EventAdapter(List<Event> eventList, String currentUserEmail) {
        this.eventList = eventList;
        this.currentUserEmail = currentUserEmail;
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_event, parent, false);
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        Event event = eventList.get(position);
        holder.textTitle.setText(event.getTitle());
        holder.textDescription.setText(event.getDescription());
        holder.textDate.setText(event.getDate());
        holder.textLocation.setText(event.getLocation());
        Log.d("EventAdapter", " Current User Email: " + currentUserEmail + ", Event Organizer Email: " + event.getOrganizerEmail());

        if (event.getOrganizerEmail().equals(currentUserEmail)) {
            holder.deleteButton.setVisibility(View.VISIBLE);
            holder.deleteButton.setOnClickListener(v -> deleteEvent(event.getTitle(), v.getContext()));
        } else {
            holder.deleteButton.setVisibility(View.GONE);
        }
        if (!event.getOrganizerEmail().equals(currentUserEmail)) {
            holder.joinButton.setVisibility(View.VISIBLE);
            holder.joinButton.setOnClickListener(v -> joinEvent(event, currentUserEmail, v.getContext()));
        } else {
            holder.joinButton.setVisibility(View.GONE);
        }
        if (event.getAttendees() != null && !event.getAttendees().isEmpty()) {
            holder.textAttendees.setText("Attendees: " + String.join(", ", event.getAttendees()));
            holder.textAttendees.setVisibility(View.VISIBLE);
        } else {
            holder.textAttendees.setText("No attendees yet");
            holder.textAttendees.setVisibility(View.VISIBLE);
        }

    }

    @Override
    public int getItemCount() {
        return eventList.size();
    }
    public void updateEventList(List<Event> filteredEvents) {
        this.eventList = filteredEvents;
        notifyDataSetChanged();
    }
    private void deleteEvent(String eventTitle, Context context) {
        db.collection("events")
                .whereEqualTo("title", eventTitle)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        db.collection("events").document(document.getId()).delete()
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(context, "Event deleted successfully", Toast.LENGTH_SHORT).show();
                                    eventList.removeIf(event -> event.getTitle().equals(eventTitle));
                                    notifyDataSetChanged();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(context, "Failed to delete event", Toast.LENGTH_SHORT).show();
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Event not found", Toast.LENGTH_SHORT).show();
                });
    }
    private void joinEvent(Event event, String currentUserEmail, Context context) {
        if (event.getAttendees() == null) {
            event.setAttendees(new ArrayList<>());
        }

        if (!event.getAttendees().contains(currentUserEmail)) {
            event.getAttendees().add(currentUserEmail);

            db.collection("events")
                    .whereEqualTo("title", event.getTitle())
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            db.collection("events").document(document.getId())
                                    .update("attendees", event.getAttendees())
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(context, "Successfully joined the event!", Toast.LENGTH_SHORT).show();
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(context, "Failed to join the event.", Toast.LENGTH_SHORT).show();
                                    });
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(context, "Event not found.", Toast.LENGTH_SHORT).show();
                    });
        } else {
            Toast.makeText(context, "You have already joined this event.", Toast.LENGTH_SHORT).show();
        }
    }



    static class EventViewHolder extends RecyclerView.ViewHolder {
        TextView textTitle, textDescription, textDate, textLocation, textAttendees;
        ImageButton deleteButton, joinButton;

        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            textTitle = itemView.findViewById(R.id.textEventTitle);
            textDescription = itemView.findViewById(R.id.textEventDescription);
            textDate = itemView.findViewById(R.id.textEventDate);
            textLocation = itemView.findViewById(R.id.textEventLocation);
            textAttendees = itemView.findViewById(R.id.textAttendees);
            deleteButton = itemView.findViewById(R.id.deleteButton);
            joinButton = itemView.findViewById(R.id.joinButton);
        }
    }
}
