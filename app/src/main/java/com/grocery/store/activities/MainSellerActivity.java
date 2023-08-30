package com.grocery.store.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.grocery.store.adapters.AdapterOrderShop;
import com.grocery.store.adapters.AdapterProductSeller;
import com.grocery.store.Constants;
import com.grocery.store.models.ModelOrderShop;
import com.grocery.store.models.ModelProduct;
import com.grocery.store.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public class MainSellerActivity extends AppCompatActivity {

    private ImageButton logoutBt, editProfileBt, shoppingBt,filterProductBt,filterOrdersBt,reviewsBt;
    private RecyclerView productsRv,ordersRv;
    private EditText searchProductEt;
    private TextView nameTv, emailTv, shopNameTv,filteredOrdersTv, categoryItemTv,tabProductsTv,filterProductTv,tabOrdersTv;
    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;
    private RelativeLayout productsRl,ordersRl;
    private ArrayList<ModelProduct> productList;
    private AdapterProductSeller adapterProductSeller;

    private ArrayList<ModelOrderShop> orderShopArrayList;
    private AdapterOrderShop adapterOrderShop;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_seller);

        nameTv = findViewById(R.id.nameTv);
        emailTv = findViewById(R.id.emailTv);
        shopNameTv = findViewById(R.id.shopNameTv);
        logoutBt = findViewById(R.id.logoutBt);
        editProfileBt = findViewById(R.id.editProfileBt);
        shoppingBt = findViewById(R.id.shoppingBt);
        tabProductsTv = findViewById(R.id.tabProductsTv);
        tabOrdersTv = findViewById(R.id.tabOrdersTv);
        filteredOrdersTv = findViewById(R.id.filteredOrdersTv);
        productsRl = findViewById(R.id.productsRl);
        ordersRl = findViewById(R.id.ordersRl);
        ordersRv = findViewById(R.id.ordersRv);

        filterProductBt = findViewById(R.id.filterProductBt);
        filterOrdersBt = findViewById(R.id.filterOrdersBt);
        reviewsBt = findViewById(R.id.reviewsBt);

        searchProductEt = findViewById(R.id.searchProductEt);
        filterProductTv = findViewById(R.id.filterProductTv);
        productsRv = findViewById(R.id.productsRv);

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait..");
        progressDialog.setCanceledOnTouchOutside(false);
        firebaseAuth = FirebaseAuth.getInstance();
        checkUser();
        loadAllProducts();
        loadAllOrders();

        logoutBt.setOnClickListener(view -> makeMeOffline());
        shoppingBt.setOnClickListener(view -> startActivity(new Intent(MainSellerActivity.this, AddProductActivity.class)));

        tabProductsTv.setOnClickListener(view -> {
            //load products
            showProductsUI();
        });
        tabOrdersTv.setOnClickListener(view -> {
            //load orders
            showOrdersUI();
        });
        filterProductBt.setOnClickListener(view -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainSellerActivity.this);
            builder.setTitle("Choose Category")
                    .setItems(Constants.productCategories1, (dialogInterface, i) -> {
                        // get selected item
                        String selected = Constants.productCategories1[i];
                        filterProductTv.setText(selected);
                        if(selected.equals("All")){
                            //load all
                            loadAllProducts();
                        }else{
                            //load filtered
                            loadFilteredProducts(selected);
                        }
                    })
                    .show();
        });
        filterOrdersBt.setOnClickListener(view -> {
            String[] options = {"All","InProgress","Completed","Cancelled"};
            AlertDialog.Builder builder = new AlertDialog.Builder(MainSellerActivity.this);
            builder.setTitle("Filter Orders:")
                    .setItems(options, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            //handle item click
                            if(i == 0){
                                //all clicked
                                filteredOrdersTv.setText("Showing All Orders");
                                adapterOrderShop.getFilter().filter("");
                            }else {
                                String optionClicked = options[i];
                                filteredOrdersTv.setText("Showing "+ optionClicked + " Orders");
                                adapterOrderShop.getFilter().filter(optionClicked);
                            }
                        }
                    })
                    .show();
        });
        reviewsBt.setOnClickListener(view -> {
            //open same review activity as used in user main page
            Intent intent = new Intent(MainSellerActivity.this,WriteReviewActivity.class);
            intent.putExtra("shopUid",firebaseAuth.getUid());
            startActivity(intent);
        });

        searchProductEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                try {
                    adapterProductSeller.getFilter().filter(charSequence);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });


    }

    private void loadAllOrders() {
        orderShopArrayList = new ArrayList<>();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(firebaseAuth.getUid()).child("Orders")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        orderShopArrayList.clear();
                        for (DataSnapshot ds: snapshot.getChildren()){
                            ModelOrderShop modelOrderShop = ds.getValue(ModelOrderShop.class);
                            orderShopArrayList.add(modelOrderShop);
                        }
                        //setup adapter
                        adapterOrderShop = new AdapterOrderShop(MainSellerActivity.this,orderShopArrayList);
                        ordersRv.setAdapter(adapterOrderShop);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
    private void loadFilteredProducts(String selected) {
        productList = new ArrayList<>();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(firebaseAuth.getUid()).child("products")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        // before getting reset list
                        for (DataSnapshot ds : snapshot.getChildren()){
                            String productCategory = ""+ds.child("productCategory").getValue();
                            //selected category matches
                            if (selected.equals(productCategory)){
                                ModelProduct modelProduct = ds.getValue(ModelProduct.class);
                                productList.add(modelProduct);
                            }
                        }
                        adapterProductSeller = new AdapterProductSeller(MainSellerActivity.this,productList);
                        productsRv.setAdapter(adapterProductSeller);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void loadAllProducts() {
        productList = new ArrayList<>();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(firebaseAuth.getUid()).child("products")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        // before getting reset list
                        for (DataSnapshot ds : snapshot.getChildren()){
                            ModelProduct modelProduct = ds.getValue(ModelProduct.class);
                            productList.add(modelProduct);
                        }
                        adapterProductSeller = new AdapterProductSeller(MainSellerActivity.this,productList);
                        productsRv.setAdapter(adapterProductSeller);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void showProductsUI() {
        //Showing products ui and hide order ui
        productsRl.setVisibility(View.VISIBLE);
        ordersRl.setVisibility(View.GONE);

        tabProductsTv.setTextColor(getResources().getColor(R.color.white));
        tabProductsTv.setBackgroundResource(R.drawable.shape_rect02);

        tabOrdersTv.setTextColor(getResources().getColor(R.color.baseColor));
        tabOrdersTv.setBackgroundColor(getResources().getColor(android.R.color.transparent));

    }
    private void showOrdersUI() {
        //Showing order ui and hide product ui
        productsRl.setVisibility(View.GONE);
        ordersRl.setVisibility(View.VISIBLE);

        tabProductsTv.setTextColor(getResources().getColor(R.color.baseColor));
        tabProductsTv.setBackgroundColor(getResources().getColor(android.R.color.transparent));

        tabOrdersTv.setTextColor(getResources().getColor(R.color.white));
        tabOrdersTv.setBackgroundResource(R.drawable.shape_rect02);

    }



        private void makeMeOffline () {
            progressDialog.setMessage("Logging out...");

            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("online", "false");

            //update values to db
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
            ref.child(Objects.requireNonNull(firebaseAuth.getUid())).updateChildren(hashMap)
                    .addOnSuccessListener(unused -> {
                        firebaseAuth.signOut();
                        checkUser();
                    })
                    .addOnFailureListener(e -> {
                        progressDialog.dismiss();
                        Toast.makeText(MainSellerActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }

        private void checkUser () {
            FirebaseUser user = firebaseAuth.getCurrentUser();
            if (user == null) {
                startActivity(new Intent(MainSellerActivity.this, LoginActivity.class));
                finish();
            } else {
                loadMyInfo();
            }
        }

        private void loadMyInfo () {
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
            ref.orderByChild("uid").equalTo(firebaseAuth.getUid())
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for (DataSnapshot ds : snapshot.getChildren()) {
                                //get data from db
                                String name = "" + ds.child("name").getValue();
                                String accountType = "" + ds.child("accountType").getValue();
                                String email = "" + ds.child("email").getValue();
                                String shopName = "" + ds.child("shopName").getValue();

                                //set data to textview
                                nameTv.setText(name + "(" + accountType + ")");
                                emailTv.setText(email);
                                shopNameTv.setText(shopName);
                            }

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
        }



}