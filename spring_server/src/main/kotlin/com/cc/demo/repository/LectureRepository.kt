package com.cc.demo.repository

import com.cc.demo.entity.Lecture
import org.springframework.data.jpa.repository.JpaRepository

interface LectureRepository : JpaRepository<Lecture, Long>
