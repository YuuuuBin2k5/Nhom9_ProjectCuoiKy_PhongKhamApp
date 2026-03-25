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

public class OdontogramView extends View {

    public interface OnToothSelectedListener {
        void onToothSelected(int toothNumber);
    }

    private OnToothSelectedListener listener;
    
    // FDI tooth numbering system (32 teeth for adults)
    private static final int[][] TOOTH_LAYOUT = {
        {18, 17, 16, 15, 14, 13, 12, 11}, // Upper right
        {21, 22, 23, 24, 25, 26, 27, 28}, // Upper left
        {48, 47, 46, 45, 44, 43, 42, 41}, // Lower right
        {31, 32, 33, 34, 35, 36, 37, 38}  // Lower left
    };
    
    private Paint toothPaint;
    private Paint selectedPaint;
    private Paint textPaint;
    private Paint borderPaint;
    
    private Map<Integer, RectF> toothBounds = new HashMap<>();
    private Map<Integer, String> toothStatus = new HashMap<>();
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
        selectedPaint.setColor(Color.parseColor("#1A56DB")); // Primary blue
        selectedPaint.setStyle(Paint.Style.FILL);
        
        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.BLACK);
        textPaint.setTextSize(24f);
        textPaint.setTextAlign(Paint.Align.CENTER);
        
        borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        borderPaint.setColor(Color.parseColor("#64748B"));
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(2f);
        
        // Initialize tooth status (all healthy by default)
        for (int[] quadrant : TOOTH_LAYOUT) {
            for (int tooth : quadrant) {
                toothStatus.put(tooth, "healthy");
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
            
            // Choose paint based on selection and status
            Paint fillPaint = (toothNumber == selectedTooth) ? selectedPaint : getToothPaint(toothNumber);
            
            // Draw tooth
            canvas.drawRoundRect(bounds, 8f, 8f, fillPaint);
            canvas.drawRoundRect(bounds, 8f, 8f, borderPaint);
            
            // Draw tooth number
            float textX = bounds.centerX();
            float textY = bounds.centerY() + (textPaint.getTextSize() / 3);
            
            textPaint.setColor((toothNumber == selectedTooth) ? Color.WHITE : Color.BLACK);
            canvas.drawText(String.valueOf(toothNumber), textX, textY, textPaint);
        }
        
        // Draw quadrant labels
        textPaint.setColor(Color.parseColor("#64748B"));
        textPaint.setTextSize(16f);
        
        canvas.drawText("Hàm trên", centerX, 30f, textPaint);
        canvas.drawText("Hàm dưới", centerX, getHeight() - 10f, textPaint);
        
        textPaint.setTextSize(24f); // Reset text size
    }

    private Paint getToothPaint(int toothNumber) {
        String status = toothStatus.get(toothNumber);
        if (status == null) status = "healthy";
        
        Paint paint = new Paint(toothPaint);
        
        switch (status) {
            case "caries":
                paint.setColor(Color.parseColor("#D32F2F")); // Red for caries
                break;
            case "filled":
                paint.setColor(Color.parseColor("#1565C0")); // Blue for filled
                break;
            case "requested":
                paint.setColor(Color.parseColor("#4CAF50")); // Green for patient request
                break;
            case "rct":
                paint.setColor(Color.parseColor("#E65100")); // Orange for root canal
                break;
            default:
                paint.setColor(Color.WHITE); // White for healthy
                break;
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
                    selectedTooth = entry.getKey();
                    invalidate();
                    
                    if (listener != null) {
                        listener.onToothSelected(selectedTooth);
                    }
                    return true;
                }
            }
        }
        return super.onTouchEvent(event);
    }

    // Public methods for updating tooth status
    public void setToothStatus(int toothNumber, String status) {
        toothStatus.put(toothNumber, status);
        invalidate();
    }

    public String getToothStatus(int toothNumber) {
        return toothStatus.get(toothNumber);
    }

    public void setSelectedTooth(int toothNumber) {
        selectedTooth = toothNumber;
        invalidate();
    }

    public int getSelectedTooth() {
        return selectedTooth;
    }

    public void setOnToothSelectedListener(OnToothSelectedListener listener) {
        this.listener = listener;
    }

    // Method to clear selection
    public void clearSelection() {
        selectedTooth = -1;
        invalidate();
    }
}