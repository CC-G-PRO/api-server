package com.cc.demo.response

import com.cc.demo.entity.GraduationEvaluation
import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

data class ReportUploadResponse(
    val message: String,
    val data: GraduationEvaluation
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class ReportData(
    @JsonProperty("basic_info")
    val basicInfo: BasicInfo,

    @JsonProperty("graduation_info")
    val graduationInfo: GraduationInfo,

    @JsonProperty("liberal_arts_info")
    val liberalArtsInfo: List<LiberalArtsInfo>,

    @JsonProperty("major_info")
    val majorInfo: MajorInfo,

    @JsonProperty("course_info")
    val courseInfo: List<CourseInfo>
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class BasicInfo(
    @JsonProperty("student_number")
    val studentNumber: String,

    @JsonProperty("student_name")
    val studentName: String,

    val department: String,

    val grade: Int,

    @JsonProperty("enroll_semester")
    val enrollSemester: Int,

    @JsonProperty("evaluation_date")
    val evaluationDate: String
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class GraduationInfo(
    val credit: GraduationRequirement<Int>,
    val grades: GraduationRequirement<Float>,
    val english: GraduationRequirement<Int>,
    val paper: GraduationRequirement<Int>
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class GraduationRequirement<T>(
    val earned: T,
    val required: T,
    val valid: Boolean
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class LiberalArtsInfo(
    val category: String,
    val required: Int,
    val earned: Int,
    val valid: Boolean
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class MajorInfo(
    @JsonProperty("advanced_major")
    val advancedMajor: String,

    @JsonProperty("reference_year")
    val referenceYear: String,

    @JsonProperty("major_basic")
    val majorBasic: RequirementCredit,

    @JsonProperty("major_required")
    val majorRequired: RequirementCredit,

    @JsonProperty("major_required_plus_elective")
    val majorRequiredPlusElective: RequirementCredit,

    val passed: Boolean,

    @JsonProperty("major_industry_required")
    val majorIndustryRequired: RequirementCredit
)

data class RequirementCredit(
    val earned: Int,
    val required: Int
)

data class CourseInfo(
    @JsonProperty("subject_code")
    val subjectCode: String,
    @JsonProperty("lecture_code")
    val lectureCode: String,
    @JsonProperty("subject_name")
    val subjectName: String,
    @JsonProperty("enroll_year")
    val enrollYear: String,
    @JsonProperty("enroll_semester")
    val enrollSemester: String,

    val category: String
)

