package com.mwendasoft.supmart.viewadapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.support.v7.widget.RecyclerView;

import com.mwendasoft.supmart.R;
import com.mwendasoft.supmart.models.Product;

import java.util.ArrayList;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    private ArrayList<Product> productList;

    public ProductAdapter(ArrayList<Product> list) {
        this.productList = list;
    }

    public static class ProductViewHolder extends RecyclerView.ViewHolder {
        public TextView codeView, nameView, packageView, supplierView;

        public ProductViewHolder(View itemView) {
            super(itemView);
            codeView = (TextView) itemView.findViewById(R.id.productCode);
            nameView = (TextView) itemView.findViewById(R.id.productName);
            packageView = (TextView) itemView.findViewById(R.id.productPackage);  // optional
            supplierView = (TextView) itemView.findViewById(R.id.productSupplier);
        }
    }

    @Override
    public ProductViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
			.inflate(R.layout.item_product, parent, false);
        return new ProductViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ProductViewHolder holder, int position) {
        Product p = productList.get(position);
        holder.codeView.setText(p.productCode);
        holder.nameView.setText(p.productName);
        holder.packageView.setText("Package: " + p.productPackage); // optional
        holder.supplierView.setText("Supplier: " + p.supplierName); // ✅ display supplier name
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }
}
