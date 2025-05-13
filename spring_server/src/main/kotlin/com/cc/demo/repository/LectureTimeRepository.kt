package com.cc.demo.repository

import com.cc.demo.entity.LectureTime
import org.springframework.data.jpa.repository.JpaRepository

interface LectureTimeRepository : JpaRepository<LectureTime, Long>
