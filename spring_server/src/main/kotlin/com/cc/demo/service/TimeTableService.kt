package com.cc.demo.service

import com.cc.demo.entity.Lecture
import com.cc.demo.entity.TimeTable
import com.cc.demo.entity.TimeTableLecture
import com.cc.demo.enumerate.TimeTableType
import com.cc.demo.repository.LectureCartRepository
import com.cc.demo.repository.TimetableLectureRepository
import com.cc.demo.repository.TimetableRepository
import com.cc.demo.repository.UserRepository
import com.cc.demo.response.TimetableResponse
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class TimeTableService(
    val lectureCartRepository: LectureCartRepository,
    val userRepository: UserRepository,
    val timetableRepository: TimetableRepository,
    val timetableLectureRepository : TimetableLectureRepository
) {

    @Transactional
    fun regenerateTimeTables(
        userId: Long,
        minCredit: Int,
        maxCredit: Int
    ): List<Pair<TimeTable, List<TimeTableLecture>>> {
        deleteAllTimetablesForUser(userId)
        return generateTimeTable(userId, minCredit, maxCredit)
    }

    fun deleteAllTimetablesForUser(userId: Long) {
        val timetables = timetableRepository.findByUserId(userId)
            .filter { it.type == TimeTableType.GENERATED } // 자동 생성된 시간표만 삭제

        timetables.forEach { timetable ->
            timetableLectureRepository.deleteByTimetableId(timetable.id)
        }

        timetableRepository.deleteAll(timetables)
    }

    fun deleteTimetable(tableId: Long) {
        val timetable = timetableRepository.findById(tableId)
            .orElseThrow { IllegalArgumentException("해당 시간표가 존재하지 않습니다. id=$tableId") }

        timetableLectureRepository.deleteByTimetableId(timetable.id)

        timetableRepository.delete(timetable)
    }

    fun generateTimeTable(userId: Long, minCredit: Int, maxCredit: Int): List<Pair<TimeTable, List<TimeTableLecture>>> {
        val user = userRepository.findById(userId)
            .orElseThrow { IllegalArgumentException("User를 찾을 수 없습니다!") }

        val carts = lectureCartRepository.findByUserId(userId)
        val lectures = carts.sortedBy { it.priority }.map { it.lecture }

        val lectureCombinations = generateLectureList(lectures, minCredit, maxCredit)

        val now = LocalDateTime.now()
        val results = mutableListOf<Pair<TimeTable, List<TimeTableLecture>>>()

        for ((index, combo) in lectureCombinations.withIndex()) {
            val recommended = TimeTable(
                user = user,
                createdAt = now
            )

            val savedTimetable = timetableRepository.save(recommended)

            val recommendedLectures = combo.map { lecture ->
                TimeTableLecture(
                    timetable = savedTimetable,
                    lecture = lecture
                )
            }

            timetableLectureRepository.saveAll(recommendedLectures)

            results.add(savedTimetable to recommendedLectures)
        }

        return results
    }

    //backtracking 으로 하면 시간 줄어들 듯 으음..
    fun generateLectureList(
        lectures: List<Lecture>, //cart 에 담긴 lectures
        minCredit: Int,
        maxCredit: Int
    ): List<List<Lecture>> {
        val results = mutableListOf<List<Lecture>>()

        fun backtrack(
            start: Int,
            path: MutableList<Lecture>,
            totalCredit: Int
        ) {
            if (results.size >= 20) return

            if (totalCredit in minCredit..maxCredit) { //min max 사이 일 때만 path 에 추가
                results.add(path.toList())
            }
            if (totalCredit >= maxCredit) return //초과시 버림

            for (i in start until lectures.size) { //재귀로 줍줍
                val lecture = lectures[i]

                if (path.any { isTimeOverlap(it, lecture) }) continue //겹치면 버리고
                if (path.any { it.subject.subjectName == lecture.subject.subjectName }) continue //같은 과목일 경우 버림.

                path.add(lecture)
                backtrack(i + 1, path, totalCredit + lecture.subject.credit )
                path.removeLast()

                if (results.size >= 20) return
            }
        }

        backtrack(0, mutableListOf(), 0)
        return results
    }

    fun isTimeOverlap(lectureA: Lecture, lectureB: Lecture): Boolean {
        for (timeA in lectureA.times) { // bigo = 4
            for (timeB in lectureB.times) {
                if (timeA.day == timeB.day) {
                    if (timeA.startTime < timeB.endTime && timeA.endTime > timeB.startTime) {
                        return true
                    }
                }
            }
        }
        return false
    }

    fun getTimeTables(userId: Long, type : TimeTableType): List<TimetableResponse> {
        val timetables = timetableRepository.findByUserId(userId)
            .filter { it.type == type }

        return timetables.map { buildTimetableResponse(it) }
    }

    fun getTimetableDetails(tableId: Long): TimetableResponse {
        val timetable = timetableRepository.findById(tableId)
            .orElseThrow { IllegalArgumentException("해당 시간표가 존재하지 않습니다. id=$tableId") }

        return buildTimetableResponse(timetable)
    }

    private fun buildTimetableResponse(timetable: TimeTable): TimetableResponse {
        val lectures = timetableLectureRepository.findByTimetableId(timetable.id)
        return TimetableResponse.from(timetable, lectures)
    }


    //생성된 Get timetable
    //post 요청
//    fun searchTimeTable(prompt: List<String>): List<Lecture> {
//
//    }
}