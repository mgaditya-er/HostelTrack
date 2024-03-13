package com.example.hosteltrack;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class Absent extends AppCompatActivity {

    private RecyclerView recyclerView;
    private static final String TAG = "AbsentActivity";

    private StudentAdapter adapter;
    private List<Student> absentStudentsList;

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_absent);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Initialize RecyclerView and adapter
        recyclerView = findViewById(R.id.recyclerViewAbsent);
        absentStudentsList = new ArrayList<>();
        adapter = new StudentAdapter(absentStudentsList);

        // Set up RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

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
                                if (name != null) {
                                    // Create a Student object and add it to the list
                                    absentStudentsList.add(new Student(uuid, name));
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
                                if (name != null) {
                                    absentStudentsList.add(new Student(uuid, name));
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
}
