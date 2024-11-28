package com.example.realmusicplayer

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.RenderEffect
import android.graphics.Shader
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.LayerDrawable
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.util.concurrent.TimeUnit

class MusicPlayer : AppCompatActivity() {

    private var mediaPlayer: MediaPlayer? = null
    private val handler = Handler(Looper.getMainLooper())
    private var isPlaying = true
    private var currentIndex = 0 // 현재 재생 중인 노래의 인덱스
    private lateinit var songList: List<Song> // 노래 리스트

    var isShuffleEnabled = false
    var isRepeatEnabled = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.music_player)

        val selectedSong = intent.getSerializableExtra("SELECTED_SONG") as? Song
        songList = DataProvider.getSongs() // 전체 노래 리스트

        val bgImg = findViewById<ImageView>(R.id.rectangleView)
        val musicTitle = findViewById<TextView>(R.id.musicTitle)
        val artistName = findViewById<TextView>(R.id.artistName)
        val imageView = findViewById<ImageView>(R.id.imageView)
        val songDuration = findViewById<TextView>(R.id.songDuration)
        val currentTime = findViewById<TextView>(R.id.currentTime)
        val seekBar = findViewById<SeekBar>(R.id.seekBar)
        val backButton = findViewById<Button>(R.id.addButton)
        val playButton = findViewById<ImageButton>(R.id.playButton)
        val nextButton = findViewById<ImageButton>(R.id.nextButton)
        val previousButton = findViewById<ImageButton>(R.id.previousButton)
        val shuffleButton = findViewById<ImageButton>(R.id.shuffleButton)
        val repeatButton = findViewById<ImageButton>(R.id.repeatButton)

        val defaultColor = Color.parseColor("#B0B0B0") // 연한 색상 (비활성화)
        val activeColor = Color.parseColor("#000000")

        shuffleButton.setColorFilter(defaultColor, PorterDuff.Mode.SRC_IN)
        repeatButton.setColorFilter(defaultColor, PorterDuff.Mode.SRC_IN)

        // 셔플 버튼 클릭 리스너
        shuffleButton.setOnClickListener {
            isShuffleEnabled = !isShuffleEnabled
            if (isShuffleEnabled) {
                shuffleButton.setColorFilter(activeColor, PorterDuff.Mode.SRC_IN)
            } else {
                shuffleButton.setColorFilter(defaultColor, PorterDuff.Mode.SRC_IN)
            }
        }

        // 반복 버튼 클릭 리스너
        repeatButton.setOnClickListener {
            isRepeatEnabled = !isRepeatEnabled
            if (isRepeatEnabled) {
                repeatButton.setColorFilter(activeColor, PorterDuff.Mode.SRC_IN)
            } else {
                repeatButton.setColorFilter(defaultColor, PorterDuff.Mode.SRC_IN)
            }
        }

        // 현재 재생 중인 노래의 인덱스 설정
        currentIndex = songList.indexOf(selectedSong)

        // 초기 로드
        selectedSong?.let {
            loadSong(bgImg,currentIndex, musicTitle, artistName, imageView, songDuration, currentTime, seekBar, playButton)
        }

        // Play/Pause 버튼 클릭 리스너
        playButton.setOnClickListener {
            mediaPlayer?.let {
                if (isPlaying) {
                    it.pause()
                    playButton.setImageResource(R.drawable.play)
                } else {
                    it.start()
                    playButton.setImageResource(R.drawable.pause)
                }
                isPlaying = !isPlaying
            }
        }

        // Next 버튼 클릭 리스너
        nextButton.setOnClickListener {
            playNextSong(bgImg,musicTitle, artistName, imageView, songDuration, currentTime, seekBar, playButton)
        }

        // Previous 버튼 클릭 리스너
        previousButton.setOnClickListener {
            playPreviousSong(bgImg, musicTitle, artistName, imageView, songDuration, currentTime, seekBar, playButton)
        }

        // SeekBar 변경 리스너
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    mediaPlayer?.seekTo(progress)
                    currentTime.text = formatTime(progress)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                if (isPlaying) {
                    mediaPlayer?.pause()
                }
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                if (isPlaying) {
                    mediaPlayer?.start()
                }
            }
        })

        // SeekBar 및 currentTime 업데이트
        updateSeekBar(seekBar, currentTime)

        // 뒤로가기 버튼 리스너
        backButton.setOnClickListener {
            mediaPlayer?.release()
            mediaPlayer = null
            finish()
        }
    }

    private fun loadSong(
        bgImg: ImageView,
        index: Int,
        musicTitle: TextView,
        artistName: TextView,
        imageView: ImageView,
        songDuration: TextView,
        currentTime: TextView,
        seekBar: SeekBar,
        playButton: ImageButton
    ) {
        val song = songList[index]
        mediaPlayer?.release() // 기존 MediaPlayer 해제
        mediaPlayer = MediaPlayer.create(this, song.rawResId).apply {
            setOnPreparedListener {
                start()
                seekBar.max = duration
                songDuration.text = formatTime(duration)
            }

            setOnCompletionListener {
                // 노래가 끝난 후 상태에 따라 다음 곡 재생
                handler.postDelayed({
                    playNextSong(bgImg, musicTitle, artistName, imageView, songDuration, currentTime, seekBar, playButton)
                }, 2000)
            }
        }

        val bitmap = BitmapFactory.decodeResource(resources, song.imageResId)
        applyBlurEffect(bgImg, bitmap)

        musicTitle.text = song.title
        artistName.text = song.artist
        imageView.setImageResource(song.imageResId)
        bgImg.setImageResource(song.imageResId)
        currentTime.text = "00:00"
        seekBar.progress = 0
        isPlaying = true
        playButton.setImageResource(R.drawable.pause)
    }

    private fun playNextSong(
        bgImg: ImageView,
        musicTitle: TextView,
        artistName: TextView,
        imageView: ImageView,
        songDuration: TextView,
        currentTime: TextView,
        seekBar: SeekBar,
        playButton: ImageButton
    ) {
        if (isRepeatEnabled) {
            // 반복 재생인 경우 현재 노래를 다시 재생
            loadSong(bgImg, currentIndex, musicTitle, artistName, imageView, songDuration, currentTime, seekBar, playButton)
        } else if (isShuffleEnabled) {
            // 셔플 재생인 경우 랜덤으로 다음 노래 선택
            val randomIndex = (songList.indices - currentIndex).random() // 현재 인덱스 제외한 랜덤
            currentIndex = randomIndex
            loadSong(bgImg, currentIndex, musicTitle, artistName, imageView, songDuration, currentTime, seekBar, playButton)
        } else {
            // 기본 재생 순서
            currentIndex = (currentIndex + 1) % songList.size
            loadSong(bgImg, currentIndex, musicTitle, artistName, imageView, songDuration, currentTime, seekBar, playButton)
        }
    }


    private fun playPreviousSong(
        bgImg: ImageView,
        musicTitle: TextView,
        artistName: TextView,
        imageView: ImageView,
        songDuration: TextView,
        currentTime: TextView,
        seekBar: SeekBar,
        playButton: ImageButton
    ) {
        currentIndex = if (currentIndex - 1 < 0) songList.size - 1 else currentIndex - 1
        loadSong(bgImg, currentIndex, musicTitle, artistName, imageView, songDuration, currentTime, seekBar, playButton)
    }

    private fun updateSeekBar(seekBar: SeekBar, currentTime: TextView) {
        handler.postDelayed(object : Runnable {
            override fun run() {
                mediaPlayer?.let {
                    seekBar.progress = it.currentPosition
                    currentTime.text = formatTime(it.currentPosition)
                }
                handler.postDelayed(this, 1000)
            }
        }, 1000)
    }

    private fun formatTime(ms: Int): String {
        val minutes = TimeUnit.MILLISECONDS.toMinutes(ms.toLong())
        val seconds = TimeUnit.MILLISECONDS.toSeconds(ms.toLong()) % 60
        return String.format("%02d:%02d", minutes, seconds)
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
        mediaPlayer?.release()
        mediaPlayer = null
    }

    private fun applyBlurEffect(imageView: ImageView, bitmap: Bitmap) {
        // 스케일링을 통해 Blur 효과를 강화
        val scaledBitmap = Bitmap.createScaledBitmap(bitmap, bitmap.width / 4, bitmap.height / 4, false)
        val blurredDrawable = BitmapDrawable(imageView.resources, scaledBitmap)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // RenderEffect를 사용하여 Blur 효과 적용
            imageView.setRenderEffect(
                RenderEffect.createBlurEffect(40f, 40f, Shader.TileMode.CLAMP)
            )
        }

        // 반투명 검정색 오버레이 생성
        val darkOverlay = BitmapDrawable(imageView.resources)
        darkOverlay.setColorFilter(Color.parseColor("#80000000"), PorterDuff.Mode.SRC_OVER) // 50% 투명도

        // Blur 이미지와 오버레이 결합
        val layers = arrayOf(blurredDrawable, darkOverlay)
        val layerDrawable = LayerDrawable(layers)

        // ImageView에 결합된 Drawable 설정
        imageView.setImageDrawable(layerDrawable)
    }

    private fun createScaledBitmap(bitmap: Bitmap): Bitmap {
        // 단순히 크기를 줄여 Blur 효과를 흉내내는 방법
        return Bitmap.createScaledBitmap(bitmap, bitmap.width / 4, bitmap.height / 4, false)
    }
}
