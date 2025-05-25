package com.cc.demo.controller

import com.cc.demo.enumerate.TimeTableType
import com.cc.demo.request.TimeTableCreateRequest
import com.cc.demo.request.TimeTableFileterRequest
import com.cc.demo.request.TimeTableUpdateRequest
import com.cc.demo.response.CommonResponse
import com.cc.demo.response.GraduationEvaluationPreview
import com.cc.demo.response.RecommendedTimetableResponse
import com.cc.demo.response.TimetableResponse
import com.cc.demo.service.NLPService
import com.cc.demo.service.TimeTableService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController


// TODO: 추후 JWT에서 사용자 ID 추출하도록 수정
//TODO :  졸업 사정 진단표 기반으로 특정 시간표와 이수 후 졸업 사정 진단표 충족하는지 여부 조사. <- 이거 어떻게 할건지도 한 번 알아봐야겟다.

@RequestMapping("/timetable")
@RestController
class TimeTableController (
    private val timeTableService: TimeTableService,
    private val nlpService: NLPService
){

    @GetMapping("/random")
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
        @RequestBody req: TimeTableFileterRequest
    ): ResponseEntity<Any> {
        return try {
            val userId: Long = 1

            val prompt = nlpService.generatePrompt(req.filter)

            val filter = nlpService.callNlpApi(prompt)

            val allTimetables = timeTableService.getTimeTables(userId, type = TimeTableType.GENERATED)
            val filteredTimetables = timeTableService.filterTimeTable(filter, allTimetables)

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

    //TODO : lectures 시간표 겹치는지 확인해주고 create, update 할 것.

    //type 지정으로 무작위 생성인지 , 사용자가 커스텀한 건지 구분해서 가져올 수 있음.
    @GetMapping("/")
    fun getList(
        @RequestParam(name = "type", required = true) type : TimeTableType,
    ): ResponseEntity<Any> {
        val userId: Long = 1

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
        ): ResponseEntity<Any> {
        val userId: Long = 1

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
        catch (e: Exception){
            ResponseEntity.status(500).body(e.message) //response 부분 status code 정밀하게 해야함.
        }
    }

    //특정 id 의 timetable 가져옴.
    @GetMapping("/{id}")
    fun get(@PathVariable id: Long): ResponseEntity<Any> {
        val userId: Long = 1

        return try {
            val timeTable: TimetableResponse = timeTableService.getTimetableDetails(userId,id)
            val gradInfo : GraduationEvaluationPreview = timeTableService.graduationInfoWithTimeTable(userId, timeTable)

            val data = {
                "timetable" to timeTable
                "graduation_info" to gradInfo
            }
            ResponseEntity.ok(
                CommonResponse(
                    message = "Success to Get data",
                    valid = true,
                    data = data,
                )
            )
        } catch (e: Exception) {
            ResponseEntity.status(500).body(e.message)
        }
    }

    //저장이 필요한 경우에는 단순히 id 값만 넘기면 되는 로직임.
    @PutMapping("/{id}")
    fun put(@PathVariable id: Long, @RequestBody request: TimeTableUpdateRequest): ResponseEntity<Any> {

        return try {
            val userId : Long = 1
            val res : TimetableResponse =  timeTableService.updateTimeTable(content = request, userId = userId, timeTableId = id)

            ResponseEntity.ok(CommonResponse(
                message = "Success to put data",
                valid = true,
                data = res
            ))

        } catch (e: IllegalArgumentException) {
            ResponseEntity.status(404).body(mapOf("error" to e.message))
        } catch (e: Exception) {
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