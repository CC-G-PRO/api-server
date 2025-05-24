package com.cc.demo.service

import TimeTableFilter
import com.cc.demo.entity.Lecture
import com.cc.demo.entity.TimeTable
import com.cc.demo.entity.TimeTableLecture
import com.cc.demo.enumerate.TimeTableType
import com.cc.demo.repository.LectureCartRepository
import com.cc.demo.repository.LectureRepository
import com.cc.demo.repository.TimetableLectureRepository
import com.cc.demo.repository.TimetableRepository
import com.cc.demo.repository.UserRepository
import com.cc.demo.request.TimeTableCreateRequest
import com.cc.demo.request.TimeTableUpdateRequest
import com.cc.demo.response.TimetableResponse
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime

@Service
class TimeTableService(
    val lectureCartRepository: LectureCartRepository,
    val userRepository: UserRepository,
    val timetableRepository: TimetableRepository,
    val timetableLectureRepository: TimetableLectureRepository,
    private val lectureService: LectureService,
    private val lectureRepository: LectureRepository
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

    @Transactional
    fun createTimeTables(content : TimeTableCreateRequest, userId : Long) : TimetableResponse {
        val user = userRepository.findById(userId)
            .orElseThrow { IllegalArgumentException("User를 찾을 수 없습니다!") }

        //create timetable
        val timetable = TimeTable(user = user , createdAt = LocalDateTime.now() )
        timetableRepository.save(timetable)

        val lectures: List<TimeTableLecture> = content.lectures.map { lectureId ->
            val lecture = lectureRepository.findById(lectureId)
                .orElseThrow { IllegalArgumentException("해당 강의를 찾을 수 없습니다") }

            val ttl = TimeTableLecture(
                timetable = timetable,
                lecture = lecture
            )
            timetableLectureRepository.save(ttl)

            ttl
        }
        return TimetableResponse.from(timetable, lectures)
    }

    @Transactional
    fun updateTimeTable(
        content : TimeTableUpdateRequest,
        userId : Long,
        timeTableId : Long,
    ) : TimetableResponse {
        val user = userRepository.findById(userId)

        val timetable = timetableRepository.findById(timeTableId)
            .orElseThrow {IllegalArgumentException("해당 시간표가 존재하지 않습니다. id=$timeTableId")  }
        if (timetable.user.id != userId) {
            throw IllegalArgumentException("해당 시간표가 존재하지 않습니다")
        }
        //timetable type 수정
        if( timetable.type != TimeTableType.CUSTOM ) {
            timetable.type = TimeTableType.CUSTOM
        }

        val lectures: List<TimeTableLecture> = content.lectures?.map { it ->
            //all timetable lecture item will be flushed
            timetableLectureRepository.deleteByTimetableId(timetable.id)

            val ttl = TimeTableLecture(
                timetable = timetable,
                lecture = lectureRepository.findById(it).
                    orElseThrow { IllegalArgumentException("해당 강의가 존재하지 않습니다.") }
            )

            timetableLectureRepository.save(ttl)

        } ?: emptyList()

        return TimetableResponse.from(timetable, lectures)
    }

    fun filterTimeTable(filter : TimeTableFilter, timetables: List<TimetableResponse>) : List<TimetableResponse> {
        return timetables.filter { timetable ->
            val courses = timetable.courses

            // 포함 과목 체크
            if (filter.includeSubjects.isNotEmpty() &&
                courses.none { filter.includeSubjects.contains(it.courseName) }
            ) return@filter false

            // 제외 과목 체크
            if (filter.excludeSubjects.isNotEmpty() &&
                courses.any { filter.excludeSubjects.contains(it.courseName) }
            ) return@filter false

            // 포함 교수 체크
            if (filter.includeProfessors.isNotEmpty() &&
                courses.none { filter.includeProfessors.contains(it.professorName) }
            ) return@filter false

            // 제외 교수 체크
            if (filter.excludeProfessors.isNotEmpty() &&
                courses.any { filter.excludeProfessors.contains(it.professorName) }
            ) return@filter false

            // 허용 요일 필터
            if (filter.allowedDays.isNotEmpty()) {
                val daysInTimetable = courses.flatMap { it.time }.map { it.day }.distinct()
                if (daysInTimetable.any { it !in filter.allowedDays }) {
                    return@filter false
                }
            }

            // 요일별 최대 수업 수 필터
            if (filter.maxClassesPerDay != null) {
                val dayCounts = mutableMapOf<String, Int>()
                courses.flatMap { it.time }.forEach {
                    dayCounts[it.day] = dayCounts.getOrDefault(it.day, 0) + 1
                }
                if (dayCounts.any { it.value > filter.maxClassesPerDay }) {
                    return@filter false
                }
            }

            // excludeBeforeMinutes 필터 (예: 오전 9시 + n분 이전 수업이 있으면 제외)
            if (filter.excludeBeforeMinutes != null) {
                val thresholdTime = LocalTime.of(9, 0).plusMinutes(filter.excludeBeforeMinutes.toLong())
                val hasTooEarlyLecture = courses.flatMap { it.time }.any {
                    LocalTime.parse(it.startTime) < thresholdTime
                }
                if (hasTooEarlyLecture) return@filter false
            }

            // 공강 시간 필터
            if (filter.minFreeHours != null) {
                val lecturesByDay = courses.flatMap { it.time }.groupBy { it.day }

                for ((_, lectures) in lecturesByDay) {
                    val sorted = lectures.sortedBy { LocalTime.parse(it.startTime) }

                    for (i in 0 until sorted.size - 1) {
                        val end = LocalTime.parse(sorted[i].endTime)
                        val nextStart = LocalTime.parse(sorted[i + 1].startTime)
                        val gap = Duration.between(end, nextStart).toMinutes()
                        if (gap in 1 until filter.minFreeHours * 60) {
                            return@filter false
                        }
                    }
                }
            }
            true
        }

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

}