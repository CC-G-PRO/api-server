package com.cc.demo.repository

import com.cc.demo.entity.LectureCart
import com.cc.demo.entity.User
import org.springframework.data.jpa.repository.JpaRepository

interface LectureCartRepository : JpaRepository<LectureCart, Long>{
    fun findByUserId(userId: Long): List<LectureCart>
    fun findAllByUser(user: User): List<LectureCart>
    fun deleteByUserAndLectureId(user: User, lectureId: Long)
    fun findByUserAndLectureId(user: User, lectureId: Long): LectureCart?
}
