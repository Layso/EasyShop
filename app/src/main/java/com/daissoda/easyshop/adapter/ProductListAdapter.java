package com.daissoda.easyshop.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.daissoda.easyshop.R;
import com.daissoda.easyshop.datamodel.PriceDataModel;
import com.daissoda.easyshop.datamodel.ProductDataModel;
import com.daissoda.easyshop.datamodel.ShopDataModel;

import java.util.ArrayList;
import java.util.Map;

public class ProductListAdapter extends BaseAdapter {
	int resource;
	Context context;
	ArrayList<ProductDataModel> products;
	Map<String, PriceDataModel> prices;
	Map<String, ShopDataModel> shops;


	public ProductListAdapter(@NonNull Context context, int resource, @NonNull ArrayList<ProductDataModel> products, @NonNull Map<String, PriceDataModel> prices, @NonNull Map<String, ShopDataModel> shops) {
		super();
		this.shops = shops;
		this.prices = prices;
		this.context = context;
		this.products = products;
		this.resource = resource;
	}


	@Override
	public int getCount() {
		return products.size();
	}

	@Override
	public Object getItem(int position) {
		return products.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convetView, ViewGroup parent) {
		View view = LayoutInflater.from(context).inflate(resource, parent, false);

		ProductDataModel product = products.get(position);
		((TextView) view.findViewById(R.id.product_name)).setText(product.toString());


		if (product.getId().equals("")) {
			((TextView) view.findViewById(R.id.product_price)).setText("");
			((TextView) view.findViewById(R.id.shop_name)).setText("");
		} else {
			String priceCent = prices.get(product.getId()).getPriceCent();
			String priceEuro = prices.get(product.getId()).getPriceEuro();
			((TextView) view.findViewById(R.id.product_price)).setText(priceEuro + "." + (priceCent.length() == 1 ? "0" + priceCent : priceCent) + "€");
			((TextView) view.findViewById(R.id.shop_name)).setText(shops.get(product.getId()).getShopName());
		}

		return view;
	}
}
