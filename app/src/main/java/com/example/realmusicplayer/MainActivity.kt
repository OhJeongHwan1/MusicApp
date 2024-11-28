package com.example.realmusicplayer

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity() {

    private lateinit var adapter: SongAdapter // 어댑터를 멤버 변수로 설정
    private lateinit var songs: List<Song>    // 원본 데이터 리스트

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val search = findViewById<SearchView>(R.id.search)
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)

        // RecyclerView 초기화
        recyclerView.layoutManager = LinearLayoutManager(this)

        // 데이터 설정 및 어댑터 연결
        songs = DataProvider.getSongs()
        adapter = SongAdapter(songs) { selectedSong ->
            // Song 객체를 Intent로 전달
            val intent = Intent(this, MusicPlayer::class.java).apply {
                putExtra("SELECTED_SONG", selectedSong)
            }
            startActivity(intent)
        }
        recyclerView.adapter = adapter

        // SearchView에 리스너 추가
        search.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                // 검색어 제출 시 필터링
                filterSongs(query)
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                // 검색어 변경 시 실시간 필터링
                filterSongs(newText)
                return true
            }
        })
    }

    private fun filterSongs(query: String?) {
        val filteredSongs = if (!query.isNullOrEmpty()) {
            songs.filter { song ->
                song.title.contains(query, ignoreCase = true) || song.artist.contains(query, ignoreCase = true)
            }
        } else {
            songs // 검색어가 없으면 전체 목록 표시
        }
        adapter.updateData(filteredSongs) // 필터링된 데이터로 어댑터 업데이트
    }
}
