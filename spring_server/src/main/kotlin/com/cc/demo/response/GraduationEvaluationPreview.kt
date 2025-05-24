package com.cc.demo.response

import com.cc.demo.entity.GraduationEvaluation

data class GraduationEvaluationPreview(
    val original: GraduationEvaluation,
    val expected: GraduationEvaluation
)
