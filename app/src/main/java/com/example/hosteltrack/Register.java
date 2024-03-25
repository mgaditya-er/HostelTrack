package com.example.hosteltrack;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class Register extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private Spinner spinnerRole;
    private EditText editTextUsername, editTextEmail, editenrnumber,editTextPhone, editTextPassword;
    private AppCompatButton signUpBtn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        TextView signInBtn = findViewById(R.id.signInBtn);
        editTextUsername = findViewById(R.id.editTextUsername);
        editTextEmail = findViewById(R.id.editTextEmail);
        editenrnumber = findViewById(R.id.Enrollnumber);
        editTextPhone = findViewById(R.id.editTextPhone);
        editTextPassword = findViewById(R.id.editTextPassword);
        signUpBtn = findViewById(R.id.btnRegister);
        spinnerRole = findViewById(R.id.spinnerRole);

        signInBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Register.this,LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });

        signUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateForm()) {
                String enrollmentNumber = editenrnumber.getText().toString().trim();

                validenrnumber(enrollmentNumber, new EnrollmentValidationCallback() {
                    @Override
                    public void onValidationResult(boolean isValid) {
                        // Handle the validation result here
                        if (isValid) {
                            // Enrollment number is valid
                            Toast.makeText(Register.this, "velid", Toast.LENGTH_SHORT).show();

                            mAuth.createUserWithEmailAndPassword(editTextEmail.getText().toString().trim(),
                                            editTextPassword.getText().toString().trim())
                                    .addOnCompleteListener(Register.this, new OnCompleteListener<AuthResult>() {
                                        @Override
                                        public void onComplete(@NonNull Task<AuthResult> task) {
                                            if (task.isSuccessful()) {
                                                // User created successfully, save user data to Firestore
                                                saveUserDataToFirestore();

                                                // Display a success message or navigate to the next screen

                                                // If validation passes, proceed with registration
                                                // You can add your Firebase registration code here
                                                // For now, let's just display a Toast message
                                                Toast.makeText(Register.this, "Registration successful!", Toast.LENGTH_SHORT).show();
                                                Intent intent = new Intent(Register.this,LoginActivity.class);
                                                startActivity(intent);
                                                finish();
                                            } else {
                                                // If sign in fails, display a message to the user.
                                                Toast.makeText(Register.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });



                        } else {
                            // Enrollment number is not valid
                            Toast.makeText(Register.this, "Invalid", Toast.LENGTH_SHORT).show();

                        }
                    }
                });

                }

            }
        });


    }

    private void validenrnumber(final String enrollmentNumber,  final EnrollmentValidationCallback callback) {
        // Reference to the document containing enrollment numbers

        DocumentReference docRef = db.collection("Enrollmentlist").document("Studentlist");

        // Query Firestore to check if the enrollment number exists and its value is false
        docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    // Check if enrollment number exists and its value is false
                    Boolean isEnrollmentValid = documentSnapshot.getBoolean(enrollmentNumber);
                    if (isEnrollmentValid != null && !isEnrollmentValid) {
                        // Enrollment number exists and its value is false
                        // Call the callback method with true
                        callback.onValidationResult(true);
                        return;
                    }
                }
                // Enrollment number doesn't exist or its value is true
                // Call the callback method with false
                callback.onValidationResult(false);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // Failed to query Firestore
                // Call the callback method with false
                callback.onValidationResult(false);
            }
        });
    }


    interface EnrollmentValidationCallback {
        void onValidationResult(boolean isValid);
    }
    private boolean validateForm() {
        boolean isValid = true;

        String username = editTextUsername.getText().toString().trim();
        String email = editTextEmail.getText().toString().trim();
        String enrnumber = editenrnumber.getText().toString().trim();
        String phone = editTextPhone.getText().toString().trim();

        String password = editTextPassword.getText().toString().trim();

        if (username.isEmpty()) {
            isValid = false;
            editTextUsername.setError("Username is required");
        }

        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            isValid = false;
            editTextEmail.setError("Enter a valid email address");
        }

        if (enrnumber.isEmpty()) {
            isValid = false;
            editenrnumber.setError("Enter a valid Enrollment number");
        }
        if (phone.isEmpty() || !Patterns.PHONE.matcher(phone).matches()) {
            isValid = false;
            editTextPhone.setError("Enter a valid phone number");
        }

        if (password.isEmpty() || password.length() < 6) {
            isValid = false;
            editTextPassword.setError("Password must be at least 6 characters long");
        }

        return isValid;
    }

    private void saveUserDataToFirestore() {
        FirebaseUser user = mAuth.getCurrentUser();

        if (user != null) {
            String userId = user.getUid();
            String username = editTextUsername.getText().toString().trim();
            String email = user.getEmail();
            String enrnumber = editenrnumber.getText().toString().trim();
            String phone = editTextPhone.getText().toString().trim();
            String role = getSelectedRole();

            User newUser = new User(username, email, enrnumber,phone, role);

            // Adjust the collection path based on the user's role

            String collectionPath;
            if ("Admin".equals(role)) {
                collectionPath = "admins";
            } else {
                collectionPath = "students";
            }
            db.collection(collectionPath).document(userId)
                    .set(newUser)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d("Register", "User data saved to Firestore.");
                            updateEnrollmentNumber(enrnumber);

                            Toast.makeText(Register.this, "Registration successful!", Toast.LENGTH_SHORT).show();
                            finish(); // Finish the registration activity after successful registration
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w("Register", "Error saving user data to Firestore.", e);
                            Toast.makeText(Register.this, "Error saving user data.", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void updateEnrollmentNumber(final String enrollmentNumber) {
        // Reference to the document containing enrollment numbers
        DocumentReference docRef = db.collection("Enrollmentlist").document("Studentlist");

        // Update the enrollment number value to true
        docRef
                .update(enrollmentNumber, true)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Enrollment number updated successfully
                        Toast.makeText(Register.this, "Enrollment number updated successfully", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Failed to update enrollment number
                        Toast.makeText(Register.this, "Failed to update enrollment number: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private String getSelectedRole() {
        return spinnerRole.getSelectedItem().toString();
    }
}