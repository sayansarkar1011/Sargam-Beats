package com.syn10.sargambeats.ui.fragment


import android.content.Intent
import android.net.Uri
import android.os.Bundle

import androidx.activity.OnBackPressedCallback

import android.graphics.Color
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.syn10.sargambeats.R
import com.syn10.sargambeats.adapter.FavouriteAdapter
import com.syn10.sargambeats.database.MusicDatabase
import com.syn10.sargambeats.databinding.FragmentFavouritesBinding
import com.syn10.sargambeats.model.Song
import com.syn10.sargambeats.repository.FavouriteRepository
import com.syn10.sargambeats.service.MusicService
import com.syn10.sargambeats.ui.activity.MainActivity
import com.syn10.sargambeats.viewmodel.FavouriteViewModel
import com.syn10.sargambeats.viewmodel.FavouriteViewModelFactory
import kotlinx.coroutines.launch
import android.widget.EditText
import android.util.TypedValue

class FragmentFavourites : Fragment(R.layout.fragment_favourites) {


    private var _binding: FragmentFavouritesBinding? = null

    private val binding get() = _binding!!


    private lateinit var favouriteAdapter: FavouriteAdapter

    private lateinit var favouriteViewModel: FavouriteViewModel

    private var isFavouriteSelectionActive = false



    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {

        super.onViewCreated(view, savedInstanceState)


        _binding =
            FragmentFavouritesBinding.bind(view)



        val dao =
            MusicDatabase
                .getDatabase(requireContext())
                .favouriteDao()


        val repository =
            FavouriteRepository(dao)


        val factory =
            FavouriteViewModelFactory(repository)


        favouriteViewModel =
            ViewModelProvider(
                this,
                factory
            )[FavouriteViewModel::class.java]


        setupRecyclerView()


        setupSelectionControls()


        observeFavourites()


        handleBackPress()

    }




    private fun setupRecyclerView() {


        favouriteAdapter =
            FavouriteAdapter(


                onClick = { _, position ->


                    val songs =
                        favouriteAdapter
                            .getSongs()
                            .map {

                                Song(
                                    id = it.songId,
                                    title = it.title,
                                    artist = it.artist,
                                    duration = it.duration,
                                    albumArt = Uri.parse(it.albumArt),
                                    dataPath = it.dataPath
                                )

                            }


                    MusicService.songList =
                        ArrayList(songs)


                    MusicService.position =
                        position


                    requireContext()
                        .startService(

                            Intent(
                                requireContext(),
                                MusicService::class.java
                            ).setAction(
                                MusicService.ACTION_PLAY_NEW
                            )

                        )


                },



                onSelectionChanged = { count ->


                    isFavouriteSelectionActive =
                        count > 0


                    binding.selectionBar.visibility =
                        if (count > 0) {

                            View.VISIBLE

                        } else {

                            View.GONE

                        }


                    binding.tvSelectAll.text =
                        if (
                            favouriteAdapter.isAllSelected()
                        ) {

                            "Unselect All"

                        } else {

                            "Select All"

                        }


                }


            )



        binding.rvFavourites.apply {


            layoutManager =
                LinearLayoutManager(
                    requireContext()
                )


            adapter =
                favouriteAdapter

        }

    }


    private fun setupSelectionControls() {


        binding.tvSelectAll.setOnClickListener {


            if (
                favouriteAdapter.isAllSelected()
            ) {


                favouriteAdapter.clearSelection()


                binding.tvSelectAll.text =
                    "Select All"


            } else {


                favouriteAdapter.selectAll()


                binding.tvSelectAll.text =
                    "Unselect All"

            }


        }



        binding.ivShareSelected.setOnClickListener {


            val selectedSongs =
                favouriteAdapter
                    .getSelectedSongs()
                    .toList()


            if (selectedSongs.isEmpty()) {

                return@setOnClickListener

            }



            val shareUris =
                ArrayList<Uri>()


            selectedSongs.forEach { song ->


                shareUris.add(
                    Uri.parse(song.dataPath)
                )

            }



            val shareIntent =
                Intent().apply {


                    action =
                        Intent.ACTION_SEND_MULTIPLE


                    type =
                        "audio/*"


                    putParcelableArrayListExtra(
                        Intent.EXTRA_STREAM,
                        shareUris
                    )


                    addFlags(
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )

                }



            startActivity(

                Intent.createChooser(
                    shareIntent,
                    "Share Songs"
                )

            )


        }





        binding.ivDeleteSelected.setOnClickListener {


            val selectedSongs =
                favouriteAdapter
                    .getSelectedSongs()
                    .toList()


            if (selectedSongs.isEmpty()) {

                return@setOnClickListener

            }



            val dialog =
                AlertDialog.Builder(
                    requireContext()
                )

                    .setTitle(
                        "Remove Favourite"
                    )

                    .setMessage(
                        "Remove selected songs from Favourites?"
                    )


                    .setPositiveButton(
                        "Remove"
                    ) { _, _ ->


                        selectedSongs.forEach { song ->


                            favouriteViewModel
                                .removeFavourite(song)

                        }


                        favouriteAdapter
                            .clearSelection()



                        Toast.makeText(
                            requireContext(),
                            "Removed from Favourites",
                            Toast.LENGTH_SHORT
                        ).show()


                    }


                    .setNegativeButton(
                        "Cancel",
                        null
                    )


                    .create()




            dialog.setOnShowListener {


                val red =
                    Color.parseColor(
                        "#E1170A"
                    )


                dialog.getButton(
                    AlertDialog.BUTTON_POSITIVE
                ).setTextColor(red)


                dialog.getButton(
                    AlertDialog.BUTTON_NEGATIVE
                ).setTextColor(red)

            }




            dialog.show()



            dialog.window
                ?.setBackgroundDrawableResource(
                    R.drawable.dialog_bg
                )

        }

        binding.ivRenameSelected.setOnClickListener {


            val selectedSongs =
                favouriteAdapter
                    .getSelectedSongs()
                    .toList()


            if (selectedSongs.isEmpty()) {

                return@setOnClickListener

            }


            if (selectedSongs.size > 1) {


                Toast.makeText(
                    requireContext(),
                    "Select only one song",
                    Toast.LENGTH_SHORT
                ).show()


                return@setOnClickListener

            }



            val song =
                selectedSongs[0]



            val editText =
                EditText(requireContext())


            editText.setText(
                song.title
            )


            editText.setSelection(
                editText.text.length
            )



            val typedValue =
                TypedValue()


            requireContext()
                .theme
                .resolveAttribute(
                    com.google.android.material.R.attr.colorOnSurface,
                    typedValue,
                    true
                )


            editText.setTextColor(
                typedValue.data
            )



            val dialog =
                AlertDialog.Builder(
                    requireContext()
                )

                    .setTitle(
                        "Rename Favourite"
                    )

                    .setView(
                        editText
                    )


                    .setPositiveButton(
                        "Rename"
                    ) { _, _ ->


                        val newName =
                            editText.text
                                .toString()
                                .trim()


                        if (newName.isNotEmpty()) {


                            favouriteViewModel
                                .renameFavourite(
                                    song.songId,
                                    newName
                                )


                            favouriteAdapter
                                .clearSelection()



                            Toast.makeText(
                                requireContext(),
                                "Renamed",
                                Toast.LENGTH_SHORT
                            ).show()

                        }

                    }


                    .setNegativeButton(
                        "Cancel",
                        null
                    )


                    .create()




            dialog.setOnShowListener {


                val red =
                    Color.parseColor(
                        "#E1170A"
                    )


                dialog.getButton(
                    AlertDialog.BUTTON_POSITIVE
                ).setTextColor(red)


                dialog.getButton(
                    AlertDialog.BUTTON_NEGATIVE
                ).setTextColor(red)

            }



            dialog.show()



            dialog.window
                ?.setBackgroundDrawableResource(
                    R.drawable.dialog_bg
                )

        }


    }


    private fun handleBackPress() {


        requireActivity()
            .onBackPressedDispatcher
            .addCallback(
                viewLifecycleOwner,
                object : OnBackPressedCallback(true) {


                    override fun handleOnBackPressed() {

                        if (
                            isFavouriteSelectionActive
                        ) {


                            favouriteAdapter.clearSelection()


                            binding.selectionBar.visibility =
                                View.GONE


                            isFavouriteSelectionActive =
                                false


                        } else {


                            isEnabled = false


                            requireActivity()
                                .onBackPressedDispatcher
                                .onBackPressed()

                        }

                    }

                }
            )

    }


    private fun observeFavourites() {


        lifecycleScope.launch {


            favouriteViewModel
                .getAllFavourites()
                .collect { list ->


                    val sortedList =
                        list.sortedBy {
                            it.title.lowercase()
                        }


                    favouriteAdapter
                        .submitList(sortedList)



                    binding.tvNoFavourite.visibility =
                        if (sortedList.isEmpty()) {

                            View.VISIBLE

                        } else {

                            View.GONE

                        }

                }

        }

    }




    override fun onResume() {

        super.onResume()


        (activity as? MainActivity)
            ?.hideShuffleFab()

    }





    override fun onDestroyView() {

        super.onDestroyView()


        _binding = null

    }


}