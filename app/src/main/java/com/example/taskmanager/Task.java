package com.example.taskmanager; // Make sure this matches your package name

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

// @Entity annotation marks this class as a Room entity (a table in the database)
@Entity(tableName = "tasks")
public class Task {

    // @PrimaryKey annotation marks this field as the primary key for the table.
    // autoGenerate = true means Room will automatically generate unique IDs for new tasks.
    @PrimaryKey(autoGenerate = true)
    private int id;

    // @ColumnInfo allows you to customize column names if they differ from field names.
    // It's good practice to explicitly define them.
    @ColumnInfo(name = "title")
    private String title;

    @ColumnInfo(name = "date")
    private String date; // Storing as String for simplicity with DatePickerDialog output

    @ColumnInfo(name = "time")
    private String time; // Storing as String for simplicity with TimePickerDialog output

    @ColumnInfo(name = "name")
    private String name; // Optional

    @ColumnInfo(name = "description")
    private String description; // Optional

    @ColumnInfo(name = "is_completed")
    private boolean isCompleted; // To track task status

    // Constructor: Room needs a constructor to recreate objects from the database.
    // It's good practice to have one that includes all fields except the auto-generated primary key.
    public Task(String title, String date, String time, String name, String description, boolean isCompleted) {
        this.title = title;
        this.date = date;
        this.time = time;
        this.name = name;
        this.description = description;
        this.isCompleted = isCompleted;
    }

    // --- Getters and Setters ---
    // Room uses these to read and write data to/from the database.

    public int getId() {
        return id;
    }

    // Setter for ID is needed by Room, even if it's auto-generated.
    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }
}
