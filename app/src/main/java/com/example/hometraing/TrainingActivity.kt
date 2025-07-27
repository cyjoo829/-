package com.example.hometraing

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.hometraing.selected.Exercise

class TrainingActivity : AppCompatActivity() {

    private lateinit var tvExerciseName: TextView
    private lateinit var tvTimer: TextView
    private lateinit var tvCurrentExerciseCount: TextView
    private lateinit var btnStartPause: Button
    private lateinit var btnNextExercise: Button
    private lateinit var btnPreviousExercise: Button

    private var selectedExercises: ArrayList<Exercise> = ArrayList()
    private var currentExerciseIndex: Int = 0
    private var countDownTimer: CountDownTimer? = null
    private var isResting: Boolean = false // 현재 쉬는 시간인지 여부
    private var isTimerRunning: Boolean = false // 타이머가 현재 작동 중인지 여부
    private var timeLeftInMillis: Long = 0 // 남은 시간 (일시정지 시 저장)
    private val REST_TIME_SECONDS = 20

    private var totalCaloriesBurned: Int = 0
    private var totalWorkoutTimeSeconds: Int = 0
    private var lastTimerStartTime: Long = 0 // 타이머가 마지막으로 시작된 시각


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_training)

        tvExerciseName = findViewById(R.id.tv_training_exercise_name)
        tvTimer = findViewById(R.id.tv_training_timer)
        tvCurrentExerciseCount = findViewById(R.id.tv_current_exercise_count)
        btnStartPause = findViewById(R.id.btn_start_pause_training)
        btnNextExercise = findViewById(R.id.btn_next_exercise)
        btnPreviousExercise = findViewById(R.id.btn_previous_exercise)

        intent.getParcelableArrayListExtra<Exercise>("selectedExercises")?.let {
            selectedExercises.addAll(it)
        }

        if (selectedExercises.isEmpty()) {
            Toast.makeText(this, "선택된 운동이 없습니다.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        updateExerciseUI()
        btnStartPause.text = "시작"
        updateNextButtonText() // 버튼 텍스트 초기화

        btnStartPause.setOnClickListener {
            if (isTimerRunning) {
                pauseTimer()
            } else {
                startTimer()
            }
        }

        btnNextExercise.setOnClickListener {
            // 현재 타이머가 돌아가고 있다면 경과 시간 누적
            if (isTimerRunning) {
                val elapsedTime = (System.currentTimeMillis() - lastTimerStartTime) / 1000
                totalWorkoutTimeSeconds += elapsedTime.toInt()
                countDownTimer?.cancel() // 타이머 중지
                isTimerRunning = false
            }

            // 마지막 운동이거나 모든 운동이 완료된 상태에서 '운동 종료' 버튼을 누른 경우
            if (currentExerciseIndex >= selectedExercises.size -1 || currentExerciseIndex >= selectedExercises.size) {
                navigateToResultActivity()
            } else {
                moveToNextExerciseManually()
            }
        }

        btnPreviousExercise.setOnClickListener {
            moveToPreviousExercise()
        }
    }

    private fun updateExerciseUI() {
        if (currentExerciseIndex < selectedExercises.size) {
            val currentExercise = selectedExercises[currentExerciseIndex]
            tvExerciseName.text = currentExercise.name
            tvCurrentExerciseCount.text = "${currentExerciseIndex + 1} / ${selectedExercises.size}"

            if (!isResting) {
                val durationString = currentExercise.duration
                val durationInSeconds = durationString?.replace("초", "")?.trim()?.toIntOrNull() ?: 0
                timeLeftInMillis = (durationInSeconds * 1000).toLong()
            } else {
                timeLeftInMillis = (REST_TIME_SECONDS * 1000).toLong()
            }
            updateCountDownText()
            updateNextButtonText() // 버튼 텍스트 업데이트
        } else {
            // 이 블록은 모든 운동 완료 후 UI를 최종적으로 정리하는 역할만 합니다.
            // ResultActivity로의 전환은 startTimer().onFinish() 또는 수동 전환에서 이루어집니다.
            tvExerciseName.text = "운동 완료!"
            tvTimer.text = "00:00"
            Toast.makeText(this, "모든 운동을 완료했습니다!", Toast.LENGTH_LONG).show()
            btnStartPause.isEnabled = false
            btnPreviousExercise.isEnabled = false
            btnNextExercise.isEnabled = false
            countDownTimer?.cancel()
            isTimerRunning = false
            btnStartPause.text = "완료"
        }
    }

    private fun startTimer() {
        if (currentExerciseIndex >= selectedExercises.size) return

        lastTimerStartTime = System.currentTimeMillis() // 타이머 시작 시각 기록

        countDownTimer = object : CountDownTimer(timeLeftInMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timeLeftInMillis = millisUntilFinished
                updateCountDownText()
            }

            override fun onFinish() {
                val elapsedDuration = (System.currentTimeMillis() - lastTimerStartTime) / 1000
                totalWorkoutTimeSeconds += elapsedDuration.toInt() // 타이머가 돌아간 시간 누적

                isTimerRunning = false
                countDownTimer?.cancel()
                btnStartPause.text = "시작"

                if (!isResting) {
                    val currentExercise = selectedExercises[currentExerciseIndex]
                    val caloriesString = currentExercise.caloriesBurned
                    val regex = Regex("\\d+")
                    val matchResult = regex.find(caloriesString ?: "")
                    val caloriesPerExercise = matchResult?.value?.toIntOrNull() ?: 0
                    totalCaloriesBurned += caloriesPerExercise

                    // 마지막 운동이 아니면 쉬는 시간 시작
                    if (currentExerciseIndex < selectedExercises.size - 1) {
                        startRestingPhase()
                    } else {
                        // 마지막 운동 완료 후 바로 결과 화면으로 이동
                        navigateToResultActivity()
                    }
                } else {
                    // 쉬는 시간 타이머 종료 후 다음 운동으로 이동
                    currentExerciseIndex++
                    updateExerciseUI()
                    // 쉬는 시간 후 다음 운동 자동 시작을 원하면 여기에 startTimer() 호출
                }
            }
        }.start()

        isTimerRunning = true
        btnStartPause.text = "일시정지"
    }

    private fun pauseTimer() {
        if (isTimerRunning) { // 타이머가 돌아가는 중에만 누적
            val elapsedTime = (System.currentTimeMillis() - lastTimerStartTime) / 1000
            totalWorkoutTimeSeconds += elapsedTime.toInt()
        }
        countDownTimer?.cancel()
        isTimerRunning = false
        btnStartPause.text = "시작"
    }

    private fun startRestingPhase() {
        isResting = true
        tvExerciseName.text = "쉬는 시간"
        timeLeftInMillis = (REST_TIME_SECONDS * 1000).toLong()
        updateCountDownText()

        Toast.makeText(this, "20초 쉬는 시간이 시작됩니다.", Toast.LENGTH_SHORT).show()
        startTimer()
    }

    private fun moveToNextExerciseManually() {
        // 현재 타이머가 돌아가고 있다면 경과 시간 누적
        if (isTimerRunning) {
            val elapsedTime = (System.currentTimeMillis() - lastTimerStartTime) / 1000
            totalWorkoutTimeSeconds += elapsedTime.toInt()
        }
        countDownTimer?.cancel()
        isTimerRunning = false
        isResting = false
        btnStartPause.text = "시작"

        // 현재 운동의 칼로리를 누적 (수동 전환 시)
        if (currentExerciseIndex < selectedExercises.size) {
            val currentExercise = selectedExercises[currentExerciseIndex]
            val caloriesString = currentExercise.caloriesBurned
            val regex = Regex("\\d+")
            val matchResult = regex.find(caloriesString ?: "")
            val caloriesPerExercise = matchResult?.value?.toIntOrNull() ?: 0
            totalCaloriesBurned += caloriesPerExercise
        }

        if (currentExerciseIndex < selectedExercises.size - 1) {
            currentExerciseIndex++
            updateExerciseUI()
            Toast.makeText(this, "다음 운동으로 넘어갑니다.", Toast.LENGTH_SHORT).show()
        } else {
            // 마지막 운동에서 '다음 운동' 버튼을 수동으로 누르면 바로 결과 화면으로 이동
            navigateToResultActivity()
        }
    }

    private fun moveToPreviousExercise() {
        // 현재 타이머가 돌아가고 있다면 경과 시간 누적
        if (isTimerRunning) {
            val elapsedTime = (System.currentTimeMillis() - lastTimerStartTime) / 1000
            totalWorkoutTimeSeconds += elapsedTime.toInt()
        }
        countDownTimer?.cancel()
        isTimerRunning = false
        isResting = false
        btnStartPause.text = "시작"

        if (currentExerciseIndex > 0) {
            currentExerciseIndex--
            updateExerciseUI()
            Toast.makeText(this, "이전 운동으로 돌아갑니다.", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "첫 번째 운동입니다.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateCountDownText() {
        val minutes = (timeLeftInMillis / 1000) / 60
        val seconds = (timeLeftInMillis / 1000) % 60
        tvTimer.text = String.format("%02d:%02d", minutes, seconds)
    }

    private fun updateNextButtonText() {
        if (currentExerciseIndex == selectedExercises.size - 1) {
            btnNextExercise.text = "운동 종료"
        } else {
            btnNextExercise.text = "다음 운동"
        }
    }

    private fun navigateToResultActivity() {
        val intent = Intent(this, ResultActivity::class.java).apply {
            putExtra("totalCalories", totalCaloriesBurned)
            putExtra("totalWorkoutTimeSeconds", totalWorkoutTimeSeconds)
        }
        startActivity(intent)
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        countDownTimer?.cancel()
    }
}