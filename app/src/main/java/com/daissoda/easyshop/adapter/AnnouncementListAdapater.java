package com.daissoda.easyshop.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.daissoda.easyshop.R;
import com.daissoda.easyshop.datamodel.NewsDataModel;
import com.daissoda.easyshop.datamodel.ShopDataModel;

import java.util.ArrayList;
import java.util.HashMap;



public class AnnouncementListAdapater extends BaseAdapter {
	int resource;
	Context context;
	ArrayList<NewsDataModel> news;
	HashMap<String, ShopDataModel> shops;


	public AnnouncementListAdapater(@NonNull Context context, int resource, @NonNull ArrayList<NewsDataModel> news, @NonNull HashMap<String, ShopDataModel>  shops) {
		super();
		this.resource = resource;
		this.context = context;
		this.shops = shops;
		this.news = news;
	}


	@Override
	public int getCount() {
		return news.size();
	}

	@Override
	public Object getItem(int position) {
		return news.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = LayoutInflater.from(context).inflate(resource, parent, false);
		NewsDataModel announcement = news.get(position);
		ShopDataModel shop = shops.get(announcement.getShopID());

		((TextView) view.findViewById(R.id.announcement_text)).setText(announcement.getInfo());

		if (shop != null)
			((TextView) view.findViewById(R.id.owner_text)).setText(shop.getShopName() + " - "  + shop.getCity() + ", " + shop.getSubLocation());

		return view;
	}
}
