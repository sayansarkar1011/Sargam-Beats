package com.syn10.sargambeats.ui.fragment

import android.content.Intent
import com.google.android.material.color.MaterialColors
import android.net.Uri
import android.content.res.ColorStateList
import androidx.core.widget.ImageViewCompat
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.SeekBar
import com.syn10.sargambeats.viewmodel.FavouriteViewModel
import android.widget.TextView
import android.widget.Toast
import android.graphics.Color
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.syn10.sargambeats.service.MusicService
import com.syn10.sargambeats.R
import com.syn10.sargambeats.model.Song
import com.syn10.sargambeats.databinding.FragmentPlayingBinding
import com.syn10.sargambeats.ui.activity.MainActivity
import java.util.Locale
import java.util.concurrent.TimeUnit
import androidx.lifecycle.ViewModelProvider
import com.syn10.sargambeats.database.MusicDatabase
import com.syn10.sargambeats.repository.FavouriteRepository
import com.syn10.sargambeats.viewmodel.FavouriteViewModelFactory
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import com.syn10.sargambeats.database.FavouriteEntity

class FragmentPlaying : Fragment(R.layout.fragment_playing) {

    private var _binding: FragmentPlayingBinding? = null
    private val binding get() = _binding!!

    private lateinit var favouriteViewModel: FavouriteViewModel
    private var isFavourite = false

    private var isShareOn =
        false

    private val activeColor = Color.parseColor("#E1170A")

    // ================= LIFECYCLE =================

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentPlayingBinding.bind(view)


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


        setupControls()

    }

    override fun onStart() {
        super.onStart()

        MusicService.Companion.playingUiCallback = { song, playing, pos, dur ->
            activity?.runOnUiThread {

                val b = _binding ?: return@runOnUiThread

                b.playerTvSongTitle.text = song.title
                b.playerTvArtistName.text = song.artist

                Glide.with(this)
                    .load(song.albumArt)
                    .placeholder(R.drawable.app_logo)
                    .error(R.drawable.app_logo)
                    .fallback(R.drawable.app_logo)
                    .into(b.playerIvAlbumArt)

                b.playerSeekBar.max = dur
                b.playerSeekBar.progress = pos

                b.playerTvStartTime.text = formatTime(pos)
                b.playerTvEndTime.text = formatTime(dur)

                b.playerFabPlayPause.setImageResource(
                    if (playing) R.drawable.outline_pause_24 else R.drawable.outline_play_arrow_24
                )
                checkFavouriteStatus(song)
            }
        }

        MusicService.Companion.shuffleCallback = { _ ->

            activity?.runOnUiThread {

                if (_binding == null) return@runOnUiThread
                updateShuffleButton()
            }
        }
        MusicService.Companion.repeatCallback = {
            activity?.runOnUiThread {

                if (_binding == null) return@runOnUiThread

                updateRepeatButton()
            }
        }

        MusicService.Companion.equalizerCallback = {

            activity?.runOnUiThread {

                if (_binding == null) return@runOnUiThread

                updateEqualizerButton()
            }
        }
    }

    override fun onResume() {

        super.onResume()


        (activity as? MainActivity)
            ?.hideShuffleFab()


        (activity as? MainActivity)
            ?.hideThemeFab()


        syncFromService()


        updateShuffleButton()
        updateRepeatButton()
        updateEqualizerButton()
        updateShareButton()

    }

    override fun onStop() {
        super.onStop()

        MusicService.Companion.playingUiCallback = null
        MusicService.Companion.shuffleCallback = null
        MusicService.Companion.repeatCallback = null
        MusicService.Companion.equalizerCallback = null
    }

    // ================= SYNC =================

    private fun syncFromService() {
        val mp = MusicService.Companion.mediaPlayer ?: return
        if (MusicService.Companion.songList.isEmpty()) return

        updateUI(
            song = MusicService.Companion.songList[MusicService.Companion.position],
            playing = mp.isPlaying,
            pos = mp.currentPosition,
            dur = mp.duration
        )
    }

    // ================= UI =================

    private fun updateUI(
        song: Song,
        playing: Boolean,
        pos: Int,
        dur: Int
    ) {
        val b = _binding ?: return

        b.playerTvSongTitle.text = song.title
        b.playerTvArtistName.text = song.artist

        Glide.with(this)
            .load(song.albumArt)
            .placeholder(R.drawable.app_logo)
            .error(R.drawable.app_logo)
            .fallback(R.drawable.app_logo)
            .into(b.playerIvAlbumArt)

        b.playerSeekBar.max = dur
        b.playerSeekBar.progress = pos

        b.playerTvStartTime.text = formatTime(pos)
        b.playerTvEndTime.text = formatTime(dur)

        b.playerFabPlayPause.setImageResource(
            if (playing) R.drawable.outline_pause_24 else R.drawable.outline_play_arrow_24
        )
        checkFavouriteStatus(song)
    }

    // ================= CONTROLS =================

    private fun setupControls() {


        // Favourite

        binding.playerIvFavourite.setOnClickListener {


            if (MusicService.Companion.songList.isEmpty()) {
                return@setOnClickListener
            }


            val song =
                MusicService.Companion.songList[
                    MusicService.Companion.position
                ]


            val favSong =
                FavouriteEntity(
                    songId = song.id,
                    title = song.title,
                    artist = song.artist,
                    duration = song.duration,
                    albumArt = song.albumArt.toString(),
                    dataPath = song.dataPath
                )


            if (isFavourite) {

                favouriteViewModel.removeFavourite(favSong)

                isFavourite = false

                Toast.makeText(
                    requireContext(),
                    "Removed from Favourites",
                    Toast.LENGTH_SHORT
                ).show()


                val normalColor = MaterialColors.getColor(
                    binding.root,
                    com.google.android.material.R.attr.colorOnSurface
                )


                binding.playerIvFavourite.setColorFilter(
                    normalColor
                )


            } else {

                favouriteViewModel.addFavourite(favSong)

                isFavourite = true

                Toast.makeText(
                    requireContext(),
                    "Added to Favourites",
                    Toast.LENGTH_SHORT
                ).show()

                binding.playerIvFavourite.setColorFilter(
                    activeColor
                )
            }

        }




        // Play / Pause
        binding.playerFabPlayPause.setOnClickListener {
            requireContext().startService(
                Intent(requireContext(), MusicService::class.java)
                    .setAction(
                        if (MusicService.Companion.isPlaying)
                            MusicService.Companion.ACTION_PAUSE
                        else
                            MusicService.Companion.ACTION_PLAY
                    )
            )
        }

        // Next
        binding.playerIvNext.setOnClickListener {
            requireContext().startService(
                Intent(requireContext(), MusicService::class.java)
                    .setAction(MusicService.Companion.ACTION_NEXT)
            )
        }

        // Previous
        binding.playerIvPrev.setOnClickListener {
            requireContext().startService(
                Intent(requireContext(), MusicService::class.java)
                    .setAction(MusicService.Companion.ACTION_PREV)
            )
        }

        // shuffle
        binding.playerIvShuffle.setOnClickListener {

            if (!MusicService.Companion.isShuffleOn) {

                MusicService.Companion.isShuffleOn = true

                if (!MusicService.Companion.isPlaying &&
                    MusicService.Companion.songList.isNotEmpty()
                ) {

                    MusicService.Companion.position =
                        (MusicService.Companion.songList.indices).random()

                    requireContext().startService(
                        Intent(requireContext(), MusicService::class.java)
                            .setAction(MusicService.Companion.ACTION_PLAY_NEW)
                    )

                } else {

                    MusicService.Companion.notifyShuffleState()
                }

            } else {

                MusicService.Companion.isShuffleOn = false

                MusicService.Companion.shuffleHistory.clear()

                MusicService.Companion.notifyShuffleState()
            }



            updateShuffleButton()
        }

        // Repeat
        binding.playerIvRepeat.setOnClickListener {

            MusicService.Companion.isRepeatOn = !MusicService.Companion.isRepeatOn

            MusicService.Companion.notifyRepeatState()

            Toast.makeText(
                requireContext(),
                if (MusicService.Companion.isRepeatOn)
                    "Repeat On"
                else
                    "Repeat Off",
                Toast.LENGTH_SHORT
            ).show()

            updateRepeatButton()
        }

        // Share

        binding.playerIvShare.setOnClickListener {


            if (
                MusicService.Companion.songList
                    .isNotEmpty()
            ) {


                isShareOn =
                    true


                updateShareButton()


                shareSong(

                    MusicService.Companion
                        .songList
                        [MusicService.Companion.position]

                )

            }

        }

        // Equalizer

        binding.playerIvEqualizer.setOnClickListener {

            MusicService.Companion.isEqualizerOn = true

            MusicService.Companion.equalizer?.enabled = true
            MusicService.Companion.bassBoost?.enabled = true
            MusicService.Companion.loudnessEnhancer?.enabled = true

            updateEqualizerButton()

            showEqualizer()
        }

        // Seek
        binding.playerSeekBar.setOnSeekBarChangeListener(
            object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    if (fromUser) {
                        requireContext().startService(
                            Intent(requireContext(), MusicService::class.java)
                                .setAction(MusicService.Companion.ACTION_SEEK)
                                .putExtra("pos", progress)
                        )
                    }
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                override fun onStopTrackingTouch(seekBar: SeekBar?) {}
            }
        )
    }

    private fun updateShuffleButton() {

        val normalColor = MaterialColors.getColor(
            binding.root,
            com.google.android.material.R.attr.colorOnSurface
        )

        val color =
            if (MusicService.Companion.isShuffleOn)
                activeColor
            else
                normalColor


        ImageViewCompat.setImageTintList(
            binding.playerIvShuffle,
            ColorStateList.valueOf(color)
        )
    }

    private fun updateRepeatButton() {

        val normalColor = MaterialColors.getColor(
            binding.root,
            com.google.android.material.R.attr.colorOnSurface
        )

        val color =
            if (MusicService.Companion.isRepeatOn)
                activeColor
            else
                normalColor


        ImageViewCompat.setImageTintList(
            binding.playerIvRepeat,
            ColorStateList.valueOf(color)
        )
    }

    private fun updateEqualizerButton() {

        val normalColor = MaterialColors.getColor(
            binding.root,
            com.google.android.material.R.attr.colorOnSurface
        )

        val color =
            if (MusicService.Companion.isEqualizerOn)
                activeColor
            else
                normalColor


        ImageViewCompat.setImageTintList(
            binding.playerIvEqualizer,
            ColorStateList.valueOf(color)
        )
    }

    private fun updateShareButton() {

        val normalColor = MaterialColors.getColor(
            binding.root,
            com.google.android.material.R.attr.colorOnSurface
        )

        val color =
            if (isShareOn)
                activeColor
            else
                normalColor


        ImageViewCompat.setImageTintList(
            binding.playerIvShare,
            ColorStateList.valueOf(color)
        )
    }

    private fun checkFavouriteStatus(song: Song) {

        lifecycleScope.launch {

            isFavourite =
                favouriteViewModel.isFavourite(song.id)


            val normalColor = MaterialColors.getColor(
                binding.root,
                com.google.android.material.R.attr.colorOnSurface
            )


            binding.playerIvFavourite.setColorFilter(
                if (isFavourite)
                    activeColor
                else
                    normalColor
            )

        }

    }

    private fun showEqualizer() {

        val dialog = BottomSheetDialog(requireContext())

        val view = layoutInflater.inflate(
            R.layout.bottom_equalizer,
            null
        )

        dialog.setContentView(view)


        val bass =
            view.findViewById<SeekBar>(R.id.seekBass)

        val treble =
            view.findViewById<SeekBar>(R.id.seekTreble)

        val volume =
            view.findViewById<SeekBar>(R.id.seekVolumeBoost)


        val bassPercent =
            view.findViewById<TextView>(R.id.tvBassPercent)

        val treblePercent =
            view.findViewById<TextView>(R.id.tvTreblePercent)

        val volumePercent =
            view.findViewById<TextView>(R.id.tvVolumePercent)


        val bandContainer =
            view.findViewById<LinearLayout>(
                R.id.equalizerBandsContainer
            )


        // Restore saved values

        bass.progress =
            MusicService.Companion.savedBass

        treble.progress =
            MusicService.Companion.savedTreble

        volume.progress =
            MusicService.Companion.savedVolumeBoost


        bassPercent.text =
            "${MusicService.Companion.savedBass / 10}%"

        treblePercent.text =
            "${MusicService.Companion.savedTreble / 10}%"

        volumePercent.text =
            "${MusicService.Companion.savedVolumeBoost / 20}%"



        // Bass Control

        bass.setOnSeekBarChangeListener(
            object : SeekBar.OnSeekBarChangeListener {


                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {

                    MusicService.Companion.savedBass =
                        progress


                    bassPercent.text =
                        "${progress / 10}%"


                    MusicService.Companion.bassBoost
                        ?.setStrength(
                            progress.toShort()
                        )
                }


                override fun onStartTrackingTouch(s: SeekBar?) {}

                override fun onStopTrackingTouch(s: SeekBar?) {}
            }
        )



        // Treble Control

        treble.setOnSeekBarChangeListener(
            object : SeekBar.OnSeekBarChangeListener {


                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {

                    MusicService.Companion.savedTreble =
                        progress


                    treblePercent.text =
                        "${progress / 10}%"


                    val eq =
                        MusicService.Companion.equalizer ?: return


                    val lastBand =
                        (eq.numberOfBands - 1).toShort()


                    val level =
                        ((progress / 1000f) *
                                eq.bandLevelRange[1])
                            .toInt()
                            .toShort()


                    eq.setBandLevel(
                        lastBand,
                        level
                    )


                    MusicService.Companion.savedBands[lastBand] =
                        level
                }


                override fun onStartTrackingTouch(s: SeekBar?) {}

                override fun onStopTrackingTouch(s: SeekBar?) {}
            }
        )



        // Volume Control

        volume.setOnSeekBarChangeListener(
            object : SeekBar.OnSeekBarChangeListener {


                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {

                    MusicService.Companion.savedVolumeBoost =
                        progress


                    volumePercent.text =
                        "${progress / 20}%"


                    MusicService.Companion.loudnessEnhancer
                        ?.setTargetGain(
                            progress
                        )
                }


                override fun onStartTrackingTouch(s: SeekBar?) {}

                override fun onStopTrackingTouch(s: SeekBar?) {}
            }
        )



        // Frequency Bands

        val eq =
            MusicService.Companion.equalizer


        if (eq != null) {


            val min =
                eq.bandLevelRange[0]


            val max =
                eq.bandLevelRange[1]


            for (i in 0 until eq.numberOfBands) {


                val band =
                    i.toShort()


                val seek =
                    SeekBar(requireContext())


                seek.rotation = -90f


                seek.max =
                    max - min


                seek.progress =
                    eq.getBandLevel(band) - min


                seek.layoutParams =
                    LinearLayout.LayoutParams(
                        160,
                        120
                    )


                seek.setOnSeekBarChangeListener(
                    object : SeekBar.OnSeekBarChangeListener {


                        override fun onProgressChanged(
                            seekBar: SeekBar?,
                            progress: Int,
                            fromUser: Boolean
                        ) {

                            val level =
                                (progress + min)
                                    .toShort()


                            eq.setBandLevel(
                                band,
                                level
                            )


                            MusicService.Companion.savedBands[band] =
                                level
                        }


                        override fun onStartTrackingTouch(s: SeekBar?) {}

                        override fun onStopTrackingTouch(s: SeekBar?) {}
                    }
                )


                bandContainer.addView(seek)
            }
        }



        dialog.setOnDismissListener {

            MusicService.Companion.isEqualizerOn = false

            updateEqualizerButton()
        }


        dialog.show()
    }

    private fun shareSong(song: Song) {

        val uri = song.albumArt

        val audioUri = Uri.parse(song.dataPath)

        val shareIntent = Intent(Intent.ACTION_SEND).apply {

            type = "audio/*"

            putExtra(
                Intent.EXTRA_STREAM,
                audioUri
            )

            addFlags(
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        }

        startActivity(
            Intent.createChooser(
                shareIntent,
                "Share Song"
            )
        )


        binding.root.postDelayed(
            {


                isShareOn =
                    false


                updateShareButton()


            },
            1000
        )
    }

    private fun formatTime(ms: Int): String {
        val min = TimeUnit.MILLISECONDS.toMinutes(ms.toLong())
        val sec = TimeUnit.MILLISECONDS.toSeconds(ms.toLong()) % 60
        return String.Companion.format(Locale.getDefault(), "%02d:%02d", min, sec)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}