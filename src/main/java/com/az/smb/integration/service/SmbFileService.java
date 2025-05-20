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
import com.hierynomus.smbj.share.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
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
    public void writeFileToSmb(String content) {
        log.info("Writing file to SMB share folder: {}", smbProperties.getShare());
        try (SMBClient client = new SMBClient();
             Connection connection = client.connect(smbProperties.getHost())) {

            Session session = authenticateSession(connection);

            try (DiskShare diskShare = connectToDiskShare(session)) {
                String folderPath = smbProperties.getFolderPath();
                String fileName = smbProperties.getFileName();
                String fullPath = combinePath(folderPath, fileName);

                ensureDirectoryExists(diskShare, folderPath);
                writeContentToFile(diskShare, fullPath, content);

                log.info("File successfully written to SMB share: {}", fullPath);
            }
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
        return folderPath + "/" + fileName;
    }

    private void ensureDirectoryExists(DiskShare diskShare, String folderPath) {
        if (!diskShare.folderExists(folderPath)) {
            diskShare.mkdir(folderPath);
        }
    }

    private void writeContentToFile(DiskShare diskShare, String fullPath, String content) throws IOException {
        log.info("Writing content to file: {}", fullPath);
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

    public static class SmbOperationException extends RuntimeException {
        public SmbOperationException(String message, Throwable cause) {
            super(message, cause);
        }
    }

}