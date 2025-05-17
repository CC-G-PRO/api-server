package com.cc.demo.controller

import com.cc.demo.response.PdfValidationResponse
import com.cc.demo.service.PdfParsingService
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.text.PDFTextStripper
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import java.io.InputStream
import java.nio.charset.Charset


@RestController
@RequestMapping("/api/report")
class ReportParserController (
    private val pdfParsingService: PdfParsingService
){
    private val validStartRegex = Regex("^[es#@*0-9\$G%]")

    @PostMapping("/valid", consumes = ["multipart/form-data"])
    fun validatePdf(@RequestParam("file") file: MultipartFile): ResponseEntity<PdfValidationResponse> {
        return try {
            val text = extractTextFromPdf(file.inputStream)
            val decodedText = String(text.toByteArray(), Charset.forName("euc-kr")).trim()

            val firstChar = decodedText.firstOrNull()
            if (firstChar != null && validStartRegex.matches(firstChar.toString())) {
                ResponseEntity.ok(
                    PdfValidationResponse(
                        valid = true,
                        message = "유효한 졸업사정진단표 파일입니다."
                    )
                )
            } else {
                ResponseEntity.badRequest().body(
                    PdfValidationResponse(
                        valid = false,
                        message = "파일형식이 올바르지 않습니다. 졸업 진단표 파일 형식이 아닙니다."
                    )
                )
            }
        } catch (e: Exception) {
            ResponseEntity.badRequest().body(
                PdfValidationResponse(
                    valid = false,
                    message = "PDF 파싱에 실패했습니다: ${e.message}"
                )
            )
        }
    }

    private fun extractTextFromPdf(input: InputStream): String {
        PDDocument.load(input).use { document ->
            return PDFTextStripper().getText(document)
        }
    }

    @PostMapping("/upload", consumes = ["multipart/form-data"])
    fun handlePdfUpload(@RequestParam("file") file: MultipartFile?): ResponseEntity<String> {
        if (file == null || file.isEmpty) {
            return ResponseEntity.badRequest().body("파일이 없습니다.")
        }

        return try {
            val fastApiResponse = pdfParsingService.sendPdfToFastApi(file)
            ResponseEntity.ok(fastApiResponse)
        } catch (e: Exception) {
            ResponseEntity.internalServerError().body("FastAPI 요청 실패: ${e.message}")
        }
    }

}
