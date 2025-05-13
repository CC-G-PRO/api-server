package com.cc.demo.repository

import com.cc.demo.entity.Keyword
import org.springframework.data.jpa.repository.JpaRepository

interface KeywordRepository : JpaRepository<Keyword, Long>
