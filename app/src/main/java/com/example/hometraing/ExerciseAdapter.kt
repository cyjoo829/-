package com.example.hometraing

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView


class ExerciseAdapter(
    private val exerciseList: List<Exercise>,
    private val onItemClick: (Exercise) -> Unit // 람다 함수를 통해 클릭 이벤트 전달
) : RecyclerView.Adapter<ExerciseAdapter.ExerciseViewHolder>() {

    class ExerciseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val exerciseName: TextView = itemView.findViewById(R.id.tv_exercise_name)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExerciseViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.activity_item_exercise, parent, false)
        return ExerciseViewHolder(view)
    }

    override fun onBindViewHolder(holder: ExerciseViewHolder, position: Int) {
        val currentExercise = exerciseList[position]
        holder.exerciseName.text = currentExercise.name

        holder.itemView.setOnClickListener {
            onItemClick(currentExercise) // 클릭 시 람다 함수 호출
        }
    }

    override fun getItemCount() = exerciseList.size
}