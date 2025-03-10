package com.orbaic.email.responseModel;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class CustomResponse {
    public ResponseEntity successWithData(Object data) {
        Map<String, Object> newData = new LinkedHashMap<>();
        newData.put("message", "success");
        newData.put("error", false);
        newData.put("data", data);
        newData.put("code", HttpStatus.OK.value());
        return ResponseEntity.status(200).contentType(MediaType.APPLICATION_JSON).body(newData);
    }
    public Map successWithoutData(String messgae) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("message", messgae);
        result.put("code", HttpStatus.OK.value());
        result.put("error", false);
        return result;
    }

    public ResponseEntity successWithoutDataRes(String messgae) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("message", messgae);
        result.put("code", HttpStatus.OK.value());
        result.put("error", false);
        return ResponseEntity.status(200).contentType(MediaType.APPLICATION_JSON).body(result);
    }

    public Map error(String message, int code) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("message", message);
        result.put("code", code);
        result.put("error", true);
        return result;
    }

    public ResponseEntity errorResponse(Object message, int code) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("message", message);
        result.put("code", code);
        result.put("error", true);
        return ResponseEntity.status(code).contentType(MediaType.APPLICATION_JSON).body(result);
    }

    public ResponseEntity serverError(String message) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("message", message);
        result.put("code", HttpStatus.INTERNAL_SERVER_ERROR.value());
        result.put("error", true);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType(MediaType.APPLICATION_JSON).body(result);
    }
}
