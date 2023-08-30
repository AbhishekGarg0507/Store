package com.grocery.store.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.grocery.store.R;
import com.grocery.store.activities.ShopDetailsActivity;
import com.grocery.store.models.ModelCart;

import java.util.ArrayList;

import p32929.androideasysql_library.Column;
import p32929.androideasysql_library.EasyDB;

public class AdapterCartItem extends RecyclerView.Adapter<AdapterCartItem.HolderCartItem> {

    private Context context;
    private ArrayList<ModelCart> cartItems;

    public AdapterCartItem(Context context, ArrayList<ModelCart> cartItems) {
        this.context = context;
        this.cartItems = cartItems;
    }

    @NonNull
    @Override
    public HolderCartItem onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_cartitem,parent,false);
        return new HolderCartItem(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderCartItem holder, @SuppressLint("RecyclerView") int position) {
        ModelCart modelCart = cartItems.get(position);
        //getting data
        String id = modelCart.getId();
        String getpId = modelCart.getpId();
        String cost = modelCart.getCost();
        String title = modelCart.getName();
        String price = modelCart.getPrice();
        String quantity = modelCart.getQuantity();
        //setting data
        holder.itemTitleTv.setText(title);
        holder.itemPriceTv.setText(""+cost);
        holder.itemQuantityTv.setText("["+quantity+"]");
        holder.itemPriceEachTv.setText(""+price);

        //handle remove click listener, to remove item from the cart
        holder.itemRemoveTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //will craete table for if not exists, but in that case will must exist
                EasyDB easyDB = EasyDB.init(context,"ITEMS_DB")
                        .setTableName("ITEMS_TABLE")
                        .addColumn(new Column("Item_Id",new String[]{"text" , "unique"}))
                        .addColumn(new Column("Item_PId",new String[]{"text" , "not null"}))
                        .addColumn(new Column("Item_Name",new String[]{"text" , "not null"}))
                        .addColumn(new Column("Item_Price_Each",new String[]{"text" , "not null"}))
                        .addColumn(new Column("Item_Price",new String[]{"text" , "not null"}))
                        .addColumn(new Column("Item_Quantity",new String[]{"text" , "not null"}))
                        .doneTableColumn();

                easyDB.deleteRow(1,id);
                Toast.makeText(context, "Item Removed..", Toast.LENGTH_SHORT).show();

                //refresh list
                cartItems.remove(position);
                notifyItemChanged(position);
                notifyDataSetChanged();

                double tx = Double.parseDouble((((ShopDetailsActivity)context).allTotalPriceTv.getText().toString().trim().replace("Rs.","")));
                double totalPrice = tx-Double.parseDouble(cost.replace("Rs.",""));
                double deliveryFee = Double.parseDouble((((ShopDetailsActivity)context).deliveryFee.replace("Rs.","")));
                double sTotalPrice = Double.parseDouble(String.format("%.2f",totalPrice)) - Double.parseDouble(String.format("%.2f",deliveryFee));
                ((ShopDetailsActivity)context).allTotalPrice = 0.00;
                ((ShopDetailsActivity)context).sTotalTv.setText("Rs."+String.format("%.2f",sTotalPrice));
                ((ShopDetailsActivity)context).allTotalPriceTv.setText("Rs."+String.format("%.2f",Double.parseDouble(String.format("%.2f",totalPrice))));

                //after removing item from cart update cart
                ((ShopDetailsActivity)context).cartCount();
            }
        });

    }

    @Override
    public int getItemCount() {
        return cartItems.size(); //returns number of recordes
    }

    class HolderCartItem extends RecyclerView.ViewHolder{

        //view of row cart item
        private TextView itemTitleTv,itemPriceTv,itemPriceEachTv,itemQuantityTv,itemRemoveTv;
        public HolderCartItem(@NonNull View itemView) {
            super(itemView);
            itemTitleTv = itemView.findViewById(R.id.itemTitleTv);
            itemPriceTv = itemView.findViewById(R.id.itemPriceTv);
            itemPriceEachTv = itemView.findViewById(R.id.itemPriceEachTv);
            itemQuantityTv = itemView.findViewById(R.id.itemQuantityTv);
            itemRemoveTv = itemView.findViewById(R.id.itemRemoveTv);
        }
    }
}
