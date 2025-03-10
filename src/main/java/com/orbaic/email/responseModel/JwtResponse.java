package com.orbaic.email.responseModel;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Map;

public class JwtResponse {
    private HttpServletResponse response;
    private boolean error;
    private String msg;

    public JwtResponse(HttpServletResponse response, boolean error, String msg) {
        this.response = response;
        this.error = error;
        this.msg = msg;
    }

    public HttpServletResponse res() throws  IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.getWriter().write(new ObjectMapper().writeValueAsString(Map.of("error", error, "code", HttpServletResponse.SC_UNAUTHORIZED, "message", msg)));
        return response;
    }
}
