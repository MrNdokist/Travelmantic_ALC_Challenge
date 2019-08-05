package com.mrndokist.app.travelmantics;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by Acer on 04/08/2019.
 */

public class Adapter extends RecyclerView.Adapter<Adapter.DealViewHolder> {


    ArrayList<TravelDeal> deals = new ArrayList<>();;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mDatabaseReference;
    private ChildEventListener mChildEventListener;
    private Context mContext;
    private ImageView img_item;

    public Adapter(Context context) {

        this.mContext = context;
        deals.clear();
      //  FirebaseUtil.openFbReference("traveldeals",context);
        mFirebaseDatabase  = FirebaseUtil.firebaseDatabase;
        mDatabaseReference = FirebaseUtil.databaseReference;

        mChildEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                TravelDeal td = dataSnapshot.getValue(TravelDeal.class);
                Log.d("Deal", td.getTitle());
                td.setId(dataSnapshot.getKey());
                deals.add(td);
                notifyItemInserted(deals.size()-1);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        mDatabaseReference.addChildEventListener(mChildEventListener);
    }

    @NonNull
    @Override
    public DealViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View itemView = LayoutInflater.from(mContext).inflate(R.layout.deal_item_layout, parent, false);
        return new DealViewHolder(itemView);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public void onBindViewHolder(@NonNull DealViewHolder holder, int position) {

        TravelDeal deal = deals.get(position);
        holder.bind(deal);
    }

    @Override
    public int getItemCount() {
        return deals.size();
    }

    public class DealViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView tvTitle,tv_desc,tv_price;

        public DealViewHolder(View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_title_item);
            tv_desc = itemView.findViewById(R.id.tv_description_item);
            tv_price = itemView.findViewById(R.id.tv_price_item);
            img_item = itemView.findViewById(R.id.imgDeal);

            itemView.setOnClickListener(this);
        }

        public void bind(TravelDeal deal)
        {
            tvTitle.setText(deal.getTitle());
            tv_desc.setText(deal.getDescription());
            tv_price.setText(deal.getPrice());
            showImage(deal.getImageUrl());
        }

        //Chargement image
        private void showImage(String url)
        {
            if (url !=null && url.isEmpty() == false)
            {
                int width = Resources.getSystem().getDisplayMetrics().widthPixels;
                Picasso.with(img_item.getContext()).load(url).resize(60,60).centerCrop().into(img_item);
            }
        }


        @Override
        public void onClick(View view) {
            int position = getAdapterPosition();
            Log.d("Click",String.valueOf(position));

            TravelDeal selectedDeal = deals.get(position);
            Intent intent = new Intent(view.getContext(),MainActivity.class);
            intent.putExtra("Deal",selectedDeal);
            view.getContext().startActivity(intent);



        }
    }
}
