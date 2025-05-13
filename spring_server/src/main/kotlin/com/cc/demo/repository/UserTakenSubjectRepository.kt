package com.cc.demo.repository

import com.cc.demo.entity.UserTakenSubject
import org.springframework.data.jpa.repository.JpaRepository

interface UserTakenSubjectRepository : JpaRepository<UserTakenSubject, Long>
