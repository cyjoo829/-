package com.example.hometraing

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.hometraing.selected.Exercise

class StretchingActivity : AppCompatActivity() {

    private lateinit var tvStretchingIntro: TextView
    private lateinit var btnStartStretching: Button
    private lateinit var webViewStretching: WebView
    private lateinit var btnStartWorkout: Button

    private var selectedExercises: ArrayList<Exercise>? = null

    // 유튜브 영상 ID를 넣어주세요.
    // 실제 영상 ID는 'watch?v=' 뒤에 오는 문자열입니다. (예: https://www.youtube.com/watch?v=YOUR_VIDEO_ID_HERE)
    private val YOUTUBE_VIDEO_ID = "5_Qzs8_IG-M" // 여기에 실제 영상 ID를 넣어주세요!
    private val YOUTUBE_EMBED_URL = "https://www.youtube.com/embed/$YOUTUBE_VIDEO_ID?autoplay=1"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stretching)

        tvStretchingIntro = findViewById(R.id.tv_stretching_intro)
        btnStartStretching = findViewById(R.id.btn_start_stretching)
        webViewStretching = findViewById(R.id.web_view_stretching)
        btnStartWorkout = findViewById(R.id.btn_start_workout)

        // SelectExercise에서 전달받은 운동 목록
        selectedExercises = intent.getParcelableArrayListExtra("selectedExercises")

        // 초기 UI 상태 설정
        tvStretchingIntro.visibility = View.VISIBLE
        btnStartStretching.visibility = View.VISIBLE
        webViewStretching.visibility = View.GONE
        btnStartWorkout.visibility = View.GONE

        // 웹뷰 설정
        webViewStretching.settings.javaScriptEnabled = true // JavaScript 활성화 (유튜브 영상 재생에 필요)
        webViewStretching.settings.loadWithOverviewMode = true
        webViewStretching.settings.useWideViewPort = true
        webViewStretching.webChromeClient = WebChromeClient() // 전체 화면 재생 등을 위한 클라이언트 설정
        webViewStretching.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                // 페이지 로딩 완료 후 운동 시작 버튼 표시 (선택 사항: 영상 재생 완료를 감지하는 더 복잡한 로직이 필요할 수 있음)
                btnStartWorkout.visibility = View.VISIBLE
            }
        }


        // '스트레칭 시작하기' 버튼 클릭 리스너
        btnStartStretching.setOnClickListener {
            tvStretchingIntro.visibility = View.GONE
            btnStartStretching.visibility = View.GONE
            webViewStretching.visibility = View.VISIBLE

            // 유튜브 영상 로드
            webViewStretching.loadUrl(YOUTUBE_EMBED_URL)
        }

        // '운동 시작하기' 버튼 클릭 리스너
        btnStartWorkout.setOnClickListener {
            val intent = Intent(this, TrainingActivity::class.java).apply {
                putExtra("selectedExercises", selectedExercises)
            }
            startActivity(intent)
            finish() // 스트레칭 화면 종료
        }
    }

    override fun onBackPressed() {
        // 웹뷰에서 뒤로 가기 처리가 필요할 경우
        if (webViewStretching.canGoBack()) {
            webViewStretching.goBack()
        } else {
            super.onBackPressed()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        webViewStretching.destroy() // 액티비티 종료 시 웹뷰 리소스 해제
    }
}