package com.cc.demo.repository

import com.cc.demo.entity.RecommendedTimetableLecture
import org.springframework.data.jpa.repository.JpaRepository

interface RecommendedTimetableLectureRepository : JpaRepository<RecommendedTimetableLecture, Long>
