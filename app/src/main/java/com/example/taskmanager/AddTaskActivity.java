package com.example.taskmanager; // Make sure this matches your package name

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.AsyncTask; // Import for AsyncTask
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.textfield.TextInputEditText;

import java.lang.ref.WeakReference; // Import for WeakReference
import java.util.Calendar;
import java.util.Locale;

public class AddTaskActivity extends AppCompatActivity {

    private TextInputEditText editTextTaskTitle;
    private TextInputEditText editTextDate;
    private TextInputEditText editTextTime;
    private TextInputEditText editTextName;
    private TextInputEditText editTextDescription;
    private Button buttonSaveTask;

    private Calendar calendar; // To store the selected date and time

    private TaskDatabase taskDatabase; // Database instance

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task);

        // --- Toolbar Setup ---
        Toolbar toolbar = findViewById(R.id.toolbarAddTask);
        setSupportActionBar(toolbar); // Set the toolbar as the app's action bar

        // Enable the Up button (back button)
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("Add New Task"); // Set title for the toolbar
        }

        // --- Initialize UI Elements ---
        editTextTaskTitle = findViewById(R.id.editTextTaskTitle);
        editTextDate = findViewById(R.id.editTextDate);
        editTextTime = findViewById(R.id.editTextTime);
        editTextName = findViewById(R.id.editTextName);
        editTextDescription = findViewById(R.id.editTextDescription);
        buttonSaveTask = findViewById(R.id.buttonSaveTask);

        calendar = Calendar.getInstance(); // Initialize calendar with current date/time

        // Initialize the database instance
        taskDatabase = TaskDatabase.getDatabase(this);

        // --- Date Picker Setup ---
        editTextDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePicker();
            }
        });

        // --- Time Picker Setup ---
        editTextTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTimePicker();
            }
        });

        // --- Save Task Button Listener ---
        buttonSaveTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveTask();
            }
        });
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

    // Method to show DatePickerDialog
    private void showDatePicker() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        calendar.set(Calendar.YEAR, year);
                        calendar.set(Calendar.MONTH, monthOfYear);
                        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        updateDateEditText(); // Update the EditText with selected date
                    }
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    // Method to update the date EditText
    private void updateDateEditText() {
        String dateFormat = "dd/MM/yyyy"; // Define your desired date format
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat(dateFormat, Locale.getDefault());
        editTextDate.setText(sdf.format(calendar.getTime()));
    }

    // Method to show TimePickerDialog
    private void showTimePicker() {
        TimePickerDialog timePickerDialog = new TimePickerDialog(
                this,
                new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        calendar.set(Calendar.MINUTE, minute);
                        updateTimeEditText(); // Update the EditText with selected time
                    }
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                false // Set to true for 24-hour format, false for 12-hour format
        );
        timePickerDialog.show();
    }

    // Method to update the time EditText
    private void updateTimeEditText() {
        String timeFormat = "hh:mm a"; // Define your desired time format (e.g., 03:30 PM)
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat(timeFormat, Locale.getDefault());
        editTextTime.setText(sdf.format(calendar.getTime()));
    }

    // Method to handle saving the task
    private void saveTask() {
        String title = editTextTaskTitle.getText().toString().trim();
        String date = editTextDate.getText().toString().trim();
        String time = editTextTime.getText().toString().trim();
        String name = editTextName.getText().toString().trim();
        String description = editTextDescription.getText().toString().trim();

        // Basic validation: Title, Date, and Time are mandatory
        if (title.isEmpty()) {
            editTextTaskTitle.setError("Task title is required");
            editTextTaskTitle.requestFocus();
            return;
        }
        if (date.isEmpty()) {
            editTextDate.setError("Date is required");
            editTextDate.requestFocus();
            return;
        }
        if (time.isEmpty()) {
            editTextTime.setError("Time is required");
            editTextTime.requestFocus();
            return;
        }

        // Create a new Task object
        // isCompleted is false by default for new tasks
        Task newTask = new Task(title, date, time, name, description, false);

        // Insert the task into the database on a background thread
        new InsertTaskAsyncTask(taskDatabase, new WeakReference<>(this)).execute(newTask);
    }

    // AsyncTask to perform database insertion on a background thread
    private static class InsertTaskAsyncTask extends AsyncTask<Task, Void, Void> {

        private WeakReference<AddTaskActivity> activityWeakReference;
        private TaskDao taskDao;

        InsertTaskAsyncTask(TaskDatabase db, WeakReference<AddTaskActivity> activityRef) {
            this.taskDao = db.taskDao();
            this.activityWeakReference = activityRef;
        }

        @Override
        protected Void doInBackground(final Task... params) {
            taskDao.insertTask(params[0]); // Insert the task
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            AddTaskActivity activity = activityWeakReference.get();
            if (activity != null && !activity.isFinishing()) {
                Toast.makeText(activity, "Task Saved Successfully!", Toast.LENGTH_SHORT).show();
                activity.finish(); // Go back to the previous activity (MainActivity)
            }
        }
    }
}
