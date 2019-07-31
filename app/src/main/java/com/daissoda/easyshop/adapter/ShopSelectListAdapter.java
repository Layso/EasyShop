package com.daissoda.easyshop.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.daissoda.easyshop.R;
import com.daissoda.easyshop.datamodel.ShopDataModel;

import java.util.ArrayList;
import java.util.Map;

public class ShopSelectListAdapter extends BaseAdapter {
	private int resource;
	private Context context;
	private ArrayList<ShopDataModel> shops;
	private Map<String, Integer> prices;
	private Map<String, Integer> distances;
	private Map<String, Integer> missing;


	public ShopSelectListAdapter(@NonNull Context context, int resource,
	                             @NonNull ArrayList<ShopDataModel> shops,
	                             @NonNull Map<String, Integer> prices,
	                             @NonNull Map<String, Integer> distances,
	                             @NonNull Map<String, Integer> missing) {
		super();
		this.shops = shops;
		this.prices = prices;
		this.context = context;
		this.missing = missing;
		this.resource = resource;
		this.distances = distances;
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
		ShopDataModel shop = shops.get(position);
		View view = null;

		view = LayoutInflater.from(context).inflate(resource, parent, false);
		int cent = prices.get(shop.getId())%100;
		int euro = prices.get(shop.getId())/100;
		int distance = distances.get(shop.getId());
		((TextView) view.findViewById(R.id.shop_name)).setText(shop.getShopName());
		((TextView) view.findViewById(R.id.distance_info)).setText(distance >= 1000 ? (distance/1000 + "km") : (distance + "m"));

		if (missing.get(shop.getId()) > 0) {
			((TextView) view.findViewById(R.id.price_info)).setText("+" + euro + "." + (cent < 10 ? ("0" + cent) : cent) + "€");
			((TextView) view.findViewById(R.id.missing_info)).setText(missing.get(shop.getId()) + " missing item(s)");
		} else {
			((TextView) view.findViewById(R.id.price_info)).setText(euro + "." + (cent < 10 ? ("0" + cent) : cent) + "€");
			view.findViewById(R.id.missing_info).setVisibility(View.INVISIBLE);
		}

		return view;
	}
}
