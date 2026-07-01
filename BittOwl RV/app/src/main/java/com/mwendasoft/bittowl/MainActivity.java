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
import c

om.google.firebase.auth.*;
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

     // CODE OMITTED

}