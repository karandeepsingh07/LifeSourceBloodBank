package com.capstone.lifesourcebloodbank;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import de.hdodenhof.circleimageview.CircleImageView;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class HomeActivity extends AppCompatActivity {

    CardView btnDonateBlood,btnGetBlood,btnBloodFact,btnAboutUs;
    FirebaseAuth firebaseAuth;
    FirebaseDatabase database;
    DatabaseReference reference;
    TextView textName,textBloodGroup,textGender;
    CircleImageView imageViewUser;
    String state,city,uid;
    ProgressBar progressBarHome;
    LinearLayout linearLayoutHome;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        init();
        userDetail(uid);
    }
    public void init(){
        progressBarHome=findViewById(R.id.progressBarHome);
        linearLayoutHome=findViewById(R.id.linearLayoutHome);
        btnGetBlood=findViewById(R.id.btnGetBlood);
        btnDonateBlood=findViewById(R.id.btnDonateBlood);
        btnBloodFact=findViewById(R.id.btnBloodFact);
        btnAboutUs=findViewById(R.id.btnAboutUS);
        textName=findViewById(R.id.textName);
        textBloodGroup=findViewById(R.id.textBloodGroup);
        textGender=findViewById(R.id.textGender);
        imageViewUser=findViewById(R.id.imageViewUser);
        firebaseAuth=FirebaseAuth.getInstance();
        database=FirebaseDatabase.getInstance();
        reference=database.getReference();
        FirebaseUser user = firebaseAuth.getCurrentUser();
        uid = user.getUid();

        btnGetBlood.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(HomeActivity.this,FIndDonor.class);
                startActivity(intent);
            }
        });
        btnDonateBlood.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final boolean[] check = new boolean[1];
                database.getReference().child("Users").child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        check[0] =dataSnapshot.getValue(UserPC.class).isDonated();

                        if(!check[0]) {
                            Intent intent = new Intent(HomeActivity.this, DonateBlood.class);
                            startActivity(intent);
                        }
                        else
                            Toast.makeText(HomeActivity.this, "Already Donated", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        });

        btnBloodFact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(HomeActivity.this,FactActivity.class);
                startActivity(intent);
            }
        });
        btnAboutUs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(HomeActivity.this,AboutActivity.class);
                startActivity(intent);
            }
        });
    }

    public void userDetail(String uid){
        reference.child("Users").child(uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String name = dataSnapshot.child("name").getValue(String.class);
                String bloodGroup = dataSnapshot.child("bloodGroup").getValue(String.class);
                String gender = dataSnapshot.child("gender").getValue(String.class);
                String imageUrl= dataSnapshot.child("imageUrl").getValue(String.class);
                textName.setText(name);
                textBloodGroup.setText(bloodGroup);
                textGender.setText(gender);
                Picasso.get().load(imageUrl).into(imageViewUser);
                progressBarHome.setVisibility(View.GONE);
                linearLayoutHome.setVisibility(View.VISIBLE);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.home_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id=item.getItemId();
        switch (id){
            case R.id.btnLogout:
                firebaseAuth.signOut();
                Intent intent=new Intent(this,LoginActivity.class);
                startActivity(intent);
                finish();
                break;
            case R.id.btnRefer:
                try {
                    Intent i = new Intent(Intent.ACTION_SEND);
                    i.setType("text/plain");
                    i.putExtra(Intent.EXTRA_SUBJECT, "Roll Ball Live Score");
                    String sAux = "\nLet me recommend you this blood donation application\n\n";
                    sAux = sAux + "applink \n\n";
                    i.putExtra(Intent.EXTRA_TEXT, sAux);
                    startActivity(Intent.createChooser(i, "choose one"));
                } catch(Exception e) {
                    //e.toString();
                }
                break;
            case R.id.btnDelBloodDon:
                final boolean[] check = new boolean[1];
                reference.child("Users").child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        UserPC userPC=dataSnapshot.getValue(UserPC.class);
                        state=userPC.getState();
                        city=userPC.getCity();
                        check[0]=userPC.isDonated();
                        Toast.makeText(HomeActivity.this, "Bool Value :"+check[0]+state+city, Toast.LENGTH_SHORT).show();
                        if(check[0]) {

                            reference.child("Donors").child(state).child(city).child(uid).removeValue();
                            reference.child("Users").child(uid).child("donated").setValue(false);
                            Toast.makeText(HomeActivity.this, "Appeal Deleted", Toast.LENGTH_SHORT).show();
                        }
                        else
                            Toast.makeText(HomeActivity.this, "Not Donated Yet", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
        }
        return super.onOptionsItemSelected(item);
    }
}
