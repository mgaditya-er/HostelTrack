package com.example.hosteltrack;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
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


public class LoginActivity extends AppCompatActivity {
    private EditText editTextEmail, editTextPassword;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        // Check if the user is logged in

        checkUserLoggedIn();
        AppCompatButton Loginbtn = findViewById(R.id.btnlogin);
        TextView signupbtn = findViewById(R.id.signUpBtn);
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        Spinner spinnerRole = findViewById(R.id.spinnerRole);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.roles,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRole.setAdapter(adapter);

        spinnerRole.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {

                Loginbtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        String email = editTextEmail.getText().toString().trim();
                        String password = editTextPassword.getText().toString().trim();
                        String selectedRole = parentView.getItemAtPosition(position).toString();
                        loginUser(selectedRole,email,password);


                    }
                });
                // Navigate to the respective activity based on the selected role

            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // Do nothing here if needed
            }
        });

        signupbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this,Register.class);
                startActivity(intent);
                finish();
            }
        });



    }
    private void checkUserLoggedIn() {

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();

            FirebaseFirestore db = FirebaseFirestore.getInstance();

            // Check if the user is in the "admins" collection
            DocumentReference adminRef = db.collection("admins").document(userId);
            adminRef.get()
                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            if (documentSnapshot.exists()) {
                                // User is present in the "admins" collection
                                handleUserRole("Admin");
                            } else {
                                // Check if the user is in the "users" collection
                                DocumentReference userRef = db.collection("students").document(userId);
                                userRef.get()
                                        .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                            @Override
                                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                                if (documentSnapshot.exists()) {
                                                    // User is present in the "users" collection
                                                    handleUserRole("Student");
                                                } else {
                                                    // User is not in either collection
                                                    Toast.makeText(LoginActivity.this, "New User", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                // Handle failure to retrieve user data
                                            }
                                        });
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            // Handle failure to retrieve user data
                        }
                    });
        }

    }



    private void handleUserRole(String role) {
        // Handle the user's role, e.g., redirect to AdminHome or StudentHome
        if ("Admin".equals(role)) {
            // Redirect to AdminHome activity
            Intent intent = new Intent(LoginActivity.this, AdminHome.class);
            startActivity(intent);
            finish(); // Optional: finish the current activity
        } else if ("Student".equals(role)) {
            Intent intent = new Intent(LoginActivity.this, StudentHome.class);
            startActivity(intent);
            finish();
            // User is already in the StudentHome activity
        } else {
            // Handle other roles as needed
            Toast.makeText(this, "Role Issue", Toast.LENGTH_SHORT).show();
        }
    }
    private void loginUser(String selectedRole,String email,String Password) {
        mAuth.signInWithEmailAndPassword(email, Password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, handle accordingly
                            FirebaseUser user = mAuth.getCurrentUser();

                            if ("Admin".equals(selectedRole)) {
                                Toast.makeText(LoginActivity.this, "Admin!", Toast.LENGTH_SHORT).show();
                                checkIfAdmin(selectedRole);
                            } else if ("Student".equals(selectedRole)) {
                                checkIfStudent(selectedRole);

                            }

                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(LoginActivity.this, "Authentication failedd.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

    }

    private void checkIfStudent(String selectedRole) {
        FirebaseUser user = mAuth.getCurrentUser();

        if (user != null) {
            String userId = user.getUid();
            db.collection(selectedRole.toLowerCase() + "s").document(userId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            // User is a student
                            Intent intent = new Intent(LoginActivity.this, StudentHome.class);
                            Toast.makeText(LoginActivity.this, "Login successful!"+user.getUid(), Toast.LENGTH_SHORT).show();

                            startActivity(intent);
                            finish(); // Optional: finish the current activity
                        } else {
                            Toast.makeText(LoginActivity.this, "Unauthorized role", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(LoginActivity.this, "Error checking user role", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    private void checkIfAdmin(String selectedRole) {
        FirebaseUser user = mAuth.getCurrentUser();

        if (user != null) {
            String userId = user.getUid();
            db.collection(selectedRole.toLowerCase() + "s").document(userId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            // User is a student
                            Intent intent = new Intent(LoginActivity.this, AdminHome.class);
                            Toast.makeText(LoginActivity.this, "Login successful!"+user.getUid(), Toast.LENGTH_SHORT).show();

                            startActivity(intent);
                            finish(); // Optional: finish the current activity
                        } else {
                            Toast.makeText(LoginActivity.this, "Unauthorized role", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(LoginActivity.this, "Error checking user role", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }


}