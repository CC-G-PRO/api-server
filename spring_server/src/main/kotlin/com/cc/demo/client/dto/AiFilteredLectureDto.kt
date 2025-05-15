package com.cc.demo.client.dto

data class AiFilteredLectureDto(
    val lectureId: Long,
    val courseName: String,
    val description: String?,
    val aiDescription: String?
)