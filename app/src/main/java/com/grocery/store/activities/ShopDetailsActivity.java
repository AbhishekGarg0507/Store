package com.grocery.store.activities;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.grocery.store.Constants;
import com.grocery.store.R;
import com.grocery.store.adapters.AdapterCartItem;
import com.grocery.store.adapters.AdapterProductUser;
import com.grocery.store.models.ModelCart;
import com.grocery.store.models.ModelProduct;

import java.util.ArrayList;
import java.util.HashMap;

import p32929.androideasysql_library.Column;
import p32929.androideasysql_library.EasyDB;

public class ShopDetailsActivity extends AppCompatActivity {
    private ImageView shopIv;
    private ImageButton cartBt, backBt,filterProductBt,callBt,emailBt,mapBt;
    private RecyclerView productsRv;
    private EditText searchProductEt;
    private TextView emailTv, shopNameTv,phoneTv,filterProductTv, openCloseTv,addressTv,deliveryFeeTv,cartCountTv;
    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;
    private ArrayList<ModelProduct> productList;
    private AdapterProductUser adapterProductUser;
    private String shopUid;
    private String shopName,shopEmail,shopPhone,shopAddress;
    public String deliveryFee;
    private EasyDB easyDB;

    private ArrayList<ModelCart> cartItemList;
    private AdapterCartItem adapterCartItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop_details);

        emailTv = findViewById(R.id.emailTv);
        shopNameTv = findViewById(R.id.shopNameTv);
        openCloseTv = findViewById(R.id.openCloseTv);
        filterProductTv = findViewById(R.id.filterProductTv);
        phoneTv = findViewById(R.id.phoneTv);
        addressTv = findViewById(R.id.addressTv);
        deliveryFeeTv = findViewById(R.id.deliveryFeeTv);
        cartCountTv = findViewById(R.id.cartCountTv);
        cartBt = findViewById(R.id.cartBt);
        backBt = findViewById(R.id.backBt);
        callBt = findViewById(R.id.callBt);
        mapBt = findViewById(R.id.mapBt);
        shopIv = findViewById(R.id.shopIv);
        searchProductEt = findViewById(R.id.searchProductEt);
        filterProductBt = findViewById(R.id.filterProductBt);
        productsRv = findViewById(R.id.productsRv);

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please Wait...");
        progressDialog.setCanceledOnTouchOutside(false);

        shopUid = getIntent().getStringExtra("shopUid");
        firebaseAuth = firebaseAuth.getInstance();
        loadMyInfo();
        loadShopDetails();
        loadShopproducts();

        //declear it in class level and init it in onCreate
         easyDB = EasyDB.init(this,"ITEMS_DB")
                .setTableName("ITEMS_TABLE")
                .addColumn(new Column("Item_Id",new String[]{"text" , "unique"}))
                .addColumn(new Column("Item_PId",new String[]{"text" , "not null"}))
                .addColumn(new Column("Item_Name",new String[]{"text" , "not null"}))
                .addColumn(new Column("Item_Price_Each",new String[]{"text" , "not null"}))
                .addColumn(new Column("Item_Price",new String[]{"text" , "not null"}))
                .addColumn(new Column("Item_Quantity",new String[]{"text" , "not null"}))
                .doneTableColumn();


        //delete cart data , each show has its own products and orders different shops should have different cart
        deleteCartData();
        cartCount();

        searchProductEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                try {
                    adapterProductUser.getFilter().filter(charSequence);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        backBt.setOnClickListener(view -> onBackPressed());
        cartBt.setOnClickListener(view -> {
            //show cart dialog
            showCartDialog();
        });
        callBt.setOnClickListener(view -> dialPhone());
        filterProductBt.setOnClickListener(view -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(ShopDetailsActivity.this);
            builder.setTitle("Choose Category")
                    .setItems(Constants.productCategories1, (dialogInterface, i) -> {
                        // get selected item
                        String selected = Constants.productCategories1[i];
                        filterProductTv.setText(selected);
                        if(selected.equals("All")){
                            //load all
                            loadShopproducts();
                        }else{
                            //load filtered
                            adapterProductUser.getFilter().filter(selected);
                        }
                    })
                    .show();
        });


    }

    private void deleteCartData() {
        easyDB.deleteAllDataFromTable();//delete all records from the cart
    }
    public void cartCount(){
        //keepint it public for use in adapted class
        //get cart count
        int count = easyDB.getAllData().getCount();
        if (count<=0){
            //no item in cart,hide cart count icon
            cartCountTv.setVisibility(View.GONE);
        }else{
            //set count
            cartCountTv.setVisibility(View.VISIBLE);
            cartCountTv.setText(""+count);
        }
    }

    public double allTotalPrice = 0.00;
    public TextView sTotalTv,dFeeTv,allTotalPriceTv;
    private void showCartDialog() {
        //init list
        cartItemList = new ArrayList<>();
        //inflate cart layout
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_cart,null);
        //init view
        TextView shopNameTv = view.findViewById(R.id.shopNameTv);
        RecyclerView cartItemRv = view.findViewById(R.id.cartItemRv);
         sTotalTv = view.findViewById(R.id.sTotalTv);
         dFeeTv = view.findViewById(R.id.dFeeTv);
         allTotalPriceTv = view.findViewById(R.id.totalTv);
        Button checkOutBt = view.findViewById(R.id.checkOutBt);

        //dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(view);

        shopNameTv.setText(shopName);
        EasyDB easyDB = EasyDB.init(this,"ITEMS_DB")
                .setTableName("ITEMS_TABLE")
                .addColumn(new Column("Item_Id",new String[]{"text" , "unique"}))
                .addColumn(new Column("Item_PId",new String[]{"text" , "not null"}))
                .addColumn(new Column("Item_Name",new String[]{"text" , "not null"}))
                .addColumn(new Column("Item_Price_Each",new String[]{"text" , "not null"}))
                .addColumn(new Column("Item_Price",new String[]{"text" , "not null"}))
                .addColumn(new Column("Item_Quantity",new String[]{"text" , "not null"}))
                .doneTableColumn();

        //get all recordes from db
        Cursor res = easyDB.getAllData();
        while (res.moveToNext()){
            String id = res.getString(1);
            String pId = res.getString(2);
            String name = res.getString(3);
            String price = res.getString(4);
            String cost = res.getString(5);
            String quantity = res.getString(6);

            allTotalPrice = allTotalPrice + Double.parseDouble(cost);
            ModelCart modelCart = new ModelCart(""+id,
                    ""+pId,
                    ""+name,
                    ""+price,
                    ""+cost,
                    ""+quantity);

            cartItemList.add(modelCart);

        }
        //set adapter
        adapterCartItem = new AdapterCartItem(this,cartItemList);
        //set recycler view
        cartItemRv.setAdapter(adapterCartItem);
        dFeeTv.setText("Rs."+deliveryFee);
        sTotalTv.setText("Rs."+String.format("%.2f",allTotalPrice));
        double fee = Double.parseDouble(deliveryFee.replace("Rs.",""));
        double totalprice = allTotalPrice+fee;
        allTotalPriceTv.setText("Rs."+ totalprice);

        //Show dialog
        AlertDialog dialog = builder.create();
        dialog.show();

        //reset total price on dialog after dismiss
        dialog.setOnCancelListener(dialogInterface -> allTotalPrice = 0.00);

        //place order
        checkOutBt.setOnClickListener(view1 -> {
            //first validate delivery address
            if (shopAddress.equals("null")){
                Toast.makeText(this, "Please enter your address in your profile before Placing order....", Toast.LENGTH_SHORT).show();
                return;
            }
            if (shopPhone.equals("null")){
                Toast.makeText(this, "Please enter your Phone Number in your profile before Placing order....", Toast.LENGTH_SHORT).show();
                return;
            }
            if (cartItemList.size() == 0){
                //cart list is empty
                Toast.makeText(this, "No item is cart", Toast.LENGTH_SHORT).show();
            }

            submitOrder();
        });
    }

    private void submitOrder() {
        //progress dialog
        progressDialog.setMessage("Placing Order...");
        progressDialog.show();

        //for order id and order time
        String timeStamp = ""+System.currentTimeMillis();

        String cost = allTotalPriceTv.getText().toString().trim().replace("Rs.","");


        HashMap<String,String> hashMap = new HashMap<>();
        hashMap.put("orderId",""+timeStamp);
        hashMap.put("orderTime",""+timeStamp);
        hashMap.put("orderStatus","InProgress");  // in progress / completed / canceled
        hashMap.put("orderCost",""+cost);
        hashMap.put("orderBy",""+firebaseAuth.getUid());
        hashMap.put("orderTo",""+shopUid);
        hashMap.put("address",""+shopAddress);
        hashMap.put("deliveryFee",""+deliveryFee);

        //add to db
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users").child(shopUid).child("Orders");
        ref.child(timeStamp).setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        //order info added now add order items
                        for (int i=0;i<cartItemList.size();i++){
                            String pId = cartItemList.get(i).getpId();
                            String id = cartItemList.get(i).getId();
                            String cost = cartItemList.get(i).getCost();
                            String name = cartItemList.get(i).getName();
                            String price = cartItemList.get(i).getPrice();
                            String quantity = cartItemList.get(i).getQuantity();

                            HashMap<String ,String> hashMap1 = new HashMap<>();
                            hashMap1.put("pId",pId);
                            hashMap1.put("name",name);
                            hashMap1.put("cost",cost);
                            hashMap1.put("price",price);
                            hashMap1.put("quantity",quantity);

                            ref.child(timeStamp).child("Items").child(pId).setValue(hashMap1);

                        }
                        progressDialog.dismiss();
                        Toast.makeText(ShopDetailsActivity.this, "Order Placed Successfully..", Toast.LENGTH_SHORT).show();

                        //after placing order open order details page
                        Intent intent = new Intent(ShopDetailsActivity.this, OrderDetailsUserActivity.class);
                        intent.putExtra("orderTo",shopUid);
                        intent.putExtra("orderId",timeStamp);
                        startActivity(intent);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(ShopDetailsActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void dialPhone() {
        startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:"+Uri.decode(shopPhone))));
        Toast.makeText(this, ""+shopPhone, Toast.LENGTH_SHORT).show();
    }

    private void loadMyInfo() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.orderByChild("uid").equalTo(firebaseAuth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds : snapshot.getChildren()){
                            String name = ""+ds.child("name").getValue();
                            String accountType = ""+ds.child("accountType").getValue();
                            String email = ""+ds.child("email").getValue();
                            String phone = ""+ds.child("phone").getValue();
                            String city = ""+ds.child("city").getValue();

                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }
    private void loadShopproducts() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(shopUid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //get shop data
                String name = ""+snapshot.child("name").getValue();
                shopName = ""+snapshot.child("shopName").getValue();
                shopEmail = ""+snapshot.child("email").getValue();
                shopPhone = ""+snapshot.child("phone").getValue();
                shopAddress = ""+snapshot.child("address").getValue();
                deliveryFee = ""+snapshot.child("deliveryFee").getValue();
                String shopOpen = ""+snapshot.child("shopOpen").getValue();
//                String profileImage = ""+snapshot.child("profileImage").getValue();

                //set Data
                shopNameTv.setText(shopName);
                emailTv.setText(shopEmail);
                phoneTv.setText(shopPhone);
                addressTv.setText(shopAddress);
                deliveryFeeTv.setText("Delivery Fee: Rs. "+deliveryFee);
                if (shopOpen.equals("true")){
                    openCloseTv.setText("Open");
                }else {
                    openCloseTv.setText("Closed");
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void loadShopDetails() {
        productList = new ArrayList<>();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(shopUid).child("products")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //clear list before adding items
                        for (DataSnapshot ds : snapshot.getChildren()){
                            ModelProduct modelProduct = ds.getValue(ModelProduct.class);
                            productList.add(modelProduct);
                        }
                        adapterProductUser = new AdapterProductUser(ShopDetailsActivity.this,productList);
                        productsRv.setAdapter(adapterProductUser);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }


}