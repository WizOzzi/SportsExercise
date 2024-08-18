// Exercise.java
package com.example.sportsexercisetracker;

public class Exercise {
    private long id;
    private String name;
    private int reps;
    private float weight;
    private String date;
    private String formattedReps;
    private String formattedWeight;

    public Exercise(long id, String name, int reps, float weight, String date, String formattedReps, String formattedWeight) {
        this.id = id;
        this.name = name;
        this.reps = reps;
        this.weight = weight;
        this.date = date;
        this.formattedReps = formattedReps;
        this.formattedWeight = formattedWeight;
    }
    public String getFormattedReps() {
        return formattedReps;
    }

    public String getFormattedWeight() {
        return formattedWeight;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getReps() {
        return reps;
    }
    public float getWeight() {
        return weight;
    }

    public String getDate() {
        return date;
    }

    // New methods for compatibility with the updated ExerciseGroupAdapter
    public String getRepsWithSuffix() {
        return formattedReps;
    }

    public String getWeightWithSuffix() {
        return formattedWeight;
    }
}

