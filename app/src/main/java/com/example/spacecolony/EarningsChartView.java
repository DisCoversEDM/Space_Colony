package com.example.spacecolony;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import java.util.ArrayList;
import java.util.List;

public class EarningsChartView extends View {
    private Paint barPaint;
    private Paint textPaint;
    private List<Integer> data = new ArrayList<>();
    private int maxValue = 100;

    public EarningsChartView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        barPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        barPaint.setColor(ContextCompat.getColor(getContext(), R.color.teal_300));
        
        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(ContextCompat.getColor(getContext(), R.color.gray_400));
        textPaint.setTextSize(24f);
        textPaint.setTextAlign(Paint.Align.CENTER);
    }

    public void setData(List<Integer> newData) {
        this.data = newData;
        this.maxValue = 0;
        for (int value : data) {
            if (value > maxValue) maxValue = value;
        }
        if (maxValue == 0) maxValue = 100;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (data == null || data.isEmpty()) return;

        float width = getWidth();
        float height = getHeight();
        float padding = 40f;
        float chartHeight = height - padding * 2;
        float barWidth = (width - padding * 2) / Math.max(10, data.size());
        float spacing = barWidth * 0.2f;

        for (int i = 0; i < data.size(); i++) {
            float barHeight = (data.get(i) / (float) maxValue) * chartHeight;
            float left = padding + i * barWidth + spacing;
            float top = padding + (chartHeight - barHeight);
            float right = padding + (i + 1) * barWidth - spacing;
            float bottom = padding + chartHeight;

            canvas.drawRect(left, top, right, bottom, barPaint);
            
            if (i % 5 == 0 || i == data.size() - 1) {
                canvas.drawText("T" + i, left + (barWidth / 2), bottom + 30, textPaint);
            }
        }
    }
}
