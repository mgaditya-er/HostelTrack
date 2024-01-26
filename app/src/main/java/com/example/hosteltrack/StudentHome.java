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
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.text.SimpleDateFormat;

public class StudentHome extends AppCompatActivity {
    private Button btnSignOut;
    private FirebaseFirestore db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_home);


        Button btnHome = findViewById(R.id.btnHome);
        Button btnReport = findViewById(R.id.btnReport);
        EditText etCode = findViewById(R.id.etCode);
        Button btnMarkAttendance = findViewById(R.id.btnMarkAttendance);
        db = FirebaseFirestore.getInstance();
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
                    String currdate = getCurrentDate("ddMMyyyy");

                    checkAttendanceCode(attendanceCode,currdate);
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

    private void checkAttendanceCode(String attendanceCode, String currentDate) {

//        Toast.makeText(this, "--"+currentDate, Toast.LENGTH_SHORT).show();
        db.collection("attendance")
                .document(currentDate)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Document exists, check the code
                        String storedCode = documentSnapshot.getString("code");
                        if (storedCode != null && storedCode.equals(attendanceCode)) {
                            // Code matches, show success Toast
                            String currentUserUID = FirebaseAuth.getInstance().getCurrentUser().getUid();

                            // Update the document with the current user's UID
                            // Assuming db is a FirebaseFirestore instance

                            db.collection("attendance")
                                    .document(currentDate)
                                    .collection("attendees")  // Create or reference a subcollection for attendees
                                    .document(currentUserUID)  // Use the user's UID as the document ID
                                    .get()
                                    .addOnSuccessListener(documentSnapshot1 -> {
                                        if (documentSnapshot1.exists()) {
                                            // User's attendance record already exists
                                            Toast.makeText(StudentHome.this, "Attendance already marked", Toast.LENGTH_SHORT).show();
                                        } else {
                                            // User's attendance record does not exist, add it
                                            db.collection("attendance")
                                                    .document(currentDate)
                                                    .collection("attendees")
                                                    .document(currentUserUID)
                                                    .set(new HashMap<String, Object>())
                                                    .addOnSuccessListener(aVoid -> {
                                                        // Document successfully created/updated
                                                        Toast.makeText(StudentHome.this, "Attendance recorded for user", Toast.LENGTH_SHORT).show();
                                                    })
                                                    .addOnFailureListener(e -> {
                                                        // Handle errors
                                                        Toast.makeText(StudentHome.this, "Error recording attendance", Toast.LENGTH_SHORT).show();
                                                    });
                                        }
                                    })
                                    .addOnFailureListener(e -> {
                                        // Handle errors
                                        Toast.makeText(StudentHome.this, "Error checking attendance", Toast.LENGTH_SHORT).show();
                                    });


                            Toast.makeText(StudentHome.this, "Attendance Success", Toast.LENGTH_SHORT).show();
                        } else {
                            // Code doesn't match, show failure Toast
                            Toast.makeText(StudentHome.this, "Attendance Fail"+storedCode, Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        // Document doesn't exist, show failure Toast
                        Toast.makeText(StudentHome.this, "Attendance Fail", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    // Handle errors
                    Toast.makeText(StudentHome.this, "Error checking attendance", Toast.LENGTH_SHORT).show();
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