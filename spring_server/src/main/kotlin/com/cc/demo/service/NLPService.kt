package com.cc.demo.service

import TimeTableFilter
import com.cc.demo.config.GptConfig
import extractTimeTableFilterFromGPTResponse
import mu.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient

private val log = KotlinLogging.logger {}

@Service
class NLPService (
    private val gptConfig: GptConfig,
    private val webClient: WebClient // WebClient는 Bean으로 등록되어 있어야 함

){

    fun generatePrompt(naturalText: String): String {
        return """
        You are responsible for generating a JSON object for timetable filtering.
        Based on the user's description, create a JSON in the following format:

        TimeTableFilter Fields Description:
        - includeSubjects: Subjects that must appear in the timetable. If specified, at least one must be included.
        - excludeSubjects: Subjects that must not appear. Any timetable containing these is excluded.
        - includeProfessors: Professors that must teach at least one course in the timetable.
        - excludeProfessors: Professors whose courses should be excluded from the timetable.
        - allowedDays: Days of the week when classes are allowed. Timetables with classes on other days are excluded.
        - minFreeHours: Minimum break time (in hours) required between classes on the same day.
        - maxClassesPerDay: Maximum number of classes allowed per day.
        - excludeBeforeMinutes: Exclude timetables with classes starting before 9:00 AM plus this many minutes.

        JSON format to return:

        {
          "includeSubjects": [],
          "excludeSubjects": [],
          "includeProfessors": [],
          "excludeProfessors": [],
          "allowedDays": [],
          "minFreeHours": null,
          "maxClassesPerDay": null,
          "excludeBeforeMinutes": null
        }

        User's input: "$naturalText"

        Resulting JSON:
    """.trimIndent()
    }

    fun callNlpApi(prompt: String): TimeTableFilter {
        val requestBody = mapOf(
            "model" to gptConfig.model,
            "input" to prompt
        )

        val responseJson = webClient.post()
            .uri("https://api.openai.com/v1/responses")
            .header("Authorization", "Bearer ${gptConfig.secretKey}")
            .header("Content-Type", "application/json")
            .bodyValue(requestBody)
            .retrieve()
            .bodyToMono(String::class.java)
            .block() ?: throw RuntimeException("No response from OpenAI API")

        log.info("gpt-4o-mini-Received JSON: $responseJson")
        val filter: TimeTableFilter = extractTimeTableFilterFromGPTResponse(responseJson)
        println(filter)

        return filter
    }
}