package com.daissoda.easyshop;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.daissoda.easyshop.adapter.ShopListAdapter;
import com.daissoda.easyshop.datamodel.ShopDataModel;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ScanActivity extends AppCompatActivity {
	private static final int REQUEST_TAKE_PHOTO = 1;
	private static final int REQUEST_ACCESS_GPS = 2;
	private static final int INTENT_MAP_SELECT = 1;
	private static final int INTENT_SCAN_AND_APPROVE = 2;

	private Location currentLocation;
	private ArrayList<ShopDataModel> shops;
	private ShopDataModel selectedShop;
	private ShopListAdapter adapter;
	private ListView list;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_scan);


		// Set list and adapter
		shops = new ArrayList<>();
		list = findViewById(R.id.my_list);
		adapter = new ShopListAdapter(this, R.layout.shop_list_item, shops);
		list.setAdapter(adapter);
		list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				selectedShop = shops.get(position);
				if (ContextCompat.checkSelfPermission(ScanActivity.this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED) {
					ActivityCompat.requestPermissions(ScanActivity.this, new String[]{Manifest.permission.CAMERA}, REQUEST_TAKE_PHOTO);
				} else {
					ScanBill();
				}
			}
		});


		findViewById(R.id.add_shop_button).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent mapIntent = new Intent(getApplicationContext(), MapsActivity.class);
				startActivityForResult(mapIntent, INTENT_MAP_SELECT);
			}
		});


		CheckLocationPermissions();
	}

	private void CheckLocationPermissions() {
		if (ContextCompat.checkSelfPermission(ScanActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_DENIED) {
			ActivityCompat.requestPermissions(ScanActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_ACCESS_GPS);
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
				GetShopList();
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

	public void GetShopList() {
		final String DB_CHILD_SHOPS = "shops";


		DatabaseReference rootReference = FirebaseDatabase.getInstance().getReference();
		rootReference.child(DB_CHILD_SHOPS).addValueEventListener(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
				shops.clear();
				for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
					ShopDataModel currentShop = snapshot.getValue(ShopDataModel.class);
					if (DistanceToShop(currentShop) < 50000) {
						shops.add(currentShop);
					}
				}

				adapter.notifyDataSetChanged();
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





	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		if (requestCode == REQUEST_TAKE_PHOTO) {
			if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
				ScanBill();
			} else {
				finish();
			}
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
		if (requestCode == INTENT_MAP_SELECT) {
			if (resultCode == RESULT_OK) {
				CreateNewShop(data);
			}
		}

		else if (requestCode == INTENT_SCAN_AND_APPROVE) {
			if (resultCode == RESULT_OK) {
				finish();
			}
		}
	}


	private void CreateNewShop(Intent data) {
		final String DB_CHILD_SHOPS = "shops";

		// Parse longitude and latitude taken from Maps Activity
		String[] latlong =  data.getData().toString().split(",");
		final double latitude = Double.parseDouble(latlong[0]);
		final double longitude = Double.parseDouble(latlong[1]);

		try {
			// Decode the address using latitude and longitude
			Geocoder gcd = new Geocoder(getApplicationContext(), Locale.getDefault());
			final List<Address> addressList = gcd.getFromLocation(latitude, longitude, 1);
			if (addressList != null && addressList.size() > 0) {
				// Create an alert dialog for getting shop name
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setTitle("Enter the name of the shop");
				final EditText input = new EditText(this);
				input.setInputType(InputType.TYPE_CLASS_TEXT);
				builder.setView(input);

				// Positive button
				builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						Address address = addressList.get(0);
						String shopName = input.getText().toString();
						String newID = FirebaseDatabase.getInstance().getReference().child(DB_CHILD_SHOPS).push().getKey();
						ShopDataModel newShop = new ShopDataModel(newID, shopName, address.getLocality()==null ? address.getSubAdminArea() : address.getLocality(), address.getAdminArea(), Double.toString(latitude), Double.toString(longitude));
						FirebaseDatabase.getInstance()
								.getReference()
								.child(DB_CHILD_SHOPS)
								.child(newID)
								.setValue(newShop);
					}
				});

				// Negative button
				builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.cancel();
					}
				});

				builder.show();
			} else {
				Toast.makeText(this, "Failed to get address: LIST_EMPTY", Toast.LENGTH_SHORT);
			}
		} catch (Exception e) {
			Toast.makeText(this, "Failed to get address: EXCEPTION_" + e.getClass(), Toast.LENGTH_SHORT).show();
			Log.e("Except", e.getMessage());
		}
	}

	private void ScanBill() {
		Intent scanAndApproveIntent = new Intent(getApplicationContext(), ApprovalActivity.class);
		scanAndApproveIntent.putExtra("shop", selectedShop.getId());
		startActivityForResult(scanAndApproveIntent, INTENT_SCAN_AND_APPROVE);
	}
}
