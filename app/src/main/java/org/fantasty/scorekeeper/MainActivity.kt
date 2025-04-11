/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Author: Xiaofang Liu
 * Email: 302979303@qq.com
 * Date: 2025/4/12
 */
package org.fantasty.scorekeeper

import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.KeyEvent
import android.view.MotionEvent
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.activity.viewModels
import java.util.Locale
import kotlin.math.abs

class MainActivity : AppCompatActivity() {

    private lateinit var redTeamScoreTextView: TextView
    private lateinit var blueTeamScoreTextView: TextView

    // Use ViewModel to manage scores
    //private val scoreViewModel: ScoreViewModel by viewModels()

    private val textToSpeechEngine: TextToSpeech by lazy {
        TextToSpeech(this,
            TextToSpeech.OnInitListener { status ->
                if (status == TextToSpeech.SUCCESS) {
                    textToSpeechEngine.language = Locale.CHINA
                }
            })
    }

    private lateinit var scoreViewModel: ScoreViewModel
    private lateinit var databaseHelper: ScoreDatabaseHelper

    // Variables to track touch events
    private var startX = 0f
    private var startY = 0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize database helper
        databaseHelper = ScoreDatabaseHelper(this)

        // Initialize repository and ViewModel
        val repository = ScoreRepository(databaseHelper)
        scoreViewModel = ScoreViewModel(repository)

        // Initialize views
        redTeamScoreTextView = findViewById(R.id.redTeamScoreTextView)
        blueTeamScoreTextView = findViewById(R.id.blueTeamScoreTextView)
        val matchesButton: ImageButton = findViewById(R.id.matchesButton)

        val redTeamIncrementButton: Button = findViewById(R.id.redTeamIncrementButton)
        val redTeamDecrementButton: Button = findViewById(R.id.redTeamDecrementButton)
        val blueTeamIncrementButton: Button = findViewById(R.id.blueTeamIncrementButton)
        val blueTeamDecrementButton: Button = findViewById(R.id.blueTeamDecrementButton)

        redTeamIncrementButton.isEnabled = false
        redTeamDecrementButton.isEnabled = false
        blueTeamIncrementButton.isEnabled = false
        blueTeamDecrementButton.isEnabled = false

        //val resetButton: Button = findViewById(R.id.resetButton)
        val startEndButton: Button = findViewById(R.id.resetButton)
        val announceButton: Button = findViewById(R.id.announceButton)

        // Set button click listeners
        redTeamIncrementButton.setOnClickListener {
            scoreViewModel.incrementRedTeamScore()
            updateScores()
        }
        redTeamDecrementButton.setOnClickListener {
            scoreViewModel.decrementRedTeamScore()
            updateScores()
        }
        blueTeamIncrementButton.setOnClickListener {
            scoreViewModel.incrementBlueTeamScore()
            updateScores()
        }
        blueTeamDecrementButton.setOnClickListener {
            scoreViewModel.decrementBlueTeamScore()
            updateScores()
        }
        /*
        resetButton.setOnClickListener {
            scoreViewModel.resetScores()
            updateScores()
        }*/
        // Set button click listeners
        startEndButton.setOnClickListener {
            if(scoreViewModel.isMatchGoing == false) {
                scoreViewModel.startMatch()
                redTeamIncrementButton.isEnabled = true
                redTeamDecrementButton.isEnabled = true
                blueTeamIncrementButton.isEnabled = true
                blueTeamDecrementButton.isEnabled = true
                startEndButton.text = getString(R.string.end_match)
                updateScores()
            }else{
                redTeamIncrementButton.isEnabled = false
                redTeamDecrementButton.isEnabled = false
                blueTeamIncrementButton.isEnabled = false
                blueTeamDecrementButton.isEnabled = false
                scoreViewModel.endMatch()
                startEndButton.text = getString(R.string.start_match)
            }
        }

        announceButton.setOnClickListener {
            announceScores()
        }

        // Handle matches button click
        matchesButton.setOnClickListener {
            val intent = Intent(this, MatchesActivity::class.java)
            startActivity(intent)
        }

        // Update initial scores
        updateScores()
    }
    private fun updateScores() {
        redTeamScoreTextView.text = String.format("%03d", scoreViewModel.redTeamScore)
        blueTeamScoreTextView.text = String.format("%03d", scoreViewModel.blueTeamScore)
    }

    private fun announceScores() {
        val message = "红队${scoreViewModel.redTeamScore}分，蓝队${scoreViewModel.blueTeamScore}分"
        textToSpeechEngine.speak(message, TextToSpeech.QUEUE_FLUSH, null, "tts1")
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()

    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        Log.d("KeyEvent", "Key pressed::: ${KeyEvent.keyCodeToString(keyCode)}")
        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP || keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            // 拦截音量键事件
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP || keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            // 拦截音量键事件
            announceScores()
            return true
        }
        return super.onKeyUp(keyCode, event)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        // Check if the tool type is TOOL_TYPE_MOUSE
        if (event.getToolType(0) == MotionEvent.TOOL_TYPE_MOUSE && scoreViewModel.isMatchGoing == true) {
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    // Record the starting position of the touch
                    startX = event.x
                    startY = event.y
                    return true // Indicate that the ACTION_DOWN event is handled
                }
                MotionEvent.ACTION_UP -> {
                    // Calculate the difference in X and Y positions
                    val endX = event.x
                    val endY = event.y
                    val deltaX = endX - startX
                    val deltaY = endY - startY

                    // Determine the direction of the swipe
                    if (abs(deltaX) > abs(deltaY)) {
                        // Horizontal swipe
                        if (deltaX > 0) {
                            // Swipe right
                            Log.e("Swipe ", "Right")
                            scoreViewModel.incrementBlueTeamScore()
                            updateScores()
                        } else {
                            // Swipe left
                            Log.e("Swipe ", "Left")
                            scoreViewModel.decrementBlueTeamScore()
                            updateScores()
                        }
                    } else {
                        // Vertical swipe
                        if (deltaY > 0) {
                            // Swipe down
                            Log.e("Swipe ", "Down")
                            scoreViewModel.incrementRedTeamScore()
                            updateScores()
                        } else {
                            // Swipe up
                            Log.e("Swipe ", "Up")
                            scoreViewModel.decrementRedTeamScore()
                            updateScores()
                        }
                    }
                    return true // Indicate that the ACTION_UP event is handled
                }
            }
        }
        return super.onTouchEvent(event) // Pass other events to the parent class
    }
}