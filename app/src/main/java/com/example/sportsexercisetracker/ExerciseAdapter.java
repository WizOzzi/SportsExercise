// ExerciseAdapter.java
package com.example.sportsexercisetracker;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ExerciseAdapter extends RecyclerView.Adapter<ExerciseAdapter.ExerciseViewHolder> {



        private List<Exercise> exercises;
        private OnItemClickListener listener;

        public interface OnItemClickListener {
            void onItemClick(Exercise exercise);
        }

        public ExerciseAdapter(List<Exercise> exercises, OnItemClickListener listener) {
            this.exercises = exercises;
            this.listener = listener;
        }

        @NonNull
        @Override
        public ExerciseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.exercise_item, parent, false);
            return new ExerciseViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ExerciseViewHolder holder, int position) {
            holder.bind(exercises.get(position));
        }

        @Override
        public int getItemCount() {
            return exercises.size();
        }

        class ExerciseViewHolder extends RecyclerView.ViewHolder {
            TextView nameTextView, repsTextView, weightTextView;

            ExerciseViewHolder(@NonNull View itemView) {
                super(itemView);
                nameTextView = itemView.findViewById(R.id.textViewExerciseName);
                repsTextView = itemView.findViewById(R.id.textViewReps);
                weightTextView = itemView.findViewById(R.id.textViewWeight);

                itemView.setOnClickListener(v -> {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION && listener != null) {
                        listener.onItemClick(exercises.get(position));
                    }
                });
            }

            void bind(Exercise exercise) {
                nameTextView.setText(exercise.getName());
                repsTextView.setText(exercise.getFormattedReps());
                weightTextView.setText(exercise.getFormattedWeight());


            }
        }



}