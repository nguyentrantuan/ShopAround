package edu.orangecoastcollege.cs273.dnguyen1214.shoparound;

import android.app.Activity;
import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.ImageRequest;

import java.io.IOException;
import java.io.InputStream;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by duyng on 1/13/2017.
 */

public class ProductAdapter extends ArrayAdapter<Product> {

    private Context mContext;
    private int mResourceId;
    private List<Product> mAllProducts;



    /**
     * Parameterized constructor for this custom adapter.
     * @param context The context from which the MusicEventAdapter was created.
     * @param resourceId The layout resource id (e.g. R.layout.music_event_list_item)
     * @param allProducts The ArrayList containing all MusicEvent objects.
     */
    public ProductAdapter(Context context, int resourceId, ArrayList<Product> allProducts)
    {
        super(context, resourceId, allProducts);
        this.mResourceId = resourceId;
        this.mContext = context;
        this.mAllProducts = allProducts;
    }

    /**
     * Gets the view associated with the layout (sets ImageView and TextView content).
     * @param pos The position of the MusicEvent selected.
     * @param convertView The converted view.
     * @param parent The parent - ArrayAdapter
     * @return The new view with all content (ImageView and TextView) set.
     */
    public View getView(int pos, View convertView, ViewGroup parent)
    {
        Product product = mAllProducts.get(pos);
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(mResourceId, null);

        ImageView productImageView = (ImageView) view.findViewById(R.id.productImageView);
        TextView productNameTextView = (TextView) view.findViewById(R.id.productNameTextView);
        TextView productPriceTextView = (TextView) view.findViewById(R.id.productPriceTextView);
        TextView productChoiceTextView = (TextView) view.findViewById(R.id.choiceTextView);
        LinearLayout productListLinearLayout = (LinearLayout) view.findViewById(R.id.productListLinearLayout);

        productListLinearLayout.setTag(product);

        productNameTextView.setText(product.getName());
        NumberFormat format = NumberFormat.getCurrencyInstance();
        productPriceTextView.setText(format.format(product.getCurrentPrice()));

        ImageLoader imageLoader = MySingleton.getInstance(this.getContext()).getImageLoader();
        imageLoader.get(product.getProductImageUri().toString(),ImageLoader.getImageListener(productImageView,R.drawable.laptop,R.drawable.laptop));
        if (productChoiceTextView!=null)
        {
            productChoiceTextView.setText(String.valueOf(pos+1) + " choice");
        }

        return view;
    }
}
