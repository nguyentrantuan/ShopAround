package edu.orangecoastcollege.cs273.dnguyen1214.shoparound;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_CODE = 100;
    private Context context = this;
    private ArrayList<Product> topProducts,allProducts;
    private ListView productListView,topProductListView;
    private ProductAdapter productAdapter, topProductAdapter;
    private static final String TAG = HttpHandler.class.getSimpleName();
    private EditText priceFromEditText;
    private EditText priceToEditText;
    private String url;
    private Spinner priceRangeSpinner;
    private LinearLayout priceRangeLinearLayout, mainLinearLayout, productListLinearLayout;
    private String fromText,toText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        /*
        try {
            allProducts = JSONLoader.loadJSONFromAsset(context);
        }
        catch (IOException ex){
            Log.e("OC Music Events", "Error loading JSON data." + ex.getMessage());
        }
        */

        productListView = (ListView) findViewById(R.id.productListView);
        topProductListView = (ListView) findViewById(R.id.topProductListView);
        priceFromEditText = (EditText) findViewById(R.id.priceFromEditText);
        priceToEditText = (EditText) findViewById(R.id.priceToEditText);
        priceRangeSpinner = (Spinner) findViewById(R.id.priceRangeSpinner);
        priceRangeLinearLayout = (LinearLayout) findViewById(R.id.priceRangeLinearLayout);
        mainLinearLayout = (LinearLayout) findViewById(R.id.activity_main);
        productListLinearLayout = (LinearLayout) findViewById(R.id.productListLinearLayout);

        ArrayAdapter<String> priceRangeSpinnerAdapter =
                new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item,
                        new String[]{"Select price range","Custom", "$200 - $500","$500 - $700","$700 - $1000","$1000+"});
        priceRangeSpinner.setAdapter(priceRangeSpinnerAdapter);
        priceRangeSpinner.setOnItemSelectedListener(priceRangeSpinnerListener);


    }

    public AdapterView.OnItemSelectedListener priceRangeSpinnerListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            String selectedPrice = String.valueOf(parent.getItemAtPosition(position));
            if (selectedPrice.equals("Custom"))
            {
                mainLinearLayout.removeView(productListLinearLayout);
                mainLinearLayout.addView(priceRangeLinearLayout);
                priceRangeLinearLayout.setVisibility(View.VISIBLE);
                mainLinearLayout.addView(productListLinearLayout);

            }
            else {
                mainLinearLayout.removeView(priceRangeLinearLayout);
                priceRangeLinearLayout.setVisibility(View.INVISIBLE);

                if (selectedPrice.equals("$200 - $500")) {
                    fromText = "200";
                    toText = "500";
                    getData();
                } else if (selectedPrice.equals("$500 - $700")) {
                    fromText = "500";
                    toText = "700";
                    getData();
                } else if (selectedPrice.equals("$700 - $1000")) {
                    fromText = "700";
                    toText = "1000";
                    getData();
                } else if (selectedPrice.equals("$1000+")) {
                    fromText = "1000";
                    toText = "10000";
                    getData();
                }
            }
        }

        /**
         * A method that sets the current selection to 0 if nothing was selected.
         *
         * @param parent
         */
        @Override
        public void onNothingSelected(AdapterView<?> parent) {
            parent.setSelection(0);
        }
    };



    private class GetLaptops extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Toast.makeText(MainActivity.this,"Json Data is downloading",Toast.LENGTH_LONG).show();

        }

        @Override
        protected Void doInBackground(Void... arg0) {
            HttpHandler sh = new HttpHandler();
            // Making a request to url and getting response

            String jsonStr = sh.makeServiceCall(url);

            Log.e(TAG, "Response from url: " + jsonStr);
            if (jsonStr != null) {
                try {
                    allProducts = new ArrayList<>();
                    topProducts = new ArrayList<>();
                    JSONObject jsonObj = new JSONObject(jsonStr);

                    // Getting JSON Array node
                    JSONArray allProductsJSON = jsonObj.getJSONArray("laptop");

                    int numberOfProducts = allProductsJSON.length();
                    for (int i = 0; i < numberOfProducts; i++)
                    {
                        JSONObject productJSON = allProductsJSON.getJSONObject(i);

                        String sku = productJSON.getString("SKU");
                        String name = productJSON.getString("ProductName");
                        String seller = productJSON.getString("seller");
                        float currentPrice = (float) productJSON.getDouble("CurrentPrice");
                        float originalPrice = 1000f; //(float) productJSON.getInt("OriginalPrice")/100;

                        HashMap<String, Float> prices = new HashMap<String, Float>();

                        Uri productImageUri = Uri.parse(productJSON.getString("imgsrc"));
                        String productUrl = productJSON.getString("link");

                        Product product = new Product(sku,name,seller,currentPrice,originalPrice,prices,productUrl, productImageUri);
                        allProducts.add(product);
                    }
                    for (int i=0;i<3;i++)
                    {
                        topProducts.add(allProducts.get(0));
                        allProducts.remove(0);
                    }


                } catch (final JSONException e) {
                    Log.e(TAG, "Json parsing error: " + e.getMessage());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(),
                                    "Json parsing error: " + e.getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                    });

                } 

            } else {
                Log.e(TAG, "Couldn't get json from server.");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(),
                                "Couldn't get json from server. Check LogCat for possible errors!",
                                Toast.LENGTH_LONG).show();
                    }
                });
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            productAdapter = new ProductAdapter(MainActivity.this,R.layout.product_list_item,allProducts);
            productListView.setAdapter(productAdapter);

            topProductAdapter= new ProductAdapter(MainActivity.this,R.layout.top_product_list_item,topProducts);
            topProductListView.setAdapter(topProductAdapter);


        }
    }

    public void viewProductDetails(View view) {

        if (view instanceof LinearLayout) {
            LinearLayout selectedLinearLayout = (LinearLayout) view;

            Product product = (Product) selectedLinearLayout.getTag();
            // TODO: Use an Intent to start the GameDetailsActivity with the data it needs to correctly inflate its views.
            Intent detailsIntent = new Intent(this, ProductDetailsActivity.class);
            detailsIntent.putExtra("laptop",product);

            startActivity(detailsIntent);
        }

    }

    private void getData(){
        url = "http://54.90.116.35/get_product_details.php?low=" + fromText +"&high="+ toText;
        // List of all the permissions we need to request from user
        ArrayList<String> permList = new ArrayList<>();
        int internetPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET);
        if (internetPermission != PackageManager.PERMISSION_GRANTED)
            permList.add(Manifest.permission.INTERNET);


        if (permList.size() > 0) {
            // Convert the ArrayList into an Array of Strings
            String[] perms = new String[permList.size()];

            // Request permission from the user

            ActivityCompat.requestPermissions(this, permList.toArray(perms), REQUEST_CODE);
        }

        if (internetPermission == PackageManager.PERMISSION_GRANTED) {
            new GetLaptops().execute();
        }
    }
    public void search(View view)
    {
        fromText = priceFromEditText.getText().toString();
        toText = priceToEditText.getText().toString();
        if (fromText.isEmpty()||toText.isEmpty()){
            Toast.makeText(this,"Price range cannot be empty!",Toast.LENGTH_LONG).show();
        }
        else {
            getData();
            mainLinearLayout.removeView(priceRangeLinearLayout);
            priceRangeLinearLayout.setVisibility(View.INVISIBLE);

        }
    }
}
