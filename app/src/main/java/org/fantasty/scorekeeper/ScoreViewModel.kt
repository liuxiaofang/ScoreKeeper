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

import androidx.lifecycle.ViewModel

class ScoreViewModel(private val repository: ScoreRepository) : ViewModel() {
    var redTeamScore: Int = 0
    var blueTeamScore: Int = 0
    var isMatchGoing: Boolean = false

    private var matchStartTime: Long? = null

    // Start the match
    fun startMatch() {
        matchStartTime = System.currentTimeMillis()
        isMatchGoing = true
        redTeamScore = 0
        blueTeamScore = 0
        matchStartTime?.let { repository.startMatch(it) }
    }

    // End the match
    fun endMatch()
    {
        isMatchGoing = false
        matchStartTime?.let {
            val endTime = System.currentTimeMillis()
            repository.endMatch(it, endTime, redTeamScore, blueTeamScore)
        }
        matchStartTime = null
    }

    fun incrementRedTeamScore() {
        if (isMatchGoing) {
            redTeamScore++
            matchStartTime?.let {
                repository.recordScore(it, "红队", redTeamScore, 1 , System.currentTimeMillis())
            }
        }
    }

    fun decrementRedTeamScore() {
        if (isMatchGoing) {
            redTeamScore--
            if (redTeamScore < 0)
                redTeamScore = 0

            matchStartTime?.let {
                repository.recordScore(it, "红队", redTeamScore, -1, System.currentTimeMillis())
            }
        }
    }

    fun incrementBlueTeamScore() {
        if (isMatchGoing) {
            blueTeamScore++
            matchStartTime?.let {
                repository.recordScore(it, "蓝队", blueTeamScore, 1, System.currentTimeMillis())
            }
        }
    }

    fun decrementBlueTeamScore() {
        if (isMatchGoing) {
            blueTeamScore--
            if (blueTeamScore < 0)
                blueTeamScore = 0

            matchStartTime?.let {
                repository.recordScore(it, "蓝队", blueTeamScore, -1, System.currentTimeMillis())
            }
        }
    }

    fun resetScores() {
        redTeamScore = 0
        blueTeamScore = 0
    }
}