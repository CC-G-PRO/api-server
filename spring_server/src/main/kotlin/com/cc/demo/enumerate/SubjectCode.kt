package com.cc.demo.enumerate

import kotlin.text.contains

//그냥 맵핑요으로 사용함.
enum class SubjectCode(val code: String, val description: String) {
    MAJOR_BASIC("11", "전공 기초 과목"),
    MAJOR_REQUIRED("04", "전공 필수 과목"),
    MAJOR_ELECTIVE("05", "전공 선택 과목"),
    GENERAL_REQUIRED("16", "교양 필수"),
    GENERAL_DISTRIBUTED("15", "배분이수 교양"),
    GENERAL_FREE("17", "자유이수 교양");

    companion object {
        private val codeMap = SubjectCode.entries.associateBy { it.code }

        fun fromCode(code: String): SubjectCode? = codeMap[code]
    }
}

enum class IndustryCode(val code: String) {
    STARTUP_BUSINESS("SW스타트업비즈니스"),
    STARTUP_PROJECT("SW스타트업프로젝트"),
    BIGDATA_PROGRAMMING("빅데이터프로그래밍"),
    BIGDATA_PROJECT("빅데이터프로젝트"),
    CLOUD_COMPUTING("클라우드컴퓨팅"),
    CLOUD_PROJECT("클라우드프로젝트"),
    MOBILE_WEB_PROGRAMMING("모바일/웹서비스프로그래밍"),
    MOBILE_WEB_PROJECT("모바일/웹서비스프로젝트"),
    BLOCKCHAIN("블록체인"),
    BLOCKCHAIN_PROJECT("블록체인프로젝트"),
    TECHNOLOGY_COLLOQUIUM_1("최신기술콜로키움1"),
    TECHNOLOGY_COLLOQUIUM_2("최신기술콜로키움2(SWCON)"),
    STARTUP_PRACTICE("창업현장실습"),
    FIELD_PRACTICE("단기현장실습/장기현장실습"),
    RESEARCH_ACTIVITY("연구연수활동1･2");

    companion object {
        private val codeMap = IndustryCode.entries.associateBy { it.code }

        fun fromSubjectName(name: String): IndustryCode? {
            return codeMap[name]
        }

        val industryRequiredSubjectNames: Set<String>
            get() = codeMap.keys
    }
}
