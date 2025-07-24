package com.example.hometraing // 여러분의 실제 패키지명

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import android.util.Log // Logcat 출력을 위해 추가

// 메인화면
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_main) // 메인 화면 레이아웃

            val btnGoSelectExercise: Button = findViewById(R.id.btn_go_select_exercise)

            btnGoSelectExercise.setOnClickListener {
                val intent = Intent(this, SelectExercise::class.java) // SelectExercise 클래스 참조
                startActivity(intent)
            }
    }
}