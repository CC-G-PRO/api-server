package com.cc.demo.repository

import com.cc.demo.entity.Subject
import org.springframework.data.jpa.repository.JpaRepository

interface SubjectRepository : JpaRepository<Subject, Long>
