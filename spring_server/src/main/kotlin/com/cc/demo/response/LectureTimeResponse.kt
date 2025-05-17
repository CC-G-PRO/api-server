package com.cc.demo.response

import com.cc.demo.entity.LectureTime

data class LectureTimeResponse(
    val day: String,         // "Mon", "Tue" ...
    val startTime: String,   // "10:00"
    val endTime: String      // "11:15"
)
{
    companion object {
        fun from(entity: LectureTime): LectureTimeResponse {
            return LectureTimeResponse(
                day = entity.day.name.substring(0, 1) + entity.day.name.substring(1, 3).lowercase(), // "MON" → "Mon"
                startTime = entity.startTime.toString().substring(0, 5), // "10:00:00" → "10:00"
                endTime = entity.endTime.toString().substring(0, 5)
            )
        }
    }
}