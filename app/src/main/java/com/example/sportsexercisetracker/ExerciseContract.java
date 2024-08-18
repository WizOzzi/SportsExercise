// ExerciseContract.java
package com.example.sportsexercisetracker;

import android.provider.BaseColumns;

public final class ExerciseContract {
    private ExerciseContract() {}

    public static class ExerciseEntry implements BaseColumns {
        public static final String TABLE_NAME = "exercise";
        public static final String COLUMN_NAME_EXERCISE = "exercise";
        public static final String COLUMN_NAME_REPS = "reps";
        public static final String COLUMN_NAME_WEIGHT = "weight";
        public static final String COLUMN_NAME_DATE = "date";
        public static final String COLUMN_NAME_EXERCISE_TYPE_ID = "exercise_type_id";
    }

    public static class ExerciseTypeEntry implements BaseColumns {
        public static final String TABLE_NAME = "exercise_type";
        public static final String COLUMN_NAME_TYPE = "type";
    }


}

