package com.syn10.sargambeats.ui.activity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.syn10.sargambeats.ui.fragment.FragmentSongs
import com.syn10.sargambeats.service.MusicService
import com.syn10.sargambeats.R
import com.syn10.sargambeats.model.Song
import com.syn10.sargambeats.adapter.ViewPagerAdapter
import com.syn10.sargambeats.databinding.ActivityMainBinding
import kotlin.math.abs
import com.syn10.sargambeats.ui.fragment.FragmentPlaylist

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var lastMiniSongId: Long = -1

    private var isNightMode =
        false

    private var isSearchMode = false
    private var searchEditText: EditText? = null
    private var lastNonPlayingTab = 0

    // ================= LIFECYCLE =================

    private fun requestPermissions() {

        val permissions = mutableListOf<String>()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {

            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                permissions.add(Manifest.permission.POST_NOTIFICATIONS)
            }

            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.READ_MEDIA_AUDIO
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                permissions.add(Manifest.permission.READ_MEDIA_AUDIO)
            }

        } else {

            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }

        if (permissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                this,
                permissions.toTypedArray(),
                1001
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {


        val prefs =
            getSharedPreferences(
                "theme_pref",
                MODE_PRIVATE
            )


        isNightMode =
            prefs.getBoolean(
                "night",
                false
            )


        AppCompatDelegate
            .setDefaultNightMode(

                if (isNightMode)

                    AppCompatDelegate.MODE_NIGHT_YES

                else

                    AppCompatDelegate.MODE_NIGHT_NO

            )


        super.onCreate(
            savedInstanceState
        )


        requestPermissions()


        binding =
            ActivityMainBinding.inflate(
                layoutInflater
            )


        setContentView(
            binding.root
        )


        updateThemeIcon()




        setSupportActionBar(
            binding.mainToolbar
        )


        binding.mainToolbar.removeAllViews()


        layoutInflater.inflate(
            R.layout.toolbar_main_content,
            binding.mainToolbar,
            true
        )

        // ViewPager
        binding.viewPager.adapter = ViewPagerAdapter(this)
        binding.viewPager.currentItem = 0
        binding.viewPager.offscreenPageLimit = 3
        (binding.viewPager.getChildAt(0) as? RecyclerView)?.itemAnimator = null

        hideMiniPlayer()
        setViewPagerBottomMargin(80)

        // Bottom nav
        binding.mainIvNavSongs.setOnClickListener {

            updateToolbarByPosition(0)
            selectTab(0)

        }


        binding.mainIvNavPlayer.setOnClickListener {

            updateToolbarByPosition(1)
            selectTab(1)

        }


        binding.mainIvNavPlaylists.setOnClickListener {

            updateToolbarByPosition(2)
            selectTab(2)

        }


        binding.mainIvNavFavourites.setOnClickListener {

            updateToolbarByPosition(3)
            selectTab(3)

        }

        bindToolbarSearchIcon()
        setupMiniPlayerControls()
        updateBottomNav(0)



        binding.mainFabTheme
            .setOnClickListener {


                isNightMode =
                    !isNightMode


                getSharedPreferences(
                    "theme_pref",
                    MODE_PRIVATE
                )
                    .edit()
                    .putBoolean(
                        "night",
                        isNightMode
                    )
                    .apply()


                AppCompatDelegate
                    .setDefaultNightMode(

                        if (isNightMode)

                            AppCompatDelegate.MODE_NIGHT_YES

                        else

                            AppCompatDelegate.MODE_NIGHT_NO

                    )





                updateThemeIcon()

            }


        // Page sync



        binding.viewPager.registerOnPageChangeCallback(

            object : ViewPager2.OnPageChangeCallback() {


                override fun onPageSelected(
                    position: Int
                ) {


                    updateToolbarByPosition(
                        position
                    )


                    updateBottomNav(
                        position
                    )


                    if (position == 1) {

                        hideThemeFab()

                    } else {

                        showThemeFab()

                    }


                    // Mini player hide in Playing tab
                    if (position == 1) {

                        hideMiniPlayer()
                        setViewPagerBottomMargin(80)

                    } else {

                        if (MusicService.Companion.mediaPlayer != null) {

                            showMiniPlayer()
                            setViewPagerBottomMargin(152)

                        } else {

                            hideMiniPlayer()
                            setViewPagerBottomMargin(80)

                        }

                        lastNonPlayingTab = position
                    }

                }

            }

        )


        // Back handling
        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {

                override fun handleOnBackPressed() {


                    if (isSearchMode) {


                        exitSearchMode()


                        return

                    }


                    if (
                        binding.viewPager.currentItem == 0
                    ) {


                        isEnabled = false


                        onBackPressedDispatcher
                            .onBackPressed()


                        isEnabled = true


                        return

                    }



                    if (
                        binding.viewPager.currentItem == 1
                    ) {


                        selectTab(
                            lastNonPlayingTab
                        )


                        return

                    }



                    isEnabled = false


                    onBackPressedDispatcher
                        .onBackPressed()

                }

            }
        )
    }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(
            requestCode,
            permissions,
            grantResults
        )

        if (requestCode == 1001) {
            recreate()
        }
    }

    /**
     * 🔑 ONLY SOURCE OF TRUTH
     * Service → Mini Player UI
     */
    override fun onStart() {
        super.onStart()

        // Always keep callback attached
        MusicService.Companion.uiCallback = { song, playing, pos, dur ->
            runOnUiThread {
                updateMiniPlayer(song, playing, pos, dur)
            }
        }

        // Instantly restore mini player if a song is already playing
        MusicService.Companion.mediaPlayer?.let { mp ->
            if (MusicService.Companion.songList.isNotEmpty()) {
                updateMiniPlayer(
                    MusicService.Companion.songList[MusicService.Companion.position],
                    mp.isPlaying,
                    mp.currentPosition,
                    mp.duration
                )
            }
        }
    }

    override fun onPause() {
        super.onPause()
        // Do not clear callback here
    }

    // ================= MINI PLAYER =================

    private fun showMiniPlayer() {
        binding.miniPlayerContainer.root.visibility = View.VISIBLE
    }

    private fun hideMiniPlayer() {
        binding.miniPlayerContainer.root.visibility = View.GONE
    }

    private fun setViewPagerBottomMargin(dp: Int) {
        val params = binding.viewPager.layoutParams as ViewGroup.MarginLayoutParams
        params.bottomMargin = (dp * resources.displayMetrics.density).toInt()
        binding.viewPager.layoutParams = params
    }

    private fun updateMiniPlayer(song: Song, isPlaying: Boolean, pos: Int, dur: Int) {
        val root = binding.miniPlayerContainer.root

        val title = root.findViewById<TextView>(R.id.miniTvTitle)
        val artist = root.findViewById<TextView>(R.id.miniTvArtist)
        val seekBar = root.findViewById<SeekBar>(R.id.miniSeekBar)
        val playBtn = root.findViewById<ImageView>(R.id.miniBtnPlayPause)

        // update text only when changed
        if (title.text != song.title) {
            title.text = song.title
        }

        if (artist.text != song.artist) {
            artist.text = song.artist
        }


        // album reload only when song changes
        if (lastMiniSongId != song.id) {

            lastMiniSongId = song.id

            Glide.with(this)
                .load(song.albumArt)
                .placeholder(R.drawable.app_logo)
                .error(R.drawable.app_logo)
                .fallback(R.drawable.app_logo)
                .dontAnimate()
                .into(root.findViewById(R.id.miniIvAlbum))
        }


        // avoid unnecessary seekbar refresh
        if (seekBar.max != dur) {
            seekBar.max = dur
        }

        if (abs(seekBar.progress - pos) > 500) {
            seekBar.progress = pos
        }


        val icon = if (isPlaying)
            R.drawable.outline_pause_24
        else
            R.drawable.outline_play_arrow_24

        if (playBtn.tag != icon) {
            playBtn.setImageResource(icon)
            playBtn.tag = icon
        }


        if (
            root.visibility != View.VISIBLE &&
            binding.viewPager.currentItem != 1 &&
            song.id != -1L
        ) {

            showMiniPlayer()
            setViewPagerBottomMargin(152)

        }
    }

    /**
     * ⚠️ IMPORTANT
     * Mini player NEVER changes UI state by itself
     * It ONLY sends actions to MusicService
     */
    private fun setupMiniPlayerControls() {
        val root = binding.miniPlayerContainer.root

        // open full player
        root.setOnClickListener { selectTab(1) }

        // seek
        root.findViewById<SeekBar>(R.id.miniSeekBar)
            .setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(sb: SeekBar?, progress: Int, fromUser: Boolean) {
                    if (fromUser) {
                        startService(
                            Intent(this@MainActivity, MusicService::class.java)
                                .setAction(MusicService.Companion.ACTION_SEEK)
                                .putExtra("pos", progress)
                        )
                    }
                }
                override fun onStartTrackingTouch(sb: SeekBar?) {}
                override fun onStopTrackingTouch(sb: SeekBar?) {}
            })

        // ▶️ PLAY / PAUSE
        root.findViewById<ImageView>(R.id.miniBtnPlayPause).setOnClickListener {
            startService(
                Intent(this, MusicService::class.java)
                    .setAction(
                        if (MusicService.Companion.isPlaying)
                            MusicService.Companion.ACTION_PAUSE
                        else
                            MusicService.Companion.ACTION_PLAY
                    )
            )
        }

        // ⏭ NEXT  ✅ ADDED
        root.findViewById<ImageView>(R.id.miniBtnNext)?.setOnClickListener {
            startService(
                Intent(this, MusicService::class.java)
                    .setAction(MusicService.Companion.ACTION_NEXT)
            )
        }

        // ⏮ PREV  ✅ ADDED
        root.findViewById<ImageView>(R.id.miniBtnPrev)?.setOnClickListener {
            startService(
                Intent(this, MusicService::class.java)
                    .setAction(MusicService.Companion.ACTION_PREV)
            )
        }
    }


    // ================= TOOLBAR =================



    fun updateToolbar(
        title: String,
        showSearch: Boolean
    ) {


        val title1 =
            binding.mainToolbar
                .findViewById<TextView>(
                    R.id.mini_tv_app_title_sargam
                )


        val title2 =
            binding.mainToolbar
                .findViewById<TextView>(
                    R.id.mini_tv_app_title_beats
                )


        val search =
            binding.mainToolbar
                .findViewById<ImageView>(
                    R.id.main_iv_search
                )


        if (title == "SargamBeats") {


            title1?.text =
                "Sargam "


            title2?.text =
                "Beats"


            title2?.visibility =
                View.VISIBLE


        } else {


            title1?.text =
                title


            title2?.text =
                ""


            title2?.visibility =
                View.GONE

        }


        search?.visibility =
            if (showSearch)
                View.VISIBLE
            else
                View.GONE

    }
    // ================= SEARCH =================

    private fun bindToolbarSearchIcon() {
        binding.mainToolbar.findViewById<ImageView>(R.id.main_iv_search)
            ?.setOnClickListener {
                if (isSearchMode) exitSearchMode() else enterSearchMode()
            }
    }

    private fun enterSearchMode() {
        isSearchMode = true

        searchEditText = EditText(this).apply {
            hint = "Search songs or artist"
            background = null
            addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    FragmentSongs.Companion.filterSongs(s.toString())
                }
                override fun afterTextChanged(s: Editable?) {}
            })
        }

        binding.mainToolbar.removeAllViews()
        binding.mainToolbar.addView(searchEditText)
        searchEditText?.requestFocus()
        showKeyboard(searchEditText!!)
    }

    private fun exitSearchMode() {
        isSearchMode = false
        FragmentSongs.Companion.clearFilter()
        hideKeyboard()

        searchEditText = null
        binding.mainToolbar.removeAllViews()
        layoutInflater.inflate(R.layout.toolbar_main_content, binding.mainToolbar)
        bindToolbarSearchIcon()
    }

    private fun showKeyboard(view: View) {
        (getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager)
            .showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
    }

    private fun hideKeyboard() {
        (getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager)
            .hideSoftInputFromWindow(binding.root.windowToken, 0)
    }

    // ================= NAV =================


    private fun updateToolbarByPosition(
        position: Int
    ) {


        when (position) {


            0 -> updateToolbar(
                "SargamBeats",
                true
            )


            1 -> updateToolbar(
                "Playing Tab",
                false
            )


            2 -> updateToolbar(
                "Playlist",
                false
            )


            3 -> updateToolbar(
                "Favourite",
                false
            )

        }

    }

    fun selectTab(pos: Int) {

        if (binding.viewPager.currentItem != pos) {

            binding.viewPager.currentItem =
                pos

        }


        updateBottomNav(
            pos
        )

    }

    private fun updateBottomNav(position: Int) {
        val active = getColor(R.color.Theme_main)
        val inactive = getColor(R.color.material_gray)

        binding.mainIvNavSongs.setColorFilter(if (position == 0) active else inactive)
        binding.mainIvNavPlayer.setColorFilter(if (position == 1) active else inactive)
        binding.mainIvNavPlaylists.setColorFilter(if (position == 2) active else inactive)
        binding.mainIvNavFavourites.setColorFilter(if (position == 3) active else inactive)
    }

    // ================= FAB =================

    private fun updateThemeIcon() {


        binding.mainFabTheme
            .setImageResource(

                if (isNightMode)

                    R.drawable.outline_moon_stars_24

                else

                    R.drawable.outline_sunny_24

            )

    }

    fun showThemeFab() {

        binding.mainFabTheme.visibility =
            View.VISIBLE

    }


    fun hideThemeFab() {

        binding.mainFabTheme.visibility =
            View.GONE

    }

    fun showShuffleFab() {

        binding.mainFabContainer.visibility = View.VISIBLE

        binding.mainFabShuffle.setImageResource(R.drawable.shuffle_btn)

        binding.mainFabShuffle.setOnClickListener {

            // OFF -> ON
            if (!MusicService.Companion.isShuffleOn) {

                MusicService.Companion.isShuffleOn = true

                if (!MusicService.Companion.isPlaying &&
                    MusicService.Companion.songList.isNotEmpty()
                ) {

                    MusicService.Companion.position =
                        (MusicService.Companion.songList.indices).random()

                    startService(
                        Intent(this, MusicService::class.java)
                            .setAction(MusicService.Companion.ACTION_PLAY_NEW)
                    )

                } else {

                    MusicService.Companion.notifyShuffleState()

                }
            }

            // ON -> OFF
            else {

                MusicService.Companion.isShuffleOn = false

                MusicService.Companion.shuffleHistory.clear()

                MusicService.Companion.notifyShuffleState()
            }
        }
    }



    fun showAddPlaylistFab() {

        binding.mainFabContainer.visibility = View.VISIBLE

        binding.mainFabShuffle.setImageResource(
            R.drawable.add
        )


        binding.mainFabShuffle.setOnClickListener {

            val fragment =
                supportFragmentManager
                    .findFragmentByTag(
                        "f${binding.viewPager.currentItem}"
                    )


            if (fragment is FragmentPlaylist) {

                fragment.createPlaylistDialog()

            }

        }
    }

    fun hideShuffleFab() {
        binding.mainFabContainer.visibility = View.GONE
    }
    override fun onDestroy() {
        MusicService.Companion.uiCallback = null
        super.onDestroy()
    }
}