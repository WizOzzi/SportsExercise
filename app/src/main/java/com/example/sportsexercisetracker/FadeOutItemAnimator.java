package com.example.sportsexercisetracker;


import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.view.animation.AccelerateInterpolator;

import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.RecyclerView;

public class FadeOutItemAnimator extends DefaultItemAnimator {
    @Override
    public boolean animateRemove(RecyclerView.ViewHolder holder) {
        holder.itemView.animate().alpha(0).scaleX(0).scaleY(0).setDuration(300)
                .setInterpolator(new AccelerateInterpolator())
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        dispatchRemoveFinished(holder);
                        holder.itemView.setAlpha(1);
                        holder.itemView.setScaleX(1);
                        holder.itemView.setScaleY(1);
                    }
                }).start();
        return false;
    }
}
