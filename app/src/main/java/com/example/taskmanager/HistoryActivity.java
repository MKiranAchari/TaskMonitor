package com.example.taskmanager; // Make sure this matches your package name

import android.os.AsyncTask; // Import for AsyncTask
import android.os.Bundle;
import android.view.MenuItem; // Import for MenuItem (for back button)
import android.widget.Toast; // Import for Toast

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager; // Import for LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView; // Import for RecyclerView

import java.lang.ref.WeakReference; // Import for WeakReference
import java.util.ArrayList;
import java.util.List;

// HistoryActivity implements TaskAdapter.OnTaskActionListener, though for history
// we might not need to update tasks directly from this screen.
// However, it's good practice to pass a listener if the adapter expects one.
public class HistoryActivity extends AppCompatActivity implements TaskAdapter.OnTaskActionListener {

    private RecyclerView recyclerViewHistoryTasks;
    private TaskAdapter taskAdapter;
    private List<Task> taskList; // List to hold all tasks

    private TaskDatabase taskDatabase; // Database instance

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        // --- Toolbar Setup ---
        Toolbar toolbar = findViewById(R.id.toolbarHistory);
        setSupportActionBar(toolbar);

        // Enable the Up button (back button)
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("Task History"); // Set title for the toolbar
        }

        // --- RecyclerView Setup ---
        recyclerViewHistoryTasks = findViewById(R.id.recyclerViewHistoryTasks);
        recyclerViewHistoryTasks.setLayoutManager(new LinearLayoutManager(this));

        taskList = new ArrayList<>(); // Initialize an empty list
        // Pass 'this' as the listener, even if we primarily update from MainActivity
        taskAdapter = new TaskAdapter(taskList, this);
        recyclerViewHistoryTasks.setAdapter(taskAdapter);

        // Initialize the database instance
        taskDatabase = TaskDatabase.getDatabase(this);

        // Load all tasks when the activity is created
        loadAllTasks();
    }

    // Handle the Up button (back button) press
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed(); // Go back to the previous activity
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // Method to load all tasks from the database for history
    private void loadAllTasks() {
        new LoadAllTasksAsyncTask(taskDatabase, new WeakReference<>(this)).execute();
    }

    // Implementation of the OnTaskActionListener interface
    // For history, we might not want to allow direct completion changes here,
    // but the adapter requires an implementation.
    // If a task is marked complete/incomplete here, we should update it in DB.
    @Override
    public void onTaskCompleteChanged(Task task, boolean isChecked) {
        // Update the task's completion status in the database
        // This will update the task in the database but won't remove it from history.
        new UpdateTaskAsyncTask(taskDatabase, new WeakReference<>(this)).execute(task);
        // We don't need to reload all tasks here, as the strikethrough is handled by adapter.
        // If you want to re-sort or filter, you might call loadAllTasks() again.
    }

    // AsyncTask to load all tasks from the database on a background thread
    private static class LoadAllTasksAsyncTask extends AsyncTask<Void, Void, List<Task>> {

        private WeakReference<HistoryActivity> activityWeakReference;
        private TaskDao taskDao;

        LoadAllTasksAsyncTask(TaskDatabase db, WeakReference<HistoryActivity> activityRef) {
            this.taskDao = db.taskDao();
            this.activityWeakReference = activityRef;
        }

        @Override
        protected List<Task> doInBackground(Void... voids) {
            // Get all tasks (in-progress and completed) for the history screen
            return taskDao.getAllTasksForHistory();
        }

        @Override
        protected void onPostExecute(List<Task> tasks) {
            super.onPostExecute(tasks);
            HistoryActivity activity = activityWeakReference.get();
            if (activity != null && !activity.isFinishing()) {
                activity.taskList.clear(); // Clear existing data
                activity.taskList.addAll(tasks); // Add new data
                activity.taskAdapter.notifyDataSetChanged(); // Notify adapter to refresh UI
            }
        }
    }

    // AsyncTask to update a task in the database on a background thread
    // This is similar to the one in MainActivity, but used here if history allows status change
    private static class UpdateTaskAsyncTask extends AsyncTask<Task, Void, Void> {

        private WeakReference<HistoryActivity> activityWeakReference;
        private TaskDao taskDao;

        UpdateTaskAsyncTask(TaskDatabase db, WeakReference<HistoryActivity> activityRef) {
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
            HistoryActivity activity = activityWeakReference.get();
            if (activity != null && !activity.isFinishing()) {
                Toast.makeText(activity, "Task status updated from history!", Toast.LENGTH_SHORT).show();
                // No need to reload tasks here as the strikethrough is handled by adapter.
                // If you want to re-sort or filter based on status, you might call loadAllTasks() again.
            }
        }
    }
}
