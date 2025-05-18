package com.cc.demo.repository

import com.cc.demo.entity.UserTakenSubject
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface UserTakenSubjectRepository : JpaRepository<UserTakenSubject, Long> {
    fun findByUserId(userId: Long): List<UserTakenSubject>

    @Modifying
    @Query("DELETE FROM UserTakenSubject c WHERE c.user.id = :userId")
    fun deleteAllByUserId(@Param("userId") userId: Long)

}