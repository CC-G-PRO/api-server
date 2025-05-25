package com.cc.demo.converter

import com.cc.demo.entity.GraduationEvaluation
import com.cc.demo.entity.User
import com.cc.demo.response.ReportData
import java.time.LocalDateTime

class GraduationEvaluationConverter {

    companion object{
        fun convertToGraduationEvaluation(
            reportData: ReportData,
            user: User
        ): GraduationEvaluation {
            val basicInfo = reportData.basicInfo
            val graduationInfo = reportData.graduationInfo

            return GraduationEvaluation(
                user = user,
                entryYear = basicInfo.curriculumYear,
                studentName = basicInfo.studentName,
                department = basicInfo.department,
                leftSemester = basicInfo.enrollSemester,

                evaluationDate = LocalDateTime.parse(basicInfo.evaluationDate),

                totalCreditsEarned = graduationInfo.credit.earned,
                totalCreditsRequired = graduationInfo.credit.required,

                generalFreeCreditsEarned = reportData.liberalArtsInfo.find { it.category == "자유이수" }?.earned ?: 0,
                generalFreeCreditsRequired = reportData.liberalArtsInfo.find { it.category == "자유이수" }?.required ?: 0,

                generalBreadthCreditsEarned = reportData.liberalArtsInfo.find { it.category.contains("배분이수교과")}?.earned,
                generalBreadthCreditsRequired = reportData.liberalArtsInfo.find { it.category.contains("배분이수교과") }?.required,

                generalRequiredCreditsEarned = reportData.liberalArtsInfo.find { it.category == "필수교과" }?.earned ?: 0,
                generalRequiredCreditsRequired = reportData.liberalArtsInfo.find { it.category == "필수교과" }?.required ?: 0,

                majorBasicCreditsEarned = reportData.majorInfo.majorBasic.earned,
                majorBasicCreditsRequired = reportData.majorInfo.majorBasic.required,

                majorRequiredCreditsEarned = reportData.majorInfo.majorRequired.earned,
                majorRequiredCreditsRequired = reportData.majorInfo.majorRequired.required,

                majorElectiveCreditsEarned = reportData.majorInfo.majorRequiredPlusElective.earned,
                majorElectiveCreditsRequired = reportData.majorInfo.majorRequiredPlusElective.required,

                majorIndustryCreditsEarned = reportData.majorInfo.majorIndustryRequired.earned,
                majorIndustryCreditsRequired =  reportData.majorInfo.majorIndustryRequired.required,

                hasGraduationThesis = graduationInfo.paper.earned > 0,
                englishCourseCount = graduationInfo.english.earned,
                englishCourseRequired = graduationInfo.english.required,
                studentNumber = reportData.basicInfo.studentNumber
            )
        }


    }
}