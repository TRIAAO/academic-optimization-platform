package com.triacompany.academic.status;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/system-status")
@RequiredArgsConstructor
public class SystemStatusController {

    private final SystemStatusService systemStatusService;

    @GetMapping
    public SystemStatusResponse getSystemStatus() {
        return systemStatusService.getSystemStatus();
    }
}