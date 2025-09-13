package com.mwendasoft.bittowl;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.*;
import android.support.v4.content.*;
import android.*;
import android.support.v4.app.*;
import android.app.*;
import android.provider.*;
import android.content.*;
import android.util.*;
import android.graphics.*;
import android.view.*;
import android.text.*;
import android.text.style.*;

public class MainActivity extends Activity {

    private TextView txtEquation, txtResult;
    private StringBuilder equation = new StringBuilder();
    
	// Inflate the menu: This ensures the about menu / icon is shown.
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main_menu, menu);
		return true;
	}
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
		
		///=== ACTION BAR SETTINGS ==>
		// Get ActionBar
		ActionBar actionBar = getActionBar();
		if (actionBar != null) {
			actionBar.setDisplayShowTitleEnabled(false); // Hide default title

			// Create a custom TextView
			TextView titleView = new TextView(this);

			// Use SpannableString to color parts of the text
			SpannableString title = new SpannableString("BittOwl");

			// Color "Bitt" dark yellow
			title.setSpan(new ForegroundColorSpan(Color.parseColor("#2196F3")),
						  0, 4, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

			// Color "Owl" blue
			title.setSpan(new ForegroundColorSpan(Color.parseColor("#FF9800")),
						  4, 7, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

			// Apply to TextView
			titleView.setText(title);
			titleView.setTextSize(26); // Bigger font
			titleView.setTypeface(Typeface.DEFAULT_BOLD); // Bold
			titleView.setGravity(Gravity.CENTER_VERTICAL);

			// Set as custom ActionBar view
			actionBar.setDisplayShowCustomEnabled(true);
			actionBar.setCustomView(titleView);
		}
		///=== ACTION BAR SETTINGS END ==>

		txtEquation = findViewById(R.id.txtEquation);
        txtResult = findViewById(R.id.txtResult);
        
        // Input listener for buttons that add to equation
        View.OnClickListener inputListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Button b = (Button) v;
                String input = b.getText().toString();

                // Map button text to proper expression strings
                if (input.equals("x²")) {
                    equation.append("^2");
                } else if (input.equals("x³")) {
                    equation.append("^3");
                } else if (input.equals("√")) {
                    equation.append("sqrt(");
                } else if (input.equals("×")) {
                    equation.append("*");
                } else if (input.equals("÷")) {
                    equation.append("/");
                } else if (input.equals("ln")) {
                    equation.append("ln(");
                } else if (input.equals("log")) {
                    equation.append("log(");
                } else if (input.equals("sin") || input.equals("cos") || input.equals("tan")) {
                    equation.append(input + "(");
                } else if (input.equals("%")) {
                    equation.append("%");
                } else if (input.equals(".")) {
                    equation.append(".");
				} else {
                    equation.append(input);
                }

                txtEquation.setText(equation.toString());
            }
        };

        // All buttons that add to input
        int[] inputButtons = {
			R.id.btn0, R.id.btn1, R.id.btn2, R.id.btn3, R.id.btn4,
			R.id.btn5, R.id.btn6, R.id.btn7, R.id.btn8, R.id.btn9,
			R.id.btnPlus, R.id.btnMinus, R.id.btnMultiply, R.id.btnDivide, R.id.btnPow,
			R.id.btnSin, R.id.btnCos, R.id.btnTan,
			R.id.btnSqrt, R.id.btnLog, R.id.btnLn,
			R.id.btnPercent, R.id.btnPoint,
			R.id.btnOpenParen, R.id.btnCloseParen,
			R.id.btnSquare, R.id.btnCube
        };

        for (int id : inputButtons) {
            findViewById(id).setOnClickListener(inputListener);
        }

        // Clear button
        findViewById(R.id.btnClear).setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					clearCalculator();
				}
			});

        // Equal button
        findViewById(R.id.btnEqual).setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					calculateResult();
				}
			});
	}
	
	private void clearCalculator() {
        equation.setLength(0);
        txtEquation.setText("0");
        txtResult.setText("= 0");
    }

    private void calculateResult() {
        try {
            String expr = equation.toString();

            // Evaluate using ExpressionEvaluator
            double result = new ExpressionEvaluator().evaluate(expr);

            txtResult.setText("= " + result);

        } catch (Exception e) {
            txtResult.setText("Error");
        }
    }

    
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.action_about) {

			// Nice professional dialog
			AlertDialog.Builder builder = new AlertDialog.Builder(this);

			builder.setTitle("About BittOwl");
			builder.setIcon(R.drawable.ic_about); // About icon in drawable

			builder.setMessage("BittOwl Calculator\n"
							   + "Simple and precise — designed to make your daily calculations easier and more enjoyable.\n\n"
							   + "Creator: Mwenda E. Njagi\n"
							   + "Phone: +254 702 623 729\n"
							   + "Email: erickmwenda256@gmail.com\n"
							   + "Website: MwendaSoft.com");

			builder.setPositiveButton("OK", null);

			AlertDialog dialog = builder.create();
			dialog.show();

			// Make title text bold and centered
			TextView titleView = dialog.findViewById(
                this.getResources().getIdentifier("alertTitle", "id", "android"));
			if (titleView != null) {
				titleView.setGravity(Gravity.LEFT);
				titleView.setTextColor(Color.parseColor("#FFD700")); // Dark Yellow title
				titleView.setTypeface(Typeface.DEFAULT_BOLD);
			}

			// Make message text more readable
			TextView messageView = dialog.findViewById(android.R.id.message);
			if (messageView != null) {
				messageView.setTextSize(16);
				messageView.setLineSpacing(1.3f, 1.3f);
			}

			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
