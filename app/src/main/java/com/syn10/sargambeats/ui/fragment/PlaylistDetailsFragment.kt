package com.syn10.sargambeats.ui.fragment

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import android.util.TypedValue
import com.syn10.sargambeats.R
import com.syn10.sargambeats.adapter.PlaylistSongAdapter
import com.syn10.sargambeats.database.MusicDatabase
import com.syn10.sargambeats.databinding.FragmentPlaylistDetailsBinding
import com.syn10.sargambeats.model.Song
import com.syn10.sargambeats.repository.PlaylistRepository
import com.syn10.sargambeats.service.MusicService
import com.syn10.sargambeats.viewmodel.PlaylistViewModel
import com.syn10.sargambeats.viewmodel.PlaylistViewModelFactory
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.syn10.sargambeats.ui.activity.MainActivity


class PlaylistDetailsFragment :
    Fragment(R.layout.fragment_playlist_details) {


    private lateinit var binding:
            FragmentPlaylistDetailsBinding


    private lateinit var playlistViewModel:
            PlaylistViewModel


    private lateinit var adapter:
            PlaylistSongAdapter


    private var playlistId: Long = -1

    private var playlistName: String = ""

    private var isFirstLoad = true


    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {

        super.onViewCreated(view, savedInstanceState)


        binding =
            FragmentPlaylistDetailsBinding.bind(view)


        playlistId =
            arguments
                ?.getLong("playlistId")
                ?: -1

        playlistName =
            arguments
                ?.getString(
                    "playlistName"
                )
                ?: ""


        val database =
            MusicDatabase.getDatabase(
                requireContext()
            )


        val repository =
            PlaylistRepository(
                database.playlistDao(),
                database.playlistSongDao()
            )


        playlistViewModel =
            ViewModelProvider(
                this,
                PlaylistViewModelFactory(repository)
            )[PlaylistViewModel::class.java]


        setupRecyclerView()


        observeSongs()

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : androidx.activity.OnBackPressedCallback(true) {

                override fun handleOnBackPressed() {

                    parentFragmentManager.popBackStack()

                }
            }
        )

    }

    override fun onResume() {

        super.onResume()


        (activity as? MainActivity)
            ?.updateToolbar(
                "Playlist - $playlistName",
                false
            )


        (activity as? MainActivity)
            ?.hideThemeFab()


        (activity as? MainActivity)
            ?.hideShuffleFab()

    }


    private fun setupRecyclerView() {


        adapter =
            PlaylistSongAdapter(

                { position ->


                    val playableSongs =
                        ArrayList<Song>()


                    adapter.getSongs()
                        .forEach { song ->


                            playableSongs.add(

                                Song(
                                    song.songId,
                                    song.title,
                                    song.artist,
                                    song.duration,
                                    Uri.parse(song.albumArt),
                                    song.dataPath
                                )

                            )

                        }


                    MusicService.songList =
                        playableSongs


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


                { song ->


                    val title =
                        android.widget.TextView(
                            requireContext()
                        )


                    title.text =
                        "Remove Song?"


                    title.textSize =
                        20f


                    title.setPadding(
                        50,
                        40,
                        20,
                        20
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

                    title.setTextColor(
                        typedValue.data
                    )



                    val message =
                        android.widget.TextView(
                            requireContext()
                        )


                    message.text =
                        "Remove this song from playlist?"


                    message.setPadding(
                        50,
                        20,
                        50,
                        20
                    )


                    message.setTextColor(
                        typedValue.data
                    )


                    val dialog =
                        AlertDialog.Builder(
                            requireContext()
                        )

                            .setCustomTitle(
                                title
                            )

                            .setView(
                                message
                            )

                            .setPositiveButton(
                                "Remove"
                            ) { dialog, _ ->


                                playlistViewModel
                                    .removeSongFromPlaylist(
                                        song.id
                                    )


                                android.widget.Toast
                                    .makeText(
                                        requireContext(),
                                        "Song removed",
                                        android.widget.Toast.LENGTH_SHORT
                                    )
                                    .show()


                                dialog.dismiss()

                            }


                            .setNegativeButton(
                                "Cancel"
                            ) { dialog, _ ->


                                dialog.dismiss()

                            }

                            .create()


                    dialog.setOnShowListener {


                        dialog.window
                            ?.setBackgroundDrawableResource(
                                R.drawable.dialog_bg
                            )


                        // smooth dialog animation
                        dialog.window
                            ?.attributes
                            ?.windowAnimations =
                            android.R.style.Animation_Dialog


                        dialog.getButton(
                            AlertDialog.BUTTON_POSITIVE
                        ).setTextColor(
                            Color.RED
                        )


                        dialog.getButton(
                            AlertDialog.BUTTON_NEGATIVE
                        ).setTextColor(
                            Color.RED
                        )

                    }


                    dialog.show()

                }

            )


        binding.rvPlaylistSongs.apply {


            adapter =
                this@PlaylistDetailsFragment.adapter


            layoutManager =
                LinearLayoutManager(
                    requireContext()
                )

        }

    }


    private fun observeSongs() {


        viewLifecycleOwner.lifecycleScope.launch {


            viewLifecycleOwner.repeatOnLifecycle(
                Lifecycle.State.STARTED
            ) {


                playlistViewModel
                    .getSongsFromPlaylist(
                        playlistId
                    )
                    .collect { songs ->



                        if (isFirstLoad) {


                            binding.progressLoader.visibility =
                                View.VISIBLE


                            binding.rvPlaylistSongs.visibility =
                                View.GONE


                            delay(
                                1000
                            )


                        }


                        adapter.submitList(
                            songs
                        )

                        isFirstLoad = false


                        binding.tvEmptyPlaylist.visibility =
                            if (songs.isEmpty())
                                View.VISIBLE
                            else
                                View.GONE


                        binding.progressLoader.visibility =
                            View.GONE


                        binding.rvPlaylistSongs.visibility =
                            if (songs.isEmpty())
                                View.GONE
                            else
                                View.VISIBLE

                    }

            }

        }

    }

    override fun onDestroyView() {

        super.onDestroyView()


        (activity as? MainActivity)
            ?.updateToolbar(
                "Playlist",
                false
            )


        (activity as? MainActivity)
            ?.showAddPlaylistFab()

    }

}