package com.grocery.store.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.grocery.store.R;
import com.grocery.store.activities.EditProductActivity;
import com.grocery.store.activities.ShopDetailsActivity;
import com.grocery.store.models.ModelShop;

import java.util.ArrayList;

public class AdapterShop extends RecyclerView.Adapter<AdapterShop.HolderShop>{
    private Context context;
    public ArrayList<ModelShop> shopList;

    public AdapterShop(Context context, ArrayList<ModelShop> shopList){
        this.context= context;
        this.shopList = shopList;
    }

    @NonNull
    @Override
    public AdapterShop.HolderShop onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // inflate layout rowshop
        View view = LayoutInflater.from(context).inflate(R.layout.row_shop, parent,false);
        return new HolderShop(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdapterShop.HolderShop holder, int position) {
        //get data
        ModelShop modelShop  = shopList.get(position);
        String accountType = modelShop.getAccountType();
        String address = modelShop.getAddress();
        String city = modelShop.getCity();
        String state = modelShop.getState();
        String deliveryFee = modelShop.getDeliveryFee();
        String email = modelShop.getEmail();
        String online = modelShop.getOnline();
        String name = modelShop.getName();
        String phone = modelShop.getPhone();
        String uid = modelShop.getUid();
        String timestamp = modelShop.getTimestamp();
        String shopOpen = modelShop.getShopOpen();
        String shopName = modelShop.getShopName();


        //set data
        holder.shopNameTv.setText(shopName);
        holder.phoneTv.setText(phone);
        holder.addressTv.setText(address);
        // check for online owner
        if (online.equals("true")){
            holder.onlineIv.setVisibility(View.VISIBLE);
        }else {
            holder.onlineIv.setVisibility(View.GONE);
        }
        // check for open shop
        if (shopOpen.equals("true")){
            holder.shopClosedTv.setVisibility(View.GONE);
        }else {
            holder.shopClosedTv.setVisibility(View.VISIBLE);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, ShopDetailsActivity.class);
                intent.putExtra("shopUid",uid);
                context.startActivity(intent);
            }
        });

    }

    @Override
    public int getItemCount() {
        return shopList.size();
    }

    //view holder
    class HolderShop extends RecyclerView.ViewHolder{

        //Ui view of row_shop
        private ImageView onlineIv;
        private TextView shopClosedTv,shopNameTv,phoneTv,addressTv;
        private RatingBar ratingBar;

        public HolderShop(@NonNull View itemView) {
            super(itemView);

            //init ui view
            onlineIv = itemView.findViewById(R.id.onlineIv);
            shopClosedTv = itemView.findViewById(R.id.shopClosedTv);
            shopNameTv = itemView.findViewById(R.id.shopNameTv);
            phoneTv = itemView.findViewById(R.id.phoneTv);
            addressTv = itemView.findViewById(R.id.addressTv);
            ratingBar = itemView.findViewById(R.id.ratingBar);
        }
    }
}
