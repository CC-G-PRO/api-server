package com.cc.demo.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration

@Configuration
class GptConfig {
    @Value("\${openai.secret-key}")
    lateinit var secretKey: String

    @Value("\${openai.model}")
    lateinit var model: String
}

