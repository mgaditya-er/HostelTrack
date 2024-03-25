package com.example.hosteltrack;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import android.content.pm.PackageManager;
import android.location.Location;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.text.SimpleDateFormat;
import java.text.ParseException;


public class StudentHome extends AppCompatActivity {
    private Button btnSignOut;
    private static final String TAG = "StudentHome";

    private FirebaseFirestore db;
    EditText etCode;
    public static int flag=0;
    Button btnMarkAttendance;
    private FusedLocationProviderClient fusedLocationProviderClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_home);


        Button btnHome = findViewById(R.id.btnHome);
        Button btnReport = findViewById(R.id.btnReport);
        etCode = findViewById(R.id.etCode);
        btnMarkAttendance = findViewById(R.id.btnMarkAttendance);
        db = FirebaseFirestore.getInstance();
        btnSignOut = findViewById(R.id.btnSignOut);
        btnMarkAttendance.setEnabled(true);
        btnMarkAttendance.setVisibility(View.VISIBLE);
        TextView tvDate = findViewById(R.id.tvDate);

        // Get today's date in the specified format
        String currentDate = getCurrentDate("dd-MM-yyyy");

        // Set the date to the TextView
        tvDate.setText("Date: " + currentDate);



        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        timeisvalid();







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

    private void timeisvalid() {
        String currentDate = getCurrentDate("ddMMyyyy");
        db.collection("attendance")
                .document(currentDate)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        boolean isValid = false;
                        if (documentSnapshot.exists()) {
                            String startTime = documentSnapshot.getString("start_time");
                            String endTime = documentSnapshot.getString("end_time");
                            Toast.makeText(StudentHome.this, "start"+startTime, Toast.LENGTH_SHORT).show();
                            Toast.makeText(StudentHome.this, "End"+endTime, Toast.LENGTH_SHORT).show();
                            isValid = isCurrentTimeBetween(startTime, endTime);
                            Toast.makeText(StudentHome.this, ""+isValid, Toast.LENGTH_SHORT).show();
//                            if (isCurrentTimeBetween(startTime, endTime)) {
                                // Enable button for marking attendance
                                btnMarkAttendance.setVisibility(View.VISIBLE);
                                btnMarkAttendance.setEnabled(true);
                                btnMarkAttendance.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        // Get the code entered in the EditText
                                        String attendanceCode = etCode.getText().toString().trim();

                                        // Check if the code is not empty
                                        if (!attendanceCode.isEmpty()) {
                                            String currdate = getCurrentDate("ddMMyyyy");
                                            checkLocationAndMarkAttendance();
//                    checkAttendanceCode(attendanceCode, currdate);
                                            // Show a Toast with the attendance code
                                            Toast.makeText(StudentHome.this, "Attendance Code: " + attendanceCode, Toast.LENGTH_SHORT).show();
                                        } else {
                                            // Show a Toast if the code is empty
                                            Toast.makeText(StudentHome.this, "Please enter the attendance code", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });


//                            } else {
//                                // Disable button for marking attendance
//                                btnMarkAttendance.setVisibility(View.INVISIBLE);
//                                btnMarkAttendance.setEnabled(false);
//                            }


                        }

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Handle errors
                        Toast.makeText(StudentHome.this, "Error retrieving time data", Toast.LENGTH_SHORT).show();
                    }
                });
    }


    public interface TimeValidationCallback {
        void onValidationResult(boolean isValid);
    }
    private void checkLocationAndMarkAttendance () {
        // Replace these values with the admin's set location
//        double adminLatitude = 17.04625024791985; // Admin's latitude
//        double adminLongitude = 74.61982065468233; // Admin's longitude
        double adminLatitude = 18.452907138112447; // Admin's latitude
        double adminLongitude = 73.85052345448871; // Admin's longitude


        // Set the radius within which the student can mark attendance
        double radius = 1000.0; // in meters
        Toast.makeText(this, "Checking loaction", Toast.LENGTH_SHORT).show();
        // Check the student's location
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions

            return;
        }
        fusedLocationProviderClient.getLastLocation()
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        Location studentLocation = task.getResult();
                        // Check if the student is within the specified radius of the admin's location
                        if (isLocationWithinRadius(studentLocation, adminLatitude, adminLongitude, radius)) {
                            // Student is within the radius, allow attendance marking
                            Toast.makeText(this, " same location", Toast.LENGTH_SHORT).show();

                            String attendanceCode = etCode.getText().toString().trim();
                            String currdate = getCurrentDate("ddMMyyyy");
                            checkAttendanceCode(attendanceCode, currdate);
//                            markAttendance();

                        } else {
                            // Student is outside the radius, show a message
                            Toast.makeText(StudentHome.this, "You are not near the admin's location", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        // Handle location retrieval failure
                        Toast.makeText(StudentHome.this, "Error getting location", Toast.LENGTH_SHORT).show();
                    }
                });
    }
    private boolean isLocationWithinRadius(Location studentLocation, double adminLatitude, double adminLongitude, double radius) {
        float[] distance = new float[1];
        Location.distanceBetween(studentLocation.getLatitude(), studentLocation.getLongitude(),
                adminLatitude, adminLongitude, distance);
        return distance[0] <= radius;
    }
    private void markAttendance() {
        // Implement attendance marking logic here
        Toast.makeText(StudentHome.this, "Attendance marked successfully", Toast.LENGTH_SHORT).show();
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
                                                        // Query documents where "present" is 0

// Reference to the specific attendance document for the current student
                                                        DocumentReference studentAttendanceRef = db.collection("attendance")
                                                                .document(currentDate)
                                                                .collection("absent") // Assuming you're updating within the "absent" collection based on your example
                                                                .document(currentUserUID);

// Update the "present" field for the current student to 1
                                                        studentAttendanceRef.update("present", 1)
                                                                .addOnSuccessListener(aVoid1 -> Log.d(TAG, "Attendance updated to present for student UUID: " + currentUserUID))
                                                                .addOnFailureListener(e -> Log.w(TAG, "Error updating attendance for student UUID: " + currentUserUID, e));

// Reference to the "absent" collection for the current date, but adjust according to your actual structure

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
    private boolean isCurrentTimeBetween(String startTime, String endTime) {
        DateFormat dateFormat = new SimpleDateFormat("hh:mm", Locale.getDefault());
        try {
            // Get current date to set it with the parsed time
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);

            Date startDate = dateFormat.parse(startTime);
            Date endDate = dateFormat.parse(endTime);
            Date currentTime = calendar.getTime(); // Current time is set to 00:00 AM

            // Adjust current date to the time to compare
            calendar.setTime(currentTime);
            calendar.set(Calendar.HOUR_OF_DAY, calendar.get(Calendar.HOUR));
            calendar.set(Calendar.MINUTE, calendar.get(Calendar.MINUTE));

            // Check if current time is between start time and end time
            return currentTime.after(startDate) && currentTime.before(endDate);
        } catch (ParseException e) {
            e.printStackTrace();
            return false;
        }
    }


}