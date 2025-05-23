package com.cc.demo.initializer

import com.cc.demo.entity.*
import com.cc.demo.enumerate.DayOfWeek
import com.cc.demo.repository.CategoryMainRepository
import com.cc.demo.repository.LectureRepository
import com.cc.demo.repository.SubjectRepository
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.boot.CommandLineRunner
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.time.LocalTime

@Order(2)
@Component
class LectureDataInitializer(
    private val subjectRepository: SubjectRepository,
    private val lectureRepository: LectureRepository,
    private val categoryMainRepository: CategoryMainRepository,
) : CommandLineRunner {

    override fun run(vararg args: String?) {
       // if (lectureRepository.count() > 0) return

        val categories = listOf("전공필수", "전공선택", "교양필수", "교양선택")
        val categoryEntities = categories.mapIndexed { idx, name ->
            categoryMainRepository.save(
                CategoryMain(typeNumber = idx, name = name)
            )
        }

        val subjects = (1..20).map {
            val category = categoryEntities[it % categoryEntities.size]
            val subject = Subject(
                subjectCode = "CS10${it}",
                subjectName = "과목$it",
                credit = (2..4).random(),
                targetGrade = "${(1..4).random()}학년",
                categoryMain = category,
                typeNumber = 1,
                year = 2025
            )
            subjectRepository.save(subject)
        }

        (1..50).forEach {
            val subject = subjects.random()
            val lecture = Lecture(
                year = 2025,
                semester = if (it % 2 == 0) 1 else 2,
                professorName = "교수${('A'..'Z').random()}",
                lectureCode = "00${(1..3).random()}",
                isEnglish = (0..1).random() == 1,
                lecturePlace = "정보${(65..70).random().toChar()}${100 + it}",
                capacity = listOf(40, 50, 60).random(),
                syllabusUrl = "https://example.com/syllabus/cs$it",
                note = "특이사항 없음",
                language = if ((0..1).random() == 1) "한국어" else "영어",
                aiDescription = "과목에 대한 AI의 설명이다 $it",
                createdAt = LocalDateTime.now(),
                subject = subject,
                times = mutableListOf(),
                subjectName = subject.subjectName,
            )

            val weekday1 = listOf(DayOfWeek.Mon, DayOfWeek.Tue)
            val weekday2 = listOf(DayOfWeek.Wed, DayOfWeek.Thu, DayOfWeek.Fri)

            val lectureTime1 = makeTime()
            val lectureTime2 = makeTime()

            val time1 = LectureTime(
                    day = weekday1.random(),
                    startTime = lectureTime1[0],
                    endTime = lectureTime1[1],
                    lecture = lecture
                )
                val time2 = LectureTime(
                    day = weekday2.random(),
                    startTime = lectureTime2[0],
                    endTime = lectureTime2[1],
                    lecture = lecture
                )
                lecture.times += listOf(time1, time2)
                lectureRepository.save(lecture)
            }


        println("✅ 테스트용 Subject/Category/Lecture 50개 생성 완료")
    }
    fun makeTime(): List<LocalTime> {
        val startHour = (8..14).random()
        val endHour = (startHour + 1..(startHour + 3).coerceAtMost(18)).random()

        val startTime = LocalTime.of(startHour, 0)
        val endTime = LocalTime.of(endHour, 0)

        return listOf(startTime, endTime)
    }
}
