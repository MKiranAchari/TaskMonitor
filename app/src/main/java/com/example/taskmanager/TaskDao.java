package com.example.taskmanager; // Make sure this matches your package name

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

// @Dao annotation marks this interface as a Room DAO
@Dao
public interface TaskDao {

    // @Insert annotation for inserting a single Task object into the database.
    // onConflict = OnConflictStrategy.REPLACE means if a task with the same primary key
    // already exists, it will be replaced.
    @Insert
    void insertTask(Task task);

    // @Query annotation for querying data from the database.
    // This query selects all tasks from the 'tasks' table.
    @Query("SELECT * FROM tasks ORDER BY date ASC, time ASC")
    List<Task> getAllTasks();

    // @Query to get only in-progress tasks, ordered by date and time
    @Query("SELECT * FROM tasks WHERE is_completed = 0 ORDER BY date ASC, time ASC")
    List<Task> getInProgressTasks();

    // @Query to get all tasks (in-progress and completed) for history
    @Query("SELECT * FROM tasks ORDER BY date DESC, time DESC")
    List<Task> getAllTasksForHistory();

    // @Update annotation for updating existing Task objects in the database.
    @Update
    void updateTask(Task task);

    // You could also add a delete method if needed:
    // @Delete
    // void deleteTask(Task task);
}
