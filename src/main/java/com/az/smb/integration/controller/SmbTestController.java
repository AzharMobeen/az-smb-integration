package com.az.smb.integration.controller;

import com.az.smb.integration.service.SmbFileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class SmbTestController {
    private final SmbFileService smbFileService;

    public SmbTestController(SmbFileService smbFileService) {
        this.smbFileService = smbFileService;
    }

    @GetMapping("/smb/upload")
    public ResponseEntity<String> uploadToSmb() {
        log.info("Starting SMB upload");
        smbFileService.writeFileToSmb("Hello SMB!");
        return ResponseEntity.ok("SMB Upload Complete");
    }
}
