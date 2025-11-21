package com.fitlink.config;

import feign.codec.Decoder;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.cloud.openfeign.support.SpringDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

import java.util.List;

@Configuration
public class FeignConfig {

    @Bean
    public Decoder feignDecoder() {
        MappingJackson2HttpMessageConverter converter =
                new MappingJackson2HttpMessageConverter();

        converter.setSupportedMediaTypes(List.of(
                MediaType.APPLICATION_JSON,
                new MediaType("text", "json"),   // kf100에서 text/json 형태로 응답을 보내고 있기 때문에 이렇게 설정함.
                MediaType.TEXT_PLAIN
        ));

        HttpMessageConverters converters =
                new HttpMessageConverters(converter);

        return new SpringDecoder(() -> converters);
    }
}
