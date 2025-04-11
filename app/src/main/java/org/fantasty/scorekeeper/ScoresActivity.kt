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

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity

class ScoresActivity : AppCompatActivity() {

    private lateinit var repository: ScoreRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scores)

        // Initialize repository
        val databaseHelper = ScoreDatabaseHelper(this)
        repository = ScoreRepository(databaseHelper)

        val scoresListView: ListView = findViewById(R.id.scoresListView)

        // Get match start time from intent
        val matchStartTime = intent.getLongExtra("match_start_time", -1)

        // Fetch scores for the selected match from the repository
        val scores = repository.getScoresForMatch(matchStartTime)

        // Format scores for display
        val formattedScores = scores.map { score ->
            val timestamp = DateUtils.formatTimestampToDateTime(score.timestamp)
            "队伍: ${score.team}\n分数: ${score.score}\n时间: $timestamp"
        }

        // Display scores in a ListView
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, formattedScores)
        scoresListView.adapter = adapter
    }
}