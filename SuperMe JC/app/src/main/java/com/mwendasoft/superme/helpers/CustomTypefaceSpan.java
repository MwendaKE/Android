package com.mwendasoft.superme.helpers;

import android.graphics.Paint;
import android.graphics.Typeface;
import android.text.TextPaint;
import android.text.style.MetricAffectingSpan;

public class CustomTypefaceSpan extends MetricAffectingSpan {
	private final Typeface customTypeface;

	public CustomTypefaceSpan(Typeface typeface) {
		customTypeface = typeface;
	}

	@Override
	public void updateDrawState(TextPaint paint) {
		apply(paint);
	}

	@Override
	public void updateMeasureState(TextPaint paint) {
		apply(paint);
	}

	private void apply(Paint paint) {
		paint.setTypeface(customTypeface);
	}
}
