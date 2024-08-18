// ExerciseDbHelper.java
package com.example.sportsexercisetracker;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class ExerciseDbHelper extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 9;
    public static final String DATABASE_NAME = "Exercise.db";



       private static final String SQL_CREATE_EXERCISE_TABLE =
               "CREATE TABLE " + ExerciseContract.ExerciseEntry.TABLE_NAME + " (" +
                       ExerciseContract.ExerciseEntry._ID + " INTEGER PRIMARY KEY," +
                       ExerciseContract.ExerciseEntry.COLUMN_NAME_EXERCISE + " TEXT," +
                       ExerciseContract.ExerciseEntry.COLUMN_NAME_REPS + " INTEGER," +
                       ExerciseContract.ExerciseEntry.COLUMN_NAME_WEIGHT + " REAL," +
                       ExerciseContract.ExerciseEntry.COLUMN_NAME_DATE + " TEXT," +
                       ExerciseContract.ExerciseEntry.COLUMN_NAME_EXERCISE_TYPE_ID + " INTEGER," +
                       "FOREIGN KEY(" + ExerciseContract.ExerciseEntry.COLUMN_NAME_EXERCISE_TYPE_ID + ") " +
                       "REFERENCES " + ExerciseContract.ExerciseTypeEntry.TABLE_NAME + "(" + ExerciseContract.ExerciseTypeEntry._ID + "))";

       private static final String SQL_CREATE_EXERCISE_TYPE_TABLE =
               "CREATE TABLE " + ExerciseContract.ExerciseTypeEntry.TABLE_NAME + " (" +
                       ExerciseContract.ExerciseTypeEntry._ID + " INTEGER PRIMARY KEY," +
                       ExerciseContract.ExerciseTypeEntry.COLUMN_NAME_TYPE + " TEXT UNIQUE)";

       public ExerciseDbHelper(Context context) {
           super(context, DATABASE_NAME, null, DATABASE_VERSION);
       }

       @Override
       public void onCreate(SQLiteDatabase db) {
           db.execSQL(SQL_CREATE_EXERCISE_TABLE);
           db.execSQL(SQL_CREATE_EXERCISE_TYPE_TABLE);


           // Add default exercise types if needed
           String[] defaultTypes = { "Bench press", "Pull-ups", "Push-ups", "Squats"};
           for (String type : defaultTypes) {
               ContentValues values = new ContentValues();
               values.put(ExerciseContract.ExerciseTypeEntry.COLUMN_NAME_TYPE, type);
               db.insert(ExerciseContract.ExerciseTypeEntry.TABLE_NAME, null, values);
           }
       }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 9) {
            // Create the new exercise type table
            db.execSQL(SQL_CREATE_EXERCISE_TYPE_TABLE);

            // Migrate data from the old structure to the new one
            // We'll assume the old column was named "exercise_type"
            db.execSQL("INSERT INTO " + ExerciseContract.ExerciseTypeEntry.TABLE_NAME +
                    " (" + ExerciseContract.ExerciseTypeEntry.COLUMN_NAME_TYPE + ") " +
                    "SELECT DISTINCT exercise_type FROM " + ExerciseContract.ExerciseEntry.TABLE_NAME +
                    " WHERE exercise_type IS NOT NULL AND exercise_type != ''");

            // Add a new column to the exercise table for the foreign key
            db.execSQL("ALTER TABLE " + ExerciseContract.ExerciseEntry.TABLE_NAME +
                    " ADD COLUMN " + ExerciseContract.ExerciseEntry.COLUMN_NAME_EXERCISE_TYPE_ID + " INTEGER");

            // Update the foreign key in the exercise table
            db.execSQL("UPDATE " + ExerciseContract.ExerciseEntry.TABLE_NAME + " SET " +
                    ExerciseContract.ExerciseEntry.COLUMN_NAME_EXERCISE_TYPE_ID + " = " +
                    "(SELECT " + ExerciseContract.ExerciseTypeEntry._ID +
                    " FROM " + ExerciseContract.ExerciseTypeEntry.TABLE_NAME +
                    " WHERE " + ExerciseContract.ExerciseTypeEntry.COLUMN_NAME_TYPE +
                    " = " + ExerciseContract.ExerciseEntry.TABLE_NAME + ".exercise_type)");

            // Remove the old exercise type column
            // Note: SQLite doesn't support dropping columns directly, so we need to recreate the table
            db.execSQL("CREATE TABLE exercise_temp (" +
                    ExerciseContract.ExerciseEntry._ID + " INTEGER PRIMARY KEY," +
                    ExerciseContract.ExerciseEntry.COLUMN_NAME_EXERCISE + " TEXT," +
                    ExerciseContract.ExerciseEntry.COLUMN_NAME_REPS + " INTEGER," +
                    ExerciseContract.ExerciseEntry.COLUMN_NAME_WEIGHT + " REAL," +
                    ExerciseContract.ExerciseEntry.COLUMN_NAME_DATE + " TEXT," +
                    ExerciseContract.ExerciseEntry.COLUMN_NAME_EXERCISE_TYPE_ID + " INTEGER)");

            db.execSQL("INSERT INTO exercise_temp SELECT " +
                    ExerciseContract.ExerciseEntry._ID + ", " +
                    ExerciseContract.ExerciseEntry.COLUMN_NAME_EXERCISE + ", " +
                    ExerciseContract.ExerciseEntry.COLUMN_NAME_REPS + ", " +
                    ExerciseContract.ExerciseEntry.COLUMN_NAME_WEIGHT + ", " +
                    ExerciseContract.ExerciseEntry.COLUMN_NAME_DATE + ", " +
                    ExerciseContract.ExerciseEntry.COLUMN_NAME_EXERCISE_TYPE_ID +
                    " FROM " + ExerciseContract.ExerciseEntry.TABLE_NAME);

            db.execSQL("DROP TABLE " + ExerciseContract.ExerciseEntry.TABLE_NAME);
            db.execSQL("ALTER TABLE exercise_temp RENAME TO " + ExerciseContract.ExerciseEntry.TABLE_NAME);
        }
    }


    public long addExerciseType(String type) {
        SQLiteDatabase db = this.getWritableDatabase();
        long id = -1;
        try {
            ContentValues values = new ContentValues();
            values.put(ExerciseContract.ExerciseTypeEntry.COLUMN_NAME_TYPE, type);
            id = db.insert(ExerciseContract.ExerciseTypeEntry.TABLE_NAME, null, values);
        } catch (Exception e) {
            Log.e("DatabaseError", "Error adding exercise type: " + e.getMessage());
        }
        return id;
    }


  public boolean deleteExerciseType(String type) {
      SQLiteDatabase db = this.getWritableDatabase();
      try {
          Log.d("DBHelper", "Attempting to delete exercise type: " + type);
          int result = db.delete(ExerciseContract.ExerciseTypeEntry.TABLE_NAME,
                  ExerciseContract.ExerciseTypeEntry.COLUMN_NAME_TYPE + "=?",
                  new String[]{type});
          Log.d("DBHelper", "Delete result: " + result);
          return result > 0;
      } catch (Exception e) {
          Log.e("DBHelper", "Error deleting exercise type: " + type, e);
          return false;
      } finally {
          db.close();
      }
  }

    public List<String> getAllExerciseTypes() {
        List<String> types = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = null;
        try {
            cursor = db.query(
                    ExerciseContract.ExerciseTypeEntry.TABLE_NAME,
                    new String[]{ExerciseContract.ExerciseTypeEntry.COLUMN_NAME_TYPE},
                    null, null, null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                int columnIndex = cursor.getColumnIndex(ExerciseContract.ExerciseTypeEntry.COLUMN_NAME_TYPE);
                if (columnIndex != -1) {
                    do {
                        types.add(cursor.getString(columnIndex));
                    } while (cursor.moveToNext());
                } else {
                    Log.e("DatabaseError", "Column " + ExerciseContract.ExerciseTypeEntry.COLUMN_NAME_TYPE + " not found");
                }
            }
        } catch (Exception e) {
            Log.e("DatabaseError", "Error querying exercise types: " + e.getMessage());
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }

        return types;
    }

}

