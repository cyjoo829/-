package com.example.hometraing

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class SelectedExerciseAdapter(
    private val selectedExerciseList: MutableList<Exercise>,
    private val onItemClick: (Exercise) -> Unit
) : RecyclerView.Adapter<SelectedExerciseAdapter.SelectedExerciseViewHolder>() {

    // 뷰 홀더 클래스
    class SelectedExerciseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val exerciseName: TextView = itemView.findViewById(R.id.tv_selected_exercise_name)
    }

    // 뷰 홀더 생성
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SelectedExerciseViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.activity_item_select_exercise, parent, false)
        return SelectedExerciseViewHolder(view)
    }

    // 뷰 홀더에 데이터 바인딩
    override fun onBindViewHolder(holder: SelectedExerciseViewHolder, position: Int) {
        val currentExercise = selectedExerciseList[position]
        holder.exerciseName.text = currentExercise.name

        holder.itemView.setOnClickListener {
            onItemClick(currentExercise)
        }
    }

    // 목록의 아이템 수 반환
    override fun getItemCount() = selectedExerciseList.size

    // 데이터 변경 시 RecyclerView를 갱신하는 메서드 (추가)
    fun updateList() {
        notifyDataSetChanged()
    }
}