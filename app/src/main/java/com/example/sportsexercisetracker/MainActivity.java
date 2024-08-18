// MainActivity.java
package com.example.sportsexercisetracker;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private Spinner spinnerExercise;
    private EditText editTextReps;
    private EditText editTextWeight;
    private EditText editTextDate;
    private Button buttonSave;
    private Button buttonView;
    private ExerciseDbHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        spinnerExercise = findViewById(R.id.spinner_exercise);
        editTextReps = findViewById(R.id.editText_reps);
        editTextWeight = findViewById(R.id.editText_weight);
        editTextDate = findViewById(R.id.editText_date);
        buttonSave = findViewById(R.id.button_save);
        buttonView = findViewById(R.id.button_view);

        dbHelper = new ExerciseDbHelper(this);
       // updateExerciseTypeSpinner();
        // Set up the spinner with exercise options
        List<String> exerciseTypes = dbHelper.getAllExerciseTypes();
        String[] exercises = {"Push-ups", "Sit-ups", "Squats", "Lunges", "Plank", "Bench press"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, exerciseTypes);
        spinnerExercise.setAdapter(adapter);

        // Set up date picker dialog
        editTextDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog();
            }
        });

        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveExercise();
            }
        });

        buttonView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ViewExercisesActivity.class);
                startActivity(intent);
            }
        });

        Button addExerciseTypeButton = findViewById(R.id.button_add_exercise_type);
        addExerciseTypeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddExerciseTypeDialog();
            }
        });

        Button buttonDeleteExerciseType = findViewById(R.id.buttonDeleteExerciseType);
        buttonDeleteExerciseType.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDeleteExerciseTypeDialog();
            }
        });

    }

    private void showDatePickerDialog() {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        // Month is zero-based, so add 1 to make it human-readable
                        String selectedDate = year + "-" + (month + 1) + "-" + dayOfMonth;
                        editTextDate.setText(selectedDate);
                    }
                }, year, month, day);
        datePickerDialog.show();
    }

    private long saveExercise() {
       /* String exerciseName = spinnerExercise.getSelectedItem().toString();
        int reps = Integer.parseInt(editTextReps.getText().toString());
        int weight = Integer.parseInt(editTextWeight.getText().toString());
        String date = editTextDate.getText().toString();
        */

        String exerciseName = spinnerExercise.getSelectedItem().toString();
        String exerciseType = spinnerExercise.getSelectedItem().toString();

        int reps = 0;
        if (!editTextReps.getText().toString().isEmpty()) {
            try {
                reps = Integer.parseInt(editTextReps.getText().toString());
            } catch (NumberFormatException e) {
                // Handle the exception if needed
            }
        }

        float weight = 0;
        // When getting input from user (e.g., in EditText)
        String weightString = editTextWeight.getText().toString();
        if (!editTextWeight.getText().toString().isEmpty()) {
            try {
                weight = Float.parseFloat(weightString.replace(",", "."));
            } catch (NumberFormatException e) {
                // Handle the exception if needed
            }
        }

        String date = editTextDate.getText().toString();
        if (date.isEmpty()) {
            // Get current date if no date is selected
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            date = sdf.format(new Date());
        }


        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(ExerciseContract.ExerciseEntry.COLUMN_NAME_EXERCISE, exerciseName);
        values.put(ExerciseContract.ExerciseEntry.COLUMN_NAME_REPS, reps);
        values.put(ExerciseContract.ExerciseEntry.COLUMN_NAME_WEIGHT, weight);
        values.put(ExerciseContract.ExerciseEntry.COLUMN_NAME_DATE, date);
       // values.put(ExerciseContract.ExerciseEntry.COLUMN_NAME_EXERCISE_TYPE_ID, exerciseType);

        // Get the ID of the exercise type
        long exerciseTypeId = getExerciseTypeId(exerciseType);
        values.put(ExerciseContract.ExerciseEntry.COLUMN_NAME_EXERCISE_TYPE_ID, exerciseTypeId);

        long newRowId = db.insert(ExerciseContract.ExerciseEntry.TABLE_NAME, null, values);
        if (newRowId != -1) {

            Toast.makeText(this, "Exercise saved", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Error saving exercise", Toast.LENGTH_SHORT).show();
        }
        return newRowId;
    }

    private long getExerciseTypeId(String type) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        long id = -1;

        try {
            Cursor cursor = db.query(ExerciseContract.ExerciseTypeEntry.TABLE_NAME,
                    new String[]{ExerciseContract.ExerciseTypeEntry._ID},
                    ExerciseContract.ExerciseTypeEntry.COLUMN_NAME_TYPE + "=?",
                    new String[]{type}, null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                id = cursor.getLong(0);
            }

            if (cursor != null) {
                cursor.close();
            }
        } catch (Exception e) {
            Log.e("DatabaseError", "Error querying exercise type: " + e.getMessage());
        }

        if (id == -1) {
            // If the exercise type doesn't exist, add it
            id = dbHelper.addExerciseType(type);
        }

        return id;
    }
    private void showAddExerciseTypeDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add New Exercise Type");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String newExerciseType = input.getText().toString();
                if (!newExerciseType.isEmpty()) {
                    dbHelper.addExerciseType(newExerciseType);
                    updateExerciseTypeSpinner();
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }


   private void updateExerciseTypeSpinner() {
       List<String> exerciseTypes = dbHelper.getAllExerciseTypes();
       ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, exerciseTypes);
       adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
       spinnerExercise.setAdapter(adapter);
   }




    private void showDeleteExerciseTypeDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete Exercise Type");

        final Spinner spinner = new Spinner(this);
        final ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, dbHelper.getAllExerciseTypes());
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        builder.setView(spinner);

        builder.setPositiveButton("Delete", null); // We'll set the listener later

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        final AlertDialog dialog = builder.create();
        dialog.show();

        // Set the positive button listener after dialog is shown
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String selectedExerciseType = (String) spinner.getSelectedItem();
                if (selectedExerciseType != null && !selectedExerciseType.isEmpty()) {
                    try {
                        boolean deleted = dbHelper.deleteExerciseType(selectedExerciseType);
                        if (deleted) {
                            adapter.remove(selectedExerciseType);
                            adapter.notifyDataSetChanged();
                            updateExerciseTypeSpinner();
                            Toast.makeText(getApplicationContext(), "Exercise type deleted successfully", Toast.LENGTH_SHORT).show();
                            if (adapter.getCount() == 0) {
                                dialog.dismiss();
                            }
                        } else {
                            Toast.makeText(getApplicationContext(), "Failed to delete exercise type", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Log.e("DeleteExerciseType", "Error deleting exercise type", e);
                        Toast.makeText(getApplicationContext(), "Error deleting exercise type", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }
}
