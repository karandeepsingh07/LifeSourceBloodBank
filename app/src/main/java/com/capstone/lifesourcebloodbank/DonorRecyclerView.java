package com.capstone.lifesourcebloodbank;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.RecyclerView;

public class DonorRecyclerView extends RecyclerView.Adapter<DonorRecyclerView.PostViewHolder> {
    private ArrayList<DonorPC> donors;
    private Context context;

    public DonorRecyclerView(ArrayList<DonorPC> donors, Context context) {
        this.donors = donors;
        this.context = context;
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater=LayoutInflater.from(context);
        View view=inflater.inflate(R.layout.donor_card,parent,false);
        return new PostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        final DonorPC donorPC = donors.get(position);
        holder.textViewUserName.setText(donorPC.getDonorName());
        holder.textViewBloodGroup.setText(donorPC.getDonorBloodGroup());
        holder.textViewUseDetail.setText(donorPC.getDonorAddress());
        Picasso.get().load(donorPC.getDonorImage()).into(holder.imageView);

        holder.buttonDonorContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    Intent callIntent = new Intent(Intent.ACTION_CALL);
                    callIntent.setData(Uri.parse("tel:"+donorPC.getDonorPhoneNumber()));
                    context.startActivity(callIntent);
                } catch (ActivityNotFoundException activityException) {
                    Log.e("Calling a Phone Number", "Call failed", activityException);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return donors.size();
    }

    public class PostViewHolder extends  RecyclerView.ViewHolder{

        public TextView textViewUserName,textViewUseDetail,textViewBloodGroup;
        public Button buttonDonorContact;
        public ImageView imageView;
        public PostViewHolder(@NonNull View itemView) {
            super(itemView);

            textViewUserName = itemView.findViewById(R.id.textUserName);
            textViewUseDetail = itemView.findViewById(R.id.textUserDetail);
            textViewBloodGroup=itemView.findViewById(R.id.textUserBloodGroup);
            buttonDonorContact = itemView.findViewById(R.id.buttonContact);
            imageView = itemView.findViewById(R.id.userImage);
        }
    }
}
