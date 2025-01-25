package com.example.headtohead;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private Button btnclassic, btnsettings, btnrules, btntf;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btnclassic = findViewById(R.id.btnclassic);
        btnclassic.setOnClickListener(this);
        btnsettings = findViewById(R.id.btnsettings);
        btnsettings.setOnClickListener(this);
        btnrules = findViewById(R.id.btnrules);
        btnrules.setOnClickListener(this);
        btntf = findViewById(R.id.btntf);
        btntf.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        if (v == btnclassic) {
        Intent i = new Intent(this,GameActivityClassic.class);
        startActivity(i);
        }
        if (v == btntf) {
            Intent i = new Intent(this,GameActivityTF.class);
            startActivity(i);
        }
        if (v == btnrules) {
            Intent i = new Intent(this,Rules.class);
            startActivity(i);
        }
        if (v == btnsettings) {
            Intent i = new Intent(this,Settings.class);
            startActivityForResult(i,RESULT_OK);
        }
    }
}