package com.cc.demo.service

import com.cc.demo.converter.GraduationEvaluationConverter
import com.cc.demo.entity.GraduationEvaluation
import com.cc.demo.repository.GraduationEvaluationRepository
import com.cc.demo.repository.UserRepository
import com.cc.demo.repository.UserTakenSubjectRepository
import com.cc.demo.response.ReportData
import com.cc.demo.response.ReportUploadResponse
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import jakarta.transaction.Transactional
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.text.PDFTextStripper
import org.springframework.core.io.ByteArrayResource
import org.springframework.http.*
import org.springframework.stereotype.Service
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.client.RestTemplate
import org.springframework.web.multipart.MultipartFile
import java.io.InputStream
import java.nio.charset.Charset

@Service
class PdfParsingService (
    private val userRepository: UserRepository,
    private val userTakenCourseService: UserTakenCouseService,
    private val graduationEvaluationRepository: GraduationEvaluationRepository,
    private val userTakenSubjectRepository: UserTakenSubjectRepository
) {

    fun sendPdfToFastApi(file: MultipartFile): ReportData {
        val headers = HttpHeaders()
        headers.contentType = MediaType.MULTIPART_FORM_DATA

        val body: MultiValueMap<String, Any> = LinkedMultiValueMap()

        val byteArrayResource = object : ByteArrayResource(file.bytes) {
            override fun getFilename(): String = file.originalFilename ?: "file.pdf"
        }

        body.add("pdf_file", byteArrayResource)

        val requestEntity = HttpEntity(body, headers)
        val restTemplate = RestTemplate()

        val url = "http://fastapi-app:8000/parse-pdf/"

        val response = restTemplate.postForEntity(url, requestEntity, String::class.java)

        if (response.statusCode == HttpStatus.OK && response.body != null) {
            val objectMapper = jacksonObjectMapper()
            return objectMapper.readValue(response.body!!)
        } else {
            throw RuntimeException("FastAPI 요청 실패: ${response.statusCode}")
        }
    }

    @Transactional
    fun processReportUpload(file: MultipartFile, userId: Long): ReportUploadResponse {
        userTakenSubjectRepository.deleteAllByUserId(userId)
        graduationEvaluationRepository.deleteByUserId(userId)

        val fastApiResponse = sendPdfToFastApi(file)

        val user = userRepository.findById(userId).orElseThrow { RuntimeException("User not found") }

        userTakenCourseService.saveCourseInfos(userId, fastApiResponse.courseInfo)

        val graduationEvaluation = GraduationEvaluationConverter.convertToGraduationEvaluation(fastApiResponse, user)

        graduationEvaluationRepository.save(graduationEvaluation)

        return ReportUploadResponse(
            message = "success to parsing data",
            data = graduationEvaluation
        )
    }

    fun getGraduationInfo(userId: Long): ReportUploadResponse {
        val data = graduationEvaluationRepository.findByUserId(userId)
            .firstOrNull() ?: throw NoSuchElementException("No graduation evaluation found for userId=$userId")

        return ReportUploadResponse(
            message = "success to parsing data",
            data = data
        )
    }

    fun validatePdf(fileInputStream: InputStream, charset: Charset = Charset.forName("euc-kr")): Boolean {
        val validStartRegex = Regex("^[es#@*0-9\$G%]")
        val text = extractTextFromPdf(fileInputStream)
        val decodedText = String(text.toByteArray(), charset).trim()
        val firstChar = decodedText.firstOrNull()

        return firstChar != null && validStartRegex.matches(firstChar.toString())
    }

    private fun extractTextFromPdf(input: InputStream): String {
        PDDocument.load(input).use { document ->
            return PDFTextStripper().getText(document)
        }
    }

}
