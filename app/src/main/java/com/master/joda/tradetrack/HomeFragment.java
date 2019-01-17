package com.master.joda.tradetrack;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.master.joda.tradetrack.model.Item;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * A fragment representing a list of Items.
 */
public class HomeFragment extends Fragment {

    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mItemsDatabaseReference;
    private FirebaseRecyclerAdapter mAdapter;

    private SharedPreferences sharedPreferences;

    @BindView(R.id.list)
    RecyclerView mRecyclerView;

    private Unbinder unbinder;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public HomeFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_item_list, container, false);

        unbinder = ButterKnife.bind(this, view);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());

        // initialise FirebaseDatabase
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mItemsDatabaseReference = mFirebaseDatabase.getReference();

        setupRecyclerView();

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        mAdapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        mAdapter.stopListening();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    private void setupRecyclerView() {
        Query query = mItemsDatabaseReference.child("");

        FirebaseRecyclerOptions<Item> options =
                new FirebaseRecyclerOptions.Builder<Item>()
                        .setQuery(query, Item.class)
                        .build();

        mAdapter = new FirebaseRecyclerAdapter<Item, ViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull ViewHolder holder, int position, @NonNull Item model) {
                holder.bind(model);

                final String itemId = model.getId();
                final double itemCostPrice = model.getCostPrice();
                final double itemSellingPrice = model.getSellingPrice();

                holder.mEditItem.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(getContext(), AddItemActivity.class);
                        intent.putExtra("", itemId);
                        startActivity(intent);
                    }
                });

                holder.mRecordSale.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int storedProfit = sharedPreferences.getInt("KEY_STORED_PROFIT", 0);
                        double profit = itemSellingPrice - itemCostPrice;
                        storedProfit = storedProfit + (int) profit;
                        sharedPreferences.edit()
                                .putInt("KEY_STORED_PROFIT", storedProfit)
                                .apply();
                    }
                });
            }

            @NonNull
            @Override
            public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.fragment_item, parent, false);
                return new ViewHolder(view);
            }
        };


        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setAdapter(mAdapter);
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.item_name)
        TextView mItemName;
        @BindView(R.id.edit_item)
        ImageButton mEditItem;
        @BindView(R.id.record_sale)
        ImageButton mRecordSale;

        ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }

        void bind(Item item) {
            mItemName.setText(item.getName());
        }
    }
}
