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

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MatchesActivity : AppCompatActivity() {

    private lateinit var repository: ScoreRepository
    private lateinit var matchesAdapter: ArrayAdapter<String>
    private lateinit var matches: MutableList<Match>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_matches)

        // Initialize repository
        val databaseHelper = ScoreDatabaseHelper(this)
        repository = ScoreRepository(databaseHelper)

        val matchesListView: ListView = findViewById(R.id.matchesListView)

        // Fetch matches from the repository
        matches = repository.getAllMatches().toMutableList()

        // Format matches for display
        val formattedMatches = matches.map { match ->
            val startTime = DateUtils.formatTimestampToDateTime(match.startTime)
            //val endTime = match.endTime?.let { DateUtils.formatTimestampToDateTime(it) } ?: "进行中"
            val endTime = match.endTime?.let {
                if (it < DateUtils.parseDateToTimestamp("2024-12-01")) ""
                else DateUtils.formatTimestampToDateTime(it)
            } ?: "进行中"
            val redTeamScore = match.redTeamScore
            val blueTeamScore = match.blueTeamScore
            "开始: $startTime\n结束: $endTime\n红队得分: $redTeamScore\n蓝队得分: $blueTeamScore"
        }.toMutableList()

        // Set up adapter
        matchesAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, formattedMatches)
        matchesListView.adapter = matchesAdapter

        // Handle item click
        matchesListView.setOnItemClickListener { _, _, position, _ ->
            val selectedMatch = matches[position]
            val intent = Intent(this, ScoresActivity::class.java)
            intent.putExtra("match_start_time", selectedMatch.startTime)
            startActivity(intent)
        }

        // Handle item long click for deletion
        matchesListView.setOnItemLongClickListener { _, _, position, _ ->
            val selectedMatch = matches[position]
            showDeleteConfirmationDialog(selectedMatch, position)
            true
        }
    }

    private fun showDeleteConfirmationDialog(match: Match, position: Int) {
        AlertDialog.Builder(this)
            .setTitle("删除比赛")
            .setMessage("确定要删除这场比赛及其计分记录吗？")
            .setPositiveButton("删除") { _, _ ->
                deleteMatch(match, position)
            }
            .setNegativeButton("取消", null)
            .show()
    }

    private fun deleteMatch(match: Match, position: Int) {
        // Delete match and its associated scores from the database
        repository.deleteMatch(match.startTime)

        // Remove match from the list and update the adapter
        matches.removeAt(position)
        matchesAdapter.remove(matchesAdapter.getItem(position))

        Toast.makeText(this, "比赛及其计分记录已删除", Toast.LENGTH_SHORT).show()
    }
}