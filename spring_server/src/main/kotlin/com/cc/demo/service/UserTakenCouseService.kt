package com.cc.demo.service

import com.cc.demo.entity.UserTakenSubject
import com.cc.demo.repository.UserRepository
import com.cc.demo.repository.UserTakenSubjectRepository
import com.cc.demo.response.CourseInfo
import mu.KotlinLogging
import org.springframework.stereotype.Service


@Service
class UserTakenCouseService (
    private val userRepository: UserRepository,
    private val userTakenSubjectRepository: UserTakenSubjectRepository

){
    fun saveCourseInfos(userId: Long, courseInfos: List<CourseInfo>) {
        val user = userRepository.findById(userId)
            .orElseThrow { IllegalArgumentException("사용자 정보를 찾을 수 없습니다.") }
        print(courseInfos[0].lectureCode)
        val subjects = courseInfos.map {
            UserTakenSubject(
                user = user,
                subjectCode = it.lectureCode,
                subjectName = it.subjectName,
                enrollYear = it.enrollYear.toInt(),
                enrollSemester = it.enrollSemester.toInt(),
                category = it.category
            )
        }
        userTakenSubjectRepository.saveAll(subjects)
    }

}