package com.cc.demo.service

import TimeTableFilter
import com.cc.demo.entity.Lecture
import com.cc.demo.entity.TimeTable
import com.cc.demo.entity.TimeTableLecture
import com.cc.demo.enumerate.Category
import com.cc.demo.enumerate.IndustryCode
import com.cc.demo.enumerate.MajorCategory
import com.cc.demo.enumerate.TimeTableType
import com.cc.demo.exception.OverlappingLectureException
import com.cc.demo.repository.GraduationEvaluationRepository
import com.cc.demo.repository.LectureCartRepository
import com.cc.demo.repository.LectureRepository
import com.cc.demo.repository.TimetableLectureRepository
import com.cc.demo.repository.TimetableRepository
import com.cc.demo.repository.UserRepository
import com.cc.demo.repository.UserTakenSubjectRepository
import com.cc.demo.request.TimeTableCreateRequest
import com.cc.demo.request.TimeTableUpdateRequest
import com.cc.demo.response.GraduationEvaluationPreview
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
    private val lectureRepository: LectureRepository,
    private val graduationEvaluationRepository: GraduationEvaluationRepository,
    private val userTakenSubjectRepository: UserTakenSubjectRepository,
    private val curriculumService: CurriculumService
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
                if (path.any { it.subjectName == lecture.subjectName }) continue //같은 과목일 경우 버림.

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

//    @Transactional
//    fun createTimeTables(content : TimeTableCreateRequest, userId : Long) : TimetableResponse {
//        val user = userRepository.findById(userId)
//            .orElseThrow { IllegalArgumentException("User를 찾을 수 없습니다!") }
//
//        //create timetable
//        val timetable = TimeTable(user = user , createdAt = LocalDateTime.now() )
//
//        timetableRepository.save(timetable)
//        if(content.lectures != null){
//            val lectures: List<TimeTableLecture> = content.lectures.map { lectureId ->
//                val lecture = lectureRepository.findById(lectureId)
//                    .orElseThrow { IllegalArgumentException("해당 강의를 찾을 수 없습니다") }
//
//                val ttl = TimeTableLecture(
//                    timetable = timetable,
//                    lecture = lecture
//                )
//                timetableLectureRepository.save(ttl)
//
//                ttl
//            }
//            return TimetableResponse.from(timetable, lectures)
//        }
//        return TimetableResponse.from(timetable,  emptyList())
//    }

    fun checkLectureOverlap(lectures: List<Lecture>) {
        val lectureTimes = lectures.flatMap { it.times }

        for (i in lectureTimes.indices) {
            for (j in i + 1 until lectureTimes.size) {
                val t1 = lectureTimes[i]
                val t2 = lectureTimes[j]

                if (t1.day == t2.day &&
                    t1.startTime < t2.endTime &&
                    t2.startTime < t1.endTime
                ) {
                    throw OverlappingLectureException("강의 시간표가 겹치는 강의가 존재합니다: ${t1.lecture.subjectName} 와 ${t2.lecture.subjectName}")
                }
            }
        }
    }

    @Transactional
    fun createTimeTables(content: TimeTableCreateRequest, userId: Long): TimetableResponse {
        val user = userRepository.findById(userId)
            .orElseThrow { IllegalArgumentException("User를 찾을 수 없습니다!") }

        val lecturesFromRequest = content.lectures ?: emptyList()

        val lectures = lecturesFromRequest.map { lectureId ->
            lectureRepository.findById(lectureId)
                .orElseThrow { OverlappingLectureException("강의 시간표가 겹치는 강의가 존재합니다") }
        }

        checkLectureOverlap(lectures)

        val timetable = TimeTable(user = user, createdAt = LocalDateTime.now())
        timetableRepository.save(timetable)

        val timetableLectures: List<TimeTableLecture> = lectures.map { lecture ->
            val ttl = TimeTableLecture(
                timetable = timetable,
                lecture = lecture
            )
            timetableLectureRepository.save(ttl)
            ttl
        }

        return TimetableResponse.from(timetable, timetableLectures)
    }


//    @Transactional
//    fun updateTimeTable(
//        content : TimeTableUpdateRequest,
//        userId : Long,
//        timeTableId : Long,
//    ) : TimetableResponse {
//        val user = userRepository.findById(userId)
//
//        val timetable = timetableRepository.findById(timeTableId)
//            .orElseThrow {IllegalArgumentException("해당 시간표가 존재하지 않습니다. id=$timeTableId")  }
//        if (timetable.user.id != userId) {
//            throw IllegalArgumentException("해당 시간표가 존재하지 않습니다")
//        }
//        //timetable type 수정
//        if( timetable.type != TimeTableType.CUSTOM ) {
//            timetable.type = TimeTableType.CUSTOM
//        }
//
//        val lectures: List<TimeTableLecture> = content.lectures?.map { it ->
//            timetableLectureRepository.deleteByTimetableId(timetable.id)
//
//            val ttl = TimeTableLecture(
//                timetable = timetable,
//                lecture = lectureRepository.findById(it).
//                    orElseThrow { IllegalArgumentException("해당 강의가 존재하지 않습니다.") }
//            )
//
//            timetableLectureRepository.save(ttl)
//
//        } ?: emptyList()
//
//        return TimetableResponse.from(timetable, lectures)
//    }

    @Transactional
    fun updateTimeTable(
        content: TimeTableUpdateRequest,
        userId: Long,
        timeTableId: Long,
        ): TimetableResponse {
        val user = userRepository.findById(userId)
            .orElseThrow { IllegalArgumentException("User를 찾을 수 없습니다!") }

        val timetable = timetableRepository.findById(timeTableId)
            .orElseThrow { IllegalArgumentException("해당 시간표가 존재하지 않습니다. id=$timeTableId") }

        if (timetable.user.id != userId) {
            throw IllegalArgumentException("권한이 없습니다.")
        }

        if (timetable.type != TimeTableType.CUSTOM) {
            timetable.type = TimeTableType.CUSTOM
        }

        val lectures: List<Lecture> = content.lectures?.map { lectureId ->
            lectureRepository.findById(lectureId)
                .orElseThrow { IllegalArgumentException("해당 강의가 존재하지 않습니다.") }
        } ?: emptyList()

        checkLectureOverlap(lectures)

        timetableLectureRepository.deleteByTimetableId(timetable.id)

        val timetableLectures: List<TimeTableLecture> = lectures.map { lecture ->
            val ttl = TimeTableLecture(
                timetable = timetable,
                lecture = lecture
            )
            timetableLectureRepository.save(ttl)
            ttl
        }

        return TimetableResponse.from(timetable, timetableLectures)
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

    //time table with graduation 아 재수강은 좀 ...
    fun graduationInfoWithTimeTable(userId: Long, tableDetail : TimetableResponse): GraduationEvaluationPreview {

        val currentInfo = graduationEvaluationRepository.findByUserId(userId).firstOrNull()
            ?: throw RuntimeException("졸업 평가 정보가 존재하지 않습니다.")

        //user 학번에 맞는 교육과정 표 가져오는 거임.

        val alreadyTakenSubjects = userTakenSubjectRepository.findByUserId(userId).map { it.subjectCode }.toSet()
        val newCourses = tableDetail.courses.filter { it.subjectCode !in alreadyTakenSubjects } //재수강 과목은 할당하지 않음.
        val majorCategory  = curriculumService.getMajorCategoryMapByEntryYear(currentInfo.entryYear)

        var updated = currentInfo.copy(
            totalCreditsEarned = currentInfo.totalCreditsEarned,
            generalFreeCreditsEarned = currentInfo.generalFreeCreditsEarned,
            generalBreadthCreditsEarned = currentInfo.generalBreadthCreditsEarned,
            generalRequiredCreditsEarned = currentInfo.generalRequiredCreditsEarned,
            majorBasicCreditsEarned = currentInfo.majorBasicCreditsEarned,
            majorRequiredCreditsEarned = currentInfo.majorRequiredCreditsEarned,
            majorElectiveCreditsEarned = currentInfo.majorElectiveCreditsEarned,
            majorIndustryCreditsEarned = currentInfo.majorIndustryCreditsEarned,

            englishCourseCount = currentInfo.englishCourseCount,
        )

        for (course in newCourses) {
            val credit = course.credit
            val category : Category = course.category
            val isEnglish = course.isEnglish

            when (category) {
                Category.FREE_GENERAL -> {
                    updated = updated.copy(generalFreeCreditsEarned = updated.generalFreeCreditsEarned + credit)
                }

                Category.DISTRIBUTION_GENERAL -> {
                    updated = updated.copy(generalBreadthCreditsEarned = (updated.generalBreadthCreditsEarned ?: 0) + credit)
                }

                Category.REQUIRED_GENERAL -> {
                    updated = updated.copy(generalRequiredCreditsEarned = updated.generalRequiredCreditsEarned + credit)
                }

                Category.MAJOR -> { //전공일 경우에 교육과정에 따라서 전공 필수인지 선택인지 기초인지 판단해야함.
                    val subId = course.subjectId.toLong()
                    if (majorCategory.containsKey(subId)) {
                        when (majorCategory[subId]) {
                            MajorCategory.MAJOR_BASIC -> {
                                updated = updated.copy(majorBasicCreditsEarned = updated.majorBasicCreditsEarned + credit)
                            }
                            MajorCategory.MAJOR_REQUIRED -> {
                                updated = updated.copy(majorRequiredCreditsEarned = updated.majorRequiredCreditsEarned + credit)
                            }

                            MajorCategory.MAJOR_ELECTIVE -> {
                                updated = updated.copy(majorElectiveCreditsEarned = updated.majorElectiveCreditsEarned + credit)
                            }
                            else -> {
                                //예외.
                            }
                        }
                    }
                }

                else -> {
                    //pass
                }
            }

            if (IndustryCode.fromSubjectName(course.courseName) != null) { //산학 필수인지 체크하는 코드임.
                updated = updated.copy(majorIndustryCreditsEarned = updated.majorIndustryCreditsEarned + credit)
            }

            if (isEnglish) {
                updated = updated.copy(englishCourseCount = updated.englishCourseCount + 1)
            }

            updated = updated.copy(totalCreditsEarned = updated.totalCreditsEarned + credit)
        }

        return GraduationEvaluationPreview(
            original = currentInfo,
            expected = updated
        )
    }

    fun getTimeTables(userId: Long, type : TimeTableType): List<TimetableResponse> {
        val timetables = timetableRepository.findByUserId(userId)
            .filter { it.type == type }

        return timetables.map { buildTimetableResponse(it) }
    }

    fun getTimetableDetails(userId: Long, tableId: Long): TimetableResponse {
        val timetable = timetableRepository.findById(tableId)
            .orElseThrow { IllegalArgumentException("해당 시간표가 존재하지 않습니다. id=$tableId") }
        if (timetable.user.id != userId){
            throw IllegalArgumentException("잘못된 접근입니다. id=$tableId")
        }
        return buildTimetableResponse(timetable)
    }

    private fun buildTimetableResponse(timetable: TimeTable): TimetableResponse {
        val lectures = timetableLectureRepository.findByTimetableId(timetable.id)
        return TimetableResponse.from(timetable, lectures)
    }
}