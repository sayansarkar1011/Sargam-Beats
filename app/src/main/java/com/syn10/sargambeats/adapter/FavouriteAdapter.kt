package com.syn10.sargambeats.adapter


import android.content.res.ColorStateList
import android.graphics.Color
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.syn10.sargambeats.R
import com.syn10.sargambeats.database.FavouriteEntity
import com.syn10.sargambeats.databinding.ItemSongBinding


class FavouriteAdapter(

    private val onClick: (FavouriteEntity, Int) -> Unit,

    private val onSelectionChanged: (Int) -> Unit

) : RecyclerView.Adapter<FavouriteAdapter.FavouriteViewHolder>() {


    private val favouriteList =
        ArrayList<FavouriteEntity>()


    private val selectedSongs =
        ArrayList<FavouriteEntity>()


    private var isSelectionMode =
        false



    inner class FavouriteViewHolder(
        val binding: ItemSongBinding
    ) : RecyclerView.ViewHolder(binding.root)




    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): FavouriteViewHolder {


        val binding =
            ItemSongBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )


        return FavouriteViewHolder(binding)

    }




    override fun onBindViewHolder(
        holder: FavouriteViewHolder,
        position: Int
    ) {


        val song =
            favouriteList[position]



        holder.binding.tvTitle.text =
            song.title


        holder.binding.tvArtist.text =
            song.artist


        holder.binding.tvDuration.text =
            formatDuration(song.duration)




        Glide.with(holder.itemView.context)
            .load(Uri.parse(song.albumArt))
            .placeholder(R.drawable.app_logo)
            .error(R.drawable.app_logo)
            .into(holder.binding.ivAlbumArt)




        holder.binding.checkSelect.visibility =
            if (isSelectionMode) {

                View.VISIBLE

            } else {

                View.GONE

            }



        holder.binding.checkSelect.buttonTintList =
            ColorStateList.valueOf(
                Color.parseColor("#E1170A")
            )



        holder.binding.checkSelect.isChecked =
            selectedSongs.contains(song)





        holder.itemView.setOnClickListener {


            if (isSelectionMode) {


                toggleSelection(
                    song
                )


            } else {


                onClick(
                    song,
                    position
                )

            }

        }





        holder.itemView.setOnLongClickListener {


            isSelectionMode =
                true



            toggleSelection(
                song
            )



            true

        }


    }






    private fun toggleSelection(
        song: FavouriteEntity
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


            isSelectionMode =
                false

        }




        notifyDataSetChanged()



        onSelectionChanged(
            selectedSongs.size
        )

    }






    fun clearSelection() {


        selectedSongs.clear()



        isSelectionMode =
            false



        notifyDataSetChanged()



        onSelectionChanged(
            0
        )

    }






    fun isSelectionMode(): Boolean {


        return isSelectionMode

    }






    fun selectAll() {


        selectedSongs.clear()



        selectedSongs.addAll(
            favouriteList
        )



        isSelectionMode =
            true



        notifyDataSetChanged()



        onSelectionChanged(
            selectedSongs.size
        )

    }






    fun getSelectedSongs():
            List<FavouriteEntity> {


        return selectedSongs

    }






    fun isAllSelected(): Boolean {


        return selectedSongs.size ==
                favouriteList.size &&
                favouriteList.isNotEmpty()

    }







    override fun getItemCount(): Int {


        return favouriteList.size

    }







    fun submitList(
        list: List<FavouriteEntity>
    ) {


        favouriteList.clear()



        favouriteList.addAll(
            list
        )



        notifyDataSetChanged()

    }







    fun getSongs():
            List<FavouriteEntity> {


        return favouriteList

    }







    private fun formatDuration(
        duration: Long
    ): String {


        val minutes =
            duration / 1000 / 60



        val seconds =
            duration / 1000 % 60



        return String.format(
            "%02d:%02d",
            minutes,
            seconds
        )

    }


}