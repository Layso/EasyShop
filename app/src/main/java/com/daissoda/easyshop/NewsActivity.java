package com.daissoda.easyshop;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;

import com.daissoda.easyshop.adapter.AnnouncementListAdapater;
import com.daissoda.easyshop.datamodel.NewsDataModel;
import com.daissoda.easyshop.datamodel.ShopDataModel;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class NewsActivity extends AppCompatActivity {
	private static final String DB_CHILD_NEWS = "news";
	private static final String DB_CHILD_SHOPS = "shops";

	ArrayList<NewsDataModel> news;
	HashMap<String, ShopDataModel> shops;
	AnnouncementListAdapater adapter;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_news);


		news = new ArrayList<>();
		shops = new HashMap<>();
		adapter = new AnnouncementListAdapater(getApplicationContext(), R.layout.announcement_list_item, news, shops);
		((ListView) findViewById(R.id.announcement_list)).setAdapter(adapter);


		GetNews();
	}

	private void GetNews() {
		DatabaseReference newsRef = FirebaseDatabase.getInstance().getReference().child(DB_CHILD_NEWS);

		newsRef.addChildEventListener(new ChildEventListener() {
			@Override
			public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
				final NewsDataModel announcement =  dataSnapshot.getValue(NewsDataModel.class);
				news.add(0, announcement);

				FirebaseDatabase.getInstance().getReference().child(DB_CHILD_SHOPS).child(announcement.getShopID()).addListenerForSingleValueEvent(new ValueEventListener() {
					@Override
					public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
						ShopDataModel shop = dataSnapshot.getValue(ShopDataModel.class);
						shops.put(announcement.getShopID(), shop);
						adapter.notifyDataSetChanged();
					}

					@Override
					public void onCancelled(@NonNull DatabaseError databaseError) {

					}
				});
			}

			@Override
			public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

			}

			@Override
			public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

			}

			@Override
			public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

			}

			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) {

			}
		});
	}
}
