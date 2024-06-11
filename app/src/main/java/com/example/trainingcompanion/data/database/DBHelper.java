package com.example.trainingcompanion.data.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import android.content.ContentValues;

import com.example.trainingcompanion.data.model.Category;
import com.example.trainingcompanion.data.model.Exercise;
import com.example.trainingcompanion.data.model.User;
import com.example.trainingcompanion.data.model.Workout;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class DBHelper extends SQLiteOpenHelper {
    public static final int version = 2;
    public static String dbName = "training-companion.db";

    public static final String WORKOUT_TABLE = "workouts";
    public static final String COL_ID = "id";
    public static final String COL_NAME = "name";
    public static final String COL_CATEGORY = "category";
    public static final String COL_WORKOUT_EXERCISES = "exercises";
    public static final String COL_INFO = "info";

    public static final String EXERCISES_TABLE = "exercises";
    public static final String COL_DURATION = "duration";

    public static final String CATEGORIES_TABLE = "categories";

    public static final String USER_TABLE = "user";
    public static final String COL_AGE = "age";
    public static final String COL_WEIGHT = "weight";
    public static final String COL_HEIGHT = "height";
    public static final String COL_SEX = "sex";

    public static final String HISTORY_TABLE = "history";
    public static final String COL_DATE = "date";
    public static final String COL_WORKOUT = "workout";
    public static final String COL_AVG = "avg";
    public static final String COL_MAX = "max";
    public static final String COL_MIN = "min";
    public static final String COL_CALORIES = "calories";

    private static final String CREATE_WORKOUTS_TABLE = "create table if not exists " + WORKOUT_TABLE + "(" +
            COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
            COL_NAME + " TEXT NOT NULL," +
            COL_CATEGORY + " INTEGER NOT NULL, " +
            COL_WORKOUT_EXERCISES + " TEXT NOT NULL, " +
            COL_INFO + " TEXT NOT NULL);";

    private static final String CREATE_EXERCISES_TABLE = "create table if not exists " + EXERCISES_TABLE + "(" +
            COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
            COL_NAME + " TEXT NOT NULL," +
            COL_CATEGORY + " INTEGER NOT NULL, " +
            COL_DURATION + " INT NOT NULL, " +
            COL_INFO + " TEXT NOT NULL);";

    private static final String CREATE_CATEGORIES_TABLE = "create table if not exists " + CATEGORIES_TABLE + "(" +
            COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
            COL_NAME + " TEXT NOT NULL);";

    private static final String CREATE_USER_TABLE = "create table if not exists " + USER_TABLE + "(" +
            COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
            COL_AGE + " INT NOT NULL, " +
            COL_WEIGHT + " INT NOT NULL, " +
            COL_HEIGHT + " INT NOT NULL, " +
            COL_SEX + " TEXT NOT NULL);";

    private static final String CREATE_HISTORY_TABLE = "create table if not exists " + HISTORY_TABLE + "(" +
            COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
            COL_DATE + " TEXT NOT NULL," +
            COL_WORKOUT + " TEXT NOT NULL, " +
            COL_AVG + " INT NOT NULL, " +
            COL_MIN + " INT NOT NULL, " +
            COL_MAX + " INT NOT NULL, " +
            COL_CALORIES + " TEXT NOT NULL);";

    private static final String DROP_WORKOUTS_TABLE = "DROP TABLE IF EXISTS " + WORKOUT_TABLE;
    private static final String DROP_EXERCISES_TABLE = "DROP TABLE IF EXISTS " + EXERCISES_TABLE;
    private static final String DROP_CATEGORIES_TABLE = "DROP TABLE IF EXISTS " + CATEGORIES_TABLE;
    private static final String DROP_USER_TABLE = "DROP TABLE IF EXISTS " + USER_TABLE;
    private static final String DROP_HISTORY_TABLE = "DROP TABLE IF EXISTS " + HISTORY_TABLE;

    SQLiteDatabase database;
    private Context context;

    public DBHelper(Context context) {
        super(context, dbName, null, version);
        this.context = context.getApplicationContext();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        try {
            this.database = db;
            db.execSQL(CREATE_EXERCISES_TABLE);
            db.execSQL(CREATE_WORKOUTS_TABLE);
            db.execSQL(CREATE_CATEGORIES_TABLE);
            db.execSQL(CREATE_USER_TABLE);
            db.execSQL(CREATE_HISTORY_TABLE);

            insertEmptyUser();
            putDefaultData();

        } catch (Exception e) {
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        dropTables(db);
        onCreate(db);
    }

    private boolean insertExercise(Exercise exercise) {
        //SQLiteDatabase db=this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_NAME, exercise.getName());
        cv.put(COL_CATEGORY, exercise.getCategory().getId());
        cv.put(COL_DURATION, exercise.getDuration());
        cv.put(COL_INFO, exercise.getInstruction());

        long result = database.insert(EXERCISES_TABLE, null, cv);
        return result != -1;
    }

    private boolean insertWorkout(Workout workout) {
        //SQLiteDatabase db=this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_NAME, workout.getName());
        cv.put(COL_CATEGORY, workout.getCategory().getId());
        String exercises = workout.getExercises().stream()
                .map(Exercise::getName)
                .collect(Collectors.joining("⊕"));
        cv.put(COL_WORKOUT_EXERCISES, exercises);
        cv.put(COL_INFO, workout.getInfo());

        long result = database.insert(WORKOUT_TABLE, null, cv);
        return result != -1;
    }

    private boolean insertCategory(Category category) {
        ContentValues cv = new ContentValues();
        cv.put(COL_ID, category.getId());
        cv.put(COL_NAME, category.getName());

        long result = database.insert(CATEGORIES_TABLE, null, cv);
        return result != -1;
    }

    private void insertEmptyUser() {
        ContentValues cv = new ContentValues();
        cv.put(COL_AGE, 0);
        cv.put(COL_HEIGHT, 0);
        cv.put(COL_WEIGHT, 0);
        cv.put(COL_SEX, "");
        database.insert(USER_TABLE, null, cv);
    }

    private void putDefaultData() {
        List<Category> categories = new ArrayList<>(Arrays.asList(
                new Category(1, "Arms"),
                new Category(2, "Legs"),
                new Category(3, "Torso"),
                new Category(4, "Cardio"),
                new Category(5, "Strength"),
                new Category(6, "Mixed")
        ));
        for (Category c : categories) {
            insertCategory(c);
        }

        List<Exercise> exerciseList = new ArrayList<>(Arrays.asList(
                new Exercise("Running", 50, categories.get(3), "Use your legs to run"),
                new Exercise("Pushups", 20, categories.get(0), "Use your arms to push your body up"),
                new Exercise("Pullups", 55, categories.get(0), "Use your arms to pull your body up"),
                new Exercise("Squats", 1, categories.get(1), "Use your legs to squat and get up"),
                new Exercise("Jumps", 1, categories.get(5), "Use your legs to jump up and down"),
                new Exercise("Bicep Curls", 15, categories.get(0), "Lift weights to work your biceps"),
                new Exercise("Lunges", 10, categories.get(1), "Step forward and lower your hips"),
                new Exercise("Plank", 5, categories.get(2), "Hold your body in a straight line"),
                new Exercise("Burpees", 10, categories.get(5), "Perform a squat, jump, and pushup"),
                new Exercise("Cycling", 45, categories.get(3), "Pedal to work your legs and cardio"),
                new Exercise("Bench Press", 20, categories.get(4), "Press the barbell up from your chest"),
                new Exercise("Deadlifts", 25, categories.get(5), "Lift the barbell from the ground to your hips"),
                new Exercise("Mountain Climbers", 10, categories.get(5), "Run in place with hands on the ground"),
                new Exercise("Sit-ups", 15, categories.get(2), "Lift your torso to your knees"),
                new Exercise("Jump Rope", 20, categories.get(3), "Jump over the rope continuously"),
                new Exercise("Tricep Dips", 10, categories.get(0), "Lower and raise your body using your arms"),
                new Exercise("Leg Press", 20, categories.get(1), "Push the weight away with your legs"),
                new Exercise("Army Twists", 10, categories.get(2), "Twist your torso side to side"),
                new Exercise("High Knees", 5, categories.get(3), "Run in place lifting your knees high"),
                new Exercise("Box Jumps", 15, categories.get(5), "Jump onto and off a box or platform"),
                new Exercise("Shoulder Press", 20, categories.get(0), "Press the weights above your head"),
                new Exercise("Calf Raises", 10, categories.get(1), "Raise your heels to stand on your toes"),
                new Exercise("Flutter Kicks", 5, categories.get(2), "Kick your legs up and down while lying down")
        ));
        for (Exercise e : exerciseList) {
            insertExercise(e);
        }

        List<Workout> workoutList = new ArrayList<>(Arrays.asList(
                new Workout("Legs and Cardio", categories.get(5), new ArrayList<>(Arrays.asList(exerciseList.get(0), exerciseList.get(3))), "Basic legs workout"),
                new Workout("Mixed Workout 1", categories.get(5), new ArrayList<>(Arrays.asList(exerciseList.get(3), exerciseList.get(4), exerciseList.get(0))), "Basic mixed workout"),
                new Workout("Arms", categories.get(0), new ArrayList<>(Arrays.asList(exerciseList.get(1), exerciseList.get(2))), "Basic arms workout"),
                new Workout("Shortest", categories.get(5), new ArrayList<>(Arrays.asList(exerciseList.get(4))), "Shortest workout"),
                new Workout("Legs", categories.get(1), new ArrayList<>(Arrays.asList(exerciseList.get(7), exerciseList.get(6), exerciseList.get(16))), "Leg strengthening workout"),
                new Workout("Torso", categories.get(2), new ArrayList<>(Arrays.asList(exerciseList.get(8), exerciseList.get(9), exerciseList.get(14))), "Torso conditioning workout"),
                new Workout("Cardio Blast", categories.get(3), new ArrayList<>(Arrays.asList(exerciseList.get(0), exerciseList.get(11), exerciseList.get(19))), "High-intensity cardio workout"),
                new Workout("Strength and Endurance", categories.get(4), new ArrayList<>(Arrays.asList(exerciseList.get(10), exerciseList.get(12))), "Strength and endurance training"),
                new Workout("Upper Body", categories.get(0), new ArrayList<>(Arrays.asList(exerciseList.get(15), exerciseList.get(20))), "Upper body muscle building"),
                new Workout("Core Strength", categories.get(2), new ArrayList<>(Arrays.asList(exerciseList.get(8), exerciseList.get(17))), "Core strength workout"),
                new Workout("Full Body", categories.get(5), new ArrayList<>(Arrays.asList(exerciseList.get(8), exerciseList.get(12), exerciseList.get(4))), "Comprehensive full body workout"),
                new Workout("Power Legs", categories.get(1), new ArrayList<>(Arrays.asList(exerciseList.get(16), exerciseList.get(17))), "Leg power workout"),
                new Workout("Agility and Speed", categories.get(5), new ArrayList<>(Arrays.asList(exerciseList.get(13), exerciseList.get(18))), "Workout focusing on agility and speed")
        ));
        for (Workout w : workoutList) {
            insertWorkout(w);
        }
    }

    public void resetDatabase() {
        SQLiteDatabase db = getWritableDatabase();
        dropTables(db);
        onCreate(db);
    }

    private void dropTables(SQLiteDatabase db) {
        db.execSQL(DROP_WORKOUTS_TABLE);
        db.execSQL(DROP_EXERCISES_TABLE);
        db.execSQL(DROP_CATEGORIES_TABLE);
        db.execSQL(DROP_USER_TABLE);
        db.execSQL(DROP_HISTORY_TABLE);
    }

    public void resetDefaultData() {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL(DROP_WORKOUTS_TABLE);
        db.execSQL(DROP_EXERCISES_TABLE);
        db.execSQL(DROP_CATEGORIES_TABLE);
        db.execSQL(DROP_HISTORY_TABLE);
        setDefaultData(db);
    }

    private void setDefaultData(SQLiteDatabase db) {
        db.execSQL(CREATE_EXERCISES_TABLE);
        db.execSQL(CREATE_WORKOUTS_TABLE);
        db.execSQL(CREATE_CATEGORIES_TABLE);
        putDefaultData();
    }

    public void resetHistoryTable() {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL(DROP_HISTORY_TABLE);
        db.execSQL(CREATE_HISTORY_TABLE);
    }
}