package com.example.hosteltrack;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class Present extends AppCompatActivity {

    private RecyclerView recyclerView;
    private static final String TAG = "PresentActivity";

    private StudentAdapter adapter;
    private List<Student> presentStudentsList;

    private TextView tvPresentCount;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_present);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Initialize RecyclerView and adapter
        recyclerView = findViewById(R.id.recyclerViewPresent);
        presentStudentsList = new ArrayList<>();
        adapter = new StudentAdapter(presentStudentsList);
        TextView tvDayDate = findViewById(R.id.tvDayDate);

        // Set up RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        tvPresentCount = findViewById(R.id.tvPresentCount);

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, d MMMM yyyy", Locale.getDefault());
        String dayDate = dateFormat.format(calendar.getTime());
// Set the text to the TextView
        tvDayDate.setText(dayDate);
        // Update the count whenever the adapter's data changes
        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                updatePresentCount();
            }
        });
        // Fetch UUIDs of present students from Firestore
        // Reference to the "present" subcollection for the current date
        String currentDate = getCurrentDate("ddMMyyyy");
        CollectionReference presentCollection = db.collection("attendance")
                .document(currentDate)
                .collection("absent");

        // Fetch all documents with "present" = 1 from the "present" subcollection
        presentCollection.whereEqualTo("present", 1)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<String> presentStudentUUIDs = new ArrayList<>();
                        for (DocumentSnapshot document : task.getResult()) {
                            String uuid = document.getId();
                            presentStudentUUIDs.add(uuid);
                        }

                        // Fetch student details based on present UUIDs
                        fetchPresentStudentsDetails(presentStudentUUIDs);
                    } else {
                        Log.w(TAG, "Error getting documents.", task.getException());
                    }
                });
    }

    private void updatePresentCount() {
        int count = presentStudentsList.size();
        tvPresentCount.setText("Count " + count);
    }

    private void fetchPresentStudentsDetails(List<String> presentStudentUUIDs) {
        for (String uuid : presentStudentUUIDs) {
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
                                    presentStudentsList.add(new Student(uuid, name,number));
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
}
