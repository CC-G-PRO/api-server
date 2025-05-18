package com.cc.demo.service

import com.cc.demo.entity.Lecture
import com.cc.demo.entity.RecommendedTimetable
import com.cc.demo.entity.RecommendedTimetableLecture
import com.cc.demo.repository.LectureCartRepository
import com.cc.demo.repository.RecommendedTimetableRepository
import com.cc.demo.repository.UserRepository
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class TimeTableService(
    val lectureCartRepository: LectureCartRepository,
    val userRepository: UserRepository,
    val recommendedTimetableRepository: RecommendedTimetableRepository,
) {

    //Todo: generateTime 하기 전에 all delete 하고 해야할 듯.
    @Transactional
    fun generateTimeTable(userId: Long, minCredit: Int, maxCredit: Int) : List<RecommendedTimetable> {
        val user = userRepository.findById(userId).orElseThrow { IllegalArgumentException("User를 찾을 수 없습니다!!") }
        val carts = lectureCartRepository.findByUserId(userId)
        val lectures = carts.map { it.lecture }
        println("Lectures in cart:")
        lectures.forEach {
            println("Lecture: ${it.subject.subjectName}, Credit: ${it.subject.credit}")
        }

        val lectureCombinations = generateLectureList(lectures, minCredit, maxCredit) //백트래킹 좀 느릴 것 같긴한데.... 흠

        val now = LocalDateTime.now()
        val results = mutableListOf<RecommendedTimetable>()

        for ((index, combo) in lectureCombinations.withIndex()) {
            val recommended = RecommendedTimetable( //dto 만들어서 할까
                user = user,
                description = "추천 시간표 #${index + 1}",
                createdAt = now
            )

            combo.forEach { lecture ->
                recommended.lectures.add(
                    RecommendedTimetableLecture(
                        timetable = recommended,
                        lecture = lecture
                    )
                )
            }

            results.add(recommended)
            recommendedTimetableRepository.save(recommended)
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
            if (totalCredit in minCredit..maxCredit) { //min max 사이 일 때만 path 에 추가
                results.add(path.toList())
            }
            if (totalCredit >= maxCredit) return //초과시 버림

            for (i in start until lectures.size) { //재귀로 줍줍
                val lecture = lectures[i]

                if (path.any { isTimeOverlap(it, lecture) }) continue //겹치면 버리고

                path.add(lecture)
                backtrack(i + 1, path, totalCredit + lecture.subject.credit )
                path.removeLast()
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


}