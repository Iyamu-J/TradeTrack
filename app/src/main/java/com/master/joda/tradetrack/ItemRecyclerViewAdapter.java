package com.master.joda.tradetrack;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.master.joda.tradetrack.model.Item;

import java.util.List;

public class ItemRecyclerViewAdapter extends RecyclerView.Adapter<ItemRecyclerViewAdapter.ViewHolder> {

    private Context mContext;
    private List<Item> mValues;

    public ItemRecyclerViewAdapter(Context context) {
        mContext = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        final Item item = mValues.get(position);
        holder.mItemName.setText(item.getName());

        holder.mEditItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, AddItemActivity.class);
                intent.putExtra("ITEM_ID_EXTRA", item.getId());
                mContext.startActivity(intent);
            }
        });

        holder.mRecordSale.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
                double storedProfit = (double) sharedPreferences.getFloat("KEY_STORED_PROFIT", 0);
                double profit = item.getSellingPrice() - item.getCostPrice();
                storedProfit = storedProfit + profit;
                sharedPreferences.edit()
                        .putFloat("KEY_STORED_PROFIT", (float) storedProfit)
                        .apply();
            }
        });
    }

    @Override
    public int getItemCount() {
        if (mValues.size() == 0) {
            return 0;
        } else {
            return mValues.size();
        }
    }

    public void setValues(List<Item> mValues) {
        this.mValues = mValues;
        notifyDataSetChanged();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView mItemName;
        ImageButton mEditItem;
        ImageButton mRecordSale;

        ViewHolder(View view) {
            super(view);
            mItemName = view.findViewById(R.id.item_name);
            mEditItem = view.findViewById(R.id.edit_item);
            mRecordSale = view.findViewById(R.id.record_sale);
        }
    }
}
