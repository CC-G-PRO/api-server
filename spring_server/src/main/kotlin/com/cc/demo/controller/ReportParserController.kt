package com.cc.demo.controller

import com.cc.demo.repository.UserRepository
import com.cc.demo.response.CommonResponse
import com.cc.demo.security.UserPrincipal
import com.cc.demo.service.PdfParsingService
import com.cc.demo.service.UserTakenCouseService
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.text.PDFTextStripper
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import java.io.InputStream
import java.nio.charset.Charset

//TODO : api 대부분 완성되면 jwt 기반 @AuthenticationPrincipal user 추가할 것. 일단 테스트용으로 1 넣어둠.

@RestController
@RequestMapping("/report")
class ReportParserController (
    private val pdfParsingService: PdfParsingService,
){
    @PostMapping("/valid", consumes = ["multipart/form-data"])
    fun validatePdf(@RequestParam("file") file: MultipartFile): ResponseEntity<CommonResponse> {
        return try {
            val isValid = pdfParsingService.validatePdf(file.inputStream)

            if (isValid) {
                ResponseEntity.ok(CommonResponse(true, "유효한 졸업사정진단표 파일입니다."))
            } else {
                ResponseEntity.badRequest().body(CommonResponse(false, "파일형식이 올바르지 않습니다. 졸업 진단표 파일 형식이 아닙니다."))
            }
        } catch (e: Exception) {
            ResponseEntity.badRequest().body(
                CommonResponse(
                    valid = false,
                    message = "PDF 파싱에 실패했습니다: ${e.message}"
                )
            )
        }
    }

    @PostMapping("", consumes = ["multipart/form-data"])
    fun handlePdfUpload(
        @RequestParam("file") file: MultipartFile?,
        @AuthenticationPrincipal user: UserPrincipal,
    ): ResponseEntity<Any> {
        if (file == null || file.isEmpty) {
            return ResponseEntity.badRequest().body("파일이 없습니다.")
        }
        return try {
            val response = pdfParsingService.processReportUpload(file, user.id) //userId 말고 user 넘기게 고쳐야함.
            ResponseEntity.ok(response)
ㅌ
        } catch (e: Exception) {
            ResponseEntity.internalServerError().body("FastAPI 요청 실패: ${e.message}")
        }
    }

}
