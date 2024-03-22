package com.excel.sheet.controller;

import java.io.IOException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.excel.sheet.service.ExcelService;

import jakarta.servlet.http.HttpServletResponse;
import net.sf.jasperreports.engine.JRException;

@RestController
@RequestMapping("/excel")
public class UploadController {

    @Autowired
    private ExcelService excelService;

  
    @PostMapping("/upload")
    public String uploadExcel(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return "Please select a file to upload.";
        }

        try {
            excelService.processExcelFile(file);
            return "File uploaded successfully!";
        } catch (IOException e) {
            return "Failed to upload file: " + e.getMessage();
        }
    }
    @GetMapping("/download")
    public ResponseEntity<?> downloadExcelFile(HttpServletResponse response) {
        try {
            excelService.generateAndExportExcel(response);
            return ResponseEntity.ok().build();
        } catch (IOException | JRException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to generate and download the Excel report: " + e.getMessage());
        }    }
    
    @GetMapping("/template")
    public ResponseEntity<ByteArrayResource> downloadTemplate() {
        try {
            ByteArrayResource resource = excelService.downloadTemplate();

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType("application/vnd.ms-excel"))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=userReport.xlsx")
                    .body(resource);
        } catch (IOException | JRException e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }
}
