package com.hcmute.mobile_android.ui.widgets;

import android.content.Context;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.RelativeSizeSpan;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.hcmute.mobile_android.R;

public class ToothlyNumericKeypad extends FrameLayout {

    public interface OnKeyListener {
        void onDigit(int digit);

        void onBackspace();
    }

    private OnKeyListener listener;

    public ToothlyNumericKeypad(@NonNull Context context) {
        super(context);
        init(context);
    }

    public ToothlyNumericKeypad(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public void setKeyListener(OnKeyListener listener) {
        this.listener = listener;
    }

    private void init(Context context) {
        LayoutInflater.from(context).inflate(R.layout.widget_toothly_keypad, this, true);
        bindDigit(R.id.key1, 1);
        bindDigit(R.id.key2, 2, "Check-up");
        bindDigit(R.id.key3, 3);
        bindDigit(R.id.key4, 4);
        bindDigit(R.id.key5, 5, "Prevention");
        bindDigit(R.id.key6, 6);
        bindDigit(R.id.key7, 7);
        bindDigit(R.id.key8, 8, "Polish");
        bindDigit(R.id.key9, 9);
        bindDigit(R.id.key0, 0, "Health");

        TextView back = findViewById(R.id.keyBack);
        back.setOnClickListener(v -> {
            if (listener != null) {
                listener.onBackspace();
            }
        });
    }

    private void bindDigit(int id, int digit) {
        bindDigit(id, digit, null);
    }

    private void bindDigit(int id, int digit, @Nullable String subtitle) {
        TextView tv = findViewById(id);
        if (subtitle != null) {
            String top = String.valueOf(digit);
            SpannableString s = new SpannableString(top + "\n" + subtitle);
            s.setSpan(new RelativeSizeSpan(1.35f), 0, top.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            s.setSpan(new RelativeSizeSpan(0.65f), top.length(), s.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            tv.setText(s);
        } else {
            tv.setText(String.valueOf(digit));
        }
        tv.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDigit(digit);
            }
        });
    }
}
