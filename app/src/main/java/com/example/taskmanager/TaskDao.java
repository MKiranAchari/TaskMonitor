package com.example.taskmanager;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface TaskDao {

    // Method to insert a new task into the database
    @Insert
    void insert(Task task);

    // Method to update an existing task (e.g., when it is marked as complete)
    @Update
    void updateTask(Task task);

    // Method to get all tasks from the database, ordered by date and time
    @Query("SELECT * FROM task_table ORDER BY task_date_time ASC")
    List<Task> getAllTasks();

    // Method to get only the in-progress (not completed) tasks
    @Query("SELECT * FROM task_table WHERE is_completed = 0 ORDER BY task_date_time ASC")
    List<Task> getInProgressTasks();
}
