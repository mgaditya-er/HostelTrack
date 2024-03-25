package com.example.hosteltrack;

import androidx.appcompat.app.AppCompatActivity;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import android.Manifest;
import android.widget.TextView;
import android.widget.Toast;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Set;

public class Absent extends AppCompatActivity {
    private static final int PERMISSION_SEND_SMS = 101;


    private RecyclerView recyclerView;
    private static final String TAG = "AbsentActivity";

    private StudentAdapter adapter;
    private List<Student> absentStudentsList;

    private FirebaseFirestore db;
    private TextView tvAbsentCount;

    FloatingActionButton fabAdd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_absent);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();
        fabAdd = findViewById(R.id.fabAdd);
        TextView tvDayDate = findViewById(R.id.tvDayDate);

        // Initialize RecyclerView and adapter
        recyclerView = findViewById(R.id.recyclerViewAbsent);
        absentStudentsList = new ArrayList<>();
        adapter = new StudentAdapter(absentStudentsList);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, PERMISSION_SEND_SMS);
        }
        // Set up RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        tvAbsentCount = findViewById(R.id.tvAbsentCount);

        // Update the count whenever the adapter's data changes
        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                updateAbsentCount();
            }
        });

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, d MMMM yyyy", Locale.getDefault());
        String dayDate = dateFormat.format(calendar.getTime());
// Set the text to the TextView
        tvDayDate.setText(dayDate);

        // Fetch UUIDs of absent students from Firestore
        // Reference to the "absent" subcollection for the current date
        String currentDate = getCurrentDate("ddMMyyyy");

        CollectionReference absentCollection = db.collection("attendance")
                .document(currentDate)
                .collection("absent");

// Fetch all documents with "status" = 0 from the "absent" subcollection
        absentCollection.whereEqualTo("present", 0)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<String> absentStudentUUIDs = new ArrayList<>();
                        for (DocumentSnapshot document : task.getResult()) {
                            String uuid = document.getId();
                            absentStudentUUIDs.add(uuid);
                        }

                        // Fetch student details based on absent UUIDs
                        fetchAbsentStudentsDetails(absentStudentUUIDs);
                    } else {
                        Log.w(TAG, "Error getting documents.", task.getException());
                    }
                });


        fabAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendSmsToAbsentStudents();
            }
        });

    }

    private void updateAbsentCount() {
        int count = adapter.getItemCount();
        tvAbsentCount.setText("Count : " + count);
    }

    private void sendSmsToAbsentStudents() {
        // Check permission again (in case it's not already checked/obtained)
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED) {
            // Assuming getAbsentStudentNumbers returns a List<String> of phone numbers
//            List<String> phoneNumbers = getAbsentStudentNumbers();
            List<String> phoneNumberslist = getAbsentStudentNumbers();

// Convert the List to a Set to enforce uniqueness
            Set<String> uniquePhoneNumbersSet = new HashSet<>(phoneNumberslist);

// Convert the Set back to a List if needed
            List<String> phoneNumbers = new ArrayList<>(uniquePhoneNumbersSet);

            SmsManager smsManager = SmsManager.getDefault();
            String message = "Mark Your Attendence , Else You Will be marked as absent today !";

            for (String phoneNumber : phoneNumbers) {
                try {
                    smsManager.sendTextMessage(phoneNumber, null, message, null, null);
                } catch (Exception e) {
                    Log.e(TAG, "SMS send failed", e);
                    // Handle exception
                }
            }
        } else {
            // Request permission if not granted
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, PERMISSION_SEND_SMS);
        }
    }

    private List<String> getAbsentStudentNumbers() {
        // Your logic to extract phone numbers from absentStudentsList or elsewhere
        List<String> numbers = new ArrayList<>();
        // Dummy implementation - replace with your actual logic
        for (Student student : absentStudentsList) {
            // Assuming Student class has a getPhoneNumber method
            numbers.add(student.getContactNumber());
        }
        return numbers;
    }

    private void fetchAbsentStudentsDetails(List<String> absentStudentUUIDs) {
        for (String uuid : absentStudentUUIDs) {
            db.collection("students")
                    .document(uuid)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                String name = document.getString("username");
                                String number = document.getString("phone");
                                if (name != null) {
                                    // Create a Student object and add it to the list
                                    absentStudentsList.add(new Student(uuid, name,number));
//                                    Toast.makeText(this, "no:"+number, Toast.LENGTH_SHORT).show();
                                    adapter.notifyDataSetChanged();
                                }
                            } else {
                                Log.d(TAG, "No such document for UUID: " + uuid);
                            }
                        } else {
                            Log.w(TAG, "Error fetching student details for UUID: " + uuid, task.getException());
                        }
                    });
        }
    }


    private String getCurrentDate(String format) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(format, Locale.getDefault());
        return dateFormat.format(new Date());
    }
    // Fetch student details based on UUID
    private void fetchStudentDetails(String uuid) {
        db.collection("students")
                .document(uuid)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                String name = document.getString("name");
                                String number = document.getString("phone");
                                if (name != null) {
                                    absentStudentsList.add(new Student(uuid, name,number));
                                    adapter.notifyDataSetChanged();
                                }
                            } else {
                                Log.d(TAG, "No such document");
                            }
                        } else {
                            Log.w(TAG, "get failed with ", task.getException());
                        }
                    }
                });
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_SEND_SMS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission was granted
            } else {
                // Permission was denied
            }
        }
    }
}
