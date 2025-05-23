package com.cc.demo.controller

import com.cc.demo.enumerate.TimeTableType
import com.cc.demo.response.RecommendedTimetableResponse
import com.cc.demo.response.TimetableResponse
import com.cc.demo.service.TimeTableService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RequestMapping("/timetable")
@RestController
class TimeTableController (
    private val timeTableService: TimeTableService,
){

    @PostMapping("/random")
    fun generate(
        @RequestParam(name = "min_credit", required = true) minCredit: Int,
        @RequestParam(name = "max_credit", required = true) maxCredit: Int

    ): ResponseEntity<Any> {

        return try {

            val userId : Long = 1 //Todo : 여기 실제로는 jwt 기반으로 고쳐야 함. 일단 테스트용

            val timetables = timeTableService.regenerateTimeTables(userId, minCredit, maxCredit)

            val response = RecommendedTimetableResponse(
                filteredTimetables = timetables.map { (timetable, lectures) ->
                    TimetableResponse.from(timetable, lectures)
                }
            )

            ResponseEntity.ok(response)

        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mapOf("error" to "Failed to generate timetables", "details" to e.localizedMessage))
        }
    }

    //가장 최근에 추천된 시간표를 가져옴.
    @GetMapping("/")
    fun getRecommendedTimetable(
        @RequestParam(name = "type", required = true) type : TimeTableType,
    ): ResponseEntity<Any> {
        val userId: Long = 1 // TODO: 추후 JWT에서 사용자 ID 추출하도록 수정

        return try {
            val results = when (type) {
                TimeTableType.GENERATED -> timeTableService.getTimeTables(userId, TimeTableType.GENERATED)
                TimeTableType.CUSTOM -> timeTableService.getTimeTables(userId, TimeTableType.CUSTOM)
            }

            val response = RecommendedTimetableResponse(filteredTimetables = results)
            ResponseEntity.ok(response)
        } catch (e: Exception) {
            ResponseEntity.status(500).body("서버 오류가 발생했습니다: ${e.message}")
        }
    }

    //특정 id 의 timetable 가져옴.
    @GetMapping("/{id}")
    fun getTimeTable(@PathVariable id: Long): ResponseEntity<Any> {
        val userId: Long = 1

        return try {
            val result: TimetableResponse = timeTableService.getTimetableDetails(userId)
            ResponseEntity.ok(result)
        } catch (e: Exception) {
            ResponseEntity.status(500).body(e.message)
        }
    }
    @PutMapping("/{id}")
    fun deleteTimeTable(@PathVariable id: Long): ResponseEntity<Any> {
        return try {
            timeTableService.deleteTimetable(id)
            ResponseEntity.ok(mapOf("message" to "시간표가 성공적으로 삭제되었습니다. id=$id"))
        } catch (e: IllegalArgumentException) {
            ResponseEntity.status(404).body(mapOf("error" to e.message))
        } catch (e: Exception) {
            ResponseEntity.status(500).body(mapOf("error" to "서버 오류가 발생했습니다.", "details" to e.localizedMessage))
        }
    }


    @DeleteMapping("/{id}")
    fun deleteTimeTable(@PathVariable id: Long): ResponseEntity<Any> {
        return try {
            timeTableService.deleteTimetable(id)
            ResponseEntity.ok(mapOf("message" to "시간표가 성공적으로 삭제되었습니다. id=$id"))
        } catch (e: IllegalArgumentException) {
            ResponseEntity.status(404).body(mapOf("error" to e.message))
        } catch (e: Exception) {
            ResponseEntity.status(500).body(mapOf("error" to "서버 오류가 발생했습니다.", "details" to e.localizedMessage))
        }
    }






//    @PostMapping("/")
//    fun search(
//        @RequestBody prompt: List<String>
//    ) : ResponseEntity<Any> {
//        val userId : Long = 1 //Todo : 여기 실제로는 jwt 기반으로 고쳐야 함. 일단 테스트용
//
//
//    }

}