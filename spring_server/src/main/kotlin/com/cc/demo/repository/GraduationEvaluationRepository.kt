package com.cc.demo.repository

import com.cc.demo.entity.GraduationEvaluation
import org.springframework.data.jpa.repository.JpaRepository

interface GraduationEvaluationRepository : JpaRepository<GraduationEvaluation, Long>
