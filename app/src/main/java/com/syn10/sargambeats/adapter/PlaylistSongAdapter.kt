package com.syn10.sargambeats.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.syn10.sargambeats.R
import com.syn10.sargambeats.database.PlaylistSongEntity
import com.syn10.sargambeats.databinding.ItemSongBinding
import java.util.Locale
import java.util.concurrent.TimeUnit


class PlaylistSongAdapter(
    private val onSongClick: (Int) -> Unit,
    private val onSongLongClick: (PlaylistSongEntity) -> Unit
) : RecyclerView.Adapter<PlaylistSongAdapter.ViewHolder>() {


    private val songs =
        ArrayList<PlaylistSongEntity>()


    inner class ViewHolder(
        private val binding: ItemSongBinding
    ) : RecyclerView.ViewHolder(
        binding.root
    ) {


        fun bind(
            song: PlaylistSongEntity,
            position: Int
        ) {


            binding.tvTitle.text =
                song.title


            binding.tvArtist.text =
                song.artist


            binding.tvDuration.text =
                formatDuration(
                    song.duration
                )


            Glide.with(
                binding.root.context
            )
                .load(
                    song.albumArt
                )
                .placeholder(
                    R.drawable.app_logo
                )
                .error(
                    R.drawable.app_logo
                )
                .into(
                    binding.ivAlbumArt
                )


            binding.checkSelect.visibility =
                android.view.View.GONE


            binding.root.setOnClickListener {


                onSongClick(
                    position
                )

            }

            binding.root.setOnLongClickListener {


                onSongLongClick(
                    song
                )


                true

            }

        }

    }


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {


        val binding =
            ItemSongBinding.inflate(
                LayoutInflater.from(
                    parent.context
                ),
                parent,
                false
            )


        return ViewHolder(
            binding
        )

    }


    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {


        holder.bind(
            songs[position],
            position
        )

    }


    override fun getItemCount(): Int =
        songs.size


    fun submitList(
        newList: List<PlaylistSongEntity>
    ) {


        songs.clear()


        songs.addAll(
            newList
        )


        notifyDataSetChanged()

    }


    fun getSongs():
            ArrayList<PlaylistSongEntity> {


        return songs

    }


    private fun formatDuration(
        ms: Long
    ): String {


        return String.format(
            Locale.getDefault(),
            "%02d:%02d",

            TimeUnit.MILLISECONDS
                .toMinutes(ms),

            TimeUnit.MILLISECONDS
                .toSeconds(ms) % 60
        )

    }

}