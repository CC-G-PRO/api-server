package com.cc.demo.response

import com.cc.demo.entity.RecommendedTimetable
import com.fasterxml.jackson.annotation.JsonProperty

data class RecommendedTimetableResponse(
    @JsonProperty("filtered_timetables")
    val filteredTimetables: List<TimetableResponse>
)

data class TimetableResponse(
    @JsonProperty("timetable_id")
    val timetableId: String,
    val courses: List<TimeTableCourseResponse>,
    @JsonProperty("total_credit")
    val totalCredit : Int
){
    companion object{
        fun from(entity: RecommendedTimetable): TimetableResponse {
            return TimetableResponse(
                timetableId = entity.id.toString(),
                courses = entity.lectures.map { recommendedLecture ->
                    TimeTableCourseResponse(
                        lectureId = recommendedLecture.lecture.id.toString(),
                        courseName = recommendedLecture.lecture.subjectName,
                        time = recommendedLecture.lecture.times.map { LectureTimeResponse.from(it) },
                        professorName = recommendedLecture.lecture.professorName,
                        lecturePlace = recommendedLecture.lecture.lecturePlace,
                        syllabusUrl = recommendedLecture.lecture.syllabusUrl)
                },
                totalCredit = entity.lectures.sumOf { it.lecture.subject.credit}
            )
        }
    }

}

data class TimeTableCourseResponse(
    @JsonProperty("lecture_id")
    val lectureId: String,

    @JsonProperty("course_name")
    val courseName: String,

    @JsonProperty("professor_name")
    val professorName: String,

    @JsonProperty("lecture_place")
    val lecturePlace: String,

    @JsonProperty("syllabus_url")
    val syllabusUrl: String,

    val time: List<LectureTimeResponse>
)
