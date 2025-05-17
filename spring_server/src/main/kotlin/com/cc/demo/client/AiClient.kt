package com.cc.demo.client

import com.cc.demo.response.AiFilterResponse
import com.cc.demo.entity.Lecture
import mu.KotlinLogging
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate

private val log = KotlinLogging.logger {}

@Component
class AiClient(
    private val restTemplate: RestTemplate = RestTemplate()
) {
    companion object {
        private const val AI_URL = "http://ai-server.recommend-api"
    }

    fun filterByKeyword(keywords: List<String>, lectures: List<Lecture>): List<Lecture> {
        log.info { "🧠 AI 필터 요청 시작 - 키워드: $keywords, 강의 수: ${lectures.size}" }

        val payload = mapOf(
            "userWantedKeywords" to keywords,
            "filteredLectures" to lectures.map { lecture ->
                mapOf(
                    "lectureId" to lecture.id,
                    "courseName" to lecture.subject.subjectName,
                    "aiDescription" to lecture.aiDescription
                )
            }
        )

        val headers = HttpHeaders().apply {
            contentType = MediaType.APPLICATION_JSON
        }

        val requestEntity = HttpEntity(payload, headers)

        val response = restTemplate.exchange(
            AI_URL,
            org.springframework.http.HttpMethod.POST,
            requestEntity,
            /**
             * ai 서버가 AiFilterResponse와 같은 응답 양식을 따라야함.
             */
            object : ParameterizedTypeReference<AiFilterResponse>() {}
        )

        val filtered = response.body?.filteredLectures ?: emptyList()
        log.info { "✅ AI 응답 수신 완료 - 필터링된 강의 수: ${filtered.size}" }

        val filteredIds = filtered.map { it.lectureId }
        return lectures.filter { it.id in filteredIds }
    }
}
