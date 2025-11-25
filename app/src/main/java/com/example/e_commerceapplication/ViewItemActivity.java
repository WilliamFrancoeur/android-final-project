package com.example.e_commerceapplication;

import android.app.VoiceInteractor;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.stripe.android.PaymentConfiguration;
import com.stripe.android.paymentsheet.PaymentSheet;
import com.stripe.android.paymentsheet.PaymentSheetResult;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.stripe.android.EphemeralOperation;

import com.stripe.android.Stripe;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ViewItemActivity extends AppCompatActivity {

    TextView tvName, tvDesc, tvPrice;
    ImageView ivImage;
    Button btnBuy;

    private String PublishableKey = "";
    private String SecretKey = "";
    private String CustomersURL = "https://api.stripe.com/v1/customers";
    private String EphericalKeyURL = "https://api.stripe.com/v1/ephemeral_keys";
    private String ClientSecretURL = "https://api.stripe.com/v1/payment_intents";

    private String CustomerId = FirebaseAuth.getInstance().getUid();
    private String EphericalKey;
    private String ClientSecret;
    private PaymentSheet paymentSheet;
    private String Amount;
    private String Currency = "usd";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_view_item);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        toolbar.setNavigationOnClickListener(v -> finish());

        tvName = findViewById(R.id.tvName);
        tvDesc = findViewById(R.id.tvDesc);
        tvPrice = findViewById(R.id.tvPrice);
        ivImage = findViewById(R.id.ivImage);

        btnBuy = findViewById(R.id.btnBuy);

        Intent intent = getIntent();
        String id = intent.getStringExtra("id");
        String name = intent.getStringExtra("name");
        String description = intent.getStringExtra("description");
        double price = intent.getDoubleExtra("price",0.0);
        String imageUrl = intent.getStringExtra("imageUrl");

        Product product = new Product(id, name, description, price, imageUrl);

        tvName.setText(product.getName());
        tvDesc.setText(product.getDescription());
        tvPrice.setText("$" + product.getPrice());

        if (product.getImageURL() !=null && !product.getImageURL().isEmpty()){
            new ProductBuyAdapter.ImageLoadTask(product.getImageURL(),ivImage).execute();
        }else {
            ivImage.setImageResource(android.R.drawable.ic_menu_report_image);
        }



        int priceInt = (int)price * 100;
        Amount = Integer.toString(priceInt);

        PaymentConfiguration.init(this, PublishableKey);

        paymentSheet = new PaymentSheet(this, this::onPaymentResult);

        btnBuy.setOnClickListener(view -> {
            if (CustomerId != null && !CustomerId.isEmpty()){
                paymentFlow();
            } else {
                Toast.makeText(ViewItemActivity.this, "Customer ID is not available", Toast.LENGTH_SHORT).show();
            }

        });

        createCustomer();
    }

    private void createCustomer() {
        StringRequest request = new StringRequest(Request.Method.POST, CustomersURL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject object = new JSONObject(response);
                    CustomerId = object.getString("id");
                    Log.d("Stripe", "Customer created: " + CustomerId);

                    // Now that CustomerId is available, fetch the Ephemeral Key
                    if (CustomerId != null && !CustomerId.isEmpty()) {
                        getEphericalKey();
                    } else {
                        Toast.makeText(ViewItemActivity.this, "Failed to create customer", Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(ViewItemActivity.this, "Error creating customer: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(ViewItemActivity.this, "Error creating customer: " + error.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }
        }) {
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + SecretKey);
                return headers;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(request);
    }

    private void getEphericalKey() {
        StringRequest request = new StringRequest(Request.Method.POST, EphericalKeyURL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject object = new JSONObject(response);
                    EphericalKey = object.getString("id");
                    Log.d("Stripe", "Ephemeral Key created: " + EphericalKey);

                    // Now get the Client Secret after the Ephemeral Key is fetched
                    if (EphericalKey != null && !EphericalKey.isEmpty()) {
                        getClientSecret(CustomerId, EphericalKey);
                    } else {
                        Toast.makeText(ViewItemActivity.this, "Failed to fetch ephemeral key", Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(ViewItemActivity.this, "Error fetching ephemeral key: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(ViewItemActivity.this, "Error fetching ephemeral key: " + error.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }
        }) {
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + SecretKey);
                headers.put("Stripe-Version", "2022-11-15");
                return headers;
            }

            public Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("customer", CustomerId);
                return params;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(request);
    }

    private void getClientSecret(String customerId, String ephemeralKey) {
        StringRequest request = new StringRequest(Request.Method.POST, ClientSecretURL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject object = new JSONObject(response);
                    ClientSecret = object.getString("client_secret");
                    Log.d("Stripe", "Client Secret created: " + ClientSecret);

                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(ViewItemActivity.this, "Error fetching client secret: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(ViewItemActivity.this, "Error fetching client secret: " + error.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }
        }) {
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + SecretKey);
                headers.put("Content-Type","application/x-www-form-urlencoded");
                return headers;
            }

            public Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("customer", customerId);
                params.put("amount", Amount);
                params.put("currency", Currency);
                params.put("automatic_payment_methods[enabled]", "true");
                return params;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(request);
    }

    private void paymentFlow() {
        if (ClientSecret != null && !ClientSecret.isEmpty()) {
            paymentSheet.presentWithPaymentIntent(ClientSecret, new PaymentSheet.Configuration("Stripe", new PaymentSheet.CustomerConfiguration(
                    CustomerId, EphericalKey
            )));
        } else {
            Toast.makeText(ViewItemActivity.this, "Client Secret not available", Toast.LENGTH_SHORT).show();
        }
    }

    private void onPaymentResult(PaymentSheetResult paymentSheetResult) {
        if (paymentSheetResult instanceof PaymentSheetResult.Completed) {
            Toast.makeText(this, "Payment Success", Toast.LENGTH_SHORT).show();
            Intent intent = getIntent();
            String id = intent.getStringExtra("id");
            FirebaseDatabase.getInstance().getReference("products")
                    .child(id)
                    .removeValue();
            startActivity(new Intent(this, MainActivity.class));
        } else {
            Toast.makeText(this, "Payment Failed", Toast.LENGTH_SHORT).show();
        }
    }
}