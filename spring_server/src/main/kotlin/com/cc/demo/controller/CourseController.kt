package com.cc.demo.controller

import com.cc.demo.request.CourseSearchRequest
import com.cc.demo.response.CourseResponse
import com.cc.demo.service.LectureService
import mu.KotlinLogging
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

private val log = KotlinLogging.logger {}

@RestController
class CourseController(
    private val courseService: LectureService,
) {
    /**
     * 페이징 미구현 (request parameter엔 있음)
     */
    @PostMapping("/courses")
    fun searchCourses(
        @RequestBody request: CourseSearchRequest,
    ): ResponseEntity<List<CourseResponse>> {
        log.info { "📥 /courses 요청 수신: $request" }

        // TODO: 테스트 목적, 임시 userId = 1
        val userId = 1L
        val result = courseService.searchLectures(request, userId)

        log.info { "📤 /courses 응답 완료 - 결과 강의 수: ${result.size}" }

        return ResponseEntity.ok(result)
    }
}
