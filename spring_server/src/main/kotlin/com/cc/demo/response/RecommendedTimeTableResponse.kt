package com.cc.demo.response

import com.cc.demo.entity.RecommendedTimetable
import com.fasterxml.jackson.annotation.JsonProperty

data class RecommendedTimetableResponse(
    @JsonProperty("filtered_timetables")
    val filteredTimetables: List<TimetableResponse>
){
    companion object{
        fun from(entity: RecommendedTimetable): TimetableResponse {
            return TimetableResponse(
                timetableId = entity.id.toString(),
                courses = entity.lectures.map { recommendedLecture ->
                    TimeTableCourseResponse(
                        lectureId = recommendedLecture.lecture.id.toString(),
                        courseName = recommendedLecture.lecture.subjectName,
                        time = recommendedLecture.lecture.times.map { LectureTimeResponse.from(it) }
                    )
                }
            )
        }
    }
}

data class TimetableResponse(
    @JsonProperty("timetable_id")
    val timetableId: String,
    val courses: List<TimeTableCourseResponse>
)

data class TimeTableCourseResponse(
    @JsonProperty("lecture_id")
    val lectureId: String,

    @JsonProperty("course_name")
    val courseName: String,

    val time: List<LectureTimeResponse>
)
