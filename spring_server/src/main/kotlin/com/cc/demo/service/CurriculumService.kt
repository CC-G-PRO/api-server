package com.cc.demo.service

import com.cc.demo.entity.Curriculum
import com.cc.demo.enumerate.MajorCategory
import com.cc.demo.repository.CurriculumRepository
import org.springframework.stereotype.Service

@Service
class CurriculumService(
    private val curriculumRepository: CurriculumRepository,
) {
    //전공 분류 정보 map 형태로 반환함. subject Id 검색하면 majorCategory 확인할 수 있게.
    // key: subjectId
    // value: MajorCategory
    fun getMajorCategoryMapByEntryYear(entryYear: Int): Map<Long, MajorCategory> {
        val curriculumList: List<Curriculum> = curriculumRepository.findByEntryYear(entryYear)

        return curriculumList
            .filter { it.subject != null }
            .associate { it.subject!!.id to it.majorCategory }
    }
}
