package com.fitlink.web.controller;

import com.fitlink.apiPayload.ApiResponse;
import com.fitlink.web.dto.TestDTO;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/test")
public class TestController {

    @GetMapping
    public ApiResponse<TestDTO> getTest() {
        TestDTO data = new TestDTO();
        data.setTest("?뚯뒪?몄엯?덈떎.");

        return ApiResponse.onSuccess(data);
    }

    @GetMapping("/path/{id}")
    public ApiResponse<TestDTO> getTestWithPath(@PathVariable Long id) {
        TestDTO dto = new TestDTO();
        dto.setTest("?낅젰??id: " + id);
        return ApiResponse.onSuccess(dto);
    }

    @GetMapping("/params")
    public ApiResponse<TestDTO> getTestWithParams(
            @RequestParam(defaultValue = "default-keyword") String keyword,
            @RequestParam(required = false) Integer page
    ) {
        TestDTO dto = new TestDTO();
        dto.setTest("keyword: " + keyword + ", page: " + (page != null ? page : "?놁쓬"));
        return ApiResponse.onSuccess(dto);
    }

    @PostMapping("/body")
    public ApiResponse<TestDTO> postTestWithBody(@RequestBody TestDTO request) {
        TestDTO dto = new TestDTO();
        dto.setTest("body: " + request.getTest());
        return ApiResponse.onSuccess(dto);
    }
}
