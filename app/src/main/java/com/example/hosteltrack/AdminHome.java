package com.example.hosteltrack;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
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
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

public class AdminHome extends AppCompatActivity {

    private FusedLocationProviderClient fusedLocationProviderClient;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private TextView tvCode;

    private Button btnSignOut;
    private Button btnpresent,btnabsent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_home);
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        btnSignOut = findViewById(R.id.btnSignOut);
        Button btnHome = findViewById(R.id.btnHome);
        Button btnChangeCode = findViewById(R.id.changecode);
        tvCode = findViewById(R.id.tvCode);
        Button btnShareCode = findViewById(R.id.btnShareCode);
        btnpresent = findViewById(R.id.btnpresent);
        btnabsent = findViewById(R.id.btnabsent);
        btnpresent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent presentIntent = new Intent(AdminHome.this,Present.class);
                startActivity(presentIntent);
            }
        });
        btnabsent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent presentIntent = new Intent(AdminHome.this,Absent.class);
                startActivity(presentIntent);
            }
        });

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        Button setLocationButton = findViewById(R.id.btnSetLocation);
        setLocationButton.setOnClickListener(view -> setAdminLocation());
        setLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Call a function to set the admin's location
                Toast.makeText(AdminHome.this, "Set Location", Toast.LENGTH_SHORT).show();
//                setAdminLocation();
                double adminLatitude = 18.451817; // Admin's latitude
                double adminLongitude = 73.8502939; // Admin's longitude
                saveLocationToFirestore(adminLatitude, adminLongitude);
            }
        });


        boolean hasDateChanged = DateUtils.hasDateChanged(this);

        if (hasDateChanged) {
            // Call createData() or perform any other actions needed on date change
            createData();

            // Save the current date
            DateUtils.saveCurrentDate(this);
            String currentDate = getCurrentDate("ddMMyyyy");
            markAllStudentsAbsent(currentDate);
        }

        updateCode();

        TextView tvDate = findViewById(R.id.tvDate);

        // Get today's date in the specified format
        String currentDate = getCurrentDate("ddMMyyyy");

        String currdate = getCurrentDate("ddMMyyyy");


//        markAllStudentsAbsent(currdate);

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

    private void setAdminLocation() {


        // Replace these values with the desired location
//        double adminLatitude = 18.451817; // Admin's latitude
//        double adminLongitude = 73.8502939; // Admin's longitude

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        fusedLocationProviderClient.getLastLocation()
                .addOnCompleteListener(this, new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful() && task.getResult() != null) {
                            Location adminLocation = task.getResult();

                            // Save the admin's location to Firestore
//                            saveLocationToFirestore(adminLocation.getLatitude(), adminLocation.getLongitude());
                                    double adminLatitude = 18.451817; // Admin's latitude
                                    double adminLongitude = 73.8502939; // Admin's longitude
                                    saveLocationToFirestore(adminLatitude, adminLongitude);

                        } else {


                            Toast.makeText(AdminHome.this, "Error getting admin location", Toast.LENGTH_SHORT).show();
                        }
                    }
                });






        // Save the admin location in Firestore or perform any other necessary action
//        saveLocationToFirestore(adminLatitude, adminLongitude);

        Toast.makeText(AdminHome.this, "Admin location set successfully", Toast.LENGTH_SHORT).show();
    }
    private void saveLocationToFirestore(double latitude, double longitude) {
        // Save the admin location to Firestore or any other backend
        // You can use FirebaseFirestore.getInstance().collection("admin").document("location").set(...) for example

        String currentDate = getCurrentDate("ddMMyyyy");
        Map<String, Object> locationData = new HashMap<>();
        locationData.put("latitude", latitude);
        locationData.put("longitude", longitude);
        db.collection("attendance")
                .document(currentDate)
                .collection("hostellocation")
                .document("location")
                .set(locationData)
                .addOnSuccessListener(documentReference -> {
                    // Location data successfully stored
                    Toast.makeText(AdminHome.this, "Admin location set successfully", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    // Handle errors
                    Toast.makeText(AdminHome.this, "Error setting admin location", Toast.LENGTH_SHORT).show();
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
                        Map<String, Object> data = new HashMap<>();
                        data.put("present", 0);
                        // Add the student's UUID to the "absent" subcollection
                        absentCollection.document(studentUID)
                                .set(data)
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