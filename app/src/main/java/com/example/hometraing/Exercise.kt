package com.example.hometraing

import kotlin.time.Duration

//운동 요소
data class Exercise(
    val name: String,
    val category: String, // "상체", "복근", "하체", "전신"
    val description: String, // 운동 상세 설명
    val duration: String, //운동 시간
    val caloriesBurned : String //예상 칼로리 소모량
)