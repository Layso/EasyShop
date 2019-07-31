package com.daissoda.easyshop;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

public class MainActivity extends AppCompatActivity {
	private static final int splashDelay = 4000;



	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);


		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				Intent loginActivity = new Intent(getApplicationContext(), LoginActivity.class);
				startActivity(loginActivity);
			}
		}, splashDelay);
	}
}
