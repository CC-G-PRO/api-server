package com.cc.demo.client.dto


data class AiFilterResponse(
    val userWantedKeywords: List<String>,
    val filteredLectures: List<AiFilteredLectureDto>
)
