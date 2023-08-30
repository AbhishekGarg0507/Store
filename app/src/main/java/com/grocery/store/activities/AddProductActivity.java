package com.grocery.store.activities;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.grocery.store.Constants;
import com.grocery.store.R;

import java.util.HashMap;

public class AddProductActivity extends AppCompatActivity {

    private EditText titleEt, descriptionEt, quantityEt, priceEt, discountedPriceEt, discountedPriceNoteEt;
    private ImageButton backBtn;
    private SwitchCompat discountSwitch;
    private TextView categoryTv;
    private Button addProductBt;

    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_product);

        titleEt = findViewById(R.id.titleEt);
        descriptionEt = findViewById(R.id.descriptionEt);
        quantityEt = findViewById(R.id.quantityEt);
        priceEt = findViewById(R.id.priceEt);
        discountedPriceEt = findViewById(R.id.discountedPriceEt);
        discountedPriceNoteEt = findViewById(R.id.discountedPriceNoteEt);
        backBtn = findViewById(R.id.backBtn);
        discountSwitch = findViewById(R.id.discountSwitch);
        categoryTv = findViewById(R.id.categoryTv);
        addProductBt = findViewById(R.id.addProductBt);

        discountedPriceEt.setVisibility(View.GONE);
        discountedPriceNoteEt.setVisibility(View.GONE);

        firebaseAuth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait...");
        progressDialog.setCanceledOnTouchOutside(false);

       discountSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
           @Override
           public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
               if(isChecked){
                   discountedPriceEt.setVisibility(View.VISIBLE);
                   discountedPriceNoteEt.setVisibility(View.VISIBLE);
               }else {
                   discountedPriceEt.setVisibility(View.GONE);
                   discountedPriceNoteEt.setVisibility(View.GONE);
               }
           }
       });
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
       categoryTv.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View view) {
               categoryDialog();
           }
       });

       addProductBt.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View view) {
                inputData();
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
            discountPrice ="";
            discountNote = "";
        }
        addProduct();
    }

    private void addProduct() {
        //adding products to db
        progressDialog.setMessage("Adding Product...");
        progressDialog.show();

        String timeStamp = "" + System.currentTimeMillis();

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("productId", "" + timeStamp);
        hashMap.put("productTitle", "" + productTitle);
        hashMap.put("productDescription", "" + productDescription);
        hashMap.put("productCategory", "" + productCategory);
        hashMap.put("productQuantity", "" + productQuantity);
        hashMap.put("originalPrice", "" + originalPrice);
        hashMap.put("discountPrice", "" + discountPrice);
        hashMap.put("discountNote", "" + discountNote);
        hashMap.put("discountAvailable", "" + discountAvailable);
        hashMap.put("timeStamp", "" + timeStamp);
        hashMap.put("uid", "" + firebaseAuth.getUid());
        //add to data db
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(firebaseAuth.getUid()).child("products").child(timeStamp).setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        progressDialog.dismiss();
                        Toast.makeText(AddProductActivity.this, "Product added....", Toast.LENGTH_SHORT).show();
                        clearData();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@androidx.annotation.NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(AddProductActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }


    private void clearData() {
        //clear data after uploading
        titleEt.setText("");
        descriptionEt.setText("");
        categoryTv.setText("");
        quantityEt.setText("");
        priceEt.setText("");
        discountedPriceEt.setText("");
        discountedPriceNoteEt.setText("");
    }

    private void categoryDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Product Category").setItems(Constants.productCategories, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                String category = Constants.productCategories[which];

                categoryTv.setText(category);
            }
        })
                .show();
    }

}