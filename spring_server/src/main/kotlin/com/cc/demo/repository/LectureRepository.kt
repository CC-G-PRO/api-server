package com.cc.demo.repository

import com.cc.demo.entity.Lecture
import com.cc.demo.enumerate.Category
import com.cc.demo.enumerate.MajorCategory
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface LectureRepository : JpaRepository<Lecture, Long> {
    @Query("""
        SELECT DISTINCT l FROM Curriculum c
        JOIN c.subject s
        JOIN s.lectures l
        WHERE c.majorCategory = :majorCategory
    """)
    fun findMajorLectures(@Param("majorCategory") majorCategory: MajorCategory)
        : List<Lecture>


    @Query("""
        SELECT DISTINCT l FROM Lecture l
        JOIN l.subject s
        WHERE s.category = :category
    """)
    fun findGeneralLectures(@Param("category") category: Category)
        : List<Lecture>
}
