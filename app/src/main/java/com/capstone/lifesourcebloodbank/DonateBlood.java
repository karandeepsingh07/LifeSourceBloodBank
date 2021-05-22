package com.capstone.lifesourcebloodbank;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.FocusFinder;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.capstone.lifesourcebloodbank.Spinner.MySingleton;
import com.capstone.lifesourcebloodbank.Spinner.State;
import com.capstone.lifesourcebloodbank.Spinner.StateAdapter;
import com.google.android.gms.common.util.NumberUtils;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class DonateBlood extends AppCompatActivity {

    private static final String KEY_STATE = "state";
    private static final String KEY_CITIES = "cities";
    private EditText editTextBloodGroup,editTextAddress,editTextPhoneNumber,editTextDonorName,editTextDonorAge,editTextPincode;
    Spinner stateSpinner;
    Spinner citiesSpinner;
    Spinner spinnerBloodGroup;
    private ProgressDialog pDialog;
    private Button submitButton;
    private String cities_url = "http://api.androiddeft.com/cities/cities_array.json";
    private FirebaseDatabase database;
    private DatabaseReference reference;
    private FirebaseAuth firebaseAuth;
    private String user,donorUserName,donorName,uid;
    private String donorBloodGroup,donorAdress,donorState,donorCity,donorPhoneNumber,donorPincode,imageUrl;
    private String donorAge;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_donate_blood);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        init();



        //Display state and city name when submit button is pressed
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDataFromActivity();
                if(TextUtils.isEmpty(donorName)){
                    editTextDonorName.setError("Enter Name");
                    return;
                }
                if(spinnerBloodGroup.getSelectedItemPosition()==0){
                    Snackbar.make(v,"Select Blood Group",Snackbar.LENGTH_LONG).show();
                    return;
                }

                if(TextUtils.isEmpty(donorAge)){
                    editTextDonorAge.setError("Enter Age");
                    return;
                }
                if(TextUtils.isEmpty(donorAdress)){
                    editTextAddress.setError("Enter Address");
                    return;
                }
                if(TextUtils.isEmpty(donorPincode)){
                    editTextPincode.setError("Enter Pin Code");
                    return;
                }
                if(TextUtils.isEmpty(donorPhoneNumber)){
                    editTextPhoneNumber.setError("Enter Phone Number");
                    return;
                }
                if(donorPincode.length()<6){
                    editTextPincode.setError("Incorrect PinCode");
                    return;
                }
                if(Integer.parseInt(donorAge)<18){
                    editTextDonorAge.setError("As your age is below 18, So you are not allowed to donate");
                    return;
                }
                if(Integer.parseInt(donorAge)>61){
                    editTextDonorAge.setError("As your age is above 60, So you are not allowed to donate");
                    return;
                }
                if(donorPhoneNumber.length()!=10){
                    editTextPhoneNumber.setError("Invalid Phone Number");
                    return;
                }

                addData();
                startActivity(new Intent(DonateBlood.this,ThankYouActivity.class));
            }
        });
    }

    private void init(){
        spinnerBloodGroup=findViewById(R.id.spinnerBloodGroup);
        final String[] bloodGroups=getResources().getStringArray(R.array.array_bloodgroup);
        ArrayAdapter<String> adapter=new ArrayAdapter<>(this,R.layout.support_simple_spinner_dropdown_item, bloodGroups);
        spinnerBloodGroup.setAdapter(adapter);

        stateSpinner = findViewById(R.id.stateSpinner);
        citiesSpinner = findViewById(R.id.citiesSpinner);
        editTextAddress=findViewById(R.id.editTextAddress);
        editTextPhoneNumber=findViewById(R.id.editTextPhoneNumber);
        editTextDonorName = findViewById(R.id.editTextDonorName);
        editTextDonorAge=findViewById(R.id.editTextDonorAge);
        editTextPincode=findViewById(R.id.editTextPinCode);
        submitButton = findViewById(R.id.buttonSubmit);
        database=FirebaseDatabase.getInstance();
        reference=database.getReference("Donors");
        firebaseAuth=FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        user=firebaseUser.getEmail();
        uid = firebaseUser.getUid();


        database.getReference().child("Users").child(uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                editTextDonorName.setText(dataSnapshot.child("name").getValue(String.class));
                String bdTemp=dataSnapshot.child("bloodGroup").getValue(String.class);
                Toast.makeText(DonateBlood.this, ""+bdTemp, Toast.LENGTH_SHORT).show();
                imageUrl=dataSnapshot.child("imageUrl").getValue(String.class);
                for(int i=0;i<9;i++){
                    if(bloodGroups[i].equals(bdTemp)) {
                        spinnerBloodGroup.setSelection(i);
                        break;
                    }
                }
                displayLoader();
                loadStateCityDetails();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getDataFromActivity(){
        donorBloodGroup=spinnerBloodGroup.getSelectedItem().toString();
        donorAdress=editTextAddress.getText().toString();
        State state= (State) stateSpinner.getSelectedItem();
        donorState=state.getStateName();
        donorCity=citiesSpinner.getSelectedItem().toString();
        donorPhoneNumber=editTextPhoneNumber.getText().toString();
        donorName=editTextDonorName.getText().toString();
        donorPincode=editTextPincode.getText().toString();
        donorUserName=user;
        donorAge=editTextDonorAge.getText().toString();
    }
    private void addData(){
        DatabaseReference reference1 = reference.child(donorState).child(donorCity).child(uid);
        DonorPC donor=new DonorPC();
        donor.setDonorUserName(donorUserName);
        donor.setDonorBloodGroup(donorBloodGroup);
        donor.setDonorAddress(donorAdress);
        donor.setDonorPhoneNumber("+91"+donorPhoneNumber);
        donor.setDonorUserName(donorUserName);
        donor.setDonorName(donorName);
        donor.setDonorAge(donorAge);
        donor.setDonorImage(imageUrl);
        donor.setDonorPincode(donorPincode);
        //donor.setDonorState(donorState);
        //donor.setDonorCity(donorCity);
        reference1.setValue(donor);

        DatabaseReference reference2 = database.getReference().child("Users").child(uid);
        UserPC userPC=new UserPC();
        reference2.child("state").setValue(donorState);
        reference2.child("city").setValue(donorCity);
        reference2.child("donated").setValue(true);
    }



    private void displayLoader() {
        pDialog = new ProgressDialog(DonateBlood.this);
        pDialog.setMessage("Loading Data.. Please wait...");
        pDialog.setIndeterminate(false);
        pDialog.setCancelable(false);
        pDialog.show();

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
                            final StateAdapter stateAdapter = new StateAdapter(DonateBlood.this,
                                    R.layout.state_list, R.id.spinnerText, statesList);
                            stateSpinner.setAdapter(stateAdapter);

                            stateSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                @Override
                                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                                    //Populate City list to the second spinner when
                                    // a state is chosen from the first spinner
                                    State cityDetails = stateAdapter.getItem(position);
                                    List<String> cityList = cityDetails.getCities();
                                    ArrayAdapter citiesAdapter = new ArrayAdapter<>(DonateBlood.this,
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
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }
}
