package com.example.taskmanager; // Make sure this matches your package name

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.AsyncTask; // Import for AsyncTask
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast; // Import for Toast

import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager; // Import for LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView; // Import for RecyclerView

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.lang.ref.WeakReference; // Import for WeakReference
import java.util.ArrayList;
import java.util.List;

// MainActivity now implements TaskAdapter.OnTaskActionListener
public class MainActivity extends AppCompatActivity implements TaskAdapter.OnTaskActionListener {

    private RecyclerView recyclerViewTasks;
    private TaskAdapter taskAdapter;
    private List<Task> taskList; // List to hold tasks displayed in RecyclerView

    private TaskDatabase taskDatabase; // Database instance

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // --- Toolbar Setup ---
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // The TextView for the title is already in the XML, no need to set here
        // getSupportActionBar().setTitle("In Progress Tasks");

        // --- History Button Setup ---
        ImageButton historyButton = findViewById(R.id.historyButton);
        historyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Implement logic to open HistoryActivity
                Intent intent = new Intent(MainActivity.this, HistoryActivity.class);
                startActivity(intent);
            }
        });

        // --- Floating Action Button (FAB) Setup ---
        FloatingActionButton fabAddTask = findViewById(R.id.fabAddTask);
        fabAddTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, AddTaskActivity.class);
                startActivity(intent);
            }
        });

        // --- RecyclerView Setup ---
        recyclerViewTasks = findViewById(R.id.recyclerViewTasks);
        recyclerViewTasks.setLayoutManager(new LinearLayoutManager(this));

        taskList = new ArrayList<>(); // Initialize an empty list
        taskAdapter = new TaskAdapter(taskList, this); // Pass 'this' as the listener
        recyclerViewTasks.setAdapter(taskAdapter);

        // Initialize the database instance
        taskDatabase = TaskDatabase.getDatabase(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh tasks whenever the activity comes to the foreground
        // This ensures the list is updated after adding a new task or marking one complete
        loadTasks();
    }

    // Method to load in-progress tasks from the database
    private void loadTasks() {
        new LoadTasksAsyncTask(taskDatabase, new WeakReference<>(this)).execute();
    }

    // Implementation of the OnTaskActionListener interface
    @Override
    public void onTaskCompleteChanged(Task task, boolean isChecked) {
        // Update the task's completion status in the database
        new UpdateTaskAsyncTask(taskDatabase, new WeakReference<>(this)).execute(task);
    }

    // AsyncTask to load tasks from the database on a background thread
    private static class LoadTasksAsyncTask extends AsyncTask<Void, Void, List<Task>> {

        private WeakReference<MainActivity> activityWeakReference;
        private TaskDao taskDao;

        LoadTasksAsyncTask(TaskDatabase db, WeakReference<MainActivity> activityRef) {
            this.taskDao = db.taskDao();
            this.activityWeakReference = activityRef;
        }

        @Override
        protected List<Task> doInBackground(Void... voids) {
            // Get only in-progress tasks for the main screen
            return taskDao.getInProgressTasks();
        }

        @Override
        protected void onPostExecute(List<Task> tasks) {
            super.onPostExecute(tasks);
            MainActivity activity = activityWeakReference.get();
            if (activity != null && !activity.isFinishing()) {
                activity.taskList.clear(); // Clear existing data
                activity.taskList.addAll(tasks); // Add new data
                activity.taskAdapter.notifyDataSetChanged(); // Notify adapter to refresh UI
                // Alternatively, use activity.taskAdapter.setTasks(tasks); if you prefer that method
            }
        }
    }

    // AsyncTask to update a task in the database on a background thread
    private static class UpdateTaskAsyncTask extends AsyncTask<Task, Void, Void> {

        private WeakReference<MainActivity> activityWeakReference;
        private TaskDao taskDao;

        UpdateTaskAsyncTask(TaskDatabase db, WeakReference<MainActivity> activityRef) {
            this.taskDao = db.taskDao();
            this.activityWeakReference = activityRef;
        }

        @Override
        protected Void doInBackground(final Task... params) {
            taskDao.updateTask(params[0]); // Update the task
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            MainActivity activity = activityWeakReference.get();
            if (activity != null && !activity.isFinishing()) {
                Toast.makeText(activity, "Task status updated!", Toast.LENGTH_SHORT).show();
                activity.loadTasks(); // Reload tasks to reflect the change (e.g., hide completed task)
            }
        }
    }
}
