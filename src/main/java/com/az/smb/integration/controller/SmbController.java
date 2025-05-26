package com.az.smb.integration.controller;

import com.az.smb.integration.service.SmbFileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;

@Slf4j
@RestController
public class SmbController {
    private final SmbFileService smbFileService;

    public SmbController(SmbFileService smbFileService) {
        this.smbFileService = smbFileService;
    }

    @GetMapping("/smb/upload")
    public ResponseEntity<String> uploadToSmb() {
        log.info("Starting SMB upload");
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
        String folderPath = sdf.format(date);
        Path path = Paths.get(folderPath, "123456789");
        smbFileService.writeFileToSmb("Hello SMB!", "test.txt", path.toString());
        return ResponseEntity.ok("SMB Upload Complete");
    }
}
