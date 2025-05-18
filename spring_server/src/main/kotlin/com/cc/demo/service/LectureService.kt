package com.cc.demo.service

import com.cc.demo.client.AiClient
import com.cc.demo.repository.LectureRepository
import com.cc.demo.repository.UserTakenSubjectRepository
import com.cc.demo.request.CourseSearchRequest
import com.cc.demo.response.CourseResponse
import mu.KotlinLogging
import org.springframework.stereotype.Service

private val log = KotlinLogging.logger {}

@Service
class LectureService(
    private val lectureRepository: LectureRepository,
    private val userTakenSubjectRepository: UserTakenSubjectRepository,
    private val aiClient: AiClient
) {
    fun searchLectures(request: CourseSearchRequest, userId: Long): List<CourseResponse> {
        log.info { "🎯 강의 검색 요청: userId=$userId, 요청조건=$request" }

        val excludeCodes = if (request.excludeCompleted) {
            val codes = userTakenSubjectRepository.findByUserId(userId).map { it.subjectCode }
            log.info { "⛔ 수강 완" +
                    "료 과목 제외: $codes" }
            codes
        } else emptyList()

        val initialLectures = when (request.type) {
            "전공" -> {
                val category = request.category ?: ""
                val majors = lectureRepository.findMajorLectures(category)
                log.info { "🔍 전공 검색 결과: ${majors.size}개 (category='$category')" }
                majors
            }
            "교양" -> {
                val area = request.area ?: throw IllegalArgumentException("교양 검색 시 area는 필수입니다")
                val generals = lectureRepository.findGeneralLectures(area)
                log.info { "🔍 교양 검색 결과: ${generals.size}개 (area='$area')" }
                generals
            }
            else -> throw IllegalArgumentException("지원하지 않는 type입니다")
        }

        val filtered = if (excludeCodes.isEmpty()) {
            initialLectures
        } else {
            val result = initialLectures.filter { it.subject.subjectCode !in excludeCodes }
            log.info { "📉 수강 제외 후 남은 강의 수: ${result.size}" }
            result
        }

        val final = if (request.keywords.isNullOrEmpty()) {
            filtered
        } else {
            log.info { "🤖 AI 필터링 시작 - 키워드: ${request.keywords}" }
            val aiFiltered = aiClient.filterByKeyword(request.keywords, filtered)
            log.info { "🎯 AI 필터링 결과: ${aiFiltered.size}개" }
            aiFiltered
        }

        log.info { "✅ 최종 응답 강의 수: ${final.size}" }
        return final.map { CourseResponse.from(it) }
    }
}
