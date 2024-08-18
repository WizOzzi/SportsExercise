package com.example.sportsexercisetracker;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class ExerciseGroupAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static final int TYPE_HEADER = 0;
    public static final int TYPE_ITEM = 1;

    private List<Object> items; // Can be String (for date headers) or Exercise
    private OnDeleteClickListener onDeleteClickListener;
    private OnItemClickListener mItemClickListener;

    public interface OnDeleteClickListener {
        void onDeleteClick(Exercise exercise);
    }

    public interface OnItemClickListener {
        void onItemClick(Exercise exercise);
    }

   /* public ExerciseGroupAdapter(Map<String, Map<String, List<Exercise>>> groupedExercises) {
        items = new ArrayList<>();
        for (Map.Entry<String, Map<String, List<Exercise>>> dateEntry : groupedExercises.entrySet()) {
            items.add(dateEntry.getKey()); // Add date as header
            for (List<Exercise> exercises : dateEntry.getValue().values()) {
                items.addAll(exercises); // Add all exercises for this date
            }
        }
    }*/
   public ExerciseGroupAdapter(Map<String, Map<String, List<Exercise>>> groupedExercises) {
       items = new ArrayList<>();
       List<String> sortedDates = new ArrayList<>(groupedExercises.keySet());
       Collections.sort(sortedDates, Collections.reverseOrder());

       for (String date : sortedDates) {
           items.add(date); // Add date as header
           Map<String, List<Exercise>> exercisesByType = groupedExercises.get(date);
           for (List<Exercise> exercises : exercisesByType.values()) {
               items.addAll(exercises); // Add all exercises for this date
           }
       }
   }



    public void setOnDeleteClickListener(OnDeleteClickListener listener) {
        this.onDeleteClickListener = listener;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.mItemClickListener = listener;
    }

    @Override
    public int getItemViewType(int position) {
        return items.get(position) instanceof String ? TYPE_HEADER : TYPE_ITEM;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_HEADER) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_date_header, parent, false);
            return new HeaderViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.exercise_item, parent, false);
            return new ExerciseViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof HeaderViewHolder) {
            ((HeaderViewHolder) holder).bind((String) items.get(position));
        } else if (holder instanceof ExerciseViewHolder) {
            ((ExerciseViewHolder) holder).bind((Exercise) items.get(position));
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void deleteItem(int position) {
        if (position >= 0 && position < items.size() && items.get(position) instanceof Exercise) {
            Exercise exercise = (Exercise) items.get(position);
            if (onDeleteClickListener != null) {
                onDeleteClickListener.onDeleteClick(exercise);
            }
            items.remove(position);
            notifyItemRemoved(position);

            // Check if we need to remove the header
            if (position > 0 && items.get(position - 1) instanceof String &&
                    (position == items.size() || items.get(position) instanceof String)) {
                items.remove(position - 1);
                notifyItemRemoved(position - 1);
            }
        }
    }

    public void removeItem(Exercise exerciseToRemove) {
        int position = -1;
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i) instanceof Exercise) {
                Exercise exercise = (Exercise) items.get(i);
                if (exercise.getId() == exerciseToRemove.getId()) {
                    position = i;
                    break;
                }
            }
        }

        if (position != -1) {
            items.remove(position);
            notifyItemRemoved(position);

            // Check if we need to remove the header
            if (position > 0 && items.get(position - 1) instanceof String &&
                    (position == items.size() || items.get(position) instanceof String)) {
                items.remove(position - 1);
                notifyItemRemoved(position - 1);
            }
        }
    }

    public void updateData(Map<String, Map<String, List<Exercise>>> newGroupedExercises) {
        items.clear();
        List<String> sortedDates = new ArrayList<>(newGroupedExercises.keySet());
        Collections.sort(sortedDates, Collections.reverseOrder());

        for (String date : sortedDates) {
            items.add(date); // Add date as header
            Map<String, List<Exercise>> exercisesByType = newGroupedExercises.get(date);
            for (List<Exercise> exercises : exercisesByType.values()) {
                items.addAll(exercises); // Add all exercises for this date
            }
        }
        notifyDataSetChanged();
    }

    public Object getItem(int position) {
        return items.get(position);
    }

    /*
    public void updateData(Map<String, Map<String, List<Exercise>>> newGroupedExercises) {
        items.clear();
        for (Map.Entry<String, Map<String, List<Exercise>>> dateEntry : newGroupedExercises.entrySet()) {
            items.add(dateEntry.getKey()); // Add date as header
            for (List<Exercise> exercises : dateEntry.getValue().values()) {
                items.addAll(exercises); // Add all exercises for this date
            }
        }
        notifyDataSetChanged();
    }
*/
    class HeaderViewHolder extends RecyclerView.ViewHolder {
        TextView dateTextView;

        HeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            dateTextView = itemView.findViewById(R.id.textViewDate);
        }

        void bind(String date) {
            dateTextView.setText(date);
        }
    }

    class ExerciseViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView;
        TextView repsTextView;
        TextView weightTextView;

        ExerciseViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.textViewExerciseName);
            repsTextView = itemView.findViewById(R.id.textViewReps);
            weightTextView = itemView.findViewById(R.id.textViewWeight);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && mItemClickListener != null) {
                    Object item = items.get(position);
                    if (item instanceof Exercise) {
                        mItemClickListener.onItemClick((Exercise) item);
                    }
                }
            });
        }

        void bind(Exercise exercise) {
            nameTextView.setText(exercise.getName());
            repsTextView.setText(exercise.getRepsWithSuffix());
            weightTextView.setText(exercise.getWeightWithSuffix());
        }
    }
}