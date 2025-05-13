package com.cc.demo.repository

import com.cc.demo.entity.CategoryMain
import org.springframework.data.jpa.repository.JpaRepository

interface CategoryMainRepository : JpaRepository<CategoryMain, Long>
