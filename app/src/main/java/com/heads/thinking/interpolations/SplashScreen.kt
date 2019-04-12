package com.heads.thinking.interpolations

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle

class SplashScreen : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
