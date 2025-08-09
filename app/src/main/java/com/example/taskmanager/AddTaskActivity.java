package com.example.taskmanager;

import androidx.appcompat.app.AppCompatActivity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AddTaskActivity extends AppCompatActivity {

    private EditText editTextTitle, editTextDescription;
    private Button buttonSelectDate, buttonSelectTime, buttonSaveTask;

    private Calendar taskCalendar;
    private TaskDatabase taskDatabase;
    private ExecutorService executorService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task);

        // --- Refactored: Initializing ExecutorService for background tasks ---
        executorService = Executors.newSingleThreadExecutor();

        // --- Toolbar Setup ---
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Add New Task");
        }
        // Set back button functionality
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        editTextTitle = findViewById(R.id.editTextTaskTitle);
        editTextDescription = findViewById(R.id.editTextTaskDescription);
        buttonSelectDate = findViewById(R.id.buttonSelectDate);
        buttonSelectTime = findViewById(R.id.buttonSelectTime);
        buttonSaveTask = findViewById(R.id.buttonSaveTask);

        taskCalendar = Calendar.getInstance();

        buttonSelectDate.setOnClickListener(v -> showDatePicker());
        buttonSelectTime.setOnClickListener(v -> showTimePicker());
        buttonSaveTask.setOnClickListener(v -> saveTask());

        taskDatabase = TaskDatabase.getDatabase(this);
    }

    private void showDatePicker() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    taskCalendar.set(Calendar.YEAR, year);
                    taskCalendar.set(Calendar.MONTH, month);
                    taskCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    buttonSelectDate.setText(android.text.format.DateFormat.format("MM/dd/yyyy", taskCalendar));
                },
                taskCalendar.get(Calendar.YEAR),
                taskCalendar.get(Calendar.MONTH),
                taskCalendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    private void showTimePicker() {
        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                (view, hourOfDay, minute) -> {
                    taskCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    taskCalendar.set(Calendar.MINUTE, minute);
                    buttonSelectTime.setText(android.text.format.DateFormat.format("hh:mm a", taskCalendar));
                },
                taskCalendar.get(Calendar.HOUR_OF_DAY),
                taskCalendar.get(Calendar.MINUTE),
                false);
        timePickerDialog.show();
    }

    // Refactored saveTask method with validation
    private void saveTask() {
        String title = editTextTitle.getText().toString().trim();
        String description = editTextDescription.getText().toString().trim();

        if (TextUtils.isEmpty(title)) {
            Toast.makeText(this, "Please enter a task title", Toast.LENGTH_SHORT).show();
            return;
        }

        // New Logic: Check if the selected time is in the past
        if (taskCalendar.getTimeInMillis() < System.currentTimeMillis()) {
            Toast.makeText(this, "You cannot create a task for a past time!", Toast.LENGTH_SHORT).show();
            return;
        }

        Task task = new Task();
        task.setTitle(title);
        task.setDescription(description);
        task.setCompleted(false);
        task.setDateTime(taskCalendar.getTime());

        // Use ExecutorService to insert task on a background thread
        executorService.execute(() -> {
            taskDatabase.taskDao().insert(task);

            // Return to the main activity on the main thread
            new Handler(Looper.getMainLooper()).post(() -> {
                Toast.makeText(this, "Task saved successfully!", Toast.LENGTH_SHORT).show();
                finish();
            });
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executorService.shutdown(); // Always shut down the ExecutorService
    }
}
