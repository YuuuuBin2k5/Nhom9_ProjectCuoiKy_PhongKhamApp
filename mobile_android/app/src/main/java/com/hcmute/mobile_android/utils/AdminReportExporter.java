package com.hcmute.mobile_android.utils;

import android.content.ContentValues;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.Toast;

import com.hcmute.mobile_android.network.models.DoctorStats;
import com.hcmute.mobile_android.network.models.RevenueReport;
import com.hcmute.mobile_android.network.models.ServiceStats;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.OutputStream;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AdminReportExporter {

    public static void exportDashboardToExcel(Context context, RevenueReport revenue, List<ServiceStats> services, List<DoctorStats> doctors) {
        try (Workbook workbook = new XSSFWorkbook()) {
            // 1. Sheet Tổng quan doanh thu
            Sheet summarySheet = workbook.createSheet("Tổng quan");
            createSummarySheet(summarySheet, workbook, revenue);

            // 2. Sheet Thống kê dịch vụ
            Sheet serviceSheet = workbook.createSheet("Thống kê dịch vụ");
            createServiceSheet(serviceSheet, workbook, services);

            // 3. Sheet Hiệu suất bác sĩ
            Sheet doctorSheet = workbook.createSheet("Hiệu suất bác sĩ");
            createDoctorSheet(doctorSheet, workbook, doctors);

            // Lưu file
            saveWorkbook(context, workbook, "BaoCao_Admin_" + new SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault()).format(new Date()) + ".xlsx");
            
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, "Lỗi xuất Excel: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    public static void exportDashboardToPdf(Context context, RevenueReport revenue, List<ServiceStats> services, List<DoctorStats> doctors) {
        PdfDocument document = new PdfDocument();
        try {
            // Khổ giấy A4 (595 x 842 pixels at 72 DPI)
            PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, 1).create();
            PdfDocument.Page page = document.startPage(pageInfo);
            Canvas canvas = page.getCanvas();
            Paint paint = new Paint();

            int y = 50;
            
            // Tiêu đề
            paint.setTextSize(20f);
            paint.setFakeBoldText(true);
            canvas.drawText("BÁO CÁO QUẢN TRỊ PHÒNG KHÁM", 150, y, paint);
            
            y += 40;
            paint.setTextSize(12f);
            paint.setFakeBoldText(false);
            canvas.drawText("Ngày xuất: " + new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(new Date()), 50, y, paint);
            
            // Phần Doanh thu
            y += 40;
            paint.setFakeBoldText(true);
            canvas.drawText("1. TỔNG QUAN DOANH THU", 50, y, paint);
            paint.setFakeBoldText(false);
            y += 25;
            NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
            canvas.drawText("Tổng doanh thu: " + formatter.format(revenue.getTotalRevenue()), 70, y, paint);
            y += 20;
            canvas.drawText("Tổng lượt khám: " + revenue.getTotalAppointments(), 70, y, paint);
            
            // Phần Dịch vụ
            y += 40;
            paint.setFakeBoldText(true);
            canvas.drawText("2. THỐNG KÊ DỊCH VỤ", 50, y, paint);
            paint.setFakeBoldText(false);
            y += 25;
            canvas.drawText("Tên dịch vụ", 70, y, paint);
            canvas.drawText("Số lượt", 300, y, paint);
            canvas.drawText("Doanh thu", 400, y, paint);
            canvas.drawLine(70, y + 5, 500, y + 5, paint);
            
            y += 20;
            for (int i = 0; i < Math.min(services.size(), 10); i++) {
                ServiceStats s = services.get(i);
                canvas.drawText(s.getServiceName(), 70, y, paint);
                canvas.drawText(String.valueOf(s.getTotalBookings()), 300, y, paint);
                canvas.drawText(formatter.format(s.getTotalRevenue()), 400, y, paint);
                y += 20;
                if (y > 800) break; // Tránh tràn trang (cần xử lý nhiều trang nếu làm kỹ)
            }

            document.finishPage(page);

            // Lưu file PDF
            savePdf(context, document, "BaoCao_Admin_" + new SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault()).format(new Date()) + ".pdf");

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, "Lỗi xuất PDF: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        } finally {
            document.close();
        }
    }

    private static void savePdf(Context context, PdfDocument document, String fileName) throws Exception {
        ContentValues values = new ContentValues();
        values.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
        values.put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf");
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);
        }

        Uri uri = context.getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);
        if (uri != null) {
            try (OutputStream os = context.getContentResolver().openOutputStream(uri)) {
                document.writeTo(os);
                Toast.makeText(context, "Đã lưu PDF vào thư mục Downloads", Toast.LENGTH_LONG).show();
            }
        }
    }

    private static void createSummarySheet(Sheet sheet, Workbook workbook, RevenueReport report) {
        if (report == null) return;
        
        CellStyle headerStyle = createHeaderStyle(workbook);
        
        Row titleRow = sheet.createRow(0);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("BÁO CÁO TỔNG QUAN DOANH THU");
        titleCell.setCellStyle(headerStyle);

        Row r1 = sheet.createRow(2);
        r1.createCell(0).setCellValue("Tổng doanh thu:");
        r1.createCell(1).setCellValue(report.getTotalRevenue().doubleValue());

        Row r2 = sheet.createRow(3);
        r2.createCell(0).setCellValue("Tổng lượt khám:");
        r2.createCell(1).setCellValue(report.getTotalAppointments());

        Row r3 = sheet.createRow(4);
        r3.createCell(0).setCellValue("Trung bình/lượt:");
        r3.createCell(1).setCellValue(report.getAverageRevenuePerAppointment().doubleValue());
        
        sheet.autoSizeColumn(0);
        sheet.autoSizeColumn(1);
    }

    private static void createServiceSheet(Sheet sheet, Workbook workbook, List<ServiceStats> services) {
        if (services == null) return;
        
        String[] headers = {"Tên dịch vụ", "Số lượt đặt", "Doanh thu"};
        Row headerRow = sheet.createRow(0);
        CellStyle headerStyle = createHeaderStyle(workbook);

        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        int rowIdx = 1;
        for (ServiceStats stats : services) {
            Row row = sheet.createRow(rowIdx++);
            row.createCell(0).setCellValue(stats.getServiceName());
            row.createCell(1).setCellValue(stats.getTotalBookings());
            row.createCell(2).setCellValue(stats.getTotalRevenue().doubleValue());
        }

        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private static void createDoctorSheet(Sheet sheet, Workbook workbook, List<DoctorStats> doctors) {
        if (doctors == null) return;
        
        String[] headers = {"Bác sĩ", "Số ca khám", "Doanh thu mang lại"};
        Row headerRow = sheet.createRow(0);
        CellStyle headerStyle = createHeaderStyle(workbook);

        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        int rowIdx = 1;
        for (DoctorStats stats : doctors) {
            Row row = sheet.createRow(rowIdx++);
            row.createCell(0).setCellValue(stats.getDoctorName());
            row.createCell(1).setCellValue(stats.getTotalAppointments());
            row.createCell(2).setCellValue(0.0); // Placeholder: Doanh thu hiện chưa có trong model DoctorStats
        }

        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private static CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }

    private static void saveWorkbook(Context context, Workbook workbook, String fileName) throws Exception {
        ContentValues values = new ContentValues();
        values.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
        values.put(MediaStore.MediaColumns.MIME_TYPE, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);
        }

        Uri uri = context.getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);
        if (uri != null) {
            try (OutputStream os = context.getContentResolver().openOutputStream(uri)) {
                workbook.write(os);
                Toast.makeText(context, "Đã lưu file vào thư mục Downloads: " + fileName, Toast.LENGTH_LONG).show();
            }
        }
    }
}
