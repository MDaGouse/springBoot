package com.excel.sheet.service;

import net.sf.jasperreports.engine.JREmptyDataSource;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.excel.sheet.model.UserData;
import com.excel.sheet.repository.UserRepository;

import jakarta.servlet.http.HttpServletResponse;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import net.sf.jasperreports.export.SimpleXlsxExporterConfiguration;
import net.sf.jasperreports.export.SimpleXlsxReportConfiguration;

@Service
public class ExcelService {

    @Autowired
    private UserRepository userRepository;

    public void processExcelFile(MultipartFile file) throws IOException {
        Workbook workbook = new XSSFWorkbook(file.getInputStream());
        Sheet sheet = workbook.getSheetAt(0);

        Iterator<Row> iterator = sheet.iterator();
        // Skip the header row
        if (iterator.hasNext()) {
            iterator.next();
        }

        while (iterator.hasNext()) {
            Row currentRow = iterator.next();
            Iterator<Cell> cellIterator = currentRow.iterator();

            UserData userData = new UserData();

            // Assuming Sno is in the first column, Name in the second column, and Mobile in the third column
            Cell snoCell = cellIterator.next();
            userData.setSno((int) snoCell.getNumericCellValue());

            Cell nameCell = cellIterator.next();
            userData.setName(nameCell.getStringCellValue());

            Cell mobileCell = cellIterator.next();
            userData.setMobile(mobileCell.getNumericCellValue());

            // Save userData to database
            userRepository.save(userData);
        }

        workbook.close();
    }
    
    public void generateAndExportExcel(HttpServletResponse response) throws IOException, JRException {
        // Fetch users from the database
        List<UserData> users = userRepository.findAll();

        // Load Jasper report template (JRXML file)
        ClassPathResource jrxmlResource = new ClassPathResource("reports/userReport.jrxml");
        if (!jrxmlResource.exists()) {
            throw new FileNotFoundException("JRXML file not found");
        }

        // Compile JRXML to Jasper file
        JasperReport compiledReport = JasperCompileManager.compileReport(jrxmlResource.getInputStream());

        // Convert fetched data to JasperReports compatible data source
        JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(users);

        // Parameters, if any, to pass to the report template
        Map<String, Object> parameters = new HashMap<>();

        // Generate JasperPrint object
        JasperPrint jasperPrint = JasperFillManager.fillReport(compiledReport, parameters, dataSource);

        // Set response headers for Excel file download
        response.setContentType("application/vnd.ms-excel");
        response.setHeader("Content-Disposition", "attachment; filename=users.xlsx");

        // Export JasperPrint to Excel
        JRXlsxExporter exporter = new JRXlsxExporter();
        exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
        exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(response.getOutputStream()));

        SimpleXlsxReportConfiguration reportConfig = new SimpleXlsxReportConfiguration();
        SimpleXlsxExporterConfiguration exporterConfig = new SimpleXlsxExporterConfiguration();
        exporter.setConfiguration(reportConfig);
        exporter.setConfiguration(exporterConfig);

        exporter.exportReport();
    }

    public ByteArrayResource downloadTemplate() throws JRException, IOException {
        // Load JRXML file from the resources folder
        ClassPathResource jrxmlResource = new ClassPathResource("reports/userReport.jrxml");
        if (!jrxmlResource.exists()) {
            throw new IOException("JRXML file not found");
        }

        // Compile JRXML to Jasper file
        JasperReport compiledReport = JasperCompileManager.compileReport(jrxmlResource.getInputStream());

        // Generate JasperPrint object
        JasperPrint jasperPrint = JasperFillManager.fillReport(compiledReport, new HashMap<>(), new JREmptyDataSource());

        // Export JasperPrint to Excel
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        JRXlsxExporter exporter = new JRXlsxExporter();
        exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
        exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(outputStream));
        exporter.exportReport();

        return new ByteArrayResource(outputStream.toByteArray());
    }
}

