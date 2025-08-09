package com.example.taskmanager; // Make sure this matches your package name

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

// @Database annotation defines the database configuration.
// entities: Lists all the Room entities (tables) in this database.
// version: The database version. Increment this number whenever you change the schema.
// exportSchema: Set to false for simple projects to avoid creating schema export folders.
@Database(entities = {Task.class}, version = 1, exportSchema = false)
public abstract class TaskDatabase extends RoomDatabase {

    // Abstract method to get the TaskDao. Room will implement this for you.
    public abstract TaskDao taskDao();

    // Singleton instance of the database to prevent multiple instances
    // which can lead to performance issues or race conditions.
    private static volatile TaskDatabase INSTANCE;

    // Method to get the singleton instance of the database.
    public static TaskDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (TaskDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    TaskDatabase.class, "task_database") // Database name
                            .fallbackToDestructiveMigration() // Allows database to be rebuilt on schema changes (for development)
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
