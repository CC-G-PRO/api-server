package com.cc.demo.response

import com.cc.demo.client.dto.AiFilteredLectureDto


data class AiFilterResponse(
    val userWantedKeywords: List<String>,
    val filteredLectures: List<AiFilteredLectureDto>
)
