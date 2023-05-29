package com.example.graduateapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {

    //views
    EditText mNameEt, mSurnameEt,  mEmailEt, mPasswordEt;
    Button mRegisterBtn;
    TextView mHaveAccountTv;

    //progressbar to display while registering user
    ProgressDialog progressDialog;

    //declare an instance of firebaseauth
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        //actionbar and its title
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Create Account");

        //enable back button
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);

        //init
        mNameEt = findViewById(R.id.nameEt);
        mSurnameEt = findViewById(R.id.surnameEt);
        mEmailEt = findViewById(R.id.emailEt);
        mPasswordEt = findViewById(R.id.passwordEt);
        mRegisterBtn = findViewById(R.id.registeruser);
        mHaveAccountTv = findViewById(R.id.have_accountTv);

        //initalize firebase auth
        mAuth = FirebaseAuth.getInstance();

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Registering User...");

        //handle register btn click
        mRegisterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //getting inputs
                String firstName = mNameEt.getText().toString().trim();
                String lastName = mSurnameEt.getText().toString().trim();
                String email = mEmailEt.getText().toString().trim();
                String password = mPasswordEt.getText().toString().trim();

                if (!Patterns.EMAIL_ADDRESS.matcher(email).matches() && !email.endsWith("@std.yildiz.edu.tr")) {
                    // Set error and focus to email EditText
                    mEmailEt.setError("Invalid Email");
                    mEmailEt.setFocusable(true);
                } else if (!email.endsWith("@std.yildiz.edu.tr")) {
                    // Display an error message for invalid domain
                    mEmailEt.setError("Invalid Email Domain");
                    mEmailEt.setFocusable(true);
                } else {
                    // Register the user
                    registerUser(firstName, lastName, email, password);
                }

            }
        });

        //handle login textview click listener
        mHaveAccountTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                finish();
            }
        });



    }

    private void registerUser(String firstName, String lastName, String email, String password) {
        // inputs are valid, show progress dialog
        progressDialog.show();

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, dismiss dialog and start register activity

                            progressDialog.dismiss();
                            FirebaseUser user = mAuth.getCurrentUser();
                            user.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){
                                        //get user email and uid from auth
                                        String email = user.getEmail();
                                        String uid = user.getUid();

                                        //when user is registered store user info in firebase realtime database too
                                        //using hashmap
                                        HashMap<Object, String> hashMap = new HashMap<>();
                                        //put info in hashmap
                                        hashMap.put("email", email);
                                        hashMap.put("uid", uid);
                                        hashMap.put("name", "");// will add later (e.g. edit profile)
                                        hashMap.put("phone", "");
                                        hashMap.put("image", "");

                                        //firebase database instance
                                        FirebaseDatabase database = FirebaseDatabase.getInstance();

                                        //path to store user data named "Users"
                                        DatabaseReference reference = database.getReference("Users");

                                        //put data within hashmap in database
                                        reference.child(uid).setValue(hashMap);


                                        Toast.makeText(RegisterActivity.this, "Registered. Please verify your email.\n" +user.getEmail(), Toast.LENGTH_SHORT).show();
                                        startActivity(new Intent(RegisterActivity.this, MainActivity.class));
                                        finish();
                                    } else {
                                        progressDialog.dismiss();
                                        Toast.makeText(RegisterActivity.this, "Authentication failed.",
                                                Toast.LENGTH_SHORT).show();
                                    }

                                }
                            });


                        } else {
                            // If sign in fails, display a message to the user.
                            progressDialog.dismiss();
                            Toast.makeText(RegisterActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();

                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //error, dismiss progress dailog and get and show the error message
                        progressDialog.dismiss();
                        Toast.makeText(RegisterActivity.this, ""+ e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public boolean onSupportNavigateUp() {

        onBackPressed(); //go previous activity
        return super.onSupportNavigateUp();
    }
}