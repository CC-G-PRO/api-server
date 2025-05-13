package com.cc.demo.repository

import com.cc.demo.entity.RecommendedTimetable
import org.springframework.data.jpa.repository.JpaRepository

interface RecommendedTimetableRepository : JpaRepository<RecommendedTimetable, Long>
