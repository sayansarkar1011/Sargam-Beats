package com.syn10.sargambeats.ui.fragment

import android.Manifest



import android.content.Intent
import androidx.activity.OnBackPressedCallback
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AlertDialog
import android.graphics.Color
import kotlinx.coroutines.flow.first
import android.app.Activity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.ImageView
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.syn10.sargambeats.database.MusicDatabase
import com.syn10.sargambeats.database.PlaylistSongEntity
import com.syn10.sargambeats.repository.PlaylistRepository
import com.syn10.sargambeats.viewmodel.PlaylistViewModel
import com.syn10.sargambeats.viewmodel.PlaylistViewModelFactory
import kotlinx.coroutines.launch
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.syn10.sargambeats.adapter.MusicAdapter
import com.syn10.sargambeats.service.MusicService
import com.syn10.sargambeats.R
import com.syn10.sargambeats.model.Song
import com.syn10.sargambeats.databinding.FragmentSongsBinding
import com.syn10.sargambeats.ui.activity.MainActivity

class FragmentSongs : Fragment(R.layout.fragment_songs) {

    private var _binding: FragmentSongsBinding? = null
    private val binding get() = _binding!!

    private lateinit var backCallback:
            OnBackPressedCallback

    private val selectedSongList =
        ArrayList<Song>()


    private var isSongSelectionActive =
        false


    private lateinit var playlistViewModel:
            PlaylistViewModel

    companion object {

        var isSelectionMode = false

        private val fullSongList = ArrayList<Song>()
        private val displayList = ArrayList<Song>()
        private var adapter: MusicAdapter? = null

        fun filterSongs(query: String) {

            displayList.clear()

            displayList.addAll(
                fullSongList.filter {
                    it.title.contains(query, true) ||
                            it.artist.contains(query, true)
                }
            )

            adapter?.notifyDataSetChanged()
        }

        fun clearFilter() {


            displayList.clear()


            displayList.addAll(
                ArrayList(fullSongList)
            )


            adapter?.notifyDataSetChanged()

        }
    }


    private val permissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { granted ->

            if (granted)
                loadSongs()
            else
                Toast.makeText(
                    requireContext(),
                    "Permission required to read music",
                    Toast.LENGTH_LONG
                ).show()
        }


    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {

        super.onViewCreated(
            view,
            savedInstanceState
        )

        _binding =
            FragmentSongsBinding.bind(view)


        val database =
            MusicDatabase.getDatabase(
                requireContext()
            )


        val repository =
            PlaylistRepository(
                database.playlistDao(),
                database.playlistSongDao()
            )


        val factory =
            PlaylistViewModelFactory(
                repository
            )


        playlistViewModel =
            ViewModelProvider(
                this,
                factory
            )[PlaylistViewModel::class.java]


        binding.rvSongs.layoutManager =
            LinearLayoutManager(requireContext())


        binding.swipeRefresh.setOnRefreshListener {

            loadSongs()

            Toast.makeText(
                requireContext(),
                "Total songs : ${fullSongList.size}",
                Toast.LENGTH_SHORT
            ).show()

            binding.swipeRefresh.isRefreshing = false
        }


        loadSongs()


        binding.btnSelectAllSongs
            .setOnClickListener {


                val adapter =
                    binding.rvSongs.adapter
                            as? MusicAdapter


                adapter?.let {


                    if (it.isAllSelected()) {


                        it.clearSelection()


                        binding.btnSelectAllSongs.text =
                            "Select All"


                    } else {


                        it.selectAllSongs()


                        binding.btnSelectAllSongs.text =
                            "Unselect All"

                    }

                }

            }


        binding.btnAddPlaylist.setOnClickListener {


            if (selectedSongList.isEmpty())
                return@setOnClickListener


            lifecycleScope.launch {


                val playlists =
                    playlistViewModel
                        .playlists
                        .first()


                val playlistNames =
                    playlists.map {
                        it.name
                    }.toTypedArray()


                val dialog =
                    AlertDialog.Builder(
                        requireContext()
                    )

                        .setTitle(
                            "Add to Playlist"
                        )

                        .setItems(
                            playlistNames
                        ) { dialog, which ->


                            val playlist =
                                playlists[which]


                            lifecycleScope.launch {


                                val tempSongs =
                                    ArrayList(
                                        selectedSongList
                                    )


                                tempSongs.forEach { song ->


                                    val exist =
                                        playlistViewModel.isSongExist(
                                            playlist.playlistId,
                                            song.id
                                        )


                                    if (exist == 0) {


                                        playlistViewModel.addSongToPlaylist(

                                            PlaylistSongEntity(

                                                playlistId =
                                                    playlist.playlistId,

                                                songId =
                                                    song.id,

                                                title =
                                                    song.title,

                                                artist =
                                                    song.artist,

                                                duration =
                                                    song.duration,

                                                albumArt =
                                                    song.albumArt.toString(),

                                                dataPath =
                                                    song.dataPath

                                            )

                                        )

                                    }

                                }


                                Toast.makeText(
                                    requireContext(),
                                    "Added to ${playlist.name}",
                                    Toast.LENGTH_SHORT
                                ).show()


                                (
                                        binding.rvSongs.adapter
                                                as? MusicAdapter
                                        )?.clearSelection()


                                selectedSongList.clear()

                                dialog.dismiss()

                            }

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

                }


                dialog.show()

            }

        }

        binding.btnDeleteSongs.setOnClickListener {


            if (selectedSongList.isEmpty())
                return@setOnClickListener


            val dialog =
                AlertDialog.Builder(
                    requireContext()
                )

                    .setTitle(
                        "Delete Songs?"
                    )

                    .setMessage(
                        "Are you sure you want to delete the selected songs?"
                    )


                    .setPositiveButton(
                        "Delete"
                    ) { dialog, _ ->


                        val deleteUris =
                            selectedSongList.map {


                                Uri.parse(
                                    it.dataPath
                                )

                            }


                        if (
                            Build.VERSION.SDK_INT >=
                            Build.VERSION_CODES.R
                        ) {


                            val request =
                                MediaStore.createDeleteRequest(
                                    requireContext()
                                        .contentResolver,

                                    deleteUris
                                )


                            startIntentSenderForResult(
                                request.intentSender,
                                101,
                                null,
                                0,
                                0,
                                0,
                                null
                            )


                        } else {


                            deleteUris.forEach {


                                requireContext()
                                    .contentResolver
                                    .delete(
                                        it,
                                        null,
                                        null
                                    )

                            }


                            loadSongs()

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

        binding.btnAddFavourite.setOnClickListener {


            if (selectedSongList.isEmpty())
                return@setOnClickListener


            lifecycleScope.launch {


                val favouriteDao =
                    MusicDatabase
                        .getDatabase(requireContext())
                        .favouriteDao()


                selectedSongList.forEach { song ->


                    favouriteDao.addFavourite(

                        com.syn10.sargambeats.database.FavouriteEntity(

                            songId =
                                song.id,

                            title =
                                song.title,

                            artist =
                                song.artist,

                            duration =
                                song.duration,

                            albumArt =
                                song.albumArt.toString(),

                            dataPath =
                                song.dataPath

                        )

                    )

                }


                Toast.makeText(
                    requireContext(),
                    "Added to Favourites",
                    Toast.LENGTH_SHORT
                ).show()



                (
                        binding.rvSongs.adapter
                                as? MusicAdapter
                        )?.clearSelection()


                selectedSongList.clear()


                binding.layoutSelectionActions.visibility =
                    View.GONE


                isSongSelectionActive =
                    false

            }

        }





        binding.btnShareSongs.setOnClickListener {


            if (selectedSongList.isEmpty()) {

                return@setOnClickListener

            }


            val tempSongs =
                selectedSongList.toList()



            val shareIntent =
                Intent(
                    Intent.ACTION_SEND_MULTIPLE
                )


            shareIntent.type =
                "audio/*"



            val uris =
                ArrayList<Uri>()


            tempSongs.forEach { song ->


                uris.add(
                    Uri.parse(
                        song.dataPath
                    )
                )

            }


            shareIntent.putParcelableArrayListExtra(
                Intent.EXTRA_STREAM,
                uris
            )



            startActivity(
                Intent.createChooser(
                    shareIntent,
                    "Share Songs"
                )
            )



            adapter?.clearSelection()



            selectedSongList.clear()



            binding.layoutSelectionActions.visibility =
                View.GONE



            isSongSelectionActive =
                false



            (activity as? MainActivity)
                ?.showShuffleFab()


        }

        MusicService.Companion.songChangeCallback = {

            activity?.runOnUiThread {

                adapter?.notifyDataSetChanged()

                scrollToCurrentSong()
            }
        }



        backCallback =
            object : OnBackPressedCallback(true) {




                override fun handleOnBackPressed() {

                    val currentAdapter =
                        binding.rvSongs.adapter as? MusicAdapter

                    if (isSongSelectionActive) {

                        clearSelectionMode()

                    } else {

                        isEnabled = false

                        requireActivity()
                            .onBackPressedDispatcher
                            .onBackPressed()

                    }

                }

            }


        requireActivity()
            .onBackPressedDispatcher
            .addCallback(
                viewLifecycleOwner,
                backCallback
            )


    }

    fun clearSelectionMode() {

        val currentAdapter = binding.rvSongs.adapter as? MusicAdapter

        currentAdapter?.clearSelection()

        selectedSongList.clear()

        isSongSelectionActive = false
        isSelectionMode = false

        binding.layoutSelectionActions.visibility = View.GONE

        binding.btnSelectAllSongs.text = "Select All"

        (activity as? MainActivity)?.showShuffleFab()
        backCallback.isEnabled = true


    }


    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {

        super.onActivityResult(
            requestCode,
            resultCode,
            data
        )


        if (
            requestCode == 101 &&
            resultCode == Activity.RESULT_OK
        ) {


            selectedSongList.clear()


            (
                    binding.rvSongs.adapter
                            as? MusicAdapter
                    )?.clearSelection()


            isSongSelectionActive =
                false


            loadSongs()

            Toast.makeText(
                requireContext(),
                "Songs deleted",
                Toast.LENGTH_SHORT
            ).show()

        }

    }


    override fun onResume() {

        super.onResume()


        if (
            selectedSongList.isNotEmpty()
        ) {


            (activity as? MainActivity)
                ?.hideShuffleFab()


        } else {


            (activity as? MainActivity)
                ?.showShuffleFab()

        }


        scrollToCurrentSong()

    }


    override fun onPause() {

        super.onPause()

        (activity as? MainActivity)
            ?.hideShuffleFab()
    }


    private fun checkPermission() {

        val permission =
            if (Build.VERSION.SDK_INT >=
                Build.VERSION_CODES.TIRAMISU
            )
                Manifest.permission.READ_MEDIA_AUDIO
            else
                Manifest.permission.READ_EXTERNAL_STORAGE


        if (
            ContextCompat.checkSelfPermission(
                requireContext(),
                permission
            ) == PackageManager.PERMISSION_GRANTED
        ) {

            loadSongs()

        } else {

            permissionLauncher.launch(permission)
        }
    }


    private fun loadSongs() {

        fullSongList.clear()
        displayList.clear()


        val cursor =
            requireContext()
                .contentResolver
                .query(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    arrayOf(
                        MediaStore.Audio.Media._ID,
                        MediaStore.Audio.Media.TITLE,
                        MediaStore.Audio.Media.ARTIST,
                        MediaStore.Audio.Media.DURATION,
                        MediaStore.Audio.Media.ALBUM_ID,
                        MediaStore.Audio.Media.DATA
                    ),
                    "${MediaStore.Audio.Media.IS_MUSIC}=1 AND ${MediaStore.Audio.Media.DURATION}>=30000",
                    null,
                    "${MediaStore.Audio.Media.TITLE} ASC"
                )


        cursor?.use {

            while (it.moveToNext()) {


                val id = it.getLong(0)
                val title = it.getString(1)
                val artist = it.getString(2)
                val duration = it.getLong(3)
                val albumId = it.getLong(4)


                val dataPath =
                    it.getString(5).lowercase()


                if (
                    dataPath.contains("record") ||
                    dataPath.contains("call") ||
                    dataPath.contains("whatsapp") ||
                    dataPath.contains("voice") ||
                    dataPath.endsWith(".amr") ||
                    dataPath.endsWith(".opus")
                ) continue


                fullSongList.add(

                    Song(
                        id,
                        title,
                        artist,
                        duration,

                        Uri.parse(
                            "content://media/external/audio/albumart/$albumId"
                        ),

                        Uri.withAppendedPath(
                            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                            id.toString()
                        ).toString()
                    )

                )

            }

        }


        displayList.addAll(
            fullSongList
        )


        if (MusicService.songList.isEmpty()) {

            MusicService.songList =
                ArrayList(fullSongList)

        }



        adapter =
            MusicAdapter(

                displayList,


                { position ->


                    val song =
                        displayList[position]


                    MusicService.songList =
                        fullSongList


                    MusicService.position =
                        fullSongList.indexOf(song)



                    requireContext()
                        .startService(

                            Intent(
                                requireContext(),
                                MusicService::class.java
                            ).setAction(
                                MusicService.ACTION_PLAY_NEW
                            )

                        )



                    val toastView =
                        layoutInflater.inflate(
                            R.layout.toast_song_playing,
                            binding.root,
                            false
                        )


                    Glide.with(this)

                        .load(song.albumArt)

                        .placeholder(R.drawable.app_logo)

                        .error(R.drawable.app_logo)

                        .into(
                            toastView.findViewById<ImageView>(
                                R.id.imgAlbum
                            )
                        )



                    toastView
                        .findViewById<TextView>(
                            R.id.txtTitle
                        ).text =
                        song.title



                    Toast(requireContext()).apply {

                        duration =
                            Toast.LENGTH_SHORT

                        view =
                            toastView

                        show()

                    }

                },



                { selectedSongs ->

                    isSongSelectionActive = selectedSongs.isNotEmpty()
                    isSelectionMode = isSongSelectionActive

                    selectedSongList.clear()
                    selectedSongList.addAll(selectedSongs)

                    if (isSongSelectionActive) {

                        binding.layoutSelectionActions.visibility = View.VISIBLE

                        (activity as? MainActivity)?.hideShuffleFab()

                        val currentAdapter =
                            binding.rvSongs.adapter as? MusicAdapter

                        binding.btnSelectAllSongs.text =
                            if (currentAdapter?.isAllSelected() == true)
                                "Unselect All"
                            else
                                "Select All"

                    } else {

                        binding.layoutSelectionActions.visibility = View.GONE

                        binding.btnSelectAllSongs.text = "Select All"

                        (activity as? MainActivity)?.showShuffleFab()

                    }

                }

            )


        binding.rvSongs.adapter =
            adapter

    }


    private fun scrollToCurrentSong() {


        if (displayList.isEmpty()) return


        if (
            MusicService.Companion.songList.isEmpty()
        ) return


        if (
            MusicService.Companion.position >=
            MusicService.Companion.songList.size
        ) return



        val currentSong =
            MusicService.Companion.songList[
                MusicService.Companion.position
            ]


        val index =
            displayList.indexOfFirst {

                it.id == currentSong.id

            }


        if (index != -1) {


            binding.rvSongs.post {


                binding.rvSongs.smoothScrollToPosition(
                    index
                )

            }

        }

    }

    override fun onDestroyView() {

        super.onDestroyView()

        MusicService.Companion.songChangeCallback =
            null



        _binding =
            null

    }
}