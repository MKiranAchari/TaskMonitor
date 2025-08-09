package com.example.taskmanager;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HistoryActivity extends AppCompatActivity implements TaskAdapter.OnTaskActionListener {

    private RecyclerView recyclerViewAllTasks;
    private TaskAdapter allTasksAdapter;
    private List<Task> allTaskList;

    private TaskDatabase taskDatabase;
    private ExecutorService executorService;
    private Handler mainThreadHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        // --- Refactored: Initializing ExecutorService and Handler ---
        executorService = Executors.newSingleThreadExecutor();
        mainThreadHandler = new Handler(Looper.getMainLooper());

        // --- Toolbar Setup ---
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // The TextView for the title is already in the XML
        TextView toolbarTitle = findViewById(R.id.toolbar_title);
        toolbarTitle.setText("Task History");

        // --- RecyclerView Setup ---
        recyclerViewAllTasks = findViewById(R.id.recyclerViewAllTasks);
        recyclerViewAllTasks.setLayoutManager(new LinearLayoutManager(this));
        allTaskList = new ArrayList<>();

        // Use the new constructor for the history view, which will disable checkboxes
        allTasksAdapter = new TaskAdapter(allTaskList, this, true);
        recyclerViewAllTasks.setAdapter(allTasksAdapter);

        // Initialize the database instance
        taskDatabase = TaskDatabase.getDatabase(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadAllTasks();
    }

    // Method to load all tasks from the database using ExecutorService
    private void loadAllTasks() {
        executorService.execute(() -> {
            List<Task> tasks = taskDatabase.taskDao().getAllTasks();
            mainThreadHandler.post(() -> {
                allTasksAdapter.setTasks(tasks); // Use new `setTasks` method to update data
            });
        });
    }

    // onTaskCompleteChanged is implemented here for consistency, but the adapter
    // in this activity will have checkboxes disabled, so this method won't be called.
    @Override
    public void onTaskCompleteChanged(Task task, boolean isChecked) {
        // This method will not be called for the history view as checkboxes are disabled.
        // If it were ever to be called, we would simply do nothing to prevent unchecking.
        Toast.makeText(this, "Cannot modify a task from history!", Toast.LENGTH_SHORT).show();
    }
}
