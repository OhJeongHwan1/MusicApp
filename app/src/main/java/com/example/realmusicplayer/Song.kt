package com.example.realmusicplayer

data class Song(
    val imageResId: Int, // 이미지 리소스 ID
    val title: String,   // 노래 제목
    val artist: String,  // 가수명
    val duration: String, // 노래 길이 (예: "3:45")
    val rawResId: Int
)