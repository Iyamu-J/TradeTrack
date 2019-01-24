package com.master.joda.tradetrack;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.master.joda.tradetrack.model.Record;

import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * A simple {@link Fragment} subclass.
 */
public class ViewRecordsFragment extends Fragment {

    private Date date;
    private FirebaseUser mFirebaseUser;
    private DatabaseReference mRecordsDatabaseReference;

    public ViewRecordsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_records_list, container, false);

        mRecordsDatabaseReference = FirebaseDatabase.getInstance().getReference().child("records");
        mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        return view;
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.date)
        TextView mSalesDate;
        @BindView(R.id.profit)
        TextView mSalesProfit;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        void bind(Record record) {
            mSalesDate.setText(record.getDate());
            mSalesProfit.setText(record.getProfit());
        }
    }
}
