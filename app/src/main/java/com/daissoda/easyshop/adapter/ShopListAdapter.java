package com.daissoda.easyshop.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.daissoda.easyshop.R;
import com.daissoda.easyshop.datamodel.ShopDataModel;

import java.util.ArrayList;

public class ShopListAdapter extends BaseAdapter {
	int resource;
	Context context;
	ArrayList<ShopDataModel> shops;



	public ShopListAdapter(@NonNull Context context, int resource, @NonNull ArrayList<ShopDataModel> shops) {
		super();
		this.shops = shops;
		this.context = context;
		this.resource = resource;
	}


	@Override
	public int getCount() {
		return shops.size();
	}

	@Override
	public Object getItem(int position) {
		return shops.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = LayoutInflater.from(context).inflate(resource, parent, false);

		ShopDataModel shop = shops.get(position);
		((TextView) view.findViewById(R.id.shop_identifier)).setText(shop.getShopName() + " - " + shop.getSubLocation() + ", " + shop.getCity());


		return view;
	}
}
