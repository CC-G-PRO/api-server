package com.cc.demo.service

import org.springframework.core.io.ByteArrayResource
import org.springframework.core.io.InputStreamResource
import org.springframework.http.*
import org.springframework.stereotype.Service
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.client.RestTemplate
import org.springframework.web.multipart.MultipartFile
import java.io.InputStream

@Service
class PdfParsingService {

    fun sendPdfToFastApi(file: MultipartFile): String? {
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

        if (response.statusCode == HttpStatus.OK) {
            return response.body
        } else {
            println("Error response status: ${response.statusCode}")
            return null
        }
    }
}
