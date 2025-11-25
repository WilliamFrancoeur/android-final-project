package com.example.e_commerceapplication;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.Objects;

import kotlin.text.Regex;

public class RegisterActivity extends AppCompatActivity {

    EditText email, password, confirmPass;
    Button register, login;
    FirebaseAuth firebaseAuth;
    DatabaseReference databaseUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);

        email = findViewById(R.id.emailregister);
        password = findViewById(R.id.passwordregister);
        confirmPass = findViewById(R.id.confirmpasswordregister);

        register = findViewById(R.id.registerbtn);
        login = findViewById(R.id.loginregbtn);

        firebaseAuth = FirebaseAuth.getInstance();

        databaseUser = FirebaseDatabase.getInstance().getReference("users");

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerUser();
            }
        });
    }
    private void registerUser(){

        String emailStr = email.getText().toString().trim();
        String passStr = password.getText().toString().trim();
        String confirmPassStr = confirmPass.getText().toString().trim();

        if (TextUtils.isEmpty(emailStr) || TextUtils.isEmpty(passStr) || TextUtils.isEmpty(confirmPassStr)){
            Toast.makeText(this, "Please fill all the fields!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(emailStr).matches()){
            email.setError("Invalid Email");
            email.requestFocus();
            return;
        }
        if (passStr.length() < 8){
            password.setError("Password should be more than 8 char");
            password.requestFocus();
            return;
        }
        if (!passStr.equals(confirmPassStr)){
            confirmPass.setError("Passwords do not match");
            confirmPass.requestFocus();
            return;
        }


        firebaseAuth.createUserWithEmailAndPassword(emailStr, passStr).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()){
                    Toast.makeText(RegisterActivity.this, "Successfully registered", Toast.LENGTH_SHORT).show();

                    if (saveUser()) {

                        String email = firebaseAuth.getCurrentUser().getEmail();

                        if (email.contains("@admin.")){
                            startActivity(new Intent(RegisterActivity.this, AdminMainActivity.class));
                        }else {
                            startActivity(new Intent(RegisterActivity.this, MainActivity.class));
                        }
                    }
                } else {
                    Toast.makeText(RegisterActivity.this, "Error", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    private boolean saveUser(){

        String id = firebaseAuth.getInstance().getCurrentUser().getUid();

        if (id == null) return false;

        String email = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        String role = "";
        if (email.contains("@admin.")){
            role = "admin";
        } else {
            role = "user";
        }

        User user = new User(id, email, role);

        databaseUser.child(id).setValue(user).addOnSuccessListener(aVoid -> {
            Toast.makeText(this, "Success Product added", Toast.LENGTH_SHORT).show();
            finish();
        });


        return true;

    }
}