package com.hcmute.mobile_android.util;

import android.graphics.Bitmap;
import android.graphics.Color;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import java.util.HashMap;
import java.util.Map;

/**
 * Helper class for QR Code generation using ZXing library
 */
public class QRCodeHelper {
    
    /**
     * Generate QR Code bitmap from text
     * @param text Content to encode
     * @param width QR code width in pixels
     * @param height QR code height in pixels
     * @return Bitmap of QR code or null if error
     */
    public static Bitmap generateQRCode(String text, int width, int height) {
        try {
            QRCodeWriter writer = new QRCodeWriter();
            
            // Set encoding hints
            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
            hints.put(EncodeHintType.MARGIN, 1);
            
            // Generate bit matrix
            BitMatrix bitMatrix = writer.encode(text, BarcodeFormat.QR_CODE, width, height, hints);
            
            // Create bitmap
            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
            
            // Fill bitmap with QR code pattern
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    bitmap.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
                }
            }
            
            return bitmap;
            
        } catch (WriterException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Generate QR Code with default size (512x512)
     * @param text Content to encode
     * @return Bitmap of QR code or null if error
     */
    public static Bitmap generateQRCode(String text) {
        return generateQRCode(text, 512, 512);
    }
    
    /**
     * Generate QR Code with custom colors
     * @param text Content to encode
     * @param width QR code width
     * @param height QR code height
     * @param foregroundColor Color for QR pattern (default: black)
     * @param backgroundColor Background color (default: white)
     * @return Bitmap of QR code or null if error
     */
    public static Bitmap generateQRCode(String text, int width, int height, 
                                       int foregroundColor, int backgroundColor) {
        try {
            QRCodeWriter writer = new QRCodeWriter();
            
            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
            hints.put(EncodeHintType.MARGIN, 1);
            
            BitMatrix bitMatrix = writer.encode(text, BarcodeFormat.QR_CODE, width, height, hints);
            
            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
            
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    bitmap.setPixel(x, y, bitMatrix.get(x, y) ? foregroundColor : backgroundColor);
                }
            }
            
            return bitmap;
            
        } catch (WriterException e) {
            e.printStackTrace();
            return null;
        }
    }
}