package com.grocery.store.activities;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.grocery.store.Constants;
import com.grocery.store.R;

import java.util.HashMap;

public class EditProductActivity extends AppCompatActivity {

    private EditText titleEt, descriptionEt, quantityEt, priceEt, discountedPriceEt, discountedPriceNoteEt;
    private ImageButton backBtn;
    private SwitchCompat discountSwitch;
    private TextView categoryTv;
    private Button updateProductBt;

    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;
    private String productId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_product);
        productId = getIntent().getStringExtra("productId");

        titleEt = findViewById(R.id.titleEt);
        descriptionEt = findViewById(R.id.descriptionEt);
        quantityEt = findViewById(R.id.quantityEt);
        priceEt = findViewById(R.id.priceEt);
        discountedPriceEt = findViewById(R.id.discountedPriceEt);
        discountedPriceNoteEt = findViewById(R.id.discountedPriceNoteEt);
        backBtn = findViewById(R.id.backBtn);
        discountSwitch = findViewById(R.id.discountSwitch);
        categoryTv = findViewById(R.id.categoryTv);
        updateProductBt = findViewById(R.id.updateProductBt);

        discountedPriceEt.setVisibility(View.GONE);
        discountedPriceNoteEt.setVisibility(View.GONE);

        firebaseAuth = FirebaseAuth.getInstance();
        loadProductDetails();// to set on views

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait...");
        progressDialog.setCanceledOnTouchOutside(false);

        discountSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if(isChecked){
                discountedPriceEt.setVisibility(View.VISIBLE);
                discountedPriceNoteEt.setVisibility(View.VISIBLE);
            }else {
                discountedPriceEt.setVisibility(View.GONE);
                discountedPriceNoteEt.setVisibility(View.GONE);
            }
        });
        backBtn.setOnClickListener(view -> onBackPressed());
        categoryTv.setOnClickListener(view -> categoryDialog());

        updateProductBt.setOnClickListener(view -> inputData());

    }

    private void loadProductDetails() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(firebaseAuth.getUid()).child("products").child(productId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        // get data
                        String productId = ""+snapshot.child("productId").getValue();
                        String productTitle = ""+snapshot.child("productTitle").getValue();
                        String productDescription = ""+snapshot.child("productDescription").getValue();
                        String productCategory = ""+snapshot.child("productCategory").getValue();
                        String productQuantity = ""+snapshot.child("productQuantity").getValue();
                        String originalPrice = ""+snapshot.child("originalPrice").getValue();
                        String discountPrice = ""+snapshot.child("discountPrice").getValue();
                        String discountNote = ""+snapshot.child("discountNote").getValue();
                        String discountAvailable = ""+snapshot.child("discountAvailable").getValue();
                        String timeStamp = ""+snapshot.child("timeStamp").getValue();
                        String uid = ""+snapshot.child("uid").getValue();

                        //set data to view
                        if (discountAvailable.equals("true")){
                            discountSwitch.setChecked(true);

                            discountedPriceEt.setVisibility(View.VISIBLE);
                            discountedPriceNoteEt.setVisibility(View.VISIBLE);
                        }else {
                            discountSwitch.setChecked(false);

                            discountedPriceEt.setVisibility(View.GONE);
                            discountedPriceNoteEt.setVisibility(View.GONE);
                        }

                        titleEt.setText(productTitle);
                        descriptionEt.setText(productDescription);
                        categoryTv.setText(productCategory);
                        discountedPriceNoteEt.setText(discountNote);
                        quantityEt.setText(productQuantity);
                        priceEt.setText(originalPrice);
                        discountedPriceEt.setText(discountPrice);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private String productTitle, productDescription, productCategory, productQuantity, originalPrice, discountPrice, discountNote;
    private boolean discountAvailable = false;
    private void inputData() {
        // Input data
        productTitle = titleEt.getText().toString().trim();
        productDescription = descriptionEt.getText().toString().trim();
        productCategory = categoryTv.getText().toString().trim();
        productQuantity = quantityEt.getText().toString().trim();
        originalPrice = priceEt.getText().toString().trim();
        discountPrice = discountedPriceEt.getText().toString().trim();
        discountNote = discountedPriceNoteEt.getText().toString().trim();
        discountAvailable = discountSwitch.isChecked();

        // validate data
        if (TextUtils.isEmpty (productTitle)) {
            Toast.makeText( this,  "Title is required...", Toast.LENGTH_SHORT).show();
            return; // don't proceed further
        }
        if (TextUtils.isEmpty (productCategory)) {
            Toast.makeText(  this,  "Category is required...", Toast.LENGTH_SHORT).show();
            return; // don't proceed further
        }
        if (TextUtils.isEmpty (originalPrice)) {
            Toast.makeText(  this,  "Price is required...", Toast.LENGTH_SHORT).show();
            return; // don't proceed further
        }
        if (discountAvailable) {
            //product is with discount
            discountPrice = discountedPriceEt.getText().toString().trim();
            discountNote = discountedPriceNoteEt.getText().toString().trim();
            if (TextUtils.isEmpty (discountPrice)) {
                Toast.makeText(this, "Discount Price is required...", Toast.LENGTH_SHORT).show();
                return; // don't proceed further
            }
        }else{
            //product without discount
            discountPrice ="*";
            discountNote = "";
        }
        updateProduct();
    }

    private void updateProduct() {
        //show progress
        progressDialog.setMessage("Updating product...");
        progressDialog.show();

        HashMap<String, Object> hashProduct = new HashMap<>();
        hashProduct.put("productTitle", "" + productTitle);
        hashProduct.put("productDescription", "" + productDescription);
        hashProduct.put("productCategory", "" + productCategory);
        hashProduct.put("productQuantity", "" + productQuantity);
        hashProduct.put("originalPrice", "" + originalPrice);
        hashProduct.put("discountPrice", "" + discountPrice);
        hashProduct.put("discountNote", "" + discountNote);
        hashProduct.put("discountAvailable", "" + discountAvailable);

        //update to db
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(firebaseAuth.getUid()).child("products").child(productId)
                .updateChildren(hashProduct)
                .addOnSuccessListener(unused -> {
                    //update success
                    progressDialog.dismiss();
                    Toast.makeText(EditProductActivity.this, "Updated...", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    //update failed
                    progressDialog.dismiss();
                    Toast.makeText(EditProductActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void categoryDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Product Category").setItems(Constants.productCategories, (dialogInterface, which) -> {
            String category = Constants.productCategories[which];

            categoryTv.setText(category);
        })
                .show();
    }

}