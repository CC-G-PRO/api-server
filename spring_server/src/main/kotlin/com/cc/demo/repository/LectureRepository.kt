package com.cc.demo.repository

import com.cc.demo.entity.Lecture
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface LectureRepository : JpaRepository<Lecture, Long> {

//    @Query("""
//        SELECT DISTINCT l FROM Lecture l
//        JOIN FETCH l.subject s
//        JOIN FETCH s.categoryMain c
//        JOIN FETCH l.times t
//        WHERE c.name LIKE %:category%
//    """)
//    fun findMajorLectures(@Param("category") category: String): List<Lecture>
//
//    @Query("""
//        SELECT DISTINCT l FROM Lecture l
//        JOIN FETCH l.subject s
//        JOIN FETCH s.categoryMain c
//        JOIN s.breadthGeneralEducationList b
//        JOIN FETCH l.times t
//        WHERE b.type = :area
//    """)
//    fun findGeneralLectures(@Param("area") area: String): List<Lecture>
}
