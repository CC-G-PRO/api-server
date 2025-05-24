import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue

//GPT 응답 구조
@JsonIgnoreProperties(ignoreUnknown = true)
data class GPTResponse(val output: List<Output>)

@JsonIgnoreProperties(ignoreUnknown = true)
data class Output(
    val content: List<Content>
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class Content(val type: String, val text: String)

@JsonIgnoreProperties(ignoreUnknown = true)
data class TimeTableFilter(
    val includeSubjects: List<String>,
    val excludeSubjects: List<String>,
    val includeProfessors: List<String>,
    val excludeProfessors: List<String>,
    val allowedDays: List<String>,
    val minFreeHours: Int?, //
    val maxClassesPerDay: Int?, //하루에 최대 수업 몇 개
    val excludeBeforeMinutes: Int? //공강 텀 몇 분?
)

//TimeTableFilter 추출
fun extractTimeTableFilterFromGPTResponse(responseJson: String): TimeTableFilter {
    val mapper = jacksonObjectMapper()
    val gptResponse = mapper.readValue<GPTResponse>(responseJson)

    val rawJson = gptResponse.output
        .firstOrNull()?.content
        ?.firstOrNull()?.text
        ?.removePrefix("```json\n")
        ?.removeSuffix("\n```")
        ?: throw IllegalArgumentException("잘못된 GPT 응답 형식입니다.")

    return mapper.readValue(rawJson)
}
