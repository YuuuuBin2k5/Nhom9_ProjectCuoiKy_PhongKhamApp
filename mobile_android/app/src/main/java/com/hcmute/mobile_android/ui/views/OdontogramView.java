package com.hcmute.mobile_android.ui.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * Enhanced Odontogram View with FDI numbering and service color coding
 * - Displays 32 adult teeth in FDI system
 * - Color-codes teeth based on services applied
 * - Allows direct service selection from tooth click
 */
public class OdontogramView extends View {

    public interface OnToothServiceListener {
        void onToothSelected(int toothNumber, String serviceName);
        void onToothClicked(int toothNumber);
    }

    private OnToothServiceListener listener;
    
    // FDI tooth numbering system (32 teeth for adults)
    private static final int[][] TOOTH_LAYOUT = {
        {18, 17, 16, 15, 14, 13, 12, 11}, // Upper right
        {21, 22, 23, 24, 25, 26, 27, 28}, // Upper left
        {48, 47, 46, 45, 44, 43, 42, 41}, // Lower right
        {31, 32, 33, 34, 35, 36, 37, 38}  // Lower left
    };
    
    // Service color mapping
    private static final Map<String, Integer> SERVICE_COLORS = new HashMap<>();
    static {
        SERVICE_COLORS.put("Trám", Color.parseColor("#FFC107"));      // Amber for filling
        SERVICE_COLORS.put("Nhổ thường", Color.parseColor("#F44336")); // Red for extraction
        SERVICE_COLORS.put("Nhổ khôn", Color.parseColor("#E91E63"));   // Pink for wisdom extraction
        SERVICE_COLORS.put("Bọc sứ", Color.parseColor("#2196F3"));     // Blue for crown
        SERVICE_COLORS.put("Khám", Color.parseColor("#4CAF50"));       // Green for exam
        SERVICE_COLORS.put("X-quang", Color.parseColor("#9C27B0"));    // Purple for X-ray
        SERVICE_COLORS.put("Lấy cao", Color.parseColor("#FF9800"));    // Orange for scaling
        SERVICE_COLORS.put("Điều trị tủy", Color.parseColor("#795548")); // Brown for RCT
        SERVICE_COLORS.put("Tẩy trắng", Color.parseColor("#00BCD4"));  // Cyan for whitening
        SERVICE_COLORS.put("Niềng", Color.parseColor("#673AB7"));      // Deep purple for ortho
    }
    
    private Paint toothPaint;
    private Paint selectedPaint;
    private Paint textPaint;
    private Paint borderPaint;
    private Paint highlightPaint;
    
    private Map<Integer, RectF> toothBounds = new HashMap<>();
    private Map<Integer, String> toothServices = new HashMap<>(); // Tooth -> Service name
    private int selectedTooth = -1;
    
    private float toothSize = 40f;
    private float toothSpacing = 8f;
    private float quadrantSpacing = 20f;

    public OdontogramView(Context context) {
        super(context);
        init();
    }

    public OdontogramView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public OdontogramView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        toothPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        toothPaint.setColor(Color.WHITE);
        toothPaint.setStyle(Paint.Style.FILL);
        
        selectedPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        selectedPaint.setStyle(Paint.Style.STROKE);
        selectedPaint.setStrokeWidth(4f);
        selectedPaint.setColor(Color.parseColor("#1A56DB"));
        
        highlightPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        highlightPaint.setStyle(Paint.Style.STROKE);
        highlightPaint.setStrokeWidth(3f);
        highlightPaint.setColor(Color.parseColor("#FFD700"));
        
        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.BLACK);
        textPaint.setTextSize(20f);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setFakeBoldText(true);
        
        borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        borderPaint.setColor(Color.parseColor("#64748B"));
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(2f);
        
        // Initialize all teeth with no service
        for (int[] quadrant : TOOTH_LAYOUT) {
            for (int tooth : quadrant) {
                toothServices.put(tooth, null);
            }
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        calculateToothBounds();
    }

    private void calculateToothBounds() {
        toothBounds.clear();
        
        float centerX = getWidth() / 2f;
        float centerY = getHeight() / 2f;
        
        // Calculate starting positions for each quadrant
        float upperY = centerY - quadrantSpacing - toothSize;
        float lowerY = centerY + quadrantSpacing;
        
        // Upper quadrants
        drawQuadrant(TOOTH_LAYOUT[0], centerX - quadrantSpacing - (8 * (toothSize + toothSpacing)), upperY, true);  // Upper right
        drawQuadrant(TOOTH_LAYOUT[1], centerX + quadrantSpacing, upperY, false); // Upper left
        
        // Lower quadrants  
        drawQuadrant(TOOTH_LAYOUT[2], centerX - quadrantSpacing - (8 * (toothSize + toothSpacing)), lowerY, true);  // Lower right
        drawQuadrant(TOOTH_LAYOUT[3], centerX + quadrantSpacing, lowerY, false); // Lower left
    }

    private void drawQuadrant(int[] teeth, float startX, float startY, boolean rightToLeft) {
        for (int i = 0; i < teeth.length; i++) {
            int tooth = teeth[i];
            float x = rightToLeft ? startX + (i * (toothSize + toothSpacing)) : startX + (i * (toothSize + toothSpacing));
            
            RectF bounds = new RectF(x, startY, x + toothSize, startY + toothSize);
            toothBounds.put(tooth, bounds);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        // Draw center line
        float centerX = getWidth() / 2f;
        float centerY = getHeight() / 2f;
        
        canvas.drawLine(centerX - 100, centerY, centerX + 100, centerY, borderPaint);
        canvas.drawLine(centerX, centerY - 100, centerX, centerY + 100, borderPaint);
        
        // Draw all teeth
        for (Map.Entry<Integer, RectF> entry : toothBounds.entrySet()) {
            int toothNumber = entry.getKey();
            RectF bounds = entry.getValue();
            
            // Get service for this tooth
            String service = toothServices.get(toothNumber);
            
            // Choose fill color based on service
            Paint fillPaint = getToothFillPaint(service);
            
            // Draw tooth
            canvas.drawRoundRect(bounds, 8f, 8f, fillPaint);
            canvas.drawRoundRect(bounds, 8f, 8f, borderPaint);
            
            // Draw selection highlight if selected
            if (toothNumber == selectedTooth) {
                canvas.drawRoundRect(bounds, 8f, 8f, selectedPaint);
            }
            
            // Draw tooth number
            float textX = bounds.centerX();
            float textY = bounds.centerY() + (textPaint.getTextSize() / 3);
            
            // Text color: white if service applied, black otherwise
            textPaint.setColor(service != null ? Color.WHITE : Color.BLACK);
            canvas.drawText(String.valueOf(toothNumber), textX, textY, textPaint);
        }
        
        // Draw quadrant labels
        textPaint.setColor(Color.parseColor("#64748B"));
        textPaint.setTextSize(16f);
        
        canvas.drawText("Hàm trên", centerX, 30f, textPaint);
        canvas.drawText("Hàm dưới", centerX, getHeight() - 10f, textPaint);
        
        textPaint.setTextSize(20f); // Reset text size
    }

    private Paint getToothFillPaint(String serviceName) {
        Paint paint = new Paint(toothPaint);
        
        if (serviceName != null && SERVICE_COLORS.containsKey(serviceName)) {
            paint.setColor(SERVICE_COLORS.get(serviceName));
        } else {
            paint.setColor(Color.WHITE); // White for no service
        }
        
        return paint;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            float x = event.getX();
            float y = event.getY();
            
            // Find which tooth was touched
            for (Map.Entry<Integer, RectF> entry : toothBounds.entrySet()) {
                if (entry.getValue().contains(x, y)) {
                    int toothNumber = entry.getKey();
                    selectedTooth = toothNumber;
                    invalidate();
                    
                    if (listener != null) {
                        listener.onToothClicked(toothNumber);
                    }
                    return true;
                }
            }
        }
        return super.onTouchEvent(event);
    }

    // ===== Data Management Methods =====
    
    /**
     * Add service to a tooth
     * @param toothNumber FDI tooth number (11-48)
     * @param serviceName Service name (e.g., "Trám", "Nhổ thường")
     */
    public void addServiceToTooth(int toothNumber, String serviceName) {
        toothServices.put(toothNumber, serviceName);
        invalidate();
    }

    /**
     * Get service applied to a tooth
     */
    public String getToothService(int toothNumber) {
        return toothServices.get(toothNumber);
    }

    /**
     * Remove service from a tooth
     */
    public void removeServiceFromTooth(int toothNumber) {
        toothServices.put(toothNumber, null);
        invalidate();
    }

    /**
     * Check if tooth has a service
     */
    public boolean hasService(int toothNumber) {
        return toothServices.get(toothNumber) != null;
    }

    /**
     * Get all teeth with services
     */
    public Map<Integer, String> getTeethWithServices() {
        Map<Integer, String> result = new HashMap<>();
        for (Map.Entry<Integer, String> entry : toothServices.entrySet()) {
            if (entry.getValue() != null) {
                result.put(entry.getKey(), entry.getValue());
            }
        }
        return result;
    }

    /**
     * Clear all services
     */
    public void clearAllServices() {
        for (int tooth : toothServices.keySet()) {
            toothServices.put(tooth, null);
        }
        invalidate();
    }

    // ===== Selection Methods =====
    
    public void setSelectedTooth(int toothNumber) {
        selectedTooth = toothNumber;
        invalidate();
    }

    public int getSelectedTooth() {
        return selectedTooth;
    }

    public void clearSelection() {
        selectedTooth = -1;
        invalidate();
    }

    // ===== Listener Methods =====
    
    public void setOnToothServiceListener(OnToothServiceListener listener) {
        this.listener = listener;
    }
}