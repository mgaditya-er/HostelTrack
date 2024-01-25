package com.example.hosteltrack;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

public class AdminHome extends AppCompatActivity {

    private TextView tvCode;

    private Button btnSignOut;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_home);

        btnSignOut = findViewById(R.id.btnSignOut);
        Button btnHome = findViewById(R.id.btnHome);
        Button btnReport = findViewById(R.id.btnReport);
        Button btnChangeCode = findViewById(R.id.changecode);
        tvCode = findViewById(R.id.tvCode);
        Button btnShareCode = findViewById(R.id.btnShareCode);

        updateCode();
        TextView tvDate = findViewById(R.id.tvDate);

        // Get today's date in the specified format
        String currentDate = getCurrentDate("dd-MM-yyyy");

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
        shareIntent.putExtra(Intent.EXTRA_TEXT, "Check out Attendance code: " + codeToShare+"And Mark Your Attendance");

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