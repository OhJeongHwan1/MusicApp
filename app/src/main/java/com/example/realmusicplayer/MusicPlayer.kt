package com.example.realmusicplayer

import android.os.Bundle
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity


class MusicPlayer: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.music_player)

        val selectedSong = intent.getSerializableExtra("SELECTED_SONG") as? Song


        val musicTitle = findViewById<TextView>(R.id.musicTitle)
        val artistName = findViewById<TextView>(R.id.artistName)
        val imageView = findViewById<ImageView>(R.id.imageView)
        val songDuration = findViewById<TextView>(R.id.songDuration)
        val currentTime = findViewById<TextView>(R.id.currentTime)
        val seekBar = findViewById<SeekBar>(R.id.seekBar)

        selectedSong?.let { song ->
            musicTitle.text = song.title
            artistName.text = song.artist
            imageView.setImageResource(song.imageResId)
            songDuration.text = song.duration
            currentTime.text = "0:00" // 초기 시간 설정

            // SeekBar 초기값 설정 (예: 최대값 설정)
            val durationParts = song.duration.split(":")
            val durationInSeconds = durationParts[0].toInt() * 60 + durationParts[1].toInt()
            seekBar.max = durationInSeconds
        }
    }
}
