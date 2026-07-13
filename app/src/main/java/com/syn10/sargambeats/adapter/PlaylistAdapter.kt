package com.syn10.sargambeats.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.syn10.sargambeats.database.PlaylistEntity
import com.syn10.sargambeats.databinding.ItemPlaylistBinding


class PlaylistAdapter(

    private val onClick: (PlaylistEntity) -> Unit,

    private val onMenuClick: (
        PlaylistEntity,
        View
    ) -> Unit

) : RecyclerView.Adapter<PlaylistAdapter.PlaylistViewHolder>() {


    private val playlists =
        ArrayList<PlaylistEntity>()


    private val songCounts =
        HashMap<Long, Int>()


    inner class PlaylistViewHolder(
        val binding: ItemPlaylistBinding
    ) : RecyclerView.ViewHolder(
        binding.root
    )


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): PlaylistViewHolder {


        val binding =
            ItemPlaylistBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )


        return PlaylistViewHolder(
            binding
        )

    }


    override fun onBindViewHolder(
        holder: PlaylistViewHolder,
        position: Int
    ) {


        val playlist =
            playlists[position]


        holder.binding.tvPlaylistName.text =
            playlist.name


        val count =
            songCounts[
                playlist.playlistId
            ] ?: 0


        holder.binding.tvPlaylistSongCount.text =
            "$count Songs"


        // playlist open
        holder.itemView.setOnClickListener {


            onClick(
                playlist
            )

        }


        // 3 dot menu
        holder.binding.tvPlaylistOptions.setOnClickListener {


            onMenuClick(
                playlist,
                it
            )

        }

    }


    override fun getItemCount(): Int =
        playlists.size


    fun submitList(
        newList: List<PlaylistEntity>
    ) {


        playlists.clear()


        playlists.addAll(
            newList
        )


        notifyDataSetChanged()

    }


    fun updateSongCount(
        playlistId: Long,
        count: Int
    ) {


        songCounts[
            playlistId
        ] =
            count


        notifyDataSetChanged()

    }

}