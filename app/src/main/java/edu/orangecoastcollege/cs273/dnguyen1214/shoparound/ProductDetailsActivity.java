package edu.orangecoastcollege.cs273.dnguyen1214.shoparound;

import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Currency;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class ProductDetailsActivity extends AppCompatActivity {

    private GraphView graphView;
    private Product product;
    private static final String TAG = HttpHandler.class.getSimpleName();
    private String url;
    private List<DataPoint> list;
    private TextView productNameTextView,currentpriceTextView;
    private TextView bestPriceTextView, recommendationTextView;
    private TextView priceHistoryTextView, dateHistoryTextView;
    private String dateHistory, priceHistory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_details);
        graphView = (GraphView) findViewById(R.id.graph);
        productNameTextView = (TextView) findViewById(R.id.productNameTextView);
        bestPriceTextView = (TextView) findViewById(R.id.bestPriceTextView);
        priceHistoryTextView = (TextView) findViewById(R.id.priceHistoryTextView);
        dateHistoryTextView = (TextView) findViewById(R.id.dateHistoryTextView);
        recommendationTextView = (TextView) findViewById(R.id.recommendationTextView);
        currentpriceTextView = (TextView) findViewById(R.id.currentpriceTextView);
        Intent intent = getIntent();
        product =  intent.getParcelableExtra("laptop");
        productNameTextView.setText(product.getName());
        product.setPrices(new HashMap<String, Float>());
        url = "http://54.90.116.35/get_price_history.php?sku=" + product.getSku();
        list = new ArrayList<>();


        new GetPrices().execute();
        DateFormat dateFormat = new SimpleDateFormat("MM/yy");
        graphView.getGridLabelRenderer().setLabelFormatter(new DateAsXAxisLabelFormatter(this,dateFormat){
            @Override
            public String formatLabel(double value, boolean isValueX) {
                if (isValueX) {
                    // format as date
                    mCalendar.setTimeInMillis((long) value);
                    return mDateFormat.format(mCalendar.getTimeInMillis());
                } else {
                    return  "$"+super.formatLabel(value, isValueX);
                }
            }
        });

        Date now = new Date(Calendar.getInstance().getTimeInMillis());
        Date endOfJuly = now;
        Date endOfNovember = now;
        Date endOfFeb = now;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.US);
        try {
            String year = String.valueOf(Calendar.getInstance().get(Calendar.YEAR));
            endOfJuly = simpleDateFormat.parse(year+"-07-30 00:00:00");
            endOfNovember = simpleDateFormat.parse(year+"-11-30 00:00:00");
            endOfFeb = simpleDateFormat.parse(year+"-02-28 00:00:00");
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if ((endOfFeb.getTime()-now.getTime())> TimeUnit.DAYS.toMillis(30))
        {
            recommendationTextView.setText("The next sale period is late February");
        }
        else if ((endOfJuly.getTime()-now.getTime())> TimeUnit.DAYS.toMillis(30))
        {
            recommendationTextView.setText("The next sale period is late July");
        }
        else if ((endOfNovember.getTime()-now.getTime())> TimeUnit.DAYS.toMillis(30))
        {
            recommendationTextView.setText("The next sale period is late December ");
        }
        else
        {
            recommendationTextView.setText("Buy now!");
        }



    }

    public void buy(View view)
    {
//        Log.e("link",product.getProductUrl());
        String productUrl = product.getProductUrl();
        Uri webpage = Uri.parse(product.getProductUrl());
        Intent intent = new Intent(Intent.ACTION_VIEW, webpage);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }

    private class GetPrices extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Toast.makeText(ProductDetailsActivity.this,"Data is downloading",Toast.LENGTH_LONG).show();

        }

        @Override
        protected Void doInBackground(Void... arg0) {
            HttpHandler sh = new HttpHandler();
            // Making a request to url and getting response

            String jsonStr = sh.makeServiceCall(url);

            priceHistory = "";
            dateHistory = "";
            Log.e(TAG, "Response from url: " + jsonStr);
            if (jsonStr != null) {
                try {
                    list = new ArrayList<>();
                    JSONObject jsonObj = new JSONObject(jsonStr);

                    // Getting JSON Array node
                    JSONArray allProductsJSON = jsonObj.getJSONArray("points");

                    int numberOfPoints = allProductsJSON.length();

                    HashMap<String, Float> prices = new HashMap<String, Float>();

                    for (int i = 0; i < numberOfPoints; i++)
                    {
                        JSONObject productJSON = allProductsJSON.getJSONObject(i);

                        String dateText = productJSON.getString("Date");
                        float currentPrice = (float) productJSON.getDouble("Price");
                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.US);
                        Date date = new Date(Calendar.getInstance().getTimeInMillis());
                        try {
                            date = dateFormat.parse(dateText);
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        if (list.size()>0)
                        {
                            list.add(new DataPoint(new Date(date.getTime()-10000000),list.get(list.size()-1).getY()));
                        }
                        list.add(new DataPoint(date, currentPrice));

                        prices.put(dateText,currentPrice);
                        DateFormat dateFormat1 = new SimpleDateFormat("MM/dd/yy");
                        priceHistory+= "$ " +currentPrice+"\n";
                        dateHistory+=dateFormat1.format(date)+"\n";
                    }
                    if (list.size()==1) {
                        Date date = new Date(Calendar.getInstance().getTimeInMillis());
                        list.add(new DataPoint(date, product.getCurrentPrice()));
                    }
                    product.setPrices(prices);


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
            DataPoint[] dataPoints = new DataPoint[list.size()];
            for (int i=0;i<list.size();i++)
            {
                dataPoints[i] = list.get(i);
            }
            LineGraphSeries<DataPoint> series = new LineGraphSeries<>(dataPoints);
            graphView.addSeries(series);
            // set date label formatter
            // set date label formatter
            //graphView.getGridLabelRenderer().setNumHorizontalLabels(3); // only 4 because of the space


            Float min = Collections.min(product.getPrices().values());
            Float max = Collections.max(product.getPrices().values());
            currentpriceTextView.setText("$"+(String.valueOf(product.getCurrentPrice())));
            if (max!=min){
                if (min >= product.getCurrentPrice()) {
                    bestPriceTextView.setText("This is the best deal historically. Buy Now!");
                    bestPriceTextView.setTextColor(getResources().getColor(R.color.green));
                } else {bestPriceTextView.setText("Unless you really need one, you should wait");}
            }
            if (max == min) {
                bestPriceTextView.setText("Unless you're buying a new model, you should wait");
            }


            priceHistoryTextView.setText(priceHistory);
            dateHistoryTextView.setText(dateHistory);


            // set manual x bounds to have nice steps
            graphView.getViewport().setMinX(list.get(0).getX());
            graphView.getViewport().setMaxX(list.get(list.size()-1).getX());
            graphView.getViewport().setXAxisBoundsManual(true);

            // set manual X bounds
            graphView.getViewport().setYAxisBoundsManual(true);
            graphView.getViewport().setMinY(min-50);
            graphView.getViewport().setMaxY(max+50);

            graphView.getViewport().setScalable(true);
            graphView.getViewport().setScalableY(true);


        }
    }
}
