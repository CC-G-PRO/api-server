package com.cc.demo.controller

import com.cc.demo.enumerate.TimeTableType
import com.cc.demo.exception.OverlappingLectureException
import com.cc.demo.request.TimeTableCreateRequest
import com.cc.demo.request.TimeTableFileterRequest
import com.cc.demo.request.TimeTableUpdateRequest
import com.cc.demo.response.CommonResponse
import com.cc.demo.response.GraduationEvaluationPreview
import com.cc.demo.response.RecommendedTimetableResponse
import com.cc.demo.response.TimetableResponse
import com.cc.demo.security.UserPrincipal
import com.cc.demo.service.NLPService
import com.cc.demo.service.TimeTableService
import mu.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController


private val log = KotlinLogging.logger {}

@RequestMapping("/timetable")
@RestController
class TimeTableController (
    private val timeTableService: TimeTableService,
    private val nlpService: NLPService
){

    @GetMapping("/random")
    fun generate(
        @RequestParam(name = "min_credit", required = true) minCredit: Int,
        @RequestParam(name = "max_credit", required = true) maxCredit: Int,
        @AuthenticationPrincipal user: UserPrincipal,
        ): ResponseEntity<Any> {

        return try {
            log.info { "🟢 time table 요청 수신" }

            val userId : Long = user.id
            val timetables = timeTableService.regenerateTimeTables(userId, minCredit, maxCredit)

            val response = RecommendedTimetableResponse(
                filteredTimetables = timetables.map { (timetable, lectures) ->
                    TimetableResponse.from(timetable, lectures)
                }
            )
            log.info { "🟢 time table 응답 완료" }
            ResponseEntity.ok(
                CommonResponse(
                    message = "Success to generate timetable",
                    valid =  true,
                    data = response
                )
            )

        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mapOf("error" to "Failed to generate timetables", "details" to e.localizedMessage))
        }
    }

    @PostMapping("/random")
    fun filter(
        @RequestBody req: TimeTableFileterRequest,
        @AuthenticationPrincipal user: UserPrincipal,
        ): ResponseEntity<Any> {
        return try {
            log.info { "🟢 time table filter 요청 수신" }

            val userId: Long = user.id

            val prompt = nlpService.generatePrompt(req.filter)

            val filter = nlpService.callNlpApi(prompt)

            val allTimetables = timeTableService.getTimeTables(userId, type = TimeTableType.GENERATED)
            val filteredTimetables = timeTableService.filterTimeTable(filter, allTimetables)

            log.info { "🟢 time table filter 응답 완료" }
            ResponseEntity.ok(
                CommonResponse(
                    message = "Successfully filtered timetables",
                    valid = true,
                    data = filteredTimetables
                )
            )
        } catch (e: Exception) {
            e.printStackTrace()
            ResponseEntity.status(500).body(mapOf("error" to (e.message ?: "Internal Server Error")))
        }
    }

    @GetMapping("/")
    fun getList(
        @RequestParam(name = "type", required = true) type : TimeTableType,
        @AuthenticationPrincipal user: UserPrincipal,
        ): ResponseEntity<Any> {
        val userId: Long = user.id

        return try {
            val results = when (type) {
                TimeTableType.GENERATED -> timeTableService.getTimeTables(userId, TimeTableType.GENERATED)
                TimeTableType.CUSTOM -> timeTableService.getTimeTables(userId, TimeTableType.CUSTOM)
            }

            val response = RecommendedTimetableResponse(filteredTimetables = results)
            ResponseEntity.ok(
                CommonResponse(
                    message = "Success to get a list of timetables",
                    valid = true,
                    data = response
                )
            )
        } catch (e: Exception) {
            ResponseEntity.status(500).body("서버 오류가 발생했습니다: ${e.message}")
        }
    }

    @PostMapping("/")
    fun create(
        @RequestBody req: TimeTableCreateRequest,
        @AuthenticationPrincipal user: UserPrincipal,
        ): ResponseEntity<Any> {
        val userId: Long = user.id

        return try{
            val result : TimetableResponse = timeTableService.createTimeTables(req, userId)
            ResponseEntity.ok(
                CommonResponse(
                    message = "Success to post new timetable",
                    valid = true,
                    data = result
                )
            )
        }
        catch (e: OverlappingLectureException) {
            ResponseEntity.status(400).body(CommonResponse(
                message = e.message ?: "시간표 겹침 오류",
                valid = false
            ))
        }
        catch (e: Exception){
            ResponseEntity.status(500).body(e.message) //response 부분 status code 정밀하게 해야함.
        }
    }

    //특정 id 의 timetable 가져옴.
    @GetMapping("/{id}")
    fun get(
        @PathVariable id: Long,
        @AuthenticationPrincipal user: UserPrincipal,
    ): ResponseEntity<Any> {
        val userId: Long = user.id

        return try {
            val timeTable: TimetableResponse = timeTableService.getTimetableDetails(userId,id)
            val gradInfo : GraduationEvaluationPreview = timeTableService.graduationInfoWithTimeTable(userId, timeTable)

            val data = mapOf(
                "timetable" to timeTable,
                "graduation_info" to gradInfo
            )
            ResponseEntity.ok(
                CommonResponse(
                    message = "Success to Get data",
                    valid = true,
                    data = data,
                )
            )
        }
        catch (e: Exception) {
            ResponseEntity.status(500).body(e.message)
        }
    }

    //저장이 필요한 경우에는 단순히 id 값만 넘기면 되는 로직임.
    @PutMapping("/{id}")
    fun put(@PathVariable id: Long,
            @RequestBody request: TimeTableUpdateRequest,
            @AuthenticationPrincipal user: UserPrincipal,
    ): ResponseEntity<Any> {

        return try {
            val userId : Long = user.id
            val res : TimetableResponse =  timeTableService.updateTimeTable(content = request, userId = userId, timeTableId = id)

            ResponseEntity.ok(CommonResponse(
                message = "Success to put data",
                valid = true,
                data = res
            ))

        }
        catch (e: IllegalArgumentException) {
            ResponseEntity.status(404).body(mapOf("error" to e.message))
        }
        catch (e: OverlappingLectureException) {
            ResponseEntity.status(400).body(CommonResponse(
                message = e.message ?: "시간표 겹침 오류",
                valid = false
            ))
        }
        catch (e: Exception) {
            ResponseEntity.status(500).body(mapOf("error" to "서버 오류가 발생했습니다.", "details" to e.localizedMessage))
        }
    }


    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: Long): ResponseEntity<Any> {
        return try {
            timeTableService.deleteTimetable(id)
            ResponseEntity.ok(CommonResponse(
                message = "Success to delete",
                valid = false
            ))
        } catch (e: IllegalArgumentException) {
            ResponseEntity.status(404).body(mapOf("error" to e.message))
        } catch (e: Exception) {
            ResponseEntity.status(500).body(mapOf("error" to "서버 오류가 발생했습니다.", "details" to e.localizedMessage))
        }
    }
}