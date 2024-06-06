package com.example.trainingcompanion.data.database;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.example.trainingcompanion.data.model.Category;
import com.example.trainingcompanion.data.model.Exercise;
import com.example.trainingcompanion.data.model.User;
import com.example.trainingcompanion.data.model.Workout;
import com.example.trainingcompanion.data.model.WorkoutRecord;
import com.example.trainingcompanion.extra.Application;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class DBManager {
    private Context context;
    private SQLiteDatabase database;
    private DBHelper dbHelper;

    public DBManager() {
        this.context = Application.getAppContext();
        this.dbHelper = new DBHelper(this.context);
        open();
    }

    public DBManager open() throws SQLException {
        this.database = this.dbHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        if (database != null && database.isOpen())
            database.close();
    }

    public void resetAllDatabase() {
        this.open();
        dbHelper.resetDatabase();
        this.close();
    }

    public void resetDefaultData() {
        this.open();
        User temp = getUserData();
        dbHelper.resetDatabase();
        updateUser(temp);
        this.close();
    }

    public void resetUserData() {
        ContentValues cv = new ContentValues();
        cv.put(DBHelper.COL_AGE, 0);
        cv.put(DBHelper.COL_WEIGHT, 0);
        cv.put(DBHelper.COL_WEIGHT, 0);
        cv.put(DBHelper.COL_SEX, "");

        database.update(DBHelper.USER_TABLE, cv, "`id` = 1", null);
    }

    public boolean updateUser(User user) {
        ContentValues cv = new ContentValues();
        cv.put(DBHelper.COL_AGE, user.getAge());
        cv.put(DBHelper.COL_WEIGHT, user.getWeight());
        cv.put(DBHelper.COL_HEIGHT, user.getHeight());
        cv.put(DBHelper.COL_SEX, user.getSex());

        long result = database.update(DBHelper.USER_TABLE, cv, "`id` = 1", null);
        return result != -1;
    }

    @SuppressLint("Range")
    public User getUserData() {
        String query = "select * from " + DBHelper.USER_TABLE + " where `id` = ?";
        try (Cursor cursor = dbHelper.getWritableDatabase().rawQuery(query, new String[]{String.valueOf(1)})) {
            if (cursor.moveToFirst()) {
                int _id = cursor.getInt(cursor.getColumnIndex(DBHelper.COL_ID));
                int age = Integer.parseInt(cursor.getString(cursor.getColumnIndex(DBHelper.COL_AGE)));
                int height = Integer.parseInt(cursor.getString(cursor.getColumnIndex(DBHelper.COL_HEIGHT)));
                int weight = Integer.parseInt(cursor.getString(cursor.getColumnIndex(DBHelper.COL_WEIGHT)));
                String sex = cursor.getString(cursor.getColumnIndex(DBHelper.COL_SEX));
                return new User(age, weight, height, sex);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            //this.close();
        }
        return null;
    }

    public void insertWorkoutRecord(WorkoutRecord workoutRecord) {
        ContentValues cv = new ContentValues();
        cv.put(DBHelper.COL_DATE, String.valueOf(workoutRecord.getDate()));
        cv.put(DBHelper.COL_WORKOUT, workoutRecord.getWorkout().getName());
        cv.put(DBHelper.COL_AVG, workoutRecord.getAvgHR());
        cv.put(DBHelper.COL_MAX, workoutRecord.getMaxHR());
        cv.put(DBHelper.COL_MIN, workoutRecord.getMinHR());
        cv.put(DBHelper.COL_CALORIES, workoutRecord.getCalories());
        this.database.insert(DBHelper.HISTORY_TABLE, null, cv);
    }

    private void deleteWorkoutRecord(String workoutName) {
        database.execSQL("DELETE FROM " + DBHelper.HISTORY_TABLE + " WHERE workout= '" + workoutName + "'");
    }

    public void clearHistory() {
        this.open();
        dbHelper.resetHistoryTable();
        this.close();
    }

    public void insertWorkout(Workout workout) {
        //this.open();
        ContentValues cv = new ContentValues();
        cv.put(DBHelper.COL_NAME, workout.getName());
        cv.put(DBHelper.COL_CATEGORY, workout.getCategory().getId());
        String exercises = workout.getExercises().stream()
                .map(Exercise::getName)
                .collect(Collectors.joining("⊕"));
        cv.put(DBHelper.COL_WORKOUT_EXERCISES, exercises);
        cv.put(DBHelper.COL_INFO, workout.getInfo());
        this.database.insert(DBHelper.WORKOUT_TABLE, null, cv);
        //this.close();
    }

    public void insertExercise(Exercise exercise) {
        //this.open();
        ContentValues cv = new ContentValues();
        cv.put(DBHelper.COL_NAME, exercise.getName());
        cv.put(DBHelper.COL_CATEGORY, exercise.getCategory().getId());
        cv.put(DBHelper.COL_DURATION, exercise.getDuration());
        cv.put(DBHelper.COL_INFO, exercise.getInstruction());
        this.database.insert(DBHelper.EXERCISES_TABLE, null, cv);
        //this.close();
    }

    @SuppressLint("Range")
    public ArrayList<WorkoutRecord> getAllWorkoutRecords() {
        ArrayList<WorkoutRecord> records = new ArrayList<>();
        String query = "select * from " + DBHelper.HISTORY_TABLE;
        try (Cursor cursor = dbHelper.getWritableDatabase().rawQuery(query, null)) {
            while (cursor.moveToNext()) {
                String date = cursor.getString(cursor.getColumnIndex(DBHelper.COL_DATE));
                String workoutName = cursor.getString(cursor.getColumnIndex(DBHelper.COL_WORKOUT));
                Workout workout = getWorkout(workoutName);
                int avg = cursor.getInt(cursor.getColumnIndex(DBHelper.COL_AVG));
                int max = cursor.getInt(cursor.getColumnIndex(DBHelper.COL_MAX));
                int min = cursor.getInt(cursor.getColumnIndex(DBHelper.COL_MIN));
                int calories = cursor.getInt(cursor.getColumnIndex(DBHelper.COL_CALORIES));
                records.add(new WorkoutRecord(date, workout, avg, max, min, calories));
            }
            Collections.reverse(records);
            return records;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @SuppressLint("Range")
    public ArrayList<Exercise> getAllExercises() {
        //this.open();
        ArrayList<Exercise> exercises = new ArrayList<>();
        String query = "select * from " + DBHelper.EXERCISES_TABLE;
        try (Cursor cursor = dbHelper.getWritableDatabase().rawQuery(query, null)) {
            while (cursor.moveToNext()) {
                String name = cursor.getString(cursor.getColumnIndex(DBHelper.COL_NAME));
                int duration = cursor.getInt(cursor.getColumnIndex(DBHelper.COL_DURATION));
                Category category = getCategory(cursor.getInt(cursor.getColumnIndex(DBHelper.COL_CATEGORY)));
                String info = cursor.getString(cursor.getColumnIndex(DBHelper.COL_INFO));

                exercises.add(new Exercise(name, duration, category, info));
            }
            return exercises;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            //this.close();
        }

        return null;
    }

    @SuppressLint("Range")
    public ArrayList<Workout> getAllWorkouts() {
        //this.open();
        ArrayList<Workout> workouts = new ArrayList<>();
        String query = "select * from " + DBHelper.WORKOUT_TABLE;
        try (Cursor cursor = dbHelper.getWritableDatabase().rawQuery(query, null)) {
            while (cursor.moveToNext()) {
                String name = cursor.getString(cursor.getColumnIndex(DBHelper.COL_NAME));
                Category category = getCategory(cursor.getInt(cursor.getColumnIndex(DBHelper.COL_CATEGORY)));
                String[] exercisesString = cursor.getString(cursor.getColumnIndex(DBHelper.COL_WORKOUT_EXERCISES)).split("⊕");
                ArrayList<Exercise> exercises = new ArrayList<>();
                String info = cursor.getString(cursor.getColumnIndex(DBHelper.COL_INFO));
                for (String es : exercisesString) {
                    exercises.add(this.getExercise(es));
                }
                workouts.add(new Workout(name, category, exercises, info));
            }
            return workouts;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            //this.close();
        }
        return null;
    }

    @SuppressLint("Range")
    public List<Category> getAllCategories() {
        //this.open();
        ArrayList<Category> categories = new ArrayList<>();
        String query = "select * from " + DBHelper.CATEGORIES_TABLE;
        try (Cursor cursor = dbHelper.getWritableDatabase().rawQuery(query, null)) {
            while (cursor.moveToNext()) {
                int _id = cursor.getInt(cursor.getColumnIndex(DBHelper.COL_ID));
                String name = cursor.getString(cursor.getColumnIndex(DBHelper.COL_NAME));
                categories.add(new Category(_id, name));
            }
            return categories;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            //this.close();
        }
        return null;
    }

    @SuppressLint("Range")
    public Category getCategory(int categoryId) {
        //this.open();
        String query = "select * from " + DBHelper.CATEGORIES_TABLE + " where `id` = ?";
        try (Cursor cursor = dbHelper.getWritableDatabase().rawQuery(query, new String[]{String.valueOf(categoryId)})) {
            if (cursor.moveToFirst()) {
                int _id = cursor.getInt(cursor.getColumnIndex(DBHelper.COL_ID));
                String name = cursor.getString(cursor.getColumnIndex(DBHelper.COL_NAME));
                return new Category(_id, name);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            //this.close();
        }
        return null;
    }

    @SuppressLint("Range")
    public Category getCategory(String categoryName) {
        //this.open();
        String query = "select * from " + DBHelper.CATEGORIES_TABLE + " where `name` = ?";
        try (Cursor cursor = dbHelper.getWritableDatabase().rawQuery(query, new String[]{categoryName})) {
            if (cursor.moveToFirst()) {
                int _id = cursor.getInt(cursor.getColumnIndex(DBHelper.COL_ID));
                String name = cursor.getString(cursor.getColumnIndex(DBHelper.COL_NAME));
                return new Category(_id, name);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            //this.close();
        }
        return null;
    }

    @SuppressLint("Range")
    public Exercise getExercise(String exerciseName) {
        //this.open();
        String query = "select * from " + DBHelper.EXERCISES_TABLE + " where `name` = ?";
        try (Cursor cursor = dbHelper.getWritableDatabase().rawQuery(query, new String[]{exerciseName})) {
            if (cursor.moveToFirst()) {
                String name = cursor.getString(cursor.getColumnIndex(DBHelper.COL_NAME));
                Category category = getCategory(cursor.getInt(cursor.getColumnIndex(DBHelper.COL_CATEGORY)));
                int duration = Integer.parseInt(cursor.getString(cursor.getColumnIndex(DBHelper.COL_DURATION)));
                String info = cursor.getString(cursor.getColumnIndex(DBHelper.COL_INFO));
                return new Exercise(name, duration, category, info);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            //this.close();
        }
        return null;
    }

    @SuppressLint("Range")
    public Workout getWorkout(String workoutName) {
        //this.open();
        String query = "select * from " + DBHelper.WORKOUT_TABLE + " where `name` = ?";
        try (Cursor cursor = dbHelper.getWritableDatabase().rawQuery(query, new String[]{workoutName})) {
            if (cursor.moveToFirst()) {
                String name = cursor.getString(cursor.getColumnIndex(DBHelper.COL_NAME));
                Category category = getCategory(cursor.getInt(cursor.getColumnIndex(DBHelper.COL_CATEGORY)));
                String[] exercisesString = cursor.getString(cursor.getColumnIndex(DBHelper.COL_WORKOUT_EXERCISES)).split("⊕");
                ArrayList<Exercise> exercises = new ArrayList<>();
                for (String es : exercisesString) {
                    exercises.add(this.getExercise(es));
                }
                String info = cursor.getString(cursor.getColumnIndex(DBHelper.COL_INFO));
                return new Workout(name, category, exercises, info);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            //this.close();
        }
        return null;
    }


    private Cursor fetchWorkoutCursor() {
        Cursor cursor = this.database.query(DBHelper.WORKOUT_TABLE,
                new String[]{DBHelper.COL_NAME,
                        DBHelper.COL_CATEGORY,
                        DBHelper.COL_WORKOUT_EXERCISES},
                null, null, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
        }
        return cursor;
    }

    private Cursor fetchExerciseCursor() {
        Cursor cursor = this.database.query(DBHelper.EXERCISES_TABLE,
                new String[]{DBHelper.COL_NAME,
                        DBHelper.COL_CATEGORY,
                        DBHelper.COL_DURATION,
                        DBHelper.COL_INFO},
                null, null, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
        }
        return cursor;
    }

    public int updateWorkout(Workout workout) {
        for (WorkoutRecord wr : getAllWorkoutRecords()) {
            if (wr.getWorkout().getName().equals(workout.getName())) {
                deleteWorkoutRecord(workout.getName());
            }
        }
        ContentValues cv = new ContentValues();
        cv.put(DBHelper.COL_NAME, workout.getName());
        cv.put(DBHelper.COL_CATEGORY, workout.getCategory().getId());
        String exercises = workout.getExercises().stream()
                .map(Exercise::getName)
                .collect(Collectors.joining("⊕"));
        cv.put(DBHelper.COL_WORKOUT_EXERCISES, exercises);
        cv.put(DBHelper.COL_INFO, workout.getInfo());
        return this.database.update(DBHelper.WORKOUT_TABLE, cv, "`name` = '" + workout.getName() + "'", null);
    }

    public int updateExercise(Exercise exercise) {
        ContentValues cv = new ContentValues();
        cv.put(DBHelper.COL_NAME, exercise.getName());
        cv.put(DBHelper.COL_CATEGORY, exercise.getCategory().getId());
        cv.put(DBHelper.COL_DURATION, exercise.getDuration());
        cv.put(DBHelper.COL_INFO, exercise.getInstruction());
        return this.database.update(DBHelper.EXERCISES_TABLE, cv, "`name` = '" + exercise.getName() + "'", null);
    }

    public int updateCategory(Category category) {
        ContentValues cv = new ContentValues();
        cv.put(DBHelper.COL_ID, category.getId());
        cv.put(DBHelper.COL_NAME, category.getName());
        return this.database.update(DBHelper.EXERCISES_TABLE, cv, "id = " + category.getId(), null);
    }

    public void deleteWorkout(String name) {
        for (WorkoutRecord wr : getAllWorkoutRecords()) {
            if (wr.getWorkout().getName().equals(name)) {
                deleteWorkoutRecord(name);
            }
        }
        this.database.delete(DBHelper.WORKOUT_TABLE, "`name`= '" + name + "'", null);
    }

    public void deleteExercise(String name) {
        for (Workout w : getAllWorkouts()) {
            if (w.getExercises().stream()
                    .anyMatch(exercise -> exercise.getName().equals(name))) {
                if (w.getExerciseCount() == 1) {
                    deleteWorkout(w.getName());
                } else {
                    w.setExercises((ArrayList<Exercise>) w.getExercises().stream()
                            .filter(ex -> !ex.getName().equals(name))
                            .collect(Collectors.toList()));
                    updateWorkout(w);
                }
            }
        }
        database.execSQL("DELETE FROM " + DBHelper.EXERCISES_TABLE + " WHERE name= '" + name + "'");
    }

    public void deleteCategory(int id) {
        this.database.delete(DBHelper.CATEGORIES_TABLE, "id=" + id, null);
    }

    public void deleteCategory(String name) {
        this.database.delete(DBHelper.CATEGORIES_TABLE, "`name`= '" + name + "'", null);
    }
}
