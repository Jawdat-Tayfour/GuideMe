    package com.example.guidemeJM;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.content.Intent;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class TourGLoginActivity extends AppCompatActivity {
    private EditText nEmail , mPassword ;
    private Button nLogin , mRegistration ;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener firebaseAuthlistener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tour_glogin);
        mAuth = FirebaseAuth.getInstance();
        firebaseAuthlistener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user != null) {
                    Intent intent = new Intent(TourGLoginActivity.this, DriverMapActivity.class);
                    startActivity(intent);
                    finish();
                    return;
                }
            }
        };
        nEmail = (EditText) findViewById(R.id.email);
        mPassword = (EditText) findViewById(R.id.password);
        nLogin = (Button) findViewById(R.id.login);
        mRegistration = (Button) findViewById(R.id.registration);
        mRegistration.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String email = nEmail.getText().toString();
                final String password = mPassword.getText().toString();
                mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(TourGLoginActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (!task.isSuccessful()) {
                            Toast.makeText(TourGLoginActivity.this, "sign up error", Toast.LENGTH_SHORT).show();

                        } else {
                            String user_id = mAuth.getCurrentUser().getUid();
                            DatabaseReference current_user_db = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(user_id);
                            current_user_db.setValue(true);
                            Intent intent = new Intent(TourGLoginActivity.this , TourGLoginActivity.class) ;
                        }
                    }
                });

            }
        });
        nLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String email = nEmail.getText().toString();
                final String password = mPassword.getText().toString();
                mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(TourGLoginActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (!task.isSuccessful()) {
                            Toast.makeText(TourGLoginActivity.this, "sign in error", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
    }

        @Override
        protected void onStart () {
            super.onStart();
            mAuth.addAuthStateListener(firebaseAuthlistener);
        }

        @Override
        protected void onStop () {
            super.onStop();
            mAuth.removeAuthStateListener(firebaseAuthlistener);
        }
    }