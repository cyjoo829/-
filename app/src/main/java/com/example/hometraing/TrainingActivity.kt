package com.example.hometraing

import android.os.Bundle
import android.os.CountDownTimer
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class TrainingActivity : AppCompatActivity() {

    private lateinit var tvExerciseName: TextView
    private lateinit var tvTimer: TextView
    private lateinit var tvCurrentExerciseCount: TextView
    private lateinit var btnStartPause: Button // 시작/일시정지 버튼
    private lateinit var btnNextExercise: Button
    private lateinit var btnPreviousExercise: Button

    private var selectedExercises: ArrayList<Exercise> = ArrayList()
    private var currentExerciseIndex: Int = 0
    private var countDownTimer: CountDownTimer? = null
    private var isResting: Boolean = false // 현재 쉬는 시간인지 여부
    private var isTimerRunning: Boolean = false // 타이머가 현재 작동 중인지 여부
    private var timeLeftInMillis: Long = 0 // 남은 시간 (일시정지 시 저장)
    private val REST_TIME_SECONDS = 20

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_training)

        tvExerciseName = findViewById(R.id.tv_training_exercise_name)
        tvTimer = findViewById(R.id.tv_training_timer)
        tvCurrentExerciseCount = findViewById(R.id.tv_current_exercise_count)
        btnStartPause = findViewById(R.id.btn_start_pause_training)
        btnNextExercise = findViewById(R.id.btn_next_exercise)
        btnPreviousExercise = findViewById(R.id.btn_previous_exercise)

        // **여기에서 selectedExercises를 Intent로부터 받아옵니다.**
        intent.getParcelableArrayListExtra<Exercise>("selectedExercises")?.let {
            selectedExercises.addAll(it)
        }

        if (selectedExercises.isEmpty()) {
            Toast.makeText(this, "선택된 운동이 없습니다.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // 초기 화면 설정: 첫 운동 정보 표시하고 타이머는 시작하지 않은 상태
        updateExerciseUI()
        btnStartPause.text = "시작" // 초기 버튼 텍스트는 "시작"

        // 시작/일시정지 버튼 리스너
        btnStartPause.setOnClickListener {
            if (isTimerRunning) {
                pauseTimer()
            } else {
                startTimer()
            }
        }

        // 다음 운동 버튼 리스너
        btnNextExercise.setOnClickListener {
            moveToNextExercise()
        }

        // 이전 운동 버튼 리스너
        btnPreviousExercise.setOnClickListener {
            moveToPreviousExercise()
        }
    }

    private fun updateExerciseUI() {
        if (currentExerciseIndex < selectedExercises.size) {
            val currentExercise = selectedExercises[currentExerciseIndex]
            tvExerciseName.text = currentExercise.name
            tvCurrentExerciseCount.text = "${currentExerciseIndex + 1} / ${selectedExercises.size}"

            // 타이머 시작 전 초기 시간 설정 (운동 시간 또는 쉬는 시간)
            if (!isResting) {
                val durationString = currentExercise.duration // 예: "30초", "60초"
                val durationInSeconds = durationString?.replace("초", "")?.trim()?.toIntOrNull() ?: 0
                timeLeftInMillis = (durationInSeconds * 1000).toLong()
            } else {
                timeLeftInMillis = (REST_TIME_SECONDS * 1000).toLong()
            }
            updateCountDownText() // 텍스트만 업데이트, 타이머 시작은 안 함
        } else {
            // 모든 운동 완료
            tvExerciseName.text = "운동 완료!"
            tvTimer.text = "00:00"
            Toast.makeText(this, "모든 운동을 완료했습니다!", Toast.LENGTH_LONG).show()
            btnStartPause.isEnabled = false
            btnNextExercise.isEnabled = false
            btnPreviousExercise.isEnabled = false
            countDownTimer?.cancel()
            isTimerRunning = false
            btnStartPause.text = "완료"
        }
    }

    private fun startTimer() {
        // 운동 완료 상태라면 시작하지 않음
        if (currentExerciseIndex >= selectedExercises.size) return

        countDownTimer = object : CountDownTimer(timeLeftInMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timeLeftInMillis = millisUntilFinished
                updateCountDownText()
            }

            override fun onFinish() {
                isTimerRunning = false
                countDownTimer?.cancel()
                btnStartPause.text = "시작" // 타이머 종료 시 버튼 텍스트 초기화

                if (!isResting) {
                    // 운동 타이머 종료 후 쉬는 시간 시작
                    startRestingPhase()
                } else {
                    // 쉬는 시간 타이머 종료 후 다음 운동으로 이동
                    moveToNextExerciseAuto()
                }
            }
        }.start()

        isTimerRunning = true
        btnStartPause.text = "일시정지"
    }

    private fun pauseTimer() {
        countDownTimer?.cancel()
        isTimerRunning = false
        btnStartPause.text = "시작"
    }

    private fun startRestingPhase() {
        isResting = true
        tvExerciseName.text = "쉬는 시간"
        timeLeftInMillis = (REST_TIME_SECONDS * 1000).toLong() // 쉬는 시간으로 설정
        updateCountDownText() // 쉬는 시간으로 UI 업데이트

        Toast.makeText(this, "20초 쉬는 시간이 시작됩니다.", Toast.LENGTH_SHORT).show()
        startTimer() // 쉬는 시간 타이머 시작
    }


    private fun moveToNextExerciseAuto() {
        isResting = false // 쉬는 시간 종료
        currentExerciseIndex++
        updateExerciseUI() // 다음 운동으로 UI 업데이트 (타이머는 시작 안 함)
        // 만약 다음 운동이 시작되자마자 자동 시작을 원하면 여기서 startTimer() 호출
        // 현재는 "시작" 버튼을 다시 눌러야 시작하도록 변경했으므로 호출하지 않음
    }

    // "다음 운동" 버튼을 눌렀을 때 (수동 전환)
    private fun moveToNextExercise() {
        countDownTimer?.cancel()
        isTimerRunning = false
        isResting = false // 쉬는 시간 상태 초기화
        btnStartPause.text = "시작" // 버튼 텍스트 초기화

        if (currentExerciseIndex < selectedExercises.size - 1) {
            currentExerciseIndex++
            updateExerciseUI()
            Toast.makeText(this, "다음 운동으로 넘어갑니다.", Toast.LENGTH_SHORT).show()
        } else if (currentExerciseIndex == selectedExercises.size - 1) {
            // 마지막 운동에서 다음 버튼을 누르면 운동 완료 상태로 전환
            currentExerciseIndex++ // 인덱스를 증가시켜 운동 완료 상태로 진입
            updateExerciseUI() // "운동 완료!" 표시
        } else {
            Toast.makeText(this, "모든 운동을 완료했습니다!", Toast.LENGTH_SHORT).show()
        }
    }

    // "이전 운동" 버튼을 눌렀을 때 (수동 전환)
    private fun moveToPreviousExercise() {
        countDownTimer?.cancel()
        isTimerRunning = false
        isResting = false // 쉬는 시간 상태 초기화
        btnStartPause.text = "시작" // 버튼 텍스트 초기화

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

    override fun onDestroy() {
        super.onDestroy()
        countDownTimer?.cancel() // 액티비티 종료 시 타이머를 반드시 중지
    }
}