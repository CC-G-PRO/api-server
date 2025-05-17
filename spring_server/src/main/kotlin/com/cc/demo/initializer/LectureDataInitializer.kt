package com.cc.demo.initializer

import com.cc.demo.entity.*
import com.cc.demo.enumerate.DayOfWeek
import com.cc.demo.repository.CategoryMainRepository
import com.cc.demo.repository.LectureRepository
import com.cc.demo.repository.SubjectRepository
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.time.LocalTime

@Component
class LectureDataInitializer(
    private val subjectRepository: SubjectRepository,
    private val lectureRepository: LectureRepository,
    private val categoryMainRepository: CategoryMainRepository,
) : ApplicationRunner {

    override fun run(args: ApplicationArguments) {
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
                aiDescription = "이건 에이아이가 만들어서 넣어줄거야"
            )
            subjectRepository.save(subject)
        }

        (1..50).forEach {
            val subject = subjects.random()
            val lecture = Lecture(
                year = 2025,
                semester = if (it % 2 == 0) 1 else 2,
                professorName = "교수${('A'..'Z').random()}",
                section = "00${(1..3).random()}",
                isEnglish = (0..1).random() == 1,
                lecturePlace = "정보${(65..70).random().toChar()}${100 + it}",
                capacity = listOf(40, 50, 60).random(),
                syllabusUrl = "https://example.com/syllabus/cs$it",
                note = "특이사항 없음",
                language = if ((0..1).random() == 1) "한국어" else "영어",
                shortDescription = "과목에 대한 간단한 설명 $it",
                createdAt = LocalDateTime.now(),
                subject = subject,
                times = mutableListOf()
            )

                val time1 = LectureTime(
                    day = DayOfWeek.entries.toTypedArray().slice(0..4).random() ,
                    startTime = LocalTime.of((8..14).random(), 0),
                    endTime = LocalTime.of((15..18).random(), 0),
                    lecture = lecture
                )
                val time2 = LectureTime(
                    day = DayOfWeek.entries.toTypedArray().slice(0..4).random() ,
                    startTime = LocalTime.of((8..14).random(), 0),
                    endTime = LocalTime.of((15..18).random(), 0),
                    lecture = lecture
                )
                lecture.times += listOf(time1, time2)
                lectureRepository.save(lecture)
            }


        println("✅ 테스트용 Subject/Category/Lecture 50개 생성 완료")
    }
}
