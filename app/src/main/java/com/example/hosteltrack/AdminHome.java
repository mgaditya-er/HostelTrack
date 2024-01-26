package com.example.hosteltrack;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class AdminHome extends AppCompatActivity {

    private TextView tvCode;
    private FirebaseFirestore db;

    private Button btnSignOut;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_home);
        db = FirebaseFirestore.getInstance();

        btnSignOut = findViewById(R.id.btnSignOut);
        Button btnHome = findViewById(R.id.btnHome);
        Button btnReport = findViewById(R.id.btnReport);
        Button btnChangeCode = findViewById(R.id.changecode);
        tvCode = findViewById(R.id.tvCode);
        Button btnShareCode = findViewById(R.id.btnShareCode);





        boolean hasDateChanged = DateUtils.hasDateChanged(this);

        if (hasDateChanged) {
            // Call createData() or perform any other actions needed on date change
            createData();

            // Save the current date
            DateUtils.saveCurrentDate(this);
        }

        updateCode();

        TextView tvDate = findViewById(R.id.tvDate);

        // Get today's date in the specified format
        String currentDate = getCurrentDate("ddMMyyyy");

        String currdate = getCurrentDate("ddMMyyyy");


        markAllStudentsAbsent(currdate);

        // Set the date to the TextView
        tvDate.setText("Date: " + currentDate);
        btnChangeCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Generate and set a new random code
                updateCode();
            }
        });

        // Set OnClickListener for the "Share Code" button
        btnShareCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Share the code
                shareCode();
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
    private void updateCode() {
        // Generate a random 6-digit code
        String newCode = generateRandomCode();

        // Update the TextView with the new code
        tvCode.setText(newCode);
        storeData(newCode);

    }


    private void createData()
    {
        String currentDate = getCurrentDate("ddMMyyyy");

        int Code = 123456;
        // Create a collection with today's date as the name
        Map<String, Object> data = new HashMap<>();
        data.put("code", Code);
//        data.put("presentStudents", /* Add your list of present students here */);

        db.collection("attendance")  // Collection named "attendance"
                .document(currentDate)  // Document named with today's date
                .set(data)
                .addOnSuccessListener(documentReference -> {
                    // Data successfully stored
                    // You can add any additional logic here
                    Toast.makeText(this, "created"+data, Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    // Handle errors
                });

        markAllStudentsAbsent(currentDate);


    }

    private void markAllStudentsAbsent(String currentDate) {

        CollectionReference studentsCollection = db.collection("students");

        // Reference to the "absent" subcollection for the current date
        CollectionReference absentCollection = db.collection("attendance")
                .document(currentDate)
                .collection("absent");

        // Fetch all documents from the "students" collection
        studentsCollection.get()
                .addOnSuccessListener(querySnapshot -> {
                    // Iterate through each document and mark the student as absent
                    for (QueryDocumentSnapshot document : querySnapshot) {
                        String studentUID = document.getId();

                        // Add the student's UUID to the "absent" subcollection
                        absentCollection.document(studentUID)
                                .set(new HashMap<>())
                                .addOnSuccessListener(aVoid ->
                                        // Document successfully created/updated
                                        Toast.makeText(AdminHome.this, "All students marked absent", Toast.LENGTH_SHORT).show())
                                .addOnFailureListener(e ->
                                        // Handle errors
                                        Toast.makeText(AdminHome.this, "Error marking students absent", Toast.LENGTH_SHORT).show());
                    }
                })
                .addOnFailureListener(e ->
                        // Handle errors
                        Toast.makeText(AdminHome.this, "Error fetching students", Toast.LENGTH_SHORT).show());

    }

    private void storeData(String code) {
        // Get today's date
//        String currentDate = getCurrentDate();
        String currentDate = getCurrentDate("ddMMyyyy");


        // Create a collection with today's date as the name
        Map<String, Object> data = new HashMap<>();
        data.put("code", code);
//        data.put("presentStudents", /* Add your list of present students here */);

        db.collection("attendance")  // Collection named "attendance"
                .document(currentDate)  // Document named with today's date
                .update(data)
                .addOnSuccessListener(documentReference -> {
                    // Data successfully stored
                    // You can add any additional logic here
                    Toast.makeText(this, "created"+data, Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    // Handle errors
                });
    }
    private String generateRandomCode() {
        Random random = new Random();
        int randomNumber = random.nextInt(900000) + 100000; // Generate a random number between 100000 and 999999
        return String.valueOf(randomNumber);
    }
    private String getCurrentDate(String format) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(format, Locale.getDefault());
        return dateFormat.format(new Date());
    }
    private void shareCode() {
        // Get the code to share
        String codeToShare = tvCode.getText().toString();

        // Create an Intent to share the code
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, "Check out Attendance code: " + codeToShare+" And Mark Your Attendance");

        // Start the activity to show the sharing options
        startActivity(Intent.createChooser(shareIntent, "Share Code"));
    }
    private void signOut() {
        // Sign out from Firebase authentication
        FirebaseAuth.getInstance().signOut();

        // Redirect to the login screen
        Intent intent = new Intent(AdminHome.this, LoginActivity.class);
        startActivity(intent);
        finish(); // Optional: finish the current activity
    }
}