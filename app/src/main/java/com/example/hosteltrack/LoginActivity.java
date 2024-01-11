package com.example.hosteltrack;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;


public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        AppCompatButton Loginbtn = findViewById(R.id.btnlogin);
        TextView signupbtn = findViewById(R.id.signUpBtn);


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
                String selectedRole = parentView.getItemAtPosition(position).toString();



                Loginbtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if ("Admin".equals(selectedRole)) {
                            Intent intent = new Intent(LoginActivity.this, AdminHome.class);
                            startActivity(intent);
                            finish(); // Optional: finish the current activity
                        } else if ("Student".equals(selectedRole)) {
                            Intent intent = new Intent(LoginActivity.this, StudentHome.class);
                            startActivity(intent);
                            finish(); // Optional: finish the current activity
                        }
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
}