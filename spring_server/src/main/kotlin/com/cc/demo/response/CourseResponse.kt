package com.cc.demo.response

import com.cc.demo.entity.Lecture

data class CourseResponse(
    val lectureId: Long,                     // 강의 고유 ID
    val courseName: String,                  // 과목명
    val professor: String,                   // 담당 교수명
    val time: List<LectureTimeResponse>,     // 요일/시간 정보 리스트
    val location: String,                    // 강의실 위치 (예: 정보B101)
    val subjectCode: String,                 // 학수 번호 (예: CS101)
    val credit: Int,                         // 학점 수
    val targetGrade: String,                 // 권장 학년 (예: "2학년")
    val category: String,                    // 전공필수, 전공선택, 교양 등 상위 카테고리
    val syllabusUrl: String,                 // 강의계획서 URL
    val capacity: Int,                       // 수강 정원
    val note: String,                        // 특이사항 (예: 영어 강의 아님 등)
    val language: String,                    // 강의 언어 (예: "한국어", "영어")
    val aiDescription: String?,              // 과목 관련 키워드 리스트 (예: ["자료구조", "알고리즘"])
    val lectureCode: String,
    val subjectName: String,
    ){
    companion object {
        fun from(lecture: Lecture): CourseResponse {
            return CourseResponse(
                lectureId = lecture.id,
                courseName = lecture.subject.subjectName,
                professor = lecture.professorName,
                time = lecture.times.map { LectureTimeResponse.from(it) },
                location = lecture.lecturePlace,
                subjectCode = lecture.subject.subjectCode,
                credit = lecture.subject.credit,
                targetGrade = lecture.subject.targetGrade,
                category = lecture.subject.categoryMain.name,
                syllabusUrl = lecture.syllabusUrl,
                capacity = lecture.capacity,
                note = lecture.note,
                language = lecture.language,
                aiDescription = lecture.aiDescription,
                lectureCode = lecture.divisionCode,
                subjectName = lecture.subject.subjectName,
            )
        }
    }

}