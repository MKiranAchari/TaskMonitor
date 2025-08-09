package com.example.taskmanager; // Make sure this matches your package name

import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    private List<Task> taskList;
    private OnTaskActionListener listener; // Listener for checkbox and other actions

    // Interface to communicate actions back to the Activity/Fragment
    public interface OnTaskActionListener {
        void onTaskCompleteChanged(Task task, boolean isChecked);
        // Add other action methods here if needed, e.g., void onTaskClicked(Task task);
    }

    public TaskAdapter(List<Task> taskList, OnTaskActionListener listener) {
        this.taskList = taskList;
        this.listener = listener;
    }

    // This method is called when RecyclerView needs a new ViewHolder of the given type.
    // It inflates the item layout (task_item.xml) and returns a new TaskViewHolder.
    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.task_item, parent, false);
        return new TaskViewHolder(itemView);
    }

    // This method is called by RecyclerView to display the data at the specified position.
    // It updates the contents of the ViewHolder to reflect the item at the given position.
    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task currentTask = taskList.get(position);

        holder.textViewTitle.setText(currentTask.getTitle());
        String dateTime = currentTask.getDate() + " " + currentTask.getTime();
        holder.textViewDateTime.setText(dateTime);

        // Set optional name visibility and text
        if (currentTask.getName() != null && !currentTask.getName().isEmpty()) {
            holder.textViewName.setText("Name: " + currentTask.getName());
            holder.textViewName.setVisibility(View.VISIBLE);
        } else {
            holder.textViewName.setVisibility(View.GONE);
        }

        // Set checkbox state based on task completion status
        holder.checkBoxComplete.setChecked(currentTask.isCompleted());

        // Apply strikethrough if task is completed
        applyStrikethrough(holder.textViewTitle, currentTask.isCompleted());
        applyStrikethrough(holder.textViewDateTime, currentTask.isCompleted());
        applyStrikethrough(holder.textViewName, currentTask.isCompleted());

        // Set listener for checkbox
        holder.checkBoxComplete.setOnCheckedChangeListener(null); // Clear previous listener to prevent issues
        holder.checkBoxComplete.setChecked(currentTask.isCompleted());
        holder.checkBoxComplete.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // Update the task's completion status
            currentTask.setCompleted(isChecked);
            // Notify the listener (MainActivity) about the change
            if (listener != null) {
                listener.onTaskCompleteChanged(currentTask, isChecked);
            }
            // Apply strikethrough immediately
            applyStrikethrough(holder.textViewTitle, isChecked);
            applyStrikethrough(holder.textViewDateTime, isChecked);
            applyStrikethrough(holder.textViewName, isChecked);
        });
    }

    // Helper method to apply or remove strikethrough
    private void applyStrikethrough(TextView textView, boolean apply) {
        if (apply) {
            textView.setPaintFlags(textView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        } else {
            textView.setPaintFlags(textView.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
        }
    }

    // Returns the total number of items in the data set held by the adapter.
    @Override
    public int getItemCount() {
        return taskList.size();
    }

    // Method to update the adapter's data set and notify RecyclerView
    public void setTasks(List<Task> newTasks) {
        this.taskList = newTasks;
        notifyDataSetChanged(); // Notifies the adapter that the data has changed
    }

    // ViewHolder class: Holds references to the views for each item in the RecyclerView.
    // This helps in recycling views efficiently.
    public static class TaskViewHolder extends RecyclerView.ViewHolder {
        public TextView textViewTitle;
        public TextView textViewDateTime;
        public TextView textViewName;
        public CheckBox checkBoxComplete;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewTitle = itemView.findViewById(R.id.textViewTaskTitle);
            textViewDateTime = itemView.findViewById(R.id.textViewTaskDateTime);
            textViewName = itemView.findViewById(R.id.textViewTaskName);
            checkBoxComplete = itemView.findViewById(R.id.checkBoxTaskComplete);
        }
    }
}
