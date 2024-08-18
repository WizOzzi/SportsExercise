// ViewExercisesActivity.java
package com.example.sportsexercisetracker;

import android.content.DialogInterface;
import android.app.AlertDialog;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.provider.BaseColumns;

import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.text.DecimalFormat;

import androidx.recyclerview.widget.GridLayoutManager;
import java.util.TreeMap;

public class ViewExercisesActivity extends AppCompatActivity implements ExerciseGroupAdapter.OnDeleteClickListener,  ExerciseGroupAdapter.OnItemClickListener, SwipeToDeleteCallback.OnSwipeListener {


    private RecyclerView recyclerView;
    private ExerciseAdapter exerciseAdapter;
    private ExerciseDbHelper dbHelper;
    private Button button_back;
    private Spinner spinner_sort;
    private List<String> exerciseTypes;
    private ExerciseGroupAdapter exerciseGroupAdapter;
    private Map<String, Map<String, List<Exercise>>> groupedExercises;

   // TextView textViewExercises = findViewById(R.id.textViewExercises);
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_exercises);

        recyclerView = findViewById(R.id.recyclerView_exercises);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setItemAnimator(new FadeOutItemAnimator());





        dbHelper = new ExerciseDbHelper(this);

        // Initialize and set up the spinner
        spinner_sort = findViewById(R.id.spinner_sort);
        setupSpinner();

        // Load and group exercises
        List<Exercise> allExercises = getAllExercises();
        groupedExercises = groupExercises(allExercises);

        // Create and set the adapter
        exerciseGroupAdapter = new ExerciseGroupAdapter(groupedExercises);
        exerciseGroupAdapter.setOnDeleteClickListener(this);
        exerciseGroupAdapter.setOnItemClickListener(this);
        recyclerView.setAdapter(exerciseGroupAdapter);
/*
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new SwipeToDeleteCallback(this, exerciseGroupAdapter));
        itemTouchHelper.attachToRecyclerView(recyclerView);
*/
        SwipeToDeleteCallback swipeToDeleteCallback = new SwipeToDeleteCallback(this, exerciseGroupAdapter, this);
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(swipeToDeleteCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);


        button_back = findViewById(R.id.button_back);
        if (button_back != null) {
            button_back.setOnClickListener(v -> finish());
        } else {
            Log.e("ViewExercisesActivity", "Back button not found in layout");
        }

        loadExercises();


    }

    @Override
    public void onSwipeDelete(RecyclerView.ViewHolder viewHolder, int position) {
        Exercise exercise = (Exercise) exerciseGroupAdapter.getItem(position);
        showDeleteConfirmationDialog(viewHolder, exercise);
    }

    private void showDeleteConfirmationDialog(RecyclerView.ViewHolder viewHolder, final Exercise exercise) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Exercise")
                .setMessage("Are you sure you want to delete this exercise?")
                .setPositiveButton("Yes", (dialog, which) -> deleteExercise(exercise))
                .setNegativeButton("No", (dialog, which) -> {
                    // Reset the swiped item
                    exerciseGroupAdapter.notifyItemChanged(viewHolder.getAdapterPosition());
                })
                .show();
    }


    private void setupSpinner() {
        exerciseTypes = new ArrayList<>(getExerciseTypes());
        exerciseTypes.add(0, "All Types");

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, exerciseTypes);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_sort.setAdapter(spinnerAdapter);

        spinner_sort.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedType = exerciseTypes.get(position);
                List<Exercise> filteredExercises = filterExercises(selectedType);
                groupedExercises = groupExercises(filteredExercises);
                exerciseGroupAdapter.updateData(groupedExercises);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }


    private void loadExercises() {
        loadExercises("All Types");
    }

    private void loadExercises(String type) {
        List<Exercise> exercises = "All Types".equals(type) ? getAllExercises() : getExercisesByType(type);
        Map<String, Map<String, List<Exercise>>> groupedExercises = groupExercises(exercises);
        updateRecyclerView(groupedExercises);
    }



    private Map<String, Map<String, List<Exercise>>> groupExercises(List<Exercise> exercises) {
        Map<String, Map<String, List<Exercise>>> grouped = new TreeMap<>(Collections.reverseOrder());

        for (Exercise exercise : exercises) {
            String date = exercise.getDate();
            String type = exercise.getName();

            grouped.putIfAbsent(date, new TreeMap<>());
            grouped.get(date).putIfAbsent(type, new ArrayList<>());
            grouped.get(date).get(type).add(exercise);
        }

        return grouped;
    }


    private List<String> getExerciseTypes() {
        List<String> types = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(true, ExerciseContract.ExerciseEntry.TABLE_NAME,
                new String[]{ExerciseContract.ExerciseEntry.COLUMN_NAME_EXERCISE},
                null, null, ExerciseContract.ExerciseEntry.COLUMN_NAME_EXERCISE, null, null, null);

        while (cursor.moveToNext()) {
            String type = cursor.getString(cursor.getColumnIndexOrThrow(ExerciseContract.ExerciseEntry.COLUMN_NAME_EXERCISE));
            types.add(type);
        }
        cursor.close();
        return types;
    }

    private List<Exercise> filterExercises(String type) {
        List<Exercise> filteredList;
        if ("All Types".equals(type)) {
            filteredList = getAllExercises();
        } else {
            filteredList = getExercisesByType(type);
        }
        return filteredList;
    }

    private List<Exercise> getExercisesByType(String type) {
        List<Exercise> exerciseList = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String[] projection = {
                BaseColumns._ID,
                ExerciseContract.ExerciseEntry.COLUMN_NAME_EXERCISE,
                ExerciseContract.ExerciseEntry.COLUMN_NAME_REPS,
                ExerciseContract.ExerciseEntry.COLUMN_NAME_WEIGHT,
                ExerciseContract.ExerciseEntry.COLUMN_NAME_DATE
        };
        String selection = ExerciseContract.ExerciseEntry.COLUMN_NAME_EXERCISE + " = ?";
        String[] selectionArgs = { type };
        String sortOrder = ExerciseContract.ExerciseEntry.COLUMN_NAME_DATE + " DESC";

        Cursor cursor = db.query(
                ExerciseContract.ExerciseEntry.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );


        while (cursor.moveToNext()) {
            long id = cursor.getLong(cursor.getColumnIndexOrThrow(BaseColumns._ID));
            String name = cursor.getString(cursor.getColumnIndexOrThrow(ExerciseContract.ExerciseEntry.COLUMN_NAME_EXERCISE));
            int reps = cursor.getInt(cursor.getColumnIndexOrThrow(ExerciseContract.ExerciseEntry.COLUMN_NAME_REPS));
            float weight = cursor.getFloat(cursor.getColumnIndexOrThrow(ExerciseContract.ExerciseEntry.COLUMN_NAME_WEIGHT));
            String date = cursor.getString(cursor.getColumnIndexOrThrow(ExerciseContract.ExerciseEntry.COLUMN_NAME_DATE));

            // When displaying weight
            DecimalFormat df = new DecimalFormat("#.#"); // Show one decimal place
            String formattedWeight = df.format(weight) ;

            Exercise exercise = new Exercise(id, name, reps, weight, date, reps + "x", formattedWeight + " kg");
            exerciseList.add(exercise);
        }
        cursor.close();
        return exerciseList;
    }

    private void updateRecyclerView(Map<String, Map<String, List<Exercise>>> groupedExercises) {
        exerciseGroupAdapter = new ExerciseGroupAdapter(groupedExercises);
        exerciseGroupAdapter.setOnDeleteClickListener(this);
        recyclerView.setAdapter(exerciseGroupAdapter);
    }





    @Override
    public void onItemClick(Exercise exercise) {
        showDeleteConfirmationDialog(exercise);
    }

    @Override
    public void onDeleteClick(Exercise exercise) {
        showDeleteConfirmationDialog(exercise);
    }

    private void showDeleteConfirmationDialog(final Exercise exercise) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Exercise")
                .setMessage("Are you sure you want to delete this exercise?")
                .setPositiveButton("Yes", (dialog, which) -> deleteExercise(exercise))
                .setNegativeButton("No", null)
                .show();
    }



   /* private void deleteExercise(Exercise exercise) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String selection = BaseColumns._ID + " = ?";
        String[] selectionArgs = { String.valueOf(exercise.getId()) };
        int deletedRows = db.delete(ExerciseContract.ExerciseEntry.TABLE_NAME, selection, selectionArgs);

        if (deletedRows > 0) {
            exerciseGroupAdapter.remove(exercise);
            Toast.makeText(this, "Exercise deleted", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Failed to delete exercise", Toast.LENGTH_SHORT).show();
        }

    }
*/
    private void deleteExercise(Exercise exercise) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String selection = BaseColumns._ID + " = ?";
        String[] selectionArgs = { String.valueOf(exercise.getId()) };
        int deletedRows = db.delete(ExerciseContract.ExerciseEntry.TABLE_NAME, selection, selectionArgs);

        if (deletedRows > 0) {
            exerciseGroupAdapter.removeItem(exercise);
            Toast.makeText(this, "Exercise deleted", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Failed to delete exercise", Toast.LENGTH_SHORT).show();
        }
    }

       private List<Exercise> getAllExercises() {
            List<Exercise> exerciseList = new ArrayList<>();
            SQLiteDatabase db = dbHelper.getReadableDatabase();

            String[] projection = {
                    BaseColumns._ID,
                    ExerciseContract.ExerciseEntry.COLUMN_NAME_EXERCISE,
                    ExerciseContract.ExerciseEntry.COLUMN_NAME_REPS,
                    ExerciseContract.ExerciseEntry.COLUMN_NAME_WEIGHT,
                    ExerciseContract.ExerciseEntry.COLUMN_NAME_DATE
            };
            String sortOrder = ExerciseContract.ExerciseEntry.COLUMN_NAME_DATE + " DESC";
            Cursor cursor = db.query(
                    ExerciseContract.ExerciseEntry.TABLE_NAME,
                    projection,
                    null,
                    null,
                    null,
                    null,
                    sortOrder
            );

            while (cursor.moveToNext()) {
                long id = cursor.getLong(cursor.getColumnIndexOrThrow(BaseColumns._ID));
                String name = cursor.getString(cursor.getColumnIndexOrThrow(ExerciseContract.ExerciseEntry.COLUMN_NAME_EXERCISE));
                int reps = cursor.getInt(cursor.getColumnIndexOrThrow(ExerciseContract.ExerciseEntry.COLUMN_NAME_REPS));
                float weight = cursor.getFloat(cursor.getColumnIndexOrThrow(ExerciseContract.ExerciseEntry.COLUMN_NAME_WEIGHT));
                String date = cursor.getString(cursor.getColumnIndexOrThrow(ExerciseContract.ExerciseEntry.COLUMN_NAME_DATE));

                // When displaying weight
                DecimalFormat df = new DecimalFormat("#.#"); // Show one decimal place
                String formattedWeight = df.format(weight) ;

                // Create Exercise object with suffixes added to reps and weight
                Exercise exercise = new Exercise(
                        id,
                        name,
                        reps,
                        weight,
                        date,
                        reps + "x",  // Add "x" suffix to reps
                        formattedWeight + " kg"  // Add "kg" suffix to weight
                );

                exerciseList.add(exercise);
            }
            cursor.close();
            return exerciseList;
        }

    }

