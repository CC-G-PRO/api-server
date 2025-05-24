package com.cc.demo.request

import com.cc.demo.enumerate.TimeTableType
import com.fasterxml.jackson.annotation.JsonIgnoreProperties

//time table 생성 명령어도 잇음.
data class TimeTableCreateRequest (

    val timeTableId : Long,
    val userId : Long,
    val lectures : List<Long>, //lecture Id list
    val type: TimeTableType = TimeTableType.CUSTOM, //deafult 로
    )

data class TimeTableUpdateRequest(
    val lectures: List<Long>?,
)

data class TimeTableFileterRequest(
    val filter : String,
)
