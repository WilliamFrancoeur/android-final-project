package com.example.e_commerceapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class AdminViewActivity extends AppCompatActivity {

    ListView listViewProduct;
    DatabaseReference databaseProduct;
    ArrayList<Product> productList;

    ProductAdapter adapter;
    Toolbar toolbar;
    TextView title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_view);

        Intent intent = getIntent();
        String userId = intent.getStringExtra("UserID");

        title = findViewById(R.id.titleTextView);

        title.setText(intent.getStringExtra("email"));


        listViewProduct = findViewById(R.id.listViewProducts);
        databaseProduct = FirebaseDatabase.getInstance().getReference("products");


        productList = new ArrayList<>();
        adapter = new ProductAdapter(this, productList);
        listViewProduct.setAdapter(adapter);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        toolbar.setNavigationOnClickListener(v -> finish());


        databaseProduct.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                productList.clear();
                for (DataSnapshot possnapshot : snapshot.getChildren()) {
                    Product product = possnapshot.getValue(Product.class);
                    if (product != null && userId.equals(product.getSellerId())) {
                        productList.add(product);
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}