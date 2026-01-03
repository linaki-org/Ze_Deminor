package org.linaki.thedeminor

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.GridLayout
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.core.net.toUri


class MainActivity : ComponentActivity() {
    lateinit var btn_start: Button
    lateinit var btn_preferences: Button
    lateinit var btn_credits: Button

    private lateinit var mines_field: GridLayout
    lateinit var top_text: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        openMenu()
    }

    fun openMenu() {
        setContentView(R.layout.menu)

        btn_start = findViewById(R.id.btn_start)
        btn_preferences = findViewById(R.id.btn_preferences)
        btn_credits = findViewById(R.id.btn_credits)

        btn_preferences.setOnClickListener(::openPreferences)
        btn_start.setOnClickListener{startGame()}
        btn_credits.setOnClickListener { openCredits() }
    }
    fun openPreferences(view: View) {
        setContentView(R.layout.preferences)
    }

    fun openCredits() {
        val browserIntent = Intent(Intent.ACTION_VIEW, "http://www.linaki.org".toUri())
        startActivity(browserIntent)
    }

    fun startGame() {
        setContentView(R.layout.game)
        mines_field=findViewById(R.id.mines_field)
        top_text=findViewById(R.id.top_text)
        val deminorGame= DeminorGame(this, this)
        deminorGame.rows=30
        deminorGame.cols=30
        deminorGame.totalBombs=150
        deminorGame.generateField()

        deminorGame.drawField(mines_field)
        deminorGame.openHarmlessCell()
        deminorGame.updateTopText()

    }
}
