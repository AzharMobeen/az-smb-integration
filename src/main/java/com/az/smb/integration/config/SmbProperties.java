package com.az.smb.integration.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Setter
@Getter
@Configuration
@ConfigurationProperties(prefix = "smb")
public class SmbProperties {
    private String host;
    private String share;
    private String domain;
    private String username;
    private String password;
    private String folderPath;
    private String fileName;
}