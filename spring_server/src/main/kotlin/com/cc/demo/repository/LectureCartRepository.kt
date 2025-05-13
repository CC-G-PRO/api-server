package com.cc.demo.repository

import com.cc.demo.entity.LectureCart
import org.springframework.data.jpa.repository.JpaRepository

interface LectureCartRepository : JpaRepository<LectureCart, Long>
