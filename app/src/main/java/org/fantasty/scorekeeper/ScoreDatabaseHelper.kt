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

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class ScoreDatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "scorekeeper.db"
        private const val DATABASE_VERSION = 3 // Incremented version for schema change

        const val TABLE_MATCHES = "matches"
        const val COLUMN_START_TIME = "start_time"
        const val COLUMN_END_TIME = "end_time"

        const val TABLE_SCORES = "scores"
        const val COLUMN_MATCH_START_TIME = "match_start_time"
        const val COLUMN_TEAM = "team"
        const val COLUMN_SCORE = "score"
        const val COLUMN_POINTS = "points" // New column for points
        const val COLUMN_TIMESTAMP = "timestamp"
    }

    override fun onCreate(db: SQLiteDatabase) {
        // Create matches table
        db.execSQL(
            "CREATE TABLE $TABLE_MATCHES (" +
                    "$COLUMN_START_TIME INTEGER PRIMARY KEY, " +
                    "$COLUMN_END_TIME INTEGER, " +
                    "red_team_score INTEGER DEFAULT 0, " +
                    "blue_team_score INTEGER DEFAULT 0)"
        )

        // Create scores table
        db.execSQL(
            "CREATE TABLE $TABLE_SCORES (" +
                    "$COLUMN_MATCH_START_TIME INTEGER, " +
                    "$COLUMN_TEAM TEXT, " +
                    "$COLUMN_SCORE INTEGER, " +
                    "$COLUMN_POINTS INTEGER, " + // Added points column
                    "$COLUMN_TIMESTAMP INTEGER, " +
                    "FOREIGN KEY($COLUMN_MATCH_START_TIME) REFERENCES $TABLE_MATCHES($COLUMN_START_TIME))"
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            // Add points column to scores table
            db.execSQL("ALTER TABLE $TABLE_SCORES ADD COLUMN $COLUMN_POINTS INTEGER DEFAULT 0")
            db.execSQL("ALTER TABLE $TABLE_MATCHES ADD COLUMN red_team_score INTEGER DEFAULT 0")
            db.execSQL("ALTER TABLE $TABLE_MATCHES ADD COLUMN blue_team_score INTEGER DEFAULT 0")

        }
    }

    fun startMatch(startTime: Long) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_START_TIME, startTime)
        }
        db.insert(TABLE_MATCHES, null, values)
    }

    fun endMatch(startTime: Long, endTime: Long, redTeamScore: Int, blueTeamScore: Int) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_END_TIME, endTime)
            put("red_team_score", redTeamScore)
            put("blue_team_score", blueTeamScore)
        }
        db.update(TABLE_MATCHES, values, "$COLUMN_START_TIME = ?", arrayOf(startTime.toString()))
    }

    fun recordScore(matchStartTime: Long, team: String, score: Int, points: Int, timestamp: Long) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_MATCH_START_TIME, matchStartTime)
            put(COLUMN_TEAM, team)
            put(COLUMN_SCORE, score)
            put(COLUMN_POINTS, points) // Insert points value
            put(COLUMN_TIMESTAMP, timestamp)
        }
        db.insert(TABLE_SCORES, null, values)
    }

    fun getAllMatches(): List<Match> {
        val matches = mutableListOf<Match>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM matches ORDER BY start_time DESC", null)
        while (cursor.moveToNext()) {
            val startTime = cursor.getLong(cursor.getColumnIndexOrThrow("start_time"))
            val endTime = cursor.getLong(cursor.getColumnIndexOrThrow("end_time"))
            val redTeamScore = cursor.getInt(cursor.getColumnIndexOrThrow("red_team_score"))
            val blueTeamScore = cursor.getInt(cursor.getColumnIndexOrThrow("blue_team_score"))
            matches.add(Match(startTime, endTime, redTeamScore, blueTeamScore))
        }
        cursor.close()
        return matches
    }

    fun getScoresForMatch(matchStartTime: Long): List<Score> {
        val scores = mutableListOf<Score>()
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT * FROM $TABLE_SCORES WHERE $COLUMN_MATCH_START_TIME = ?",
            arrayOf(matchStartTime.toString())
        )
        while (cursor.moveToNext()) {
            val team = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TEAM))
            val score = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_SCORE))
            val points = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_POINTS))
            val timestamp = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_TIMESTAMP))
            scores.add(Score(team, score, points, timestamp))
        }
        cursor.close()
        return scores
    }

    // Delete a match and its associated scores
    fun deleteMatch(matchStartTime: Long) {
        val db = writableDatabase
        db.beginTransaction()
        try {
            // Delete scores associated with the match
            db.delete("scores", "match_start_time = ?", arrayOf(matchStartTime.toString()))
            // Delete the match
            db.delete("matches", "start_time = ?", arrayOf(matchStartTime.toString()))
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }
}