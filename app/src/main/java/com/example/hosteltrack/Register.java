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
import com.google.firebase.firestore.FirebaseFirestore;

public class Register extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private Spinner spinnerRole;
    private EditText editTextUsername, editTextEmail, editTextPhone, editTextPassword;
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




                }

            }
        });


    }
    private boolean validateForm() {
        boolean isValid = true;

        String username = editTextUsername.getText().toString().trim();
        String email = editTextEmail.getText().toString().trim();
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
            String phone = editTextPhone.getText().toString().trim();
            String role = getSelectedRole();

            User newUser = new User(username, email, phone, role);

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

    private String getSelectedRole() {
        return spinnerRole.getSelectedItem().toString();
    }
}