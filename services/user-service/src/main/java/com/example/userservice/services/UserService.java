package com.example.userservice.services;

import com.example.userservice.configuration.KeycloakProvider;
import com.example.userservice.configuration.Utils;
import com.example.userservice.dto.*;
import com.example.userservice.mapper.RolesMapper;
import com.example.userservice.mapper.UserInfoMapper;
import com.example.userservice.mapper.UserSessionMapper;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.Response;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellUtil;
import org.apache.poi.util.IOUtils;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.keycloak.representations.idm.UserSessionRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

@Service
@NoArgsConstructor
@Slf4j
public class UserService {

    @Autowired
    KeycloakProvider keycloakProvider;


    @Value("${keycloak.realm}")
    String realm;

    public ResponseEntity<ResultDTO> getAllUsers(){
        Keycloak keycloak = keycloakProvider.getInstance();
        ResultDTO resultDTO = new ResultDTO();

        List<UserRepresentation> userKeycloakList = keycloak.realm(realm).users().list();
        List<UserInfoGetDTO> userInfoGetDTOS = new ArrayList<>();
        for(UserRepresentation userKeycloak: userKeycloakList){
            UserInfoGetDTO userInfoGetDTO = UserInfoMapper.toUserDTO(userKeycloak);
            userInfoGetDTOS.add(userInfoGetDTO);
        }

        resultDTO.setStatus(1);
        resultDTO.setMessage("success");
        resultDTO.setData(userInfoGetDTOS);

        return ResponseEntity.ok(resultDTO);
    }

    public ResponseEntity<List<UserInfoGetDTO>> searchUserListByUsernameOrEmail(String searchQuery){
        List<UserRepresentation> userRepresentationList;
        List<UserInfoGetDTO> userInfoGetDTOS = new ArrayList<>();
        Keycloak keycloak = keycloakProvider.getInstance();

        userRepresentationList = keycloak.realm(realm).users().search(searchQuery, false);

        if(Utils.isNullOrEmpty(Collections.singletonList(userRepresentationList))){
            return ResponseEntity.ok(userInfoGetDTOS);
        }

        for(UserRepresentation userKeycloak: userRepresentationList){
            UserInfoGetDTO userInfoGetDTO = UserInfoMapper.toUserDTO(userKeycloak);
            userInfoGetDTOS.add(userInfoGetDTO);
        }

        return ResponseEntity.ok(userInfoGetDTOS);
    }



    public ResponseEntity<UserInfoGetDTO> getUserByIdOrUsernameOrEmail(String id){
        UserRepresentation userRepresentation;
        Keycloak keycloak = keycloakProvider.getInstance();
        UserInfoGetDTO userInfoGetDTO;
        List<UserRepresentation> userRepresentationList;

        try{
            userRepresentation = keycloak.realm(realm).users().get(id).toRepresentation();

            if(userRepresentation == null){
                return ResponseEntity.ok(new UserInfoGetDTO());
            }
            userInfoGetDTO = UserInfoMapper.toUserDTO(userRepresentation);

            return ResponseEntity.ok(userInfoGetDTO);

        } catch (NotFoundException exception){
            userRepresentationList = keycloak.realm(realm).users().search(id, true);

            if(userRepresentationList.isEmpty()){

                return ResponseEntity.ok(null);

            } else {
                userRepresentation = userRepresentationList.get(0);
                if(userRepresentation == null){
                    return ResponseEntity.ok(null);
                }
                userInfoGetDTO = UserInfoMapper.toUserDTO(userRepresentation);

            }

            return ResponseEntity.ok(userInfoGetDTO);
        }
    }


    public ResponseEntity<List<UserSessionGetDTO>> getUserSessionById(String id){
        Keycloak keycloak = keycloakProvider.getInstance();
        List<UserSessionGetDTO> userSessionGetDTOS = new ArrayList<>();

        List<UserSessionRepresentation> userSessionRepresentationList = keycloak.realm(realm)
                .users()
                .get(id).getUserSessions();

        for(UserSessionRepresentation session: userSessionRepresentationList){
            userSessionGetDTOS.add(UserSessionMapper.userSessionDTO(session));
        }

        return ResponseEntity.ok(userSessionGetDTOS);
    }
    public ResponseEntity<ResultDTO> updateUser(UserInfoPostDTO userInfoPostDTO, String userId){
        Keycloak keycloak = keycloakProvider.getInstance();
        ResultDTO resultDTO = new ResultDTO();
        UserRepresentation userRepresentation = keycloak.realm(realm).users().get(userId).toRepresentation();

        userRepresentation.setEmail(userInfoPostDTO.getEmail());
        userRepresentation.setFirstName(userInfoPostDTO.getFirstName());
        userRepresentation.setLastName(userInfoPostDTO.getLastName());
        userRepresentation.setAttributes(userInfoPostDTO.getAttributes());
        userRepresentation.setEnabled(userInfoPostDTO.getEnable());

        keycloak.realm(realm).users().get(userId).update(userRepresentation);

        resultDTO.setStatus(1);
        resultDTO.setData(UserInfoMapper.toUserDTO(keycloak.realm(realm).users().get(userId).toRepresentation()));
        resultDTO.setMessage("success");

        return ResponseEntity.ok(resultDTO);
    }

    public ResponseEntity<ResultDTO> disableUsers(List<String> userIds){
        Keycloak keycloak = keycloakProvider.getInstance();
        ResultDTO resultDTO = new ResultDTO();
        List<UserInfoGetDTO> userInfoGetDTOS = new ArrayList<>();
        for (String userId: userIds){
            UserRepresentation userRepresentation = keycloak.realm(realm).users().get(userId).toRepresentation();
            userRepresentation.setEnabled(false);
            keycloak.realm(realm).users().get(userId).update(userRepresentation);
            userInfoGetDTOS.add(UserInfoMapper.toUserDTO(keycloak.realm(realm).users().get(userId).toRepresentation()));
        }

        resultDTO.setStatus(1);
        resultDTO.setData(userInfoGetDTOS);
        resultDTO.setMessage("success");

        return ResponseEntity.ok(resultDTO);    }

    public ResponseEntity<ResultDTO> createUser(UserInfoPostDTO userInfoPostDTO){
        Keycloak keycloak = keycloakProvider.getInstance();
        ResultDTO resultDTO = new ResultDTO();

        UserRepresentation user = new UserRepresentation();
        user.setUsername(userInfoPostDTO.getUsername());
        user.setEmail(userInfoPostDTO.getEmail());
        user.setFirstName(userInfoPostDTO.getFirstName());
        user.setLastName(userInfoPostDTO.getLastName());
        user.setEnabled(true);


        user.setAttributes(userInfoPostDTO.getAttributes());
        user.setRealmRoles(userInfoPostDTO.getRoles());
        user.setGroups(userInfoPostDTO.getGroups());

        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setValue(userInfoPostDTO.getPassword());
        credential.setTemporary(false);

        Response response = keycloak.realm(realm).users().create(user);
        log.info("Response |  Status: {} | Status Info: {}", response.getStatus(), response.getStatusInfo());


        if(response.getStatus() ==  Response.Status.CREATED.getStatusCode()){
            String userId = response.getLocation().getPath().replaceAll(".*/([^/]+)$", "$1");
            keycloak.realm(realm).users().get(userId).resetPassword(credential);


            resultDTO.setStatus(response.getStatus());
            resultDTO.setMessage("Success");
            resultDTO.setData(userId);

        } else if(response.getStatus() == Response.Status.CONFLICT.getStatusCode()){
            resultDTO.setStatus(response.getStatus());
            resultDTO.setMessage("Tai khoan da ton tai");
            resultDTO.setData(null);

        }

        return ResponseEntity.ok(resultDTO);
    }

//    public ResponseEntity<List<RolesGetDTO>> getRolesKeycloak(){
//        Keycloak keycloak = keycloakProvider.getInstance();
//        List<RoleRepresentation> rolesResources = keycloak.realm(realm).roles().list();
//        log.info(rolesResources.get(0).toString());
//
//        List<RolesGetDTO> rolesGetDTOs = new ArrayList<>();
//        for (RoleRepresentation role : rolesResources){
//            rolesGetDTOs.add(RolesMapper.toRolesGetDTO(role));
//        }
//        return  ResponseEntity.ok(rolesGetDTOs);
//    }

    public ResponseEntity<Resource> getSampleCreateUsersExcel() throws IOException{
        File file = new ClassPathResource("sample/users_create_sample.xlsx").getFile();
        if(!file.exists()){
            return ResponseEntity.internalServerError().body(null);
        }
        try (FileInputStream fs = new FileInputStream(file)) {
            byte[] excelContent = IOUtils.toByteArray(fs);

            HttpHeaders header = new HttpHeaders();

            header.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=users_create_sample.xlsx");
            header.add("Cache-Control", "no-cache, no-store, must-revalidate");
            header.add("Pragma", "no-cache");
            header.add("Expires", "0");

            ByteArrayResource resource = new ByteArrayResource(excelContent);
            return ResponseEntity.ok()
                    .headers(header)
                    .contentLength(file.length())
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(resource);
        } catch (IOException ioException){
            log.error("Create Sample Excel Send Error", ioException.getMessage());
            return ResponseEntity.internalServerError().body(null);
        }


    }


    public ResponseEntity<Resource> uploadUsersExcel(MultipartFile excelFile) throws Exception{
        log.info("excel file:", excelFile.getName());

        Workbook workbook = new XSSFWorkbook(excelFile.getInputStream());
        Keycloak keycloak = keycloakProvider.getInstance();
        HttpHeaders header = new HttpHeaders();
        header.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=Ket_qua_tao_tai_khoan.xlsx");

        if (excelFile.isEmpty()) {
            return ResponseEntity.badRequest().body(null);
        }

        Sheet sheet = workbook.getSheetAt(0);
        for (Row row: sheet){
            int rowNum = row.getRowNum();
            if(rowNum == 0) {
                continue;
            }
            UserRepresentation userRepresentation = new UserRepresentation();
            Map<String, List<String>> attributes = new HashMap<>();
            String password = null;


            for(Cell cell: row){
                switch (cell.getColumnIndex()){
                    case 0: userRepresentation.setUsername(cell.getStringCellValue()); break;
                    case 1: password = cell.getStringCellValue(); break;
                    case 2: userRepresentation.setEmail(cell.getStringCellValue()); break;
                    case 3: userRepresentation.setLastName(cell.getStringCellValue()); break;
                    case 4: userRepresentation.setFirstName(cell.getStringCellValue()); break;
                    case 5: {
                        break;
                    }
                    case 6:{
                        List<String> phoneValues = new ArrayList<>();
                        phoneValues.add(cell.getStringCellValue());;
                        attributes.put("phone", phoneValues);
                        break;
                    }
                    case 7:
                    {
                        List<String> instituteValues = new ArrayList<>();
                        instituteValues.add(cell.getStringCellValue());
                        attributes.put("institution", instituteValues);
                        break;
                    }
                    case 8:
                        List<String> departmentValues = new ArrayList<>();
                        departmentValues.add(cell.getStringCellValue());
                        attributes.put("department", departmentValues);
                        break;
                    case 9:
                        List<String> cityValues = new ArrayList<>();
                        cityValues.add(cell.getStringCellValue());
                        attributes.put("city", cityValues);
                        break;
                    case 10:
                        List<String> countryValues = new ArrayList<>();
                        countryValues.add(cell.getStringCellValue());
                        attributes.put("country", countryValues);
                        break;
                    case 11: {
                        List<String> addressValues = new ArrayList<>();
                        addressValues.add(cell.getStringCellValue());
                        attributes.put("address", addressValues);
                        break;
                    }
                }
            }

            userRepresentation.setEnabled(true);
            userRepresentation.setAttributes(attributes);

            CredentialRepresentation credential = new CredentialRepresentation();
            credential.setType(CredentialRepresentation.PASSWORD);
            credential.setValue(password);
            credential.setTemporary(false);

            Response response = keycloak.realm(realm).users().create(userRepresentation);

            if(response.getStatus() ==  Response.Status.CREATED.getStatusCode()){
                String userId = response.getLocation().getPath().replaceAll(".*/([^/]+)$", "$1");
                keycloak.realm(realm).users().get(userId).resetPassword(credential);

                CellStyle cellStyle = workbook.createCellStyle();
                cellStyle.setFillForegroundColor(IndexedColors.GREEN.getIndex());
                cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

                CellUtil.getCell(row, 12).setCellValue("Khởi tạo thành công");
                CellUtil.getCell(row, 12).setCellStyle(cellStyle);

            } else if(response.getStatus() == Response.Status.CONFLICT.getStatusCode()){
                CellStyle cellStyle = workbook.createCellStyle();
                cellStyle.setFillForegroundColor(IndexedColors.RED.getIndex());
                cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);


                CellUtil.getCell(row, 12).setCellValue("Bị trùng username hoặc email");
                CellUtil.getCell(row, 12).setCellStyle(cellStyle);
            } else {
                CellStyle cellStyle = workbook.createCellStyle();
                cellStyle.setFillForegroundColor(IndexedColors.RED.getIndex());
                cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

                CellUtil.getCell(row, 12).setCellValue("Khởi tạo không thành công");
                CellUtil.getCell(row, 12).setCellStyle(cellStyle);
            }
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        workbook.write(out);
        workbook.close();

        ByteArrayResource resource = new ByteArrayResource(out.toByteArray());

        return ResponseEntity.ok()
                .headers(header)
                .contentLength(resource.contentLength())
                .contentType(org.springframework.http.MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }


}
