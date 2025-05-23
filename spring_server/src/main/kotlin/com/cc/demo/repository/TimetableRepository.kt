package com.cc.demo.repository

import com.cc.demo.entity.TimeTable
import org.springframework.data.jpa.repository.JpaRepository

interface TimetableRepository : JpaRepository<TimeTable, Long>{

    fun findByUserId(userId: Long): List<TimeTable>
}
