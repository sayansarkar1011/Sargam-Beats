package com.syn10.sargambeats.ui.activity

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.syn10.sargambeats.R


class SplashActivity : AppCompatActivity() {


    private val splashTime: Long =
        2000


    companion object {

        private var splashAlreadyShown =
            false

    }


    override fun onCreate(
        savedInstanceState: Bundle?
    ) {


        val prefs =
            getSharedPreferences(
                "theme_pref",
                MODE_PRIVATE
            )


        val night =
            prefs.getBoolean(
                "night",
                false
            )


        AppCompatDelegate
            .setDefaultNightMode(

                if (night)

                    AppCompatDelegate.MODE_NIGHT_YES

                else

                    AppCompatDelegate.MODE_NIGHT_NO

            )


        super.onCreate(
            savedInstanceState
        )


        if (splashAlreadyShown) {


            openMainActivity()


            return

        }


        splashAlreadyShown =
            true


        enableEdgeToEdge()


        setContentView(
            R.layout.activity_splash
        )


        // Disable back press on splash screen

        onBackPressedDispatcher.addCallback(

            this,

            object : OnBackPressedCallback(true) {


                override fun handleOnBackPressed() {

                    // do nothing

                }

            }

        )


        Handler(
            Looper.getMainLooper()
        ).postDelayed(
            {


                openMainActivity()


            },
            splashTime
        )

    }


    private fun openMainActivity() {


        startActivity(

            Intent(
                this,
                MainActivity::class.java
            )

        )


        finish()

    }

}