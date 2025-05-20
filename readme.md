# SMB Integration Service

A Spring Boot microservice that provides a simple API for interacting with SMB (Server Message Block) file shares.

## Overview

This service allows applications to write files to SMB network shares through a RESTful API. It handles authentication, file operations, and error management through a clean abstraction layer.

## Features

- 🔐 Secure authentication with SMB shares
- 📁 Directory creation and management
- 📝 File writing operations
- 🌐 RESTful API for file operations
- 🔧 Externalized configuration via YAML

## Technologies

- Java 17+
- Spring Boot
- Spring Web
- Lombok
- jcifs-ng (SMB client)
- Maven

## Configuration

Configure the SMB connection details in `application.yml`:

```yaml
smb:
  host: 192.168.0.27       # IP or hostname of SMB server
  share: SharedFolder      # Name of the SMB shared folder
  domain: "test"           # Domain for authentication
  username: myuser         # SMB username
  password: mypassword     # SMB password
  folder-path: test-folder # Relative folder path inside the share
  file-name: test.txt      # Default filename
```

## Development Environment

### Setting up a local SMB server

For development and testing purposes, you can run a local SMB server using Docker:

```bash
docker run -d \
  --name samba-server \
  --restart unless-stopped \
  -p 445:445 \
  -v $(pwd)/smb-share:/mount \
  dperson/samba \
  -u "myuser;mypassword" \
  -s "SharedFolder;/mount;yes;no;no;myuser"
```

### Docker Command Explanation

| Flag                          | Purpose                                                              |
|-------------------------------|----------------------------------------------------------------------|
| `-d`                          | Run container in background (detached mode)                          |
| `--restart unless-stopped`    | Automatically restart on reboot or crash, unless explicitly stopped  |
| `--name samba-server`         | Assigns a container name (easier for `docker exec`)                  |
| `-p 445:445`                  | Maps SMB port 445 (may require elevated permissions)                 |
| `-v $(pwd)/smb-share:/mount`  | Mounts your local `smb-share` folder to `/mount` in the container    |
| `-u`                          | Creates a user with specified username and password                  |
| `-s`                          | Sets up a shared folder (in this case, `/mount` as `SharedFolder`)   |

## API Endpoints

| Endpoint        | Method | Description                                           |
|-----------------|--------|-------------------------------------------------------|
| `/smb/upload`   | GET    | Uploads a test message to the configured SMB share    |

## Usage

### Running the Application

```bash
./mvnw spring-boot:run
```

### Testing the Upload Functionality

```bash
curl http://localhost:8080/smb/upload
```

## Project Structure

- `SmbTestController`: REST controller that exposes the SMB functionality
- `SmbFileService`: Service that handles SMB operations
- `SmbProperties`: Configuration properties for the SMB connection

## Requirements

- JDK 17+
- Network access to the SMB share
- Valid SMB user credentials

## Error Handling

The service implements proper exception handling for SMB operations:
- Connection errors
- Authentication failures
- Permission issues
- File operation failures