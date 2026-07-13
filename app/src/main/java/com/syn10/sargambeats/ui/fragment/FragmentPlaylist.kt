package com.syn10.sargambeats.ui.fragment

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.syn10.sargambeats.R
import com.syn10.sargambeats.adapter.PlaylistAdapter
import com.syn10.sargambeats.database.MusicDatabase
import com.syn10.sargambeats.databinding.FragmentPlaylistBinding
import com.syn10.sargambeats.repository.PlaylistRepository
import com.syn10.sargambeats.ui.activity.MainActivity
import com.syn10.sargambeats.viewmodel.PlaylistViewModel
import com.syn10.sargambeats.viewmodel.PlaylistViewModelFactory
import kotlinx.coroutines.launch
import androidx.appcompat.widget.PopupMenu


class FragmentPlaylist : Fragment(R.layout.fragment_playlist) {


    private lateinit var playlistViewModel: PlaylistViewModel

    private lateinit var binding: FragmentPlaylistBinding

    private lateinit var playlistAdapter: PlaylistAdapter

    private var isInsidePlaylist =
        false


    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {

        super.onViewCreated(view, savedInstanceState)


        binding =
            FragmentPlaylistBinding.bind(view)



        val database =
            MusicDatabase.getDatabase(requireContext())


        val repository =
            PlaylistRepository(
                database.playlistDao(),
                database.playlistSongDao()
            )


        val factory =
            PlaylistViewModelFactory(repository)


        playlistViewModel =
            ViewModelProvider(
                this,
                factory
            )[PlaylistViewModel::class.java]


        setupRecyclerView()

        observePlaylists()

        parentFragmentManager
            .addOnBackStackChangedListener {


                isInsidePlaylist =
                    parentFragmentManager
                        .backStackEntryCount > 0


                if (isInsidePlaylist) {


                    (activity as? MainActivity)
                        ?.hideShuffleFab()


                    (activity as? MainActivity)
                        ?.hideThemeFab()


                } else {


                    (activity as? MainActivity)
                        ?.showAddPlaylistFab()


                    (activity as? MainActivity)
                        ?.showThemeFab()

                }

            }


    }


    private fun setupRecyclerView() {


        playlistAdapter =
            PlaylistAdapter(

                { playlist ->

                    isInsidePlaylist =
                        true


                    val fragment =
                        PlaylistDetailsFragment()


                    fragment.arguments =
                        Bundle().apply {


                            putLong(
                                "playlistId",
                                playlist.playlistId
                            )


                            putString(
                                "playlistName",
                                playlist.name
                            )

                        }


                    parentFragmentManager
                        .beginTransaction()
                        .replace(
                            R.id.playlistContainer,
                            fragment
                        )
                        .addToBackStack(null)
                        .commit()

                },


                { playlist, view ->


                    val popup =
                        PopupMenu(
                            requireContext(),
                            view
                        )


                    popup.menu.add(
                        "Rename"
                    )


                    popup.menu.add(
                        "Delete"
                    )


                    popup.setOnMenuItemClickListener { item ->


                        when (item.title) {


                            "Rename" -> {


                                val input =
                                    EditText(
                                        requireContext()
                                    )


                                input.setText(
                                    playlist.name
                                )


                                input.selectAll()


                                input.requestFocus()


                                input.setTextColor(
                                    requireContext()
                                        .getColor(
                                            com.google.android.material.R.color.material_on_surface_emphasis_high_type
                                        )
                                )


                                input.setHintTextColor(
                                    requireContext()
                                        .getColor(
                                            com.google.android.material.R.color.material_on_surface_emphasis_medium
                                        )
                                )


                                val dialog =
                                    AlertDialog.Builder(
                                        requireContext()
                                    )

                                        .setTitle(
                                            "Rename Playlist"
                                        )

                                        .setView(
                                            input
                                        )

                                        .setPositiveButton(
                                            "Rename"
                                        ) { dialog, _ ->


                                            val newName =
                                                input.text
                                                    .toString()
                                                    .trim()


                                            if (newName.isNotEmpty()) {


                                                playlistViewModel
                                                    .renamePlaylist(
                                                        playlist.playlistId,
                                                        newName
                                                    )


                                                Toast.makeText(
                                                    requireContext(),
                                                    "Playlist renamed",
                                                    Toast.LENGTH_SHORT
                                                ).show()

                                            }

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


                                    dialog.window
                                        ?.setSoftInputMode(
                                            WindowManager.LayoutParams
                                                .SOFT_INPUT_STATE_ALWAYS_VISIBLE
                                        )


                                    input.requestFocus()


                                    input.selectAll()


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


                                true

                            }


                            "Delete" -> {


                                val title =
                                    android.widget.TextView(
                                        requireContext()
                                    )


                                title.text =
                                    "Delete Playlist?"


                                title.textSize =
                                    20f


                                title.setPadding(
                                    50,
                                    40,
                                    20,
                                    20
                                )


                                title.setTextColor(
                                    requireContext()
                                        .getColor(
                                            com.google.android.material.R.color.material_on_surface_emphasis_high_type
                                        )
                                )


                                val message =
                                    android.widget.TextView(
                                        requireContext()
                                    )


                                message.text =
                                    "Are you sure you want to delete ${playlist.name}?"


                                message.setPadding(
                                    50,
                                    20,
                                    50,
                                    20
                                )


                                message.setTextColor(
                                    requireContext()
                                        .getColor(
                                            com.google.android.material.R.color.material_on_surface_emphasis_high_type
                                        )
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
                                            "Delete"
                                        ) { dialog, _ ->


                                            playlistViewModel
                                                .deletePlaylist(
                                                    playlist.playlistId
                                                )


                                            Toast.makeText(
                                                requireContext(),
                                                "Playlist deleted",
                                                Toast.LENGTH_SHORT
                                            ).show()


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


                                true

                            }


                            else -> false

                        }

                    }


                    popup.show()

                }

            )


        binding.rvPlaylists.apply {


            adapter =
                playlistAdapter


            layoutManager =
                LinearLayoutManager(
                    requireContext()
                )

        }

    }


    private fun observePlaylists() {


        viewLifecycleOwner.lifecycleScope.launch {


            viewLifecycleOwner.repeatOnLifecycle(
                Lifecycle.State.STARTED
            ) {


                playlistViewModel.playlists.collect { playlists ->


                    playlistAdapter.submitList(
                        playlists
                    )



                    binding.tvNoPlaylist.visibility =
                        if (playlists.isEmpty()) {

                            View.VISIBLE

                        } else {

                            View.GONE

                        }



                    playlists.forEach { playlist ->


                        launch {


                            playlistViewModel
                                .getSongCount(
                                    playlist.playlistId
                                )
                                .collect { count ->


                                    playlistAdapter.updateSongCount(
                                        playlist.playlistId,
                                        count
                                    )

                                }

                        }

                    }

                }

            }

        }

    }


    fun createPlaylistDialog() {


        val input =
            EditText(requireContext())


        input.hint =
            "Playlist name"


        input.setTextColor(
            requireContext()
                .getColor(
                    com.google.android.material.R.color.material_on_surface_emphasis_high_type
                )
        )

        input.setHintTextColor(
            requireContext()
                .getColor(
                    com.google.android.material.R.color.material_on_surface_emphasis_medium
                )
        )


        val title =
            android.widget.TextView(requireContext())


        title.text =
            "Create Playlist"


        title.setTextColor(
            requireContext()
                .getColor(
                    com.google.android.material.R.color.material_on_surface_emphasis_high_type
                )
        )

        title.textSize =
            20f


        title.setPadding(
            50,
            40,
            20,
            20
        )


        val dialog =
            AlertDialog.Builder(requireContext())

                .setCustomTitle(title)

                .setView(input)

                .setPositiveButton(
                    "Create"
                ) { _, _ ->


                    val name =
                        input.text.toString().trim()


                    if (name.isNotEmpty()) {

                        playlistViewModel.createPlaylist(
                            name
                        )


                        Toast.makeText(
                            requireContext(),
                            "Playlist created",
                            Toast.LENGTH_SHORT
                        ).show()

                    }

                }


                .setNegativeButton(
                    "Cancel"
                ) { d, _ ->

                    d.dismiss()

                }

                .create()


        dialog.setOnShowListener {


            dialog.window
                ?.setBackgroundDrawableResource(
                    R.drawable.dialog_bg
                )


            dialog.window
                ?.attributes
                ?.windowAnimations =
                android.R.style.Animation_Dialog


            dialog.window
                ?.setDimAmount(0.6f)


            dialog.window
                ?.addFlags(
                    WindowManager.LayoutParams.FLAG_DIM_BEHIND
                )


            dialog.getButton(
                AlertDialog.BUTTON_POSITIVE
            ).setTextColor(Color.RED)


            dialog.getButton(
                AlertDialog.BUTTON_NEGATIVE
            ).setTextColor(Color.RED)

        }


        dialog.show()

    }


    override fun onResume() {

        super.onResume()


        if (isInsidePlaylist) {


            (activity as? MainActivity)
                ?.hideShuffleFab()


            (activity as? MainActivity)
                ?.hideThemeFab()


        } else {


            (activity as? MainActivity)
                ?.showAddPlaylistFab()


            (activity as? MainActivity)
                ?.showThemeFab()

        }

    }

    override fun onPause() {

        super.onPause()

        (activity as? MainActivity)
            ?.hideShuffleFab()

    }

}