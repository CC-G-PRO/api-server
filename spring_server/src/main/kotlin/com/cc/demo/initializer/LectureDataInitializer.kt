package com.cc.demo.initializer

import com.cc.demo.entity.*
import com.cc.demo.enumerate.Category
import com.cc.demo.enumerate.DayOfWeek
import com.cc.demo.enumerate.MajorCategory
import com.cc.demo.repository.CurriculumRepository
import com.cc.demo.repository.LectureRepository
import com.cc.demo.repository.SubjectRepository
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Profile
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.time.LocalTime

@Profile("dev")
@Component
@Order(2)
class LectureDataInitializer(
    private val subjectRepository: SubjectRepository,
    private val lectureRepository: LectureRepository,
    private val curriculumRepository: CurriculumRepository,
) : CommandLineRunner {

    override fun run(vararg args: String?) {
        if (lectureRepository.count() > 0) return

        val typeNumbers = listOf(11, 4, 5, 6, 7, 13, 14, 15, 16, 17)
        val entryYears = listOf(2022, 2023, 2024)
        val majorCategories = MajorCategory.entries
        val categories = Category.entries

        // 1. Subject 20개 생성
        val subjects = (1..20).map {
            val subject = Subject(
                subjectCode = "SUBJ10${it}",
                credit = (2..4).random(),
                targetGrade = "${(1..4).random()}학년",
                typeNumber = typeNumbers.random(),
                aiDescription = "AI 설명 $it",
                category = categories.random()
            )
            subjectRepository.save(subject)
        }

        // 2. Lecture 50개 생성
        repeat(50) {
            val subject = subjects.random()
            val lecture = Lecture(
                year = 2025,
                semester = if (it % 2 == 0) 1 else 2,
                professorName = "교수${('A'..'Z').random()}",
                subjectName = "과목명${it}",
                divisionCode = "00${(1..3).random()}",
                isEnglish = (0..1).random() == 1,
                lecturePlace = "정보${(65..70).random().toChar()}${100 + it}",
                capacity = listOf(40, 50, 60).random(),
                syllabusUrl = "https://example.com/syllabus/cs$it",
                note = "특이사항 없음",
                language = if ((0..1).random() == 1) "한국어" else "영어",
                createdAt = LocalDateTime.now(),
                subject = subject,
                times = mutableListOf()
            )

            // 시간 정보 추가
            val time1 = makeLectureTime(lecture, listOf(DayOfWeek.Mon, DayOfWeek.Tue).random())
            val time2 = makeLectureTime(lecture, listOf(DayOfWeek.Wed, DayOfWeek.Thu, DayOfWeek.Fri).random())
            lecture.times += listOf(time1, time2)

            lectureRepository.save(lecture)
        }

        // 3. Curriculum 생성 (entryYear x Subject 조합)
        entryYears.forEach { year ->
            subjects.shuffled().take(10).forEach { subject ->
                val curriculum = Curriculum(
                    entryYear = year,
                    majorCategory = majorCategories.random(),
                    subject = subject
                )
                curriculumRepository.save(curriculum)
            }
        }

        println("✅ Subject 20개, Lecture 50개, Curriculum 생성 완료")
    }

    private fun makeLectureTime(lecture: Lecture, day: DayOfWeek): LectureTime {
        val startHour = (8..14).random()
        val endHour = (startHour + 1..(startHour + 3).coerceAtMost(18)).random()
        return LectureTime(
            day = day,
            startTime = LocalTime.of(startHour, 0),
            endTime = LocalTime.of(endHour, 0),
            lecture = lecture
        )
    }
}
