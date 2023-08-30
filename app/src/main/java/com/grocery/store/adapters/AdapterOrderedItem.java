package com.grocery.store.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.grocery.store.R;
import com.grocery.store.models.ModelCart;
import com.grocery.store.models.ModelOrderedItem;

import java.util.ArrayList;

public class AdapterOrderedItem extends RecyclerView.Adapter<AdapterOrderedItem.HolderOrderedItem> {
    private Context context;
    private ArrayList<ModelOrderedItem> orderedItemList;

    public AdapterOrderedItem(Context context, ArrayList<ModelOrderedItem> orderedItemList) {
        this.context = context;
        this.orderedItemList = orderedItemList;
    }

    @NonNull
    @Override
    public HolderOrderedItem onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_ordereditem,parent,false);
        return new HolderOrderedItem(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderOrderedItem holder, int position) {

        //getting data at position
        ModelOrderedItem modelOrderedItem = orderedItemList.get(position);
        String getpId = modelOrderedItem.getpId();
        String name = modelOrderedItem.getName();
        String cost = modelOrderedItem.getCost();
        String price = modelOrderedItem.getPrice();
        String quantity = modelOrderedItem.getQuantity();

        //setting data
        holder.itemTitleTv.setText(name);
        holder.itemPriceTv.setText(""+cost);
        holder.itemPriceEachTv.setText(""+price);
        holder.itemQuantityTv.setText("["+quantity+"]");

    }

    @Override
    public int getItemCount() {
        return orderedItemList.size();
    }


    class HolderOrderedItem extends RecyclerView.ViewHolder{
        //viewa of row_orderItem
         private TextView itemTitleTv,itemPriceTv,itemPriceEachTv,itemQuantityTv;

        public HolderOrderedItem(@NonNull View itemView) {
            super(itemView);
            itemTitleTv = itemView.findViewById(R.id.itemTitleTv);
            itemPriceTv = itemView.findViewById(R.id.itemPriceTv);
            itemPriceEachTv = itemView.findViewById(R.id.itemPriceEachTv);
            itemQuantityTv = itemView.findViewById(R.id.itemQuantityTv);
        }
    }
}
