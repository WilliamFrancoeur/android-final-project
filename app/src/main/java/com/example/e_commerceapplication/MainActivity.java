package com.example.e_commerceapplication;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

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

public class MainActivity extends AppCompatActivity {

    Toolbar toolbar;
    ProductBuyAdapter adapter;
    ListView listViewProducts;
    ArrayList<Product> productList;
    DatabaseReference databaseProduct;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        listViewProducts = findViewById(R.id.listViewProducts);
        databaseProduct = FirebaseDatabase.getInstance().getReference("products");


        productList = new ArrayList<>();
        adapter = new ProductBuyAdapter(this, productList);
        listViewProducts.setAdapter(adapter);

        listViewProducts.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Product selectedProduct = productList.get(position);
                Intent intent = new Intent(MainActivity.this, ViewItemActivity.class);
                intent.putExtra("id", selectedProduct.getId());
                intent.putExtra("name", selectedProduct.getName());
                intent.putExtra("description", selectedProduct.getDescription());
                intent.putExtra("price", selectedProduct.getPrice());
                intent.putExtra("imageUrl", selectedProduct.getImageURL());

                startActivity(intent);
            }
        });



        toolbar = findViewById(R.id.toolbarMenu);
        setSupportActionBar(toolbar);


        databaseProduct.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                productList.clear();
                for (DataSnapshot possnapshot : snapshot.getChildren()) {
                    Product product = possnapshot.getValue(Product.class);
                    if (product != null && !userId.equals(product.getSellerId())) {
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
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        if (item.getItemId() == R.id.menu_account){
            startActivity(new Intent(MainActivity.this, AccountActivity.class));
        }
        return true;
    }
}