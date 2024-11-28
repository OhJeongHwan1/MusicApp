package com.example.realmusicplayer

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // RecyclerView 초기화
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this) // 수직 리스트

        // 데이터 설정 및 어댑터 연결
        val songs = DataProvider.getSongs()
        val adapter = SongAdapter(songs){ selectedSong ->
            // Song 객체를 Intent로 전달
            val intent = Intent(this, MusicPlayer::class.java).apply {
                putExtra("SELECTED_SONG", selectedSong)
            }
            startActivity(intent)

        }
        recyclerView.adapter = adapter
    }
}
