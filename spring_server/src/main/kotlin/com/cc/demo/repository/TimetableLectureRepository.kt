package com.cc.demo.repository

import com.cc.demo.entity.TimeTableLecture
import org.springframework.data.jpa.repository.JpaRepository

interface TimetableLectureRepository : JpaRepository<TimeTableLecture, Long>{
    fun findByTimetableId(timetableId: Long): List<TimeTableLecture>
    fun deleteByTimetableId(timetableId: Long)
}
