package com.example.treasurehunt;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

public class LandingPageActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landing_page);
    }

    public void toLogin(View view) {
        Intent intent = new Intent(LandingPageActivity.this, LoginActivity.class);
        startActivity(intent);
    }

    public void toRegister(View view) {
        Intent intent = new Intent(LandingPageActivity.this, RegisterActivity.class);
        startActivity(intent);
    }
}