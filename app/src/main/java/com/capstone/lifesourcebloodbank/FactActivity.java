package com.capstone.lifesourcebloodbank;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class FactActivity extends AppCompatActivity {

    ListView listView;
    String[] fact;
    ArrayAdapter<String> adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fact);
        fact=getResources().getStringArray(R.array.array_facts);
        listView=findViewById(R.id.listView);
        adapter=new ArrayAdapter<>(this,R.layout.support_simple_spinner_dropdown_item,fact);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent=new Intent(FactActivity.this,FactDetail.class);
                intent.putExtra("num",i);
                startActivity(intent);
            }
        });
    }
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }
}
