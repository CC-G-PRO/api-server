package com.cc.demo.response

import com.cc.demo.entity.TimeTable
import com.cc.demo.entity.TimeTableLecture
import com.fasterxml.jackson.annotation.JsonProperty

data class TimetableResponse(
    @JsonProperty("timetable_id")
    val timetableId: String,

    val courses: List<TimeTableCourseResponse>,

    @JsonProperty("total_credit")
    val totalCredit : Int
) {
    companion object {
        fun from(
            timetable: TimeTable,
            lectures: List<TimeTableLecture>
        ): TimetableResponse {
            return TimetableResponse(
                timetableId = timetable.id.toString(),
                courses = lectures.map { recommendedLecture ->
                    TimeTableCourseResponse(
                        lectureId = recommendedLecture.lecture.id.toString(),
                        courseName = recommendedLecture.lecture.subjectName,
                        time = recommendedLecture.lecture.times.map { LectureTimeResponse.from(it) },
                        professorName = recommendedLecture.lecture.professorName,
                        lecturePlace = recommendedLecture.lecture.lecturePlace,
                        credit = recommendedLecture.lecture.subject.credit,
                        syllabusUrl = recommendedLecture.lecture.syllabusUrl,
                        subjectCode = recommendedLecture.lecture.subject.subjectCode,
                        isEnglish =  recommendedLecture.lecture.isEnglish,
                    )
                },
                totalCredit = lectures.sumOf { it.lecture.subject.credit }
            )
        }
    }
}

//필요한 정보만 반환.
data class TimeTableCourseResponse(
    @JsonProperty("lecture_id")
    val lectureId: String,

    @JsonProperty("subject_code")
    val subjectCode : String,

    @JsonProperty("credit")
    val credit: Int,

    @JsonProperty("is_english")
    val isEnglish: Boolean,

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

data class RecommendedTimetableResponse(
    @JsonProperty("filtered_timetables")
    val filteredTimetables: List<TimetableResponse>
)

