package com.cc.demo.repository

import com.cc.demo.entity.GraduationEvaluation
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface GraduationEvaluationRepository : JpaRepository<GraduationEvaluation, Long>{

    @Modifying
    @Query("DELETE FROM GraduationEvaluation g WHERE g.user.id = :userId")
    fun deleteByUserId(@Param("userId") userId: Long)

    fun findByUserId(@Param("userId") userId: Long): List<GraduationEvaluation>
}
