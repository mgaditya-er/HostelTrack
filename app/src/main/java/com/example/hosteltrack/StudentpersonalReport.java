package com.example.hosteltrack;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class StudentpersonalReport extends AppCompatActivity {
    private Button btnSignOut;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_studentpersonal_report);

        Button btnHome = findViewById(R.id.btnHome);
        Button btnReport = findViewById(R.id.btnReport);


        btnHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle Home button click
                // Example: Open Home activity or perform some action
                // Replace the following line with your own code
                Toast.makeText(StudentpersonalReport.this, "Home", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(StudentpersonalReport.this, StudentHome.class);
                startActivity(intent);
                finish();
            }
        });

        btnReport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle Report button click
                // Example: Open Report activity or perform some action
                // Replace the following line with your own code
                Toast.makeText(StudentpersonalReport.this, "Report", Toast.LENGTH_SHORT).show();

            }
        });
    }
}