package com.example.storit;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class PaymentDetailsAdapter extends ArrayAdapter<CreditCard> {

    //variables
    private Context mContext;
    int mResource;
    private final List<CreditCard> mItems = new ArrayList<CreditCard>();

    //Constructor
    public PaymentDetailsAdapter(@NonNull Context context, int resource, @NonNull ArrayList<CreditCard> objects) {

        super(context, resource, objects);
        mContext = context;
        mResource = resource;

    }

    //Attaching to listView
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        String cardName = getItem(position).getCardName();
        int cardNum = getItem(position).getCardNumber();
        int month = getItem(position).getMonth();
        int year = getItem(position).getYear();

        LayoutInflater inflater = LayoutInflater.from(mContext);

        if(convertView == null){
            convertView = inflater.inflate(mResource, parent, false);
        }

        TextView tvName = (TextView) convertView.findViewById(R.id.cardName);
        TextView tvCardNum = (TextView) convertView.findViewById(R.id.cardNum);
        TextView tvExpiryDate = (TextView) convertView.findViewById(R.id.expireDate);
        Button editCard = convertView.findViewById(R.id.editCard);

        tvName.setText(cardName);
        tvCardNum.setText(""+cardNum);
        tvExpiryDate.setText(month + "/" + year);

        editCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mContext.startActivity(new Intent(getContext(), EditPaymentDetails.class));
            }
        });

        return convertView;

    }

    public void add(CreditCard item) {
        mItems.add(item);
        notifyDataSetChanged();
    }
}
