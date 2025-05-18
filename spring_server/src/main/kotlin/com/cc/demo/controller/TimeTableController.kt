package com.cc.demo.controller

import com.cc.demo.entity.RecommendedTimetable
import com.cc.demo.repository.LectureCartRepository
import com.cc.demo.response.RecommendedTimetableResponse
import com.cc.demo.response.ReportData
import com.cc.demo.response.TimetableResponse
import com.cc.demo.service.TimeTableService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RequestMapping("/timetable")
@RestController
class TimeTableController (
    private val timeTableService: TimeTableService
){

    @GetMapping("/")
    fun generate(
        @RequestParam(name = "min_credit", required = true) minCredit: Int,
        @RequestParam(name = "max_credit", required = true) maxCredit: Int

    ): ResponseEntity<Any> {

        return try {
            val userId : Long = 1 //Todo : 여기 실제로는 jwt 기반으로 고쳐야 함. 일단 테스트용
            val timetables = timeTableService.generateTimeTable(userId, minCredit, maxCredit)

            val response = RecommendedTimetableResponse(
                filteredTimetables = timetables.map { TimetableResponse.from(it) }
            )

            ResponseEntity.ok(response)

        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mapOf("error" to "Failed to generate timetables", "details" to e.localizedMessage))
        }
    }

    @PostMapping("/")
    fun search(){

    }

}