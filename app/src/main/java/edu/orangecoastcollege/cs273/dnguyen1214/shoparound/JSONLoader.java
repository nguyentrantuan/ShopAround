package edu.orangecoastcollege.cs273.dnguyen1214.shoparound;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.apache.http.NameValuePair;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by duyng on 1/13/2017.
 */

public class JSONLoader {




    /**
     * Loads JSON data from a file in the assets directory.
     * @param context The activity from which the data is loaded.
     * @throws IOException If there is an error reading from the JSON file.
     */
    public static ArrayList<Product> loadJSONFromAsset(Context context) throws IOException {
        ArrayList<Product> allProductLists = new ArrayList<>();
        String json = null;
        InputStream is = context.getAssets().open("Products.json");
        int size = is.available();
        byte[] buffer = new byte[size];
        is.read(buffer);
        is.close();
        json = new String(buffer, "UTF-8");
        try {
            JSONObject jsonRootObject = new JSONObject(json);
            JSONArray allProductsJSON = jsonRootObject.getJSONArray("Products");
            int numberOfProducts = allProductsJSON.length();
            for (int i = 0; i < numberOfProducts; i++)
            {
                JSONObject productJSON = allProductsJSON.getJSONObject(i);

                String sku = productJSON.getString("SKU");
                String name = productJSON.getString("ProductName");
                String seller = productJSON.getString("Seller");
                float currentPrice = (float) productJSON.getInt("CurrentPrice")/100;
                float originalPrice = (float) productJSON.getInt("OriginalPrice")/100;
                JSONObject pricesJSON = productJSON.getJSONObject("Prices");

                HashMap<String, Float> prices = new HashMap<String, Float>();
                Iterator<String> iterator = pricesJSON.keys();
                while (iterator.hasNext())
                {
                    String key = iterator.next();
                    try {
                        float price = (float) pricesJSON.getInt(key)/100;
                        prices.put(key,price);
                    }

                    catch (JSONException e)
                    {
                        Log.e("Shop Around prices", e.getMessage());
                    }
                }
                String productUrl = productJSON.getString("Link");
                Uri url = Uri.parse(productUrl);

                Product product = new Product(sku,name,seller,currentPrice,originalPrice,prices,productUrl,url);
                allProductLists.add(product);
            }
        }
        catch (JSONException e)
        {
            Log.e("Shop Around Products", e.getMessage());
        }
        return allProductLists;
    }

    public static ArrayList<Product> loadJSONFromHttp(Context context) throws IOException {
        ArrayList<Product> allProductLists = new ArrayList<>();
        String json = null;
        InputStream is = context.getAssets().open("Products.json");
        int size = is.available();
        byte[] buffer = new byte[size];
        is.read(buffer);
        is.close();
        json = new String(buffer, "UTF-8");
        try {
            JSONObject jsonRootObject = new JSONObject(json);
            JSONArray allProductsJSON = jsonRootObject.getJSONArray("Products");
            int numberOfProducts = allProductsJSON.length();
            for (int i = 0; i < numberOfProducts; i++)
            {
                JSONObject productJSON = allProductsJSON.getJSONObject(i);

                String sku = productJSON.getString("SKU");
                String name = productJSON.getString("ProductName");
                String seller = productJSON.getString("Seller");
                float currentPrice = (float) productJSON.getInt("CurrentPrice")/100;
                float originalPrice = (float) productJSON.getInt("OriginalPrice")/100;
                JSONObject pricesJSON = productJSON.getJSONObject("Prices");

                HashMap<String, Float> prices = new HashMap<String, Float>();
                Iterator<String> iterator = pricesJSON.keys();
                while (iterator.hasNext())
                {
                    String key = iterator.next();
                    try {
                        float price = (float) pricesJSON.getInt(key)/100;
                        prices.put(key,price);
                    }

                    catch (JSONException e)
                    {
                        Log.e("Shop Around prices", e.getMessage());
                    }
                }
                String productUrl = productJSON.getString("Link");

                Uri url = Uri.parse(productJSON.getString("Link"));

                Product product = new Product(sku,name,seller,currentPrice,originalPrice,prices,productUrl,url);
                allProductLists.add(product);
            }
        }
        catch (JSONException e)
        {
            Log.e("Shop Around Products", e.getMessage());
        }
        return allProductLists;
    }

}
