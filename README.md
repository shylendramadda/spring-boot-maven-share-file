# spring-boot-maven-share-file
Goal of this project is to create service for file sharing (Upload, download, share) between users.

### Dependencies Used:
- Spring Boot 2.3.1
- Java 1.8
- JPA
- Restfull
- Lombok
- Map Struct 1.3.1.Final
- ModelMapper 2.3.0
- H2 Database
- Spring security
- Maven
- Swagger2 2.9.2
- SwaggerUI 2.9.2

### Endpoint Exposed Endpoints:
- POST /api/register (Register User)
- GET  /api/file (Get all files)
- POST /api/file/upload (Upload a file)
- GET  /api//file/{id} (Get file by Id)
- POST /api/file/share (Share a file)

### To check API's using Swagger2 use this URL
- http://localhost:8085/swagger-ui.html#/

### To check tables and data saved in H2 Database use this URL
- http://localhost:8085/h2-console

### To make it work change the following lines in the application.properties file
file.upload-path=D:/Shylendra/FileStorage
spring.datasource.url=jdbc:h2:file:D:/Shylendra/FileStorage  