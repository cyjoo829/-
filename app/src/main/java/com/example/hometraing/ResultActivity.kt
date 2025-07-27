package com.example.hometraing

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlin.random.Random

class ResultActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result)

        val tvEncouragement: TextView = findViewById(R.id.tv_encouragement_message)
        val tvTotalCalories: TextView = findViewById(R.id.tv_total_calories)
        val tvTotalTime: TextView = findViewById(R.id.tv_total_time)

        // Intent로부터 데이터 받기
        var totalCalories = intent.getIntExtra("totalCalories", 0)
        val totalWorkoutTimeSeconds = intent.getIntExtra("totalWorkoutTimeSeconds", 0)

        val btn_back_to_main: Button = findViewById(R.id.btn_back_to_main)

        if (totalWorkoutTimeSeconds == 0) {
            totalCalories = 0
        }

        // 격려의 말 랜덤 선택
        val encouragementMessages = listOf(
            "오늘도 정말 수고하셨습니다! 당신의 노력이 빛을 발할 거예요!",
            "대단해요! 꾸준함이 당신을 변화시킬 겁니다!",
            "포기하지 않는 당신의 모습이 아름답습니다. 계속 나아가세요!",
            "오늘도 목표 달성! 당신은 해낼 수 있습니다!",
            "운동 완료! 건강한 습관을 만들어가는 당신을 응원합니다!"
        )
        tvEncouragement.text = encouragementMessages[Random.nextInt(encouragementMessages.size)]

        // 총 소모 칼로리 표시
        tvTotalCalories.text = "총 소모 칼로리: ${totalCalories} kcal"

        // 총 운동 시간 표시 (분:초 형식)
        val minutes = totalWorkoutTimeSeconds / 60
        val seconds = totalWorkoutTimeSeconds % 60
        tvTotalTime.text = "총 운동 시간: ${String.format("%02d:%02d", minutes, seconds)}"

        btn_back_to_main.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
        }

    }
}