package com.syn10.sargambeats.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.syn10.sargambeats.R
import com.syn10.sargambeats.databinding.ItemSongBinding
import com.syn10.sargambeats.model.Song
import com.syn10.sargambeats.service.MusicService
import java.util.Locale
import java.util.concurrent.TimeUnit


class MusicAdapter(

    private val songs: ArrayList<Song>,

    private val onSongClick: (Int) -> Unit,

    private val onSelectionChanged: (List<Song>) -> Unit

) : RecyclerView.Adapter<MusicAdapter.ViewHolder>() {


    private var selectionMode =
        false


    private val selectedSongs =
        ArrayList<Song>()




    inner class ViewHolder(
        private val binding: ItemSongBinding
    ) : RecyclerView.ViewHolder(binding.root) {


        fun bind(
            song: Song,
            position: Int
        ) = with(binding) {


            tvTitle.text =
                song.title


            tvArtist.text =
                song.artist


            tvDuration.text =
                formatDuration(song.duration)



            Glide.with(root.context)
                .load(song.albumArt)
                .placeholder(R.drawable.app_logo)
                .error(R.drawable.app_logo)
                .fallback(R.drawable.app_logo)
                .centerCrop()
                .into(ivAlbumArt)




            if (
                MusicService.isPlaying &&
                MusicService.songList.isNotEmpty() &&
                MusicService.position < MusicService.songList.size &&
                MusicService.songList[MusicService.position].id == song.id
            ) {


                ivAlbumArt.visibility =
                    View.INVISIBLE


                lottiePlaying.visibility =
                    View.VISIBLE


                lottiePlaying.playAnimation()


            } else {


                lottiePlaying.cancelAnimation()


                lottiePlaying.visibility =
                    View.GONE


                ivAlbumArt.visibility =
                    View.VISIBLE

            }




            checkSelect.visibility =
                if (selectionMode) {

                    View.VISIBLE

                } else {

                    View.GONE

                }



            checkSelect.isChecked =
                selectedSongs.contains(song)





            root.setOnLongClickListener {


                selectionMode =
                    true



                toggleSelection(
                    song
                )



                true

            }




            root.setOnClickListener {


                if (selectionMode) {


                    toggleSelection(
                        song
                    )


                } else {


                    onSongClick(
                        position
                    )

                }

            }


        }

    }






    private fun toggleSelection(
        song: Song
    ) {


        if (
            selectedSongs.contains(song)
        ) {


            selectedSongs.remove(song)


        } else {


            selectedSongs.add(song)

        }




        if (
            selectedSongs.isEmpty()
        ) {


            selectionMode =
                false

        }




        onSelectionChanged(
            ArrayList(selectedSongs)
        )



        notifyDataSetChanged()

    }






    fun selectAllSongs() {


        selectedSongs.clear()



        selectedSongs.addAll(
            songs
        )



        selectionMode =
            true




        onSelectionChanged(
            ArrayList(selectedSongs)
        )



        notifyDataSetChanged()

    }







    fun clearSelection() {


        selectedSongs.clear()



        selectionMode =
            false




        onSelectionChanged(
            emptyList()
        )



        notifyDataSetChanged()

    }






    fun isAllSelected(): Boolean {


        return selectedSongs.size == songs.size &&
                songs.isNotEmpty()

    }






    fun isSelectionMode(): Boolean {


        return selectionMode

    }






    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {


        val binding =
            ItemSongBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )


        return ViewHolder(binding)

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






    override fun getItemCount(): Int {


        return songs.size

    }







    private fun formatDuration(
        ms: Long
    ): String {


        val minutes =
            TimeUnit.MILLISECONDS.toMinutes(ms)


        val seconds =
            TimeUnit.MILLISECONDS.toSeconds(ms) % 60



        return String.format(
            Locale.getDefault(),
            "%02d:%02d",
            minutes,
            seconds
        )

    }


}