package com.cc.demo.repository

import com.cc.demo.entity.Curriculum
import org.springframework.data.jpa.repository.JpaRepository

interface CurriculumRepository: JpaRepository<Curriculum, Long>{
    fun findByEntryYear(entryYear: Int): List<Curriculum>
}