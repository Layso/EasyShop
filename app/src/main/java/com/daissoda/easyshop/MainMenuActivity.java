package com.daissoda.easyshop;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.daissoda.easyshop.adapter.ProductListAdapter;
import com.daissoda.easyshop.datamodel.PriceDataModel;
import com.daissoda.easyshop.datamodel.ProductDataModel;
import com.daissoda.easyshop.datamodel.ShopDataModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainMenuActivity extends AppCompatActivity {
	private final static String DB_CHILD_PRODUCTS = "products";
	private final static String DB_CHILD_PRICES = "prices";
	private final static String DB_CHILD_SHOPS = "shops";
	private final static String DB_CHILD_USERS = "users";

	ListView shopListView;
	ProductListAdapter shopListAdapter;
	ArrayAdapter<ProductDataModel> productAutoCompleteAdapter;
	AutoCompleteTextView searchBox;
	ImageView searchButton;

	ArrayList<ProductDataModel> allProducts;
	ArrayList<ProductDataModel> shopList;
	Map<String, PriceDataModel> shopListPrices;
	Map<String, ShopDataModel> shopListShops;



	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main_menu);


		allProducts = new ArrayList<>();
		shopList = new ArrayList<>();
		shopListPrices = new HashMap<>();
		shopListShops = new HashMap<>();
		searchBox = findViewById(R.id.searchBox);
		searchButton = findViewById(R.id.searchButton);
		shopListView = findViewById(R.id.productList);
		GetAllProducts();


		shopListAdapter = new ProductListAdapter(MainMenuActivity.this, R.layout.product_list_item, shopList, shopListPrices, shopListShops);
		shopListView.setAdapter(shopListAdapter);
		shopListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				ProductDataModel product = shopList.get(position);
				RemoveProductFromDB(product);
				shopList.remove(position);
				shopListAdapter.notifyDataSetChanged();
			}
		});


		productAutoCompleteAdapter = new ArrayAdapter<>(MainMenuActivity.this, android.R.layout.select_dialog_item, allProducts);
		searchBox.setAdapter(productAutoCompleteAdapter);
		searchBox.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				HideKeyboard(MainMenuActivity.this);
				searchBox.setText("");
				AddKnownProductToList(((ProductDataModel) parent.getItemAtPosition(position)), true);
			}
		});
		searchBox.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				searchButton.setImageResource(hasFocus ? android.R.drawable.checkbox_on_background : android.R.drawable.ic_search_category_default);
			}
		});

		searchButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (searchBox.getText().toString().length() != 0) {
					HideKeyboard(MainMenuActivity.this);
					AddKnownProductToList(new ProductDataModel("", searchBox.getText().toString(), 0, ""), true);
					searchBox.setText("");
				}
			}
		});


		View uploadButton = findViewById(R.id.uploadButtonView);
		uploadButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent scanIntent = new Intent(getApplicationContext(), ScanActivity.class);
				startActivityForResult(scanIntent, 0);
			}
		});

		View routeButton = findViewById(R.id.routeButtonView);
		routeButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (shopList.isEmpty()) {
					Toast.makeText(MainMenuActivity.this, "Please add items to search", Toast.LENGTH_SHORT).show();
				} else {
					Intent routeIntent = new Intent(getApplicationContext(), ShopSelectActivity.class);
					Bundle bundle = new Bundle();
					bundle.putParcelableArrayList("products", shopList);
					routeIntent.putExtras(bundle);
					startActivityForResult(routeIntent, 0);
				}
			}
		});

		View newsButton = findViewById(R.id.newsButtonView);
		newsButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent newsIntent = new Intent(getApplicationContext(), NewsActivity.class);
				startActivityForResult(newsIntent, 0);
			}
		});
	}


	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (getCurrentFocus().equals(findViewById(R.id.menu_background))) {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle("Are you sure you want to log out?");

			// Positive button
			builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					FirebaseAuth.getInstance().signOut();
					finish();
				}
			});

			// Negative button
			builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.cancel();
				}
			});

			builder.show();
		} else {
			getCurrentFocus().clearFocus();
		}

		return true;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
		//GetAllProducts();
	}

	private void AddKnownProductToList(final ProductDataModel product, final boolean addToDB) {
		DatabaseReference priceRef = FirebaseDatabase.getInstance().getReference().child(DB_CHILD_PRICES);

		if (shopListPrices.get(product.getId()) != null && shopListShops.get(product.getId()) != null) {
			shopList.add(product);
			shopListAdapter.notifyDataSetChanged();

			if (addToDB) {
				AddProductToDB(product);
			}
		} else {
			priceRef.addListenerForSingleValueEvent(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
				final PriceDataModel minPrice = new PriceDataModel();
				minPrice.setProductID(product.getId());
				String minPriceShopID = "";
				boolean set = false;

				for (DataSnapshot shopSnapshot : dataSnapshot.getChildren()) {
					for (DataSnapshot priceSnapshot : shopSnapshot.getChildren()) {
						PriceDataModel price = priceSnapshot.getValue(PriceDataModel.class);

						if (price.getProductID().equals(product.getId())) {
							if (set) {
								int min = Integer.parseInt(minPrice.getPriceEuro()) * 100 + Integer.parseInt(minPrice.getPriceCent());
								int current = Integer.parseInt(price.getPriceEuro()) * 100 + Integer.parseInt(price.getPriceCent());
								if (current < min) {
									minPrice.setPriceCent(price.getPriceCent());
									minPrice.setPriceEuro(price.getPriceEuro());
									minPriceShopID = shopSnapshot.getKey();
								}
							} else {
								minPrice.setPriceCent(price.getPriceCent());
								minPrice.setPriceEuro(price.getPriceEuro());
								minPriceShopID = shopSnapshot.getKey();
								set = true;
							}
						}
					}
				}

				final boolean finalSet = set;
				FirebaseDatabase.getInstance().getReference().child(DB_CHILD_SHOPS).child(minPriceShopID).addListenerForSingleValueEvent(new ValueEventListener() {
					@Override
					public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
						ShopDataModel minPriceShop = dataSnapshot.getValue(ShopDataModel.class);

						shopList.add(product);

						if (addToDB)
							AddProductToDB(product);

						if (finalSet) {
							shopListPrices.put(product.getId(), minPrice);
							shopListShops.put(product.getId(), minPriceShop);
						}

						shopListAdapter.notifyDataSetChanged();
					}

					@Override
					public void onCancelled(@NonNull DatabaseError databaseError) {

					}
				});
			}

			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) {

			}
		});
		}
	}

	private void GetShopListFromDB() {
		String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
		FirebaseDatabase.getInstance().getReference().child(DB_CHILD_USERS).child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

				for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
					ProductDataModel product = snapshot.getValue(ProductDataModel.class);
					shopList.clear();
					AddKnownProductToList(product, false);
				}
			}

			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) {

			}
		});
	}

	private void AddProductToDB(ProductDataModel product) {
		String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
		FirebaseDatabase.getInstance().getReference().child(DB_CHILD_USERS).child(uid).push().setValue(product);
	}

	private void RemoveProductFromDB(final ProductDataModel product) {
		final String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
		FirebaseDatabase.getInstance().getReference().child(DB_CHILD_USERS).child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
				for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
					ProductDataModel dbProduct = snapshot.getValue(ProductDataModel.class);
					if (product.getId().equals("") ? dbProduct.getName().equals(product.getName()) : dbProduct.getId().equals(product.getId())) {
						FirebaseDatabase.getInstance().getReference().child(DB_CHILD_USERS).child(uid).child(snapshot.getKey()).removeValue();
						break;
					}
				}
			}

			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) {

			}
		});

		Log.e("Remove Product: ", product.toString());
	}


	private void GetAllProducts() {
		DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
		rootRef.child(DB_CHILD_PRODUCTS).addListenerForSingleValueEvent(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
				allProducts.clear();
				for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
					ProductDataModel product = snapshot.getValue(ProductDataModel.class);
					allProducts.add(product);
				}

				productAutoCompleteAdapter.notifyDataSetChanged();
				GetShopListFromDB();
			}

			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) {

			}
		});
	}


	/**
	 * Taken from: https://stackoverflow.com/questions/1109022/close-hide-the-android-soft-keyboard
	 */
	private void HideKeyboard(Activity activity) {
		InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
		//Find the currently focused view, so we can grab the correct window token from it.
		View view = activity.getCurrentFocus();
		//If no view currently has focus, create a new one, just so we can grab a window token from it
		if (view == null) {
			view = new View(activity);
		}
		imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
		searchBox.clearFocus();
	}
}
