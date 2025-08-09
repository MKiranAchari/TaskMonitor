package com.example.taskmanager;

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
    private OnTaskActionListener listener;
    private boolean isHistoryView;

    public interface OnTaskActionListener {
        void onTaskCompleteChanged(Task task, boolean isChecked);
    }

    // Main constructor for the home screen
    public TaskAdapter(List<Task> taskList, OnTaskActionListener listener) {
        this.taskList = taskList;
        this.listener = listener;
        this.isHistoryView = false;
    }

    // New constructor for the history screen
    public TaskAdapter(List<Task> taskList, OnTaskActionListener listener, boolean isHistoryView) {
        this.taskList = taskList;
        this.listener = listener;
        this.isHistoryView = isHistoryView;
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.task_item, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task currentTask = taskList.get(position);
        holder.titleTextView.setText(currentTask.getTitle());
        holder.dateTextView.setText(currentTask.getFormattedDate());
        holder.timeTextView.setText(currentTask.getFormattedTime());

        if (isHistoryView) {
            holder.checkBox.setVisibility(View.GONE);
        } else {
            holder.checkBox.setVisibility(View.VISIBLE);
            // This is the key fix. We temporarily remove the listener
            // before setting the checked state to prevent an infinite loop
            // or an unwanted trigger during view recycling.
            holder.checkBox.setOnCheckedChangeListener(null);
            holder.checkBox.setChecked(currentTask.isCompleted());
            // Then, we re-attach the listener to correctly handle user clicks.
            holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (listener != null) {
                    listener.onTaskCompleteChanged(currentTask, isChecked);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }

    public void setTasks(List<Task> tasks) {
        this.taskList.clear();
        this.taskList.addAll(tasks);
        notifyDataSetChanged();
    }

    static class TaskViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView;
        TextView dateTextView;
        TextView timeTextView;
        CheckBox checkBox;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.titleTextView);
            dateTextView = itemView.findViewById(R.id.dateTextView);
            timeTextView = itemView.findViewById(R.id.timeTextView);
            checkBox = itemView.findViewById(R.id.taskCheckBox);
        }
    }
}
