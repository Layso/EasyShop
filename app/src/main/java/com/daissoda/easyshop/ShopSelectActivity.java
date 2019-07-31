package com.daissoda.easyshop;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;

import com.daissoda.easyshop.adapter.ShopSelectListAdapter;
import com.daissoda.easyshop.datamodel.PriceDataModel;
import com.daissoda.easyshop.datamodel.ProductDataModel;
import com.daissoda.easyshop.datamodel.ShopDataModel;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShopSelectActivity extends AppCompatActivity {
	private final static String DB_CHILD_PRODUCTS = "products";
	private final static String DB_CHILD_PRICES = "prices";
	private final static String DB_CHILD_SHOPS = "shops";

	private final static int REQUEST_ACCESS_GPS = 1;

	private ArrayList<ProductDataModel> products;
	private ArrayList<ShopDataModel> shops;
	private ArrayList<ShopDataModel> shopsAdapterList;
	private Map<String, Integer> prices;
	private Map<String, Integer> distances;
	private Map<String, Integer> missing;

	private TextView infoText;
	private ListView container;
	private RadioButton radioPrice;
	private RadioButton radioDistance;
	private RadioButton radioInclude;
	private RadioButton radioExclude;

	private boolean priceFirst;
	private boolean includeMissing;
	private Location currentLocation;

	private ShopSelectListAdapter adapter;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_shop_select);

		Bundle bundle = getIntent().getExtras();
		products = bundle.getParcelableArrayList("products");
		Log.e("Size", products.size() + " products");
		shopsAdapterList = new ArrayList<>();
		shops = new ArrayList<>();
		prices = new HashMap<>();
		missing = new HashMap<>();
		distances = new HashMap<>();

		container = findViewById(R.id.list_view);
		adapter = new ShopSelectListAdapter(this, R.layout.shop_selection_item, shopsAdapterList, prices, distances, missing);
		container.setAdapter(adapter);
		container.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Uri mapsIntentUri = Uri.parse("google.navigation:q=" + shopsAdapterList.get(position).getLatitude() + "," + shopsAdapterList.get(position).getLongitude() + "&mode=w");
				Intent mapIntent = new Intent(Intent.ACTION_VIEW, mapsIntentUri);
				startActivity(mapIntent);
			}
		});


		infoText = findViewById(R.id.info_text);

		priceFirst = true;
		radioPrice = findViewById(R.id.radio_price);
		radioPrice.setChecked(priceFirst);
		radioPrice.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				priceFirst = true;
				GenerateShopsList();
			}
		});

		radioDistance = findViewById(R.id.radio_distance);
		radioDistance.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				priceFirst = false;
				GenerateShopsList();
			}
		});

		includeMissing = true;
		radioInclude = findViewById(R.id.radio_include);
		radioInclude.setChecked(includeMissing);
		radioInclude.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				includeMissing = true;
				GenerateShopsList();
			}
		});

		radioExclude = findViewById(R.id.radio_exclude);
		radioExclude.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				includeMissing = false;
				GenerateShopsList();
			}
		});


		CheckLocationPermissions();
	}


	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		if (requestCode == REQUEST_ACCESS_GPS) {
			if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
				GetCurrentLocation();
			} else {
				GetLatestLocation();
			}
		}
	}


	private void CheckLocationPermissions() {
		if (ContextCompat.checkSelfPermission(ShopSelectActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_DENIED) {
			ActivityCompat.requestPermissions(ShopSelectActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_ACCESS_GPS);
		} else {
			GetCurrentLocation();
		}
	}

	@SuppressLint("MissingPermission")
	private void GetCurrentLocation() {
		final LocationManager locationManager = ((LocationManager) getSystemService(Context.LOCATION_SERVICE));
		LocationListener listener = new LocationListener() {
			@Override
			public void onLocationChanged(Location location) {
				locationManager.removeUpdates(this);
				currentLocation = location;
				GetAllShopsInRange();
			}

			@Override
			public void onStatusChanged(String provider, int status, Bundle extras) {

			}

			@Override
			public void onProviderEnabled(String provider) {

			}

			@Override
			public void onProviderDisabled(String provider) {

			}
		};


		locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, listener);
	}


	@SuppressLint("MissingPermission")
	private void GetLatestLocation() {
		LocationManager locationManager = ((LocationManager) getSystemService(Context.LOCATION_SERVICE));
		List<String> providers = locationManager.getProviders(true);
		for(int i=providers.size(); i>=0; ++i) {
			if (locationManager.getLastKnownLocation(providers.get(i)) != null) {
				currentLocation = locationManager.getLastKnownLocation(providers.get(i));
				GetAllShopsInRange();
				return;
			}
		}

		currentLocation = new Location("");
		currentLocation.setLongitude(0);
		currentLocation.setLatitude(0);
		GetAllShopsInRange();
	}


	private void GenerateShopsList() {
		shopsAdapterList.clear();

		SortShops();
		Log.e("Total", Integer.toString(shops.size()));
		for (ShopDataModel shop : shops) {
			if (includeMissing || missing.get(shop.getId()) == 0) {
				shopsAdapterList.add(shop);
			}
		}
		Log.e("Filtered", Integer.toString(shopsAdapterList.size()));

		if (shopsAdapterList.isEmpty()) {
			infoText.setText("No shops found for criterias");
		} else {
			infoText.setVisibility(View.INVISIBLE);
		}

		adapter.notifyDataSetChanged();
	}


	private void SortShops() {
		Collections.sort(shops, new Comparator<ShopDataModel>() {
			@Override
			public int compare(ShopDataModel o1, ShopDataModel o2) {
				if (priceFirst) {
					return prices.get(o1.getId()).compareTo(prices.get(o2.getId()));
				} else {
					return distances.get(o1.getId()).compareTo(distances.get(o2.getId()));
				}
			}
		});
	}

	private void GetAllShopsInRange() {
		FirebaseDatabase.getInstance().getReference().child(DB_CHILD_SHOPS).addListenerForSingleValueEvent(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
				for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
					ShopDataModel currentShop = snapshot.getValue(ShopDataModel.class);
					if (DistanceToShop(currentShop) < 50000) {
						shops.add(currentShop);
					}
				}


				GetProductsFromShops();
			}

			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) {

			}
		});
	}


	private void GetProductsFromShops() {
		FirebaseDatabase.getInstance().getReference().child(DB_CHILD_PRICES).addListenerForSingleValueEvent(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
				for (ShopDataModel shop : shops) {
					ArrayList<PriceDataModel> foundProducts = new ArrayList<>();
					missing.put(shop.getId(), products.size());
					if (dataSnapshot.child(shop.getId()).getValue() != null) {
						for (ProductDataModel product : products) {
							for (DataSnapshot priceSnapShot : dataSnapshot.child(shop.getId()).getChildren()) {
								PriceDataModel itemPrice = priceSnapShot.getValue(PriceDataModel.class);
								if (itemPrice.getProductID().equals(product.getId())) {
									foundProducts.add(itemPrice);
									missing.put(shop.getId(), missing.get(shop.getId())-1);
								}
							}
						}
					}

					// Get sum of all price elements
					int euro = 0, cent =0;
					for (PriceDataModel price : foundProducts) {
						euro += Integer.parseInt(price.getPriceEuro());
						cent += Integer.parseInt(price.getPriceCent());
					}

					// Set distance and price values for shop
					prices.put(shop.getId(), euro*100 + cent);
					distances.put(shop.getId(), DistanceToShop(shop));
				}

				GenerateShopsList();
			}


			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) {

			}
		});
	}


	private int DistanceToShop(ShopDataModel shop) {
		Location shopLocation = new Location("");
		shopLocation.setLatitude(Double.parseDouble(shop.getLatitude()));
		shopLocation.setLongitude(Double.parseDouble(shop.getLongitude()));

		return ((int) currentLocation.distanceTo(shopLocation));
	}
}
