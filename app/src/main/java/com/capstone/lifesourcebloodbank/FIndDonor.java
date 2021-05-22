package com.capstone.lifesourcebloodbank;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.capstone.lifesourcebloodbank.Spinner.MySingleton;
import com.capstone.lifesourcebloodbank.Spinner.State;
import com.capstone.lifesourcebloodbank.Spinner.StateAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;



public class FIndDonor extends AppCompatActivity {

    private static final String KEY_STATE = "state";
    private static final String KEY_CITIES = "cities";
    Spinner stateSpinner;
    Spinner citiesSpinner;
    Spinner spinnerBloodGroup;
    private Button buttonFind;
    private ProgressDialog pDialog;
    private String cities_url = "http://api.androiddeft.com/cities/cities_array.json";
    private String searchbloodGroup,searchState,searchCity;
    FirebaseDatabase database;
    DatabaseReference reference;
    RecyclerView recyclerViewDonor;
    GridLayoutManager recyclerLayoutManager;
    TextView dummtTExtView;
    RecyclerView.Adapter adapter;
    List<DonorPC> donors;
    ProgressBar progressBar;
    boolean check=true;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_donor);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        init();

        //Display state and city name when submit button is pressed
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,new String[] {Manifest.permission.CALL_PHONE},99);
        }
        else {
            Log.d("TAG", "Get Location permission granted");
            buttonFind.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    progressBar.setVisibility(View.VISIBLE);
                    dummtTExtView.setVisibility(View.VISIBLE);
                    donors.clear();
                    getDataFromActivity(v);
                }
            });
        }
    }

    private void init(){
        spinnerBloodGroup=findViewById(R.id.spinnerBloodGroup);
        String[] bd=getResources().getStringArray(R.array.array_bloodgroup);
        ArrayAdapter<String> adapter=new ArrayAdapter<>(this,R.layout.support_simple_spinner_dropdown_item, bd);
        spinnerBloodGroup.setAdapter(adapter);

        database = FirebaseDatabase.getInstance();
        reference = database.getReference("Donors");
        stateSpinner = findViewById(R.id.stateSpinner);
        citiesSpinner = findViewById(R.id.citiesSpinner);
        buttonFind = findViewById(R.id.buttonSubmit);
        recyclerViewDonor = findViewById(R.id.recyclerViewDonors);
        progressBar=findViewById(R.id.progressBar);
        dummtTExtView=findViewById(R.id.dummy);
        recyclerLayoutManager = new GridLayoutManager(getApplicationContext(),1,GridLayoutManager.VERTICAL,false);

        recyclerVIewInit();
        displayLoader();
    }

    private void recyclerVIewInit(){
        recyclerViewDonor.setLayoutManager(recyclerLayoutManager);

        DividerItemDecoration dividerItemDecoration =
                new DividerItemDecoration(recyclerViewDonor.getContext(),
                        recyclerLayoutManager.getOrientation());
        recyclerViewDonor.addItemDecoration(dividerItemDecoration);

        donors = new ArrayList<>();
        adapter=new DonorRecyclerView((ArrayList<DonorPC>) donors,getApplicationContext());
    }

    private void getDataFromActivity(View v){
        searchbloodGroup = spinnerBloodGroup.getSelectedItem().toString();
        State state = (State) stateSpinner.getSelectedItem();
        searchState = state.getStateName();
        searchCity = citiesSpinner.getSelectedItem().toString();
        getResult(searchbloodGroup,searchState,searchCity,v);
    }

    private void getResult(final String bloodGroup,final String state,final String city,final View v){
        DatabaseReference reference1=reference.child(state).child(city);
        reference1.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                DonorPC dn=dataSnapshot.getValue(DonorPC.class);
               // Toast.makeText(FIndDonor.this, ""+dn.getDonorBloodGroup(), Toast.LENGTH_SHORT).show();
                if(dn.getDonorBloodGroup().equals(bloodGroup)) {
                    donors.add(dataSnapshot.getValue(DonorPC.class));
                    dummtTExtView.setVisibility(View.GONE);
                }
                    recyclerViewDonor.setAdapter(adapter);
                    adapter.notifyDataSetChanged();
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                progressBar.setVisibility(View.GONE);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                progressBar.setVisibility(View.GONE);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                progressBar.setVisibility(View.GONE);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    private void displayLoader() {
        pDialog = new ProgressDialog(FIndDonor.this);
        pDialog.setMessage("Loading Data.. Please wait...");
        pDialog.setIndeterminate(false);
        pDialog.setCancelable(false);
        pDialog.show();
        loadStateCityDetails();
    }

    private void loadStateCityDetails() {
        final List<State> statesList = new ArrayList<>();
        final List<String> states = new ArrayList<>();
        JsonArrayRequest jsArrayRequest = new JsonArrayRequest
                (Request.Method.GET, cities_url, null, new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray responseArray) {
                        pDialog.dismiss();
                        try {
                            //Parse the JSON response array by iterating over it
                            for (int i = 0; i < responseArray.length(); i++) {
                                JSONObject response = responseArray.getJSONObject(i);
                                String state = response.getString(KEY_STATE);
                                JSONArray cities = response.getJSONArray(KEY_CITIES);
                                List<String> citiesList = new ArrayList<>();
                                for (int j = 0; j < cities.length(); j++) {
                                    citiesList.add(cities.getString(j));
                                }
                                statesList.add(new State(state, citiesList));
                                states.add(state);

                            }
                            final StateAdapter stateAdapter = new StateAdapter(FIndDonor.this,
                                    R.layout.state_list, R.id.spinnerText, statesList);
                            stateSpinner.setAdapter(stateAdapter);

                            stateSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                @Override
                                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                                    //Populate City list to the second spinner when
                                    // a state is chosen from the first spinner
                                    State cityDetails = stateAdapter.getItem(position);
                                    List<String> cityList = cityDetails.getCities();
                                    ArrayAdapter citiesAdapter = new ArrayAdapter<>(FIndDonor.this,
                                            R.layout.city_list, R.id.citySpinnerText, cityList);
                                    citiesSpinner.setAdapter(citiesAdapter);
                                }

                                @Override
                                public void onNothingSelected(AdapterView<?> parent) {

                                }
                            });

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        pDialog.dismiss();

                        //Display error message whenever an error occurs
                        Toast.makeText(getApplicationContext(),
                                error.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                });

        // Access the RequestQueue through your singleton class.
        MySingleton.getInstance(this).addToRequestQueue(jsArrayRequest);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 99:
                if (ActivityCompat.checkSelfPermission(this, permissions[0]) == PackageManager.PERMISSION_GRANTED) {

                }
                else{
                    Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
                }
                return;
        }
    }


    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }
}
