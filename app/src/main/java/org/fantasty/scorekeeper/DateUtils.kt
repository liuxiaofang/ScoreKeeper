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

import java.text.SimpleDateFormat
import java.util.*

object DateUtils {

    // Format timestamp to "yyyy年MM月dd日 HH:mm:ss"
    fun formatTimestampToDateTime(timestamp: Long): String {
        val date = Date(timestamp)
        val formatter = SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss", Locale.getDefault())
        return formatter.format(date)
    }
    fun parseDateToTimestamp(dateString: String, format: String = "yyyy-MM-dd"): Long {
        return try {
            val sdf = SimpleDateFormat(format, Locale.getDefault())
            val date = sdf.parse(dateString)
            date?.time ?: 0L
        } catch (e: Exception) {
            e.printStackTrace()
            0L
        }
    }
}