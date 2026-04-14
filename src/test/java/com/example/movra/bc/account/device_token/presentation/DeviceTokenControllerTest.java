package com.example.movra.bc.account.device_token.presentation;

import com.example.movra.bc.account.device_token.application.service.RegisterDeviceTokenService;
import com.example.movra.bc.account.device_token.application.service.UnregisterDeviceTokenService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DeviceTokenController.class)
@AutoConfigureMockMvc(addFilters = false)
class DeviceTokenControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RegisterDeviceTokenService registerDeviceTokenService;

    @MockBean
    private UnregisterDeviceTokenService unregisterDeviceTokenService;

    @Test
    @DisplayName("unregister accepts token in request body")
    void unregister_withRequestBody_returnsNoContent() throws Exception {
        mockMvc.perform(delete("/device-tokens")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "token": "token-abc"
                                }
                                """))
                .andExpect(status().isNoContent());

        verify(unregisterDeviceTokenService).unregister(argThat(request -> "token-abc".equals(request.token())));
    }

    @Test
    @DisplayName("unregister rejects blank token")
    void unregister_blankToken_returnsBadRequest() throws Exception {
        mockMvc.perform(delete("/device-tokens")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "token": ""
                                }
                                """))
                .andExpect(status().isBadRequest());
    }
}
