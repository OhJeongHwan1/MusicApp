package com.example.realmusicplayer

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class SongAdapter(private val songs: List<Song>,private val onItemClick: (Song) -> Unit) : RecyclerView.Adapter<SongAdapter.SongViewHolder>() {

    // ViewHolder 정의
    class SongViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val songImage: ImageView = itemView.findViewById(R.id.song_image)
        val songTitle: TextView = itemView.findViewById(R.id.song_title)
        val songArtist: TextView = itemView.findViewById(R.id.song_artist)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_song, parent, false)
        return SongViewHolder(view)
    }

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        val song = songs[position]
        holder.songImage.setImageResource(song.imageResId)
        holder.songTitle.text = song.title
        holder.songArtist.text = song.artist

        holder.itemView.setOnClickListener {
            onItemClick(song) // 클릭된 Song 전달
        }
    }

    override fun getItemCount(): Int {
        return songs.size
    }
}
