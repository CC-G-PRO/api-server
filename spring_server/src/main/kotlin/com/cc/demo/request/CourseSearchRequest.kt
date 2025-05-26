package com.cc.demo.request

import com.cc.demo.enumerate.Category
import com.cc.demo.enumerate.MajorCategory

data class CourseSearchRequest(
    /**
     *   REQUIRED_GENERAL,        // 필수교양
     *   DISTRIBUTION_GENERAL,    // 배분이수교양
     *   FREE_GENERAL,            // 자유이수교양
     *   MAJOR                    // 전공
     */
    val category: Category,

    /**    (전공일 경우에만 입력받음)
     *     MAJOR_ELECTIVE,   // 전공선택
     *     MAJOR_REQUIRED,   // 전공필수
     *     MAJOR_BASIC       // 전공기초
     */
    val majorCategory: MajorCategory? = null,
    val keywords: List<String>? = null,             // 입력 키워드
    val excludeCompleted: Boolean = false,   // 이수 과목 제외 여부
    val page: Int = 1,
    val size: Int = 10
)