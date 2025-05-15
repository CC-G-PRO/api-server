package com.cc.demo.request

data class CourseSearchRequest(
    val type: String,                        // "전공" or "교양"
    val category: String? = null,            // "필수", "선택", "기초" (전공 전용)
    val area: String? = null,                // "배분이수1", ... (교양 전용)
    val keywords: List<String>? = null,             // 입력 키워드
    val excludeCompleted: Boolean = false,   // 이수 과목 제외 여부
    val page: Int = 1,
    val size: Int = 10
)