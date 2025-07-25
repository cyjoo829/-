package com.example.hometraing

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.app.AlertDialog
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import android.text.TextWatcher
import android.text.Editable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


//선택화면
class SelectExercise : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var exerciseAdapter: ExerciseAdapter

    private lateinit var selectedExercisesRecyclerView: RecyclerView // 선택된 운동 목록
    private lateinit var selectedExerciseAdapter: SelectedExerciseAdapter // 선택된 운동 어댑터

    private val allExercises = mutableListOf<Exercise>() // 모든 운동 데이터를 저장할 리스트
    private val selectedExercises = mutableListOf<Exercise>() //사용자 선택한 운동 저장 리스트

    //요일 UI요소
    private lateinit var cbMon: CheckBox
    private lateinit var cbTue: CheckBox
    private lateinit var cbWed: CheckBox
    private lateinit var cbThu: CheckBox
    private lateinit var cbFri: CheckBox
    private lateinit var cbSat: CheckBox
    private lateinit var cbSun: CheckBox
    private lateinit var tvSelectedDays: TextView

    private val selectedDays = mutableListOf<String>()

    // 검색 기능 관련
    private lateinit var searchEditText: EditText
    private var searchJob: Job? = null
    private var currentDisplayedCategory: String = "전체" // 초기 화면은 모든 운동을 보여주도록 "전체"로 설정


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_exercise)

        // UI 요소 초기화
        val btnUpperBody: Button = findViewById(R.id.btn_upper_body)
        val btnAbs: Button = findViewById(R.id.btn_abs)
        val btnLowerBody: Button = findViewById(R.id.btn_lower_body)
        val btnFullBody: Button = findViewById(R.id.btn_full_body)
        val btnAllExercises: Button = findViewById(R.id.btn_all_exercises) // **새로 추가된 "전체" 버튼 초기화**

        recyclerView = findViewById(R.id.recycler_view_exercises)
        searchEditText = findViewById(R.id.searchEditText)

        // 요일 체크박스 초기화
        cbMon = findViewById(R.id.cb_mon)
        cbTue = findViewById(R.id.cb_tue)
        cbWed = findViewById(R.id.cb_wed)
        cbThu = findViewById(R.id.cb_thu)
        cbFri = findViewById(R.id.cb_fri)
        cbSat = findViewById(R.id.cb_sat)
        cbSun = findViewById(R.id.cb_sun)
        tvSelectedDays = findViewById(R.id.tv_selected_days)

        // 요일 체크박스 리스너 설정
        setupDayCheckBoxListeners()

        // RecyclerView 설정
        recyclerView.layoutManager = LinearLayoutManager(this)

        //선택된 운동 목록 RecyclerView 초기화
        selectedExercisesRecyclerView = findViewById(R.id.recycler_view_selected_exercises)
        selectedExercisesRecyclerView.layoutManager = LinearLayoutManager(this)
        selectedExerciseAdapter = SelectedExerciseAdapter(selectedExercises) {
                exercise ->
            selectedExercises.remove(exercise)
            Toast.makeText(this, "${exercise.name}이(가) 선택 목록에서 삭제되었습니다.", Toast.LENGTH_SHORT).show()
            selectedExerciseAdapter.updateList()
        }
        selectedExercisesRecyclerView.adapter = selectedExerciseAdapter

        // 모든 운동 데이터 초기화
        initAllExercises()

        // 버튼 클릭 리스너 설정
        btnUpperBody.setOnClickListener {
            currentDisplayedCategory = "상체"
            displayExercises(currentDisplayedCategory)
            searchEditText.text.clear()
        }
        btnAbs.setOnClickListener {
            currentDisplayedCategory = "복근"
            displayExercises(currentDisplayedCategory)
            searchEditText.text.clear()
        }
        btnLowerBody.setOnClickListener {
            currentDisplayedCategory = "하체"
            displayExercises(currentDisplayedCategory)
            searchEditText.text.clear()
        }
        btnFullBody.setOnClickListener {
            currentDisplayedCategory = "전신" // "전신" 카테고리만 표시
            displayExercises(currentDisplayedCategory)
            searchEditText.text.clear()
        }
        btnAllExercises.setOnClickListener { // **새로 추가된 "전체" 버튼 리스너**
            currentDisplayedCategory = "전체" // "전체" 카테고리로 설정
            displayExercises(currentDisplayedCategory) // 모든 운동을 보여주도록 호출
            searchEditText.text.clear()
        }

        // 초기 화면에 모든 운동 표시
        displayExercises(currentDisplayedCategory)
        selectedExerciseAdapter.updateList()

        // 검색 기능 (TextWatcher)
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                searchJob?.cancel()
                searchJob = CoroutineScope(Dispatchers.Main).launch {
                    delay(300)
                    val query = s?.toString()?.lowercase()?.trim() ?: ""
                    filterExercises(query)
                }
            }
        })
    }

    // 요일 체크박스 리스너 설정 함수
    private fun setupDayCheckBoxListeners() {
        val dayCheckBoxes = listOf(cbMon, cbTue, cbWed, cbThu, cbFri, cbSat, cbSun)
        val dayNames = listOf("월", "화", "수", "목", "금", "토", "일")

        dayCheckBoxes.forEachIndexed { index, checkBox ->
            checkBox.setOnCheckedChangeListener { _, isChecked ->
                val day = dayNames[index]
                if (isChecked) {
                    if (!selectedDays.contains(day)) {
                        selectedDays.add(day)
                    }
                } else {
                    selectedDays.remove(day)
                }
                updateSelectedDaysText()
            }
        }
    }

    // 선택된 요일 텍스트를 업데이트하는 함수
    private fun updateSelectedDaysText() {
        if (selectedDays.isEmpty()) {
            tvSelectedDays.text = "선택된 요일: 없음"
        } else {
            val sortedDays = selectedDays.sortedWith(compareBy {
                when (it) {
                    "월" -> 0; "화" -> 1; "수" -> 2; "목" -> 3; "금" -> 4; "토" -> 5; "일" -> 6
                    else -> 7
                }
            })
            tvSelectedDays.text = "선택된 요일: ${sortedDays.joinToString(", ")}"
        }
    }


    // 모든 운동 데이터를 초기화하는 함수
    private fun initAllExercises() {
        allExercises.add(Exercise("푸쉬업 (Push-up)", "상체", "가슴, 어깨, 삼두근을 단련하는 대표적인 상체 운동입니다.", "30초", "약 5~7 kcal"))
        allExercises.add(Exercise("숄더 프레스 (덤벨/밴드)", "상체", "어깨 근육을 강화하는 운동입니다. 덤벨이나 밴드를 사용할 수 있습니다.", "60초", "약 7~10 kcal"))
        allExercises.add(Exercise("숄더 탭 (Shoulder taps)", "상체", "코어 안정성과 어깨 안정성을 동시에 기를 수 있는 운동입니다.", "30초", "약 6~8 kcal"))

        allExercises.add(Exercise("크런치 (Crunch)", "복근", "복직근을 단련하는 기본적인 복근 운동입니다.", "30초", "약 5~6 kcal"))
        allExercises.add(Exercise("플랭크 (Plank)", "복근", "코어 근육을 강화하는 등척성 운동입니다.", "60초", "약 7~9 kcal"))
        allExercises.add(Exercise("레그 레이즈 (Leg raise)", "복근", "하복부를 단련하는 데 효과적인 운동입니다.", "30초", "약 6~8 kcal"))

        allExercises.add(Exercise("스쿼트 (Squat)", "하체", "하체 전체와 코어 근육을 강화하는 기본적인 하체 운동입니다.", "30초", "약 6~8 kcal"))
        allExercises.add(Exercise("런지 (Lunge)", "하체", "허벅지와 엉덩이를 강화하는 운동입니다. 균형감각 향상에도 좋습니다.", "60초", "약 10~12 kcal"))
        allExercises.add(Exercise("힙 브릿지 (Glute bridge)", "하체", "둔근을 강화하는 데 효과적인 운동입니다. 허리 부담이 적습니다.", "30초", "약 6~8 kcal"))

        allExercises.add(Exercise("버피 (Burpee)", "전신", "유산소와 무산소 운동을 결합한 전신 고강도 운동입니다.", "30초", "약 10~12 kcal"))
        allExercises.add(Exercise("점핑잭 (Jumping jacks)", "전신", "유산소 운동으로 심박수를 높이고 전신을 활성화합니다.", "30초", "약 7~9 kcal"))
        allExercises.add(Exercise("마운틴 클라이머", "전신", "코어와 하체를 동시에 사용하는 유산소성 전신 운동입니다.", "60초", "약 12~15 kcal"))
    }

    // 선택된 카테고리 또는 모든 운동 목록을 RecyclerView에 표시하는 함수
    private fun displayExercises(category: String) {
        val exercisesToDisplay = if (category == "전체") { // "전체" 카테고리일 경우
            allExercises // 모든 운동을 보여줍니다.
        } else {
            allExercises.filter { it.category == category } // 해당 카테고리 운동만 보여줍니다.
        }

        exerciseAdapter = ExerciseAdapter(exercisesToDisplay) { exercise ->
            showExerciseDetailPopup(exercise)
        }
        recyclerView.adapter = exerciseAdapter
    }

    // 검색 쿼리에 따라 운동 목록을 필터링하는 함수
    private fun filterExercises(query: String) {
        val filteredList = if (query.isEmpty()) {
            // 쿼리가 비어있을 때: 현재 표시된 카테고리에 따라 필터링
            if (currentDisplayedCategory == "전체") {
                allExercises // "전체" 카테고리일 때 모든 운동 표시
            } else {
                allExercises.filter { it.category == currentDisplayedCategory } // 특정 카테고리 운동 표시
            }
        } else {
            // 쿼리가 있을 때: 현재 표시된 카테고리 내에서 검색
            if (currentDisplayedCategory == "전체") {
                allExercises.filter { it.name.lowercase().contains(query) } // 전체 운동에서 이름으로 검색
            } else {
                allExercises.filter {
                    it.category == currentDisplayedCategory && it.name.lowercase().contains(query)
                }
            }
        }

        if (!::exerciseAdapter.isInitialized) {
            exerciseAdapter = ExerciseAdapter(emptyList()) { exercise ->
                showExerciseDetailPopup(exercise)
            }
            recyclerView.adapter = exerciseAdapter
        }
        exerciseAdapter.updateList(filteredList)
    }


    private fun showExerciseDetailPopup(exercise: Exercise) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(exercise.name)

        val details = """
            ${exercise.description}
            
            ⏱️ **운동 시간:** ${exercise.duration}
            🔥 **예상 소모 칼로리:** ${exercise.caloriesBurned}
        """.trimIndent()

        builder.setMessage(details)

        builder.setPositiveButton("닫기") { dialog, _ ->
            dialog.dismiss()
        }

        builder.setNegativeButton("선택") { dialog, _ ->
            if (selectedExercises.contains(exercise)) {
                Toast.makeText(this, "${exercise.name}은(는) 이미 선택되었습니다.", Toast.LENGTH_SHORT).show()
            } else {
                selectedExercises.add(exercise)
                Toast.makeText(this, "${exercise.name}이(가) 선택 목록에 추가되었습니다.", Toast.LENGTH_SHORT).show()
                selectedExerciseAdapter.updateList()
            }
            dialog.dismiss()
        }

        val dialog = builder.create()
        dialog.show()
    }
}