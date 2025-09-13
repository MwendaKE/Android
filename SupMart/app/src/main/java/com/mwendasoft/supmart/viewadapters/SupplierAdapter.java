package com.mwendasoft.supmart.viewadapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.support.v7.widget.RecyclerView;

import com.mwendasoft.supmart.R;
import com.mwendasoft.supmart.models.Supplier;

import java.util.ArrayList;

public class SupplierAdapter extends RecyclerView.Adapter<SupplierAdapter.SupplierViewHolder> {

    private ArrayList<Supplier> supplierList;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Supplier supplier);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public SupplierAdapter(ArrayList<Supplier> list) {
        this.supplierList = list;
    }

    public static class SupplierViewHolder extends RecyclerView.ViewHolder {
        public TextView nameView, codeView, addressView;

        public SupplierViewHolder(View itemView) {
            super(itemView);
            nameView = itemView.findViewById(R.id.supplierName);
            codeView = itemView.findViewById(R.id.supplierCode);
            addressView = itemView.findViewById(R.id.supplierAddress);
        }
    }

    @Override
    public SupplierViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
			.inflate(R.layout.item_supplier, parent, false);
        return new SupplierViewHolder(v);
    }

    @Override
    public void onBindViewHolder(SupplierViewHolder holder, int position) {
        final Supplier s = supplierList.get(position);
        holder.nameView.setText(s.supplierName);
        holder.codeView.setText("Code: " + s.supplierCode);
        holder.addressView.setText("Address: " + s.supplierAddress);

        // Set click listener
        holder.itemView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if (listener != null) {
						listener.onItemClick(s);
					}
				}
			});
    }

    @Override
    public int getItemCount() {
        return supplierList.size();
    }
}
