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

class ScoreRepository(private val databaseHelper: ScoreDatabaseHelper) {

    fun startMatch(startTime: Long) {
        databaseHelper.startMatch(startTime)
    }

    fun endMatch(startTime: Long, endTime: Long, redTeamScore: Int, blueTeamScore: Int) {
        databaseHelper.endMatch(startTime, endTime, redTeamScore, blueTeamScore)
    }

    fun recordScore(matchStartTime: Long, team: String, score: Int, points: Int, timestamp: Long) {
        databaseHelper.recordScore(matchStartTime, team, score, points, timestamp) // Pass points
    }

    // Fetch all matches
    fun getAllMatches(): List<Match> {
        return databaseHelper.getAllMatches()
    }

    // Fetch scores for a specific match
    fun getScoresForMatch(matchStartTime: Long): List<Score> {
        return databaseHelper.getScoresForMatch(matchStartTime)
    }

    // Delete a match and its associated scores
    fun deleteMatch(matchStartTime: Long) {
        databaseHelper.deleteMatch(matchStartTime)
    }
}