package com.grocery.store.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.grocery.store.FilterProductUser;
import com.grocery.store.R;
import com.grocery.store.activities.ShopDetailsActivity;
import com.grocery.store.models.ModelProduct;

import java.util.ArrayList;

import p32929.androideasysql_library.Column;
import p32929.androideasysql_library.EasyDB;

public class AdapterProductUser extends RecyclerView.Adapter<AdapterProductUser.HolderProductUser> implements Filterable {

    private Context context;
    public ArrayList<ModelProduct> productListUser,filterList;
    private FilterProductUser filter;

    public AdapterProductUser(Context context, ArrayList<ModelProduct> productListUser) {
        this.context = context;
        this.productListUser = productListUser;
        this.filterList = productListUser;
    }

    @NonNull
    @Override
    public HolderProductUser onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.product_user,parent,false);
        return new HolderProductUser(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderProductUser holder, int position) {
        ModelProduct modelProduct = productListUser.get(position);
        String discountAvailable = modelProduct.getDiscountAvailable();
        String discountNote = modelProduct.getDiscountNote();
        String discountPrice = modelProduct.getDiscountPrice();
        String productCategory = modelProduct.getProductCategory();
        String originalPrice = modelProduct.getOriginalPrice();
        String productDescription = modelProduct.getProductDescription();
        String productTitle = modelProduct.getProductTitle();
        String productQuantity = modelProduct.getProductQuantity();
        String productId = modelProduct.getProductId();
        String timeStamp = modelProduct.getTimeStamp();
//        String productIcon = modelProduct.getProductIcon();

        //setting data
        holder.titleTv.setText(productTitle);
        holder.discountNoteTv.setText(discountNote);
        holder.descriptionTv.setText(productDescription);
        holder.originalPriceTv.setText("Rs."+originalPrice);
        holder.discountedPriceTv.setText("Rs."+discountPrice);
        if(discountAvailable.equals("true")){
            //product is on discount
            holder.discountedPriceTv.setVisibility(View.VISIBLE);
            holder.discountNoteTv.setVisibility(View.VISIBLE);
            holder.originalPriceTv.setPaintFlags(holder.originalPriceTv.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        }else{
            //product is not on discount
            holder.discountedPriceTv.setVisibility(View.GONE);
            holder.discountNoteTv.setVisibility(View.GONE);
            holder.originalPriceTv.setPaintFlags(0);
        }

        holder.addToCartTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //add to cart
                showQuantityDialog(modelProduct);
            }
        });
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            }
        });
    }
    private double cost = 0.00;
    private double finalCost = 0.00;
    private int quantity = 0;
    private void showQuantityDialog(ModelProduct modelProduct) {
        //inflate layout for dialog
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_quantity,null);
        //init layout view
        TextView titleTv = view.findViewById(R.id.titleTv);
        TextView pQuantityTv = view.findViewById(R.id.pQuantityTv);
        TextView descriptionTv = view.findViewById(R.id.descriptionTv);
        TextView discountNoteTv = view.findViewById(R.id.discountNoteTv);
        TextView originalPriceTv = view.findViewById(R.id.originalPriceTv);
        TextView priceDiscountedTv = view.findViewById(R.id.priceDiscountedTv);
        TextView finalTv = view.findViewById(R.id.finalTv);
        TextView quantityTv = view.findViewById(R.id.quantityTv);
        ImageButton decrementBt = view.findViewById(R.id.decrementBt);
        ImageButton incrementBt = view.findViewById(R.id.incrementBt);
        Button continueBt = view.findViewById(R.id.continueBt);

        //get data from model
        String productId = modelProduct.getProductId();
        String description = modelProduct.getProductDescription();
        String title = modelProduct.getProductTitle();
        String productQuantity = modelProduct.getProductQuantity();
        String discountNote = modelProduct.getDiscountNote();

        String price;
        if (modelProduct.getDiscountAvailable().equals("true")){
            price = modelProduct.getDiscountPrice();
            discountNoteTv.setVisibility(View.VISIBLE);
            originalPriceTv.setPaintFlags(originalPriceTv.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);

        }else{
            discountNoteTv.setVisibility(View.GONE);
            priceDiscountedTv.setVisibility(View.GONE);
            price = modelProduct.getOriginalPrice();
        }

        cost = Double.parseDouble(price.replaceAll("Rs.",""));
        finalCost = Double.parseDouble(price.replaceAll("Rs." , ""));
        quantity = 1;

        //dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(view);

        titleTv.setText(""+title);
        pQuantityTv.setText(""+productQuantity);
        descriptionTv.setText(""+description);
        discountNoteTv.setText(""+discountNote);
        quantityTv.setText(""+quantity);
        originalPriceTv.setText("Rs. "+modelProduct.getOriginalPrice());
        priceDiscountedTv.setText("Rs. "+modelProduct.getDiscountPrice());
        finalTv.setText("Rs. "+finalCost);

        AlertDialog dialog = builder.create();
        dialog.show();
        //increment quantity of the product
        incrementBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finalCost = finalCost+cost;
                quantity++;
                finalTv.setText("Rs. "+finalCost);
                quantityTv.setText(""+quantity);
            }
        });
        //decrement quantity
        decrementBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (quantity>1){
                    finalCost = finalCost-cost;
                    quantity--;
                    finalTv.setText("Rs. "+finalCost);
                    quantityTv.setText(""+quantity);
                }
            }
        });
        continueBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String title = titleTv.getText().toString().trim();
                String priceEach = price;
                String totalprice = finalTv.getText().toString().trim().replace("Rs.","");
                String quantity = quantityTv.getText().toString().trim();

                addToCart(productId,title,priceEach,totalprice,quantity);

                dialog.dismiss();
            }
        });

    }

    private int itemId = 1;
    private void addToCart(String productId, String title, String priceEach, String price, String quantity) {
        itemId = itemId+1;
        EasyDB easyDB = EasyDB.init(context,"ITEMS_DB")
                .setTableName("ITEMS_TABLE")
                .addColumn(new Column("Item_Id",new String[]{"text" , "unique"}))
                .addColumn(new Column("Item_PId",new String[]{"text" , "not null"}))
                .addColumn(new Column("Item_Name",new String[]{"text" , "not null"}))
                .addColumn(new Column("Item_Price_Each",new String[]{"text" , "not null"}))
                .addColumn(new Column("Item_Price",new String[]{"text" , "not null"}))
                .addColumn(new Column("Item_Quantity",new String[]{"text" , "not null"}))
                .doneTableColumn();
        Boolean b = easyDB.addData("Item_Id",itemId)
                .addData("Item_PId",productId)
                .addData("Item_Name",title)
                .addData("Item_Price_Each",priceEach)
                .addData("Item_Price",price)
                .addData("Item_Quantity",quantity)
                .doneDataAdding();

        Toast.makeText(context, "Adding to cart....", Toast.LENGTH_SHORT).show();

        // update cart count
        ((ShopDetailsActivity)context).cartCount();
    }

    @Override
    public int getItemCount() {
        return productListUser.size();
    }

    @Override
    public Filter getFilter() {
        if (filter == null){
            filter = new FilterProductUser(this,filterList);
        }
        return filter;
    }

    class HolderProductUser extends RecyclerView.ViewHolder{

            private TextView discountNoteTv,titleTv,descriptionTv,addToCartTv,discountedPriceTv,originalPriceTv;
            public HolderProductUser(@NonNull View itemView) {
                super(itemView);
                titleTv = itemView.findViewById(R.id.titleTv);
                discountNoteTv = itemView.findViewById(R.id.discountNoteTv);
                descriptionTv = itemView.findViewById(R.id.descriptionTv);
                addToCartTv = itemView.findViewById(R.id.addToCartTv);
                originalPriceTv = itemView.findViewById(R.id.originalPriceTv);
                discountedPriceTv = itemView.findViewById(R.id.discountedPriceTv);
            }
        }
}
