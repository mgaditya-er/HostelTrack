package com.example.hosteltrack;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import java.util.Date;
import java.util.Locale;
import java.text.SimpleDateFormat;

public class StudentHome extends AppCompatActivity {
    private Button btnSignOut;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_home);


        Button btnHome = findViewById(R.id.btnHome);
        Button btnReport = findViewById(R.id.btnReport);
        EditText etCode = findViewById(R.id.etCode);
        Button btnMarkAttendance = findViewById(R.id.btnMarkAttendance);

        btnSignOut = findViewById(R.id.btnSignOut);

        TextView tvDate = findViewById(R.id.tvDate);

        // Get today's date in the specified format
        String currentDate = getCurrentDate("dd-MM-yyyy");

        // Set the date to the TextView
        tvDate.setText("Date: " + currentDate);

        btnMarkAttendance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get the code entered in the EditText
                String attendanceCode = etCode.getText().toString().trim();

                // Check if the code is not empty
                if (!attendanceCode.isEmpty()) {
                    // Show a Toast with the attendance code
                    Toast.makeText(StudentHome.this, "Attendance Code: " + attendanceCode, Toast.LENGTH_SHORT).show();
                } else {
                    // Show a Toast if the code is empty
                    Toast.makeText(StudentHome.this, "Please enter the attendance code", Toast.LENGTH_SHORT).show();
                }
            }
        });


        btnHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle Home button click
                // Example: Open Home activity or perform some action
                // Replace the following line with your own code
                Toast.makeText(StudentHome.this, "Home", Toast.LENGTH_SHORT).show();
            }
        });

        btnReport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle Report button click
                // Example: Open Report activity or perform some action
                // Replace the following line with your own code
                Toast.makeText(StudentHome.this, "Report", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(StudentHome.this, StudentpersonalReport.class);
                startActivity(intent);
                finish();
            }
        });




        // Set click listener for sign-out button
        btnSignOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signOut();
            }
        });
    }
    private String getCurrentDate(String format) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(format, Locale.getDefault());
        return dateFormat.format(new Date());
    }
    private void signOut() {
        // Sign out from Firebase authentication
        FirebaseAuth.getInstance().signOut();

        // Redirect to the login screen
        Intent intent = new Intent(StudentHome.this, LoginActivity.class);
        startActivity(intent);
        finish(); // Optional: finish the current activity
    }
}