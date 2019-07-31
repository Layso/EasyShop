package com.daissoda.easyshop;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class RegisterActivity extends AppCompatActivity {
	FirebaseAuth auth;



	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_register);


		// Set Firebase Auth object
		auth = FirebaseAuth.getInstance();

		// Set Register button on click action
		Button registerButton = findViewById(R.id.register_button);
		registerButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				EditText emailInput = findViewById(R.id.register_email);
				EditText passwordInput = findViewById(R.id.register_password);
				EditText passwordAgainInput = findViewById(R.id.register_password_again);
				RegisterWithCredentials(emailInput.getText().toString(), passwordInput.getText().toString(), passwordAgainInput.getText().toString());
			}
		});

		// Set Login Tab button on click action
		Button loginTabButton = findViewById(R.id.login_tab_button);
		loginTabButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
	}


	private void RegisterWithCredentials(String email, String password, String passwordAgain) {
		if ("".equals(email) || "".equals(password) || "".equals(passwordAgain)) {
			Toast.makeText(this, "Please fill the fields", Toast.LENGTH_SHORT).show();
		} else if (!password.equals(passwordAgain)) {
			Toast.makeText(this, "Please enter matching passwords", Toast.LENGTH_SHORT).show();
		} else {
			auth
					.createUserWithEmailAndPassword(email, password)
					.addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
						@Override
						public void onComplete(@NonNull Task<AuthResult> task) {
							if (task.isComplete() && task.isSuccessful()) {
								finish();
							} else {
								Toast.makeText(RegisterActivity.this, "Register failed, please try again", Toast.LENGTH_SHORT).show();
							}
						}
					});
		}
	}
}
