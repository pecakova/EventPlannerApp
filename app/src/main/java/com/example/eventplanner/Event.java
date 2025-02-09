package com.example.eventplanner;

import java.util.ArrayList;
import java.util.List;

public class Event {
    private String eventId, title, description, date, location, organizerEmail = "";
    private ArrayList<String> attendees;


    public Event() {
    }

    public Event(String eventId, String title, String description, String date, String location, String organizerEmail, ArrayList<String> attendees) {
        this.eventId = eventId;
        this.title = title;
        this.description = description;
        this.date = date;
        this.location = location;
        this.organizerEmail = organizerEmail;
        this.attendees = attendees != null ? attendees : new ArrayList<>();
    }

    public String getEventId() { return  eventId; }
    public void setEventId(String eventId) {
        this.eventId = eventId;
    }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getDate() { return date; }
    public String getLocation() {return  location; }
    public String getOrganizerEmail() { return organizerEmail != null ? organizerEmail : ""; }
    public ArrayList<String> getAttendees() {
        return attendees != null ? attendees : new ArrayList<>(); // Return an empty list if null
    }
    public void setAttendees(ArrayList<String> attendees) {
        this.attendees = attendees;
    }

}
