package com.az.smb.integration.service;

import com.az.smb.integration.config.SmbProperties;
import com.hierynomus.msdtyp.AccessMask;
import com.hierynomus.mssmb2.SMB2CreateDisposition;
import com.hierynomus.mssmb2.SMB2CreateOptions;
import com.hierynomus.mssmb2.SMB2ShareAccess;
import com.hierynomus.smbj.SMBClient;
import com.hierynomus.smbj.auth.AuthenticationContext;
import com.hierynomus.smbj.connection.Connection;
import com.hierynomus.smbj.session.Session;
import com.hierynomus.smbj.share.DiskShare;
import com.hierynomus.smbj.share.File;
import com.hierynomus.smbj.share.Share;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.EnumSet;

@Slf4j
@Service
@RequiredArgsConstructor
public class SmbFileService {

    private final SmbProperties smbProperties;

    /**
     * Writes content to a file on an SMB share using the configured properties.
     *
     * @param content The content to write to the file
     * @throws SmbOperationException If any SMB operation fails
     */
    public void writeFileToSmb(String content, String fileName, String folderPath) {
        log.info("Writing file to SMB share folder: {}", smbProperties.getShare());
        try (SMBClient client = new SMBClient();
             Connection connection = client.connect(smbProperties.getHost(), smbProperties.getPort())) {
            Session session = authenticateSession(connection);
            writeContentToFile(content, fileName, folderPath, session);
            writeContentToFile("content".concat("test"), "fileName.xml", folderPath, session);
            log.info("File successfully written to SMB share: {}", folderPath);

        } catch (Exception e) {
            log.error("Failed to write file to SMB share", e);
            throw new SmbOperationException("Error writing to SMB share", e);
        }
    }

    private Session authenticateSession(Connection connection) {
        log.info("Authenticating to SMB share with user: {}", smbProperties.getUsername());
        AuthenticationContext authContext = new AuthenticationContext(
                smbProperties.getUsername(),
                smbProperties.getPassword().toCharArray(),
                smbProperties.getDomain()
        );
        return connection.authenticate(authContext);
    }

    private DiskShare connectToDiskShare(Session session) throws IOException {
        log.info("Connecting to SMB share folder: {}", smbProperties.getShare());
        Share share = session.connectShare(smbProperties.getShare());
        if (!(share instanceof DiskShare diskShare)) {
            share.close();
            throw new IllegalStateException("Not a disk share");
        }
        return diskShare;
    }

    private String combinePath(String folderPath, String fileName) {
        return Paths.get(folderPath, fileName).toString();
    }

    private void ensureDirectoryExists(DiskShare diskShare, String folderPath) {
        // Handle paths with both forward and backslashes
        String[] pathParts = folderPath.split("[/\\\\]");
        StringBuilder currentPath = new StringBuilder();

        for (String part : pathParts) {
            if (part.isEmpty()) continue;

            if (!currentPath.isEmpty()) {
                currentPath.append("/");
            }
            currentPath.append(part);

            String pathToCheck = currentPath.toString();
            if (!diskShare.folderExists(pathToCheck)) {
                log.debug("Creating directory: {}", pathToCheck);
                diskShare.mkdir(pathToCheck);
            }
        }
    }

    private void writeContentToFile(String content, String fileName, String folderPath, Session session) throws IOException {
        log.info("Writing content to file: {}", folderPath);
        String fullPath = combinePath(folderPath, fileName);
        try (DiskShare diskShare = connectToDiskShare(session)) {
            ensureDirectoryExists(diskShare, folderPath);
            try (File file = diskShare.openFile(
                    fullPath,
                    EnumSet.of(AccessMask.GENERIC_WRITE),
                    null,
                    SMB2ShareAccess.ALL,
                    SMB2CreateDisposition.FILE_OVERWRITE_IF,
                    EnumSet.of(SMB2CreateOptions.FILE_RANDOM_ACCESS));
                 OutputStream os = file.getOutputStream()) {

                os.write(content.getBytes(StandardCharsets.UTF_8));
                os.flush();
            }
        }
    }

    public static class SmbOperationException extends RuntimeException {
        public SmbOperationException(String message, Throwable cause) {
            super(message, cause);
        }
    }

}