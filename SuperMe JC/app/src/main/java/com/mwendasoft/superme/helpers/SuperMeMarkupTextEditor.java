package com.mwendasoft.superme.helpers;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.util.AttributeSet;
import android.widget.EditText;
import android.widget.TextView;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SuperMeMarkupTextEditor extends EditText {
    private String rawText;

    public SuperMeMarkupTextEditor(Context context) {
        super(context);
        init();
    }

    public SuperMeMarkupTextEditor(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SuperMeMarkupTextEditor(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        addTextChangedListener(new TextWatcher() {
				@Override
				public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

				@Override
				public void onTextChanged(CharSequence s, int start, int before, int count) {
					rawText = s.toString();
				}

				@Override
				public void afterTextChanged(Editable s) {
					removeTextChangedListener(this);
					int cursor = getSelectionStart();
					SpannableStringBuilder ssb = new SpannableStringBuilder(rawText);
					applyAllSpans(ssb);
					setText(ssb);
					setSelection(Math.min(cursor, ssb.length()));
					addTextChangedListener(this);
				}
			});
    }

    private void applyAllSpans(SpannableStringBuilder ssb) {
        applyHeadingSpans(ssb);
        applySpan(ssb, "\\*\\*(.*?)\\*\\*", new StyleSpan(Typeface.BOLD));
        applySpan(ssb, "_(.*?)_", new StyleSpan(Typeface.ITALIC));
        applySpan(ssb, "__(.*?)__", new UnderlineSpan());
        applyColorSpan(ssb, "red", Color.RED);
        applyColorSpan(ssb, "green", Color.GREEN);
        applyColorSpan(ssb, "blue", Color.BLUE);
    }

    private void applySpan(SpannableStringBuilder ssb, String patternStr, Object span) {
        Pattern pattern = Pattern.compile(patternStr);
        Matcher matcher = pattern.matcher(ssb);
        while (matcher.find()) {
            int start = matcher.start(1);
            int end = matcher.end(1);
            ssb.setSpan(span, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }

    private void applyHeadingSpans(SpannableStringBuilder ssb) {
        String[] lines = ssb.toString().split("\n");
        int index = 0;
        for (String line : lines) {
            if (line.startsWith("# ")) {
                ssb.setSpan(new StyleSpan(Typeface.BOLD), index + 2, index + line.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                ssb.setSpan(new RelativeSizeSpan(1.5f), index + 2, index + line.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            } else if (line.startsWith("## ")) {
                ssb.setSpan(new StyleSpan(Typeface.BOLD), index + 3, index + line.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                ssb.setSpan(new RelativeSizeSpan(1.3f), index + 3, index + line.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            } else if (line.startsWith("### ")) {
                ssb.setSpan(new StyleSpan(Typeface.BOLD), index + 4, index + line.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                ssb.setSpan(new RelativeSizeSpan(1.2f), index + 4, index + line.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            index += line.length() + 1;
        }
    }

    private void applyColorSpan(SpannableStringBuilder ssb, String tag, int color) {
        // Fixed version using square brackets
		Pattern pattern = Pattern.compile("\\[" + tag + "\\](.*?)\\[/" + tag + "\\]");
		Matcher matcher = pattern.matcher(ssb);
        while (matcher.find()) {
            int start = matcher.start(1);
            int end = matcher.end(1);
            ssb.setSpan(new ForegroundColorSpan(color), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }

    public String getRawText() {
        return rawText != null ? rawText : getText().toString();
    }

    public void setTextWithMarkup(String text) {
        this.rawText = text;
        SpannableStringBuilder ssb = new SpannableStringBuilder(text);
        applyAllSpans(ssb);
        setText(ssb);
    }

    public static void displayWithoutMarkup(TextView textView, String markupText) {
        SpannableStringBuilder withSpans = new SpannableStringBuilder(markupText);
        applyAllSpansStatic(withSpans);

        String cleanText = removeMarkupTagsStatic(markupText);
        SpannableStringBuilder cleanSpannable = new SpannableStringBuilder(cleanText);

        Object[] spans = withSpans.getSpans(0, withSpans.length(), Object.class);
        for (Object span : spans) {
            int start = withSpans.getSpanStart(span);
            int end = withSpans.getSpanEnd(span);

            String originalBefore = markupText.substring(0, start);
            String cleanBefore = removeMarkupTagsStatic(originalBefore);

            String originalSpanText = markupText.substring(start, end);
            String cleanSpanText = removeMarkupTagsStatic(originalSpanText);

            int newStart = cleanBefore.length();
            int newEnd = newStart + cleanSpanText.length();

            if (newStart >= 0 && newEnd <= cleanSpannable.length()) {
                cleanSpannable.setSpan(cloneSpan(span), newStart, newEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }

        textView.setText(cleanSpannable);
    }

    private static Object cloneSpan(Object span) {
        if (span instanceof StyleSpan) {
            return new StyleSpan(((StyleSpan) span).getStyle());
        } else if (span instanceof UnderlineSpan) {
            return new UnderlineSpan();
        } else if (span instanceof ForegroundColorSpan) {
            return new ForegroundColorSpan(((ForegroundColorSpan) span).getForegroundColor());
        } else if (span instanceof RelativeSizeSpan) {
            return new RelativeSizeSpan(((RelativeSizeSpan) span).getSizeChange());
        }
        return span;
    }

    private static String removeMarkupTagsStatic(String text) {
        text = text.replaceAll("\\*\\*(.*?)\\*\\*", "$1");
        text = text.replaceAll("_(.*?)_", "$1");
        text = text.replaceAll("__(.*?)__", "$1");
        text = text.replaceAll("\\[(red|green|blue)\\](.*?)\\[/\\1\\]", "$2"); 
		text = text.replaceAll("^(#{1,3}) ", "");
        return text;
    }

    private static void applyAllSpansStatic(SpannableStringBuilder ssb) {
        String[] lines = ssb.toString().split("\n");
        int index = 0;
        for (String line : lines) {
            if (line.startsWith("# ")) {
                ssb.setSpan(new StyleSpan(Typeface.BOLD), index + 2, index + line.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                ssb.setSpan(new RelativeSizeSpan(1.5f), index + 2, index + line.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            } else if (line.startsWith("## ")) {
                ssb.setSpan(new StyleSpan(Typeface.BOLD), index + 3, index + line.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                ssb.setSpan(new RelativeSizeSpan(1.3f), index + 3, index + line.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            } else if (line.startsWith("### ")) {
                ssb.setSpan(new StyleSpan(Typeface.BOLD), index + 4, index + line.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                ssb.setSpan(new RelativeSizeSpan(1.2f), index + 4, index + line.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            index += line.length() + 1;
        }

        applyStaticSpan(ssb, "\\*\\*(.*?)\\*\\*", new StyleSpan(Typeface.BOLD));
        applyStaticSpan(ssb, "_(.*?)_", new StyleSpan(Typeface.ITALIC));
        applyStaticSpan(ssb, "__(.*?)__", new UnderlineSpan());
        applyStaticColorSpan(ssb, "red", Color.RED);
        applyStaticColorSpan(ssb, "green", Color.GREEN);
        applyStaticColorSpan(ssb, "blue", Color.BLUE);
    }

    private static void applyStaticSpan(SpannableStringBuilder ssb, String patternStr, Object span) {
        Pattern pattern = Pattern.compile(patternStr);
        Matcher matcher = pattern.matcher(ssb);
        while (matcher.find()) {
            int start = matcher.start(1);
            int end = matcher.end(1);
            ssb.setSpan(span, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }

    private static void applyStaticColorSpan(SpannableStringBuilder ssb, String tag, int color) {
        Pattern pattern = Pattern.compile("\\[" + tag + "\\](.*?)\\[/" + tag + "\\]");
		Matcher matcher = pattern.matcher(ssb);
        while (matcher.find()) {
            int start = matcher.start(1);
            int end = matcher.end(1);
            ssb.setSpan(new ForegroundColorSpan(color), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }
}
