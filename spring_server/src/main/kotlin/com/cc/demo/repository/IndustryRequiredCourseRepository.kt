package com.cc.demo.repository

import com.cc.demo.entity.IndustryRequiredCourse
import org.springframework.data.jpa.repository.JpaRepository

interface IndustryRequiredCourseRepository : JpaRepository<IndustryRequiredCourse, Long>
