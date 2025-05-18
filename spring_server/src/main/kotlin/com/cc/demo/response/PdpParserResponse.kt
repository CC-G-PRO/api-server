package com.cc.demo.response

import com.fasterxml.jackson.annotation.JsonIgnoreProperties


data class ReportUploadResponse(
    val message: String,
    val report_id: Long,
    val data: ReportData
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class ReportData(
    val basic_info: BasicInfo,
    val graduation_info: GraduationInfo,
    val liberal_arts_info: List<LiberalArtsInfo>,
    val major_info: MajorInfo
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class BasicInfo(
    val student_number: String,
    val student_name: String,
    val department: String,
    val grade: String,
    val enroll_semester: String,
    val evaluation_date: String
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class GraduationInfo(
    val credit: GraduationRequirement,
    val grades: GraduationRequirement,
    val english: GraduationRequirement,
    val paper: GraduationRequirement
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class GraduationRequirement(
    val earned: String,
    val required: String,
    val valid: Boolean
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class LiberalArtsInfo(
    val category: String,
    val required: String,
    val earned: String,
    val valid: Boolean
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class MajorInfo(
    val advanced_major: String,
    val reference_year: Int,
    val major_basic: MajorRequirement,
    val major_required: MajorRequirement,
    val major_required_plus_elective: MajorRequirement,
    val passed: Boolean,
    val major_industry_required: MajorRequirement
)

data class MajorRequirement(
    val earned: String,
    val required: String
)
