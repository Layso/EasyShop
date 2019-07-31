package com.daissoda.easyshop;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {
	private FirebaseAuth auth;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);


		// Set Firebase Auth object
		auth = FirebaseAuth.getInstance();

		// Set Login button on click action
		Button loginButton = findViewById(R.id.login_button);
		loginButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				EditText emailInput = findViewById(R.id.login_email);
				EditText passwordInput = findViewById(R.id.login_password);
				LoginWithCredentials(emailInput.getText().toString(), passwordInput.getText().toString());
			}
		});

		// Set Register Tab button on click action
		Button registerTabButton = findViewById(R.id.register_tab_button);
		registerTabButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent registerTabIntent = new Intent(getApplicationContext(), RegisterActivity.class);
				startActivity(registerTabIntent);
			}
		});
	}


	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			System.exit(0);
		}

		return super.onKeyDown(keyCode, event);
	}


	private void LoginWithCredentials(String email, String password) {
		if ("".equals(email) || "".equals(password)) {
			Toast.makeText(this, "Please fill the fields", Toast.LENGTH_SHORT).show();
		} else {
			auth
				.signInWithEmailAndPassword(email, password)
				.addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
					@Override
					public void onComplete(@NonNull Task<AuthResult> task) {
						if (task.isComplete() && task.isSuccessful()) {
							Intent mainMenuIntent = new Intent(getApplicationContext(), MainMenuActivity.class);
							startActivity(mainMenuIntent);
						} else {
							Toast.makeText(LoginActivity.this, "Login failed, please try again", Toast.LENGTH_SHORT).show();
						}
					}
				});
		}
	}
}
