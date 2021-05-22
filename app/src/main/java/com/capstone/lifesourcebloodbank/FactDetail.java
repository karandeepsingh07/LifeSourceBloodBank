package com.capstone.lifesourcebloodbank;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.widget.ImageView;

public class FactDetail extends AppCompatActivity {

    ImageView imageView;
    int[] contents= {R.drawable.fact1,R.drawable.fact2,R.drawable.fact3,R.drawable.fact4,R.drawable.fact5,R.drawable.fact6,R.drawable.fact7,R.drawable.fact8, R.drawable.fact9};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fact_detail);
        int iNo=getIntent().getExtras().getInt("num");
        imageView=findViewById(R.id.imageFact);
        imageView.setImageResource(contents[iNo]);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }
}
