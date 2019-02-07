package com.example.ocrdemo;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.ml.vision.text.FirebaseVisionText;

public class Output extends AppCompatActivity {

    LinearLayout resultBoxes;
    Button backButt;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_output);

        backButt=findViewById(R.id.backButt);
        resultBoxes=findViewById(R.id.text);

        for(FirebaseVisionText.TextBlock block :MainActivity.blocks)
        {
            TextView tv = new TextView(getApplicationContext());
            tv.setText(block.getText());
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            layoutParams.setMargins(15,15,15,15);
            tv.setLayoutParams(layoutParams);
            tv.setTextSize(20);
            tv.setTextColor(Color.WHITE);
            tv.setBackgroundColor(Color.parseColor("#1976D2"));
            resultBoxes.addView(tv);
        }
        backButt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }
}
