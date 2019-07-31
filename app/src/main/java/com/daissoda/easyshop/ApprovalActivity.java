package com.daissoda.easyshop;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.daissoda.easyshop.datamodel.PriceDataModel;
import com.daissoda.easyshop.datamodel.ProductDataModel;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.Text;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ApprovalActivity extends AppCompatActivity {
	private static final int INTENT_TAKE_PICTURE = 1;
	private static final int INTENT_CROP_IMAGE = 2;

	private static final String DB_CHILD_PRICES = "prices";
	private static final String DB_CHILD_BRANDS = "brands";
	private static final String DB_CHILD_PRODUCTS = "products";

	private File imageFile;
	private Uri imageUri;


	private DataSnapshot prices;
	private DataSnapshot brands;
	private DataSnapshot products;
	private Map<String, String> analyzeResults;
	private String selectedShopID;


	private boolean pictureAnalyzed;
	private boolean pricesReceived;
	private boolean brandsReceived;
	private boolean productsReceived;


	private Button addButton;
	private Button saveButton;
	private Button rescanButton;
	private LinearLayout listContainer;



	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_approval);
		DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();

		analyzeResults = new HashMap<>();
		pricesReceived = false;
		brandsReceived = false;
		productsReceived = false;
		pictureAnalyzed = false;
		selectedShopID = getIntent().getStringExtra("shop");
		rootRef.child(DB_CHILD_PRICES).child(selectedShopID).addListenerForSingleValueEvent(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
				prices = dataSnapshot;
				pricesReceived = true;

				if (pricesReceived && brandsReceived && productsReceived && pictureAnalyzed) {
					PrepareListLayout();
				}
			}

			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) {
				Toast.makeText(ApprovalActivity.this, "Error on getting prices", Toast.LENGTH_SHORT).show();
			}
		});


		rootRef.child(DB_CHILD_PRODUCTS).addListenerForSingleValueEvent(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
				products = dataSnapshot;
				productsReceived = true;

				if (pricesReceived && brandsReceived && productsReceived && pictureAnalyzed) {
					PrepareListLayout();
				}
			}

			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) {
				Toast.makeText(ApprovalActivity.this, "Error on getting products", Toast.LENGTH_SHORT).show();
			}
		});


		rootRef.child(DB_CHILD_BRANDS).addListenerForSingleValueEvent(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
				brands = dataSnapshot;
				brandsReceived = true;

				if (pricesReceived && brandsReceived && productsReceived && pictureAnalyzed) {
					PrepareListLayout();
				}
			}

			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) {
				Toast.makeText(ApprovalActivity.this, "Error on getting brands", Toast.LENGTH_SHORT).show();
			}
		});


		listContainer = findViewById(R.id.list_container);
		addButton = findViewById(R.id.add_item_button);
		addButton.setEnabled(false);
		addButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				LayoutInflater inflater = LayoutInflater.from(ApprovalActivity.this);
				View rowView = inflater.inflate(R.layout.bill_list_item, listContainer, false);
				PrepareRow(rowView, null, null, null, null);
				listContainer.addView(rowView);
			}
		});

		saveButton = findViewById(R.id.save_button);
		saveButton.setEnabled(false);
		saveButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				boolean ready = true;
				for (int i=0; i<listContainer.getChildCount(); ++i) {
					if (listContainer.getChildAt(i).isEnabled()) {
						ready = false;
						break;
					}
				}


				if (listContainer.getChildCount() == 0) {
					Toast.makeText(ApprovalActivity.this, "Please add items", Toast.LENGTH_SHORT).show();
				} else if (ready) {
					SaveItems();
				} else {
					Toast.makeText(ApprovalActivity.this, "Please save or delete unsaved items", Toast.LENGTH_SHORT).show();
				}
			}
		});


		rescanButton = findViewById(R.id.rescan_button);
		rescanButton.setEnabled(false);
		rescanButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				TakePicture();
			}
		});


		TakePicture();
	}


	@Override
	protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
		if (requestCode == INTENT_TAKE_PICTURE) {
			if (resultCode == RESULT_OK) {
				CropPicture();
			} else {
				finish();
			}
		} else if (requestCode == INTENT_CROP_IMAGE) {
			if (resultCode == RESULT_OK) {
				if (data != null) {
					//try {
						IncreaseContrast(((Bitmap) data.getExtras().getParcelable("data")));
					/*} catch (Exception e) {
						Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
						Log.e("Except-208", e.getMessage());
					}*/
				} else {
					Toast.makeText(this, "Data null", Toast.LENGTH_SHORT).show();
				}
			} else {
				TakePicture();
			}
		}
	}


	private void SaveItems() {
		final DatabaseReference rootReference = FirebaseDatabase.getInstance().getReference();

		rootReference.child(DB_CHILD_PRICES).child(selectedShopID).addListenerForSingleValueEvent(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
				for (int i=0; i<listContainer.getChildCount(); ++i) {
					final EditText priceIDField = listContainer.getChildAt(i).findViewById(R.id.productShopID);
					final EditText priceEuroField = listContainer.getChildAt(i).findViewById(R.id.priceEuro);
					final EditText priceCentField = listContainer.getChildAt(i).findViewById(R.id.priceCent);
					final EditText brandNameField = listContainer.getChildAt(i).findViewById(R.id.brandName);
					final EditText productUnitField = listContainer.getChildAt(i).findViewById(R.id.unitName);
					final EditText productQuantityField = listContainer.getChildAt(i).findViewById(R.id.quantityCount);
					final EditText productNameField = listContainer.getChildAt(i).findViewById(R.id.product_name);

					boolean set = false;
					for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
						PriceDataModel oldPrice = snapshot.getValue(PriceDataModel.class);
						if (oldPrice.getAbbreviation().equals(priceIDField.getText().toString())) {
							oldPrice.setPriceEuro(priceEuroField.getText().toString());
							oldPrice.setPriceCent(priceCentField.getText().toString());
							rootReference.child(DB_CHILD_PRICES).child(selectedShopID).child(oldPrice.getId()).setValue(oldPrice);
							set = true;
						}
					}

					if (!set) {
						rootReference.child(DB_CHILD_PRODUCTS).addListenerForSingleValueEvent(new ValueEventListener() {
							@Override
							public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
								String productID = "";
								for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
									ProductDataModel tempProduct = snapshot.getValue(ProductDataModel.class);
									if (tempProduct.getName().equals(productNameField.getText().toString()) &&
											tempProduct.getQuantity() == Integer.parseInt(productQuantityField.getText().toString()) &&
											tempProduct.getUnit().equals(productUnitField.getText().toString())) {
										productID = snapshot.getValue(ProductDataModel.class).getId();
										break;
									}
								}

								if (productID.equals("")) {
									productID = rootReference.child(DB_CHILD_PRODUCTS).push().getKey();
									rootReference.child(DB_CHILD_PRODUCTS).child(productID).setValue(new ProductDataModel(productID,
											productNameField.getText().toString(),
											Integer.parseInt(productQuantityField.getText().toString()),
											productUnitField.getText().toString()));
								}

								final ProductDataModel product = new ProductDataModel(productID,
										productNameField.getText().toString(),
										Integer.parseInt(productQuantityField.getText().toString()),
										productUnitField.getText().toString());

								rootReference.child(DB_CHILD_BRANDS).addListenerForSingleValueEvent(new ValueEventListener() {
									@Override
									public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
										String brandID = "";

										for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
											if (snapshot.getValue(String.class).equals(brandNameField.getText().toString())) {
												brandID = snapshot.getKey();
												break;
											}
										}

										if (brandID.equals("")) {
											brandID = rootReference.child(DB_CHILD_BRANDS).push().getKey();
											rootReference.child(DB_CHILD_BRANDS).child(brandID).setValue(brandNameField.getText().toString());
										}


										PriceDataModel newPrice = new PriceDataModel(rootReference.child(DB_CHILD_PRICES).child(selectedShopID).push().getKey(),
												priceIDField.getText().toString(),
												product.getId(),
												brandID,
												priceEuroField.getText().toString(),
												priceCentField.getText().toString());
										rootReference.child(DB_CHILD_PRICES).child(selectedShopID).child(newPrice.getId()).setValue(newPrice);
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


					setResult(RESULT_OK);
					finish();
				}
			}

			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) {

			}
		});
	}


	private void TakePicture() {
		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		Boolean isSDPresent = android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);
		Boolean isSDSupportedDevice = Environment.isExternalStorageRemovable();

		Log.e("present", Boolean.toString(isSDPresent));
		Log.e("supported", Boolean.toString(isSDSupportedDevice));

		imageFile = new File(Environment.getExternalStorageDirectory(), "file" + System.currentTimeMillis() + ".jpg");
		imageUri = Uri.fromFile(imageFile);
		intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
		intent.putExtra("return-data", true);
		startActivityForResult(intent, INTENT_TAKE_PICTURE);


		try {
			imageFile = new File(getCacheDir(), "file" + System.currentTimeMillis() + ".jpg");
		} catch (Exception e) {
			Log.e("Except", e.getMessage());
		}
	}


	private void CropPicture() {
		Intent intent = new Intent("com.android.camera.action.CROP");
		intent.setDataAndType(imageUri, "image/*");
		intent.putExtra("crop", true);
		intent.putExtra("scaleUpIfNeeded", true);
		intent.putExtra("return-data", true);
		startActivityForResult(intent, INTENT_CROP_IMAGE);
	}


	public void IncreaseContrast(Bitmap bitmap) {
		int width = bitmap.getWidth();
		int height = bitmap.getHeight();
		Bitmap highContrastBitmap = Bitmap.createBitmap(width, height, bitmap.getConfig());
		double contrast = Math.pow((100 + 100) / 100, 2);


		// For each pixel increase contrast
		for (int i=0; i<width; ++i) {
			for (int j=0; j<height; ++j) {
				// Get color values of the pixel
				int pixel = bitmap.getPixel(i, j);
				int red = Color.red(pixel);
				int green = Color.green(pixel);
				int blue = Color.blue(pixel);

				// Set new pixel value
				red = (int)(((((red / 255.0) - 0.5) * contrast) + 0.5) * 255.0);
				green = (int)(((((green / 255.0) - 0.5) * contrast) + 0.5) * 255.0);
				blue = (int)(((((blue / 255.0) - 0.5) * contrast) + 0.5) * 255.0);

				// Clamp if color is not between 0 and 255
				red = red < 0 ? 0 : (red > 255 ? 255 : red);
				green = green < 0 ? 0 : (green > 255 ? 255 : green);
				blue = blue < 0 ? 0 : (blue > 255 ? 255 : blue);

				// Set pixel in output bitmap
				highContrastBitmap.setPixel(i, j, Color.argb(Color.alpha(pixel), red, green, blue));
			}
		}

		AnalyzePicture(highContrastBitmap);
	}


	private void AnalyzePicture(Bitmap bitmap) {
		TextRecognizer recognizer = new TextRecognizer.Builder(getApplicationContext()).build();
		if (!recognizer.isOperational()) {
			Toast.makeText(this, "Recognizer is not operational", Toast.LENGTH_SHORT).show();
		} else {
			Frame frame = new Frame.Builder().setBitmap(bitmap).build();
			SparseArray<TextBlock> items = recognizer.detect(frame);
			ArrayList<Text> texts = new ArrayList<>();


			Log.e("items.size()", Integer.toString(items.size()));
			for (int i=0; i<items.size(); ++i) {
				TextBlock block = items.valueAt(i);

				//Log.e("block.size()", Integer.toString(block.getComponents().size()));
				for (Text text : block.getComponents()) {
					texts.add(text);
					Log.e("blck", text.getValue());
				}
			}


			analyzeResults = GetPriceNameCombination(texts);
			pictureAnalyzed = true;
			Log.e("Analyze result count: ", Integer.toString(analyzeResults.size()));
			Toast.makeText(this, analyzeResults.size() + " items found", Toast.LENGTH_SHORT).show();
			if (pricesReceived && brandsReceived && productsReceived && pictureAnalyzed) {
				PrepareListLayout();
			}
		}
	}


	private Map<String, String> GetPriceNameCombination(ArrayList<Text> list) {
		ArrayList<String> prices = new ArrayList<>();
		ArrayList<Integer> priceIndexes = new ArrayList<>();
		Map<String, String> combination = new HashMap<>();

		// For each word block see if it is a price tag
		for (int i=0; i<list.size()-1; ++i) {
			String string = list.get(i).getValue();

			/* To determine price tag the static part is a separator and 2 numbers after it.
			   If any match found save the textBlock. Ignore currency sign if exists
			*/
			for (int j=0; j<string.length()-1; ++j) {
				// If separator has found check next 2 characters
				if (string.charAt(j) == '.' || string.charAt(j) == ',') {
					// If currency sign doesn't exists there are 2 characters to check
					if (string.length() - j == 3) {
						if (IsNumber(string.charAt(j+1)) && IsNumber(string.charAt(j+2))) {
							prices.add(string);
							priceIndexes.add(i);
							break;
						}
					}

					// If the currency sign exists, there are 3 characters to check (ignore last)
					else if (string.length() - j == 4) {
						if (IsNumber(string.charAt(j+1)) && IsNumber(string.charAt(j+2)) && !IsNumber(string.charAt(j+3))) {
							String cleanString = string.substring(0, string.length()-2);
							prices.add(cleanString);
							priceIndexes.add(i);
							break;
						}
					}
				}

				// If there is a non-number character exists continue with next textBlock
				else if (!IsNumber(string.charAt(j))) {
					break;
				}
			}
		}


		// For each price tag found, match them to closest textBlock on Y axis
		for (int index : priceIndexes) {
			Text text = list.get(index);
			int minDistance = Math.abs(text.getBoundingBox().centerY() - list.get(index == 0 ? 1 : 0).getBoundingBox().centerY());
			int minDistanceIndex = index == 0 ? 1 : 0;

			// Linear search to find minimum distance
			for (int i=0; i<list.size(); ++i) {
				if (i != index && minDistance > Math.abs(list.get(i).getBoundingBox().centerY() - text.getBoundingBox().centerY())) {
					minDistance = Math.abs(list.get(i).getBoundingBox().centerY() - text.getBoundingBox().centerY());
					minDistanceIndex = i;
				}
			}

			// Check if already exists
			boolean exists = false;
			for (String key : combination.keySet()) {
				if (key.equals(list.get(minDistanceIndex).getValue())) {
					exists = true;
				}
			}

			// Insert final result as a combination
			if (!exists)
				combination.put(list.get(minDistanceIndex).getValue(), text.getValue());
		}


		return combination;
	}


	private boolean IsNumber(char c) {
		return (c >= '0' && c <= '9');
	}


	private void PrepareListLayout() {
		PriceDataModel info = null;
		ProductDataModel product = null;
		String brand = null;


		for (String key : analyzeResults.keySet()) {
			for (DataSnapshot snapshot : prices.getChildren()) {
				try {
					info = snapshot.getValue(PriceDataModel.class);
					if (info.getAbbreviation().equals(key)) {
						product = products.child(info.getProductID()).getValue(ProductDataModel.class);
						brand = brands.child(info.getBrandID()).getValue(String.class);
						break;
					}
				} catch (Exception e) {
					Log.e("exc-523", info.getId());
				}
			}


			LayoutInflater inflater = LayoutInflater.from(ApprovalActivity.this);
			View rowView = inflater.inflate(R.layout.bill_list_item, listContainer, false);
			PrepareRow(rowView, key, analyzeResults.get(key), product, brand);
			listContainer.addView(rowView);
		}

		addButton.setEnabled(true);
		saveButton.setEnabled(true);
		rescanButton.setEnabled(true);
	}


	private void PrepareRow(final View view, String priceID, String newPrice, ProductDataModel product, String brand) {
		EditText priceIDField = view.findViewById(R.id.productShopID);
		EditText priceEuroField = view.findViewById(R.id.priceEuro);
		EditText priceCentField = view.findViewById(R.id.priceCent);
		final EditText brandNameField = view.findViewById(R.id.brandName);
		final EditText productUnitField = view.findViewById(R.id.unitName);
		final EditText productQuantityField = view.findViewById(R.id.quantityCount);
		final EditText productNameField = view.findViewById(R.id.product_name);
		final Button editSaveButton = view.findViewById(R.id.editSaveButton);
		Button deleteButton = view.findViewById(R.id.deleteButton);
		final View background = view.findViewById(R.id.background);
		final EditText[] inputFields = {priceIDField, priceEuroField, priceCentField, brandNameField, productNameField, productQuantityField, productUnitField};

		if (priceID != null && newPrice != null) {
			String[] price = newPrice.split(",|\\.");
			priceIDField.setText(priceID);
			priceEuroField.setText(price[0]);
			priceCentField.setText(price[1]);
		}

		if (product != null && brand != null) {
			brandNameField.setText(brand);
			productUnitField.setText(product.getUnit());
			productQuantityField.setText(Integer.toString(product.getQuantity()));
			productNameField.setText(product.getName());
			view.setEnabled(false);
			DisableFields(inputFields);
			background.setBackgroundColor(Color.parseColor("#BF00FF00"));
			editSaveButton.setBackgroundResource(android.R.drawable.ic_menu_edit);
		}

		deleteButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				listContainer.removeView(view);
			}
		});

		editSaveButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (view.isEnabled()) {
					if (HasEmptyFields(inputFields)) {
						Toast.makeText(ApprovalActivity.this, "Please fill the fields", Toast.LENGTH_SHORT).show();
					} else {
						view.setEnabled(false);
						DisableFields(inputFields);
						editSaveButton.setBackgroundResource(android.R.drawable.ic_menu_edit);
						background.setBackgroundColor(Color.parseColor("#BF00FF00"));
					}
				} else {
					view.setEnabled(true);
					EnableFields(inputFields);
					editSaveButton.setBackgroundResource(android.R.drawable.ic_menu_save);
					background.setBackgroundColor(Color.parseColor("#BFFF0000"));
				}
			}
		});

		priceIDField.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {

			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				for (DataSnapshot snapshot : prices.getChildren()) {
					PriceDataModel priceModel = snapshot.getValue(PriceDataModel.class);
					if (priceModel.getAbbreviation().equals(s.toString())) {
						ProductDataModel product = products.child(priceModel.getProductID()).getValue(ProductDataModel.class);
						String brand = brands.child(priceModel.getBrandID()).getValue(String.class);

						productNameField.setText(product.getName());
						productQuantityField.setText(Integer.toString(product.getQuantity()));
						productUnitField.setText(product.getUnit());
						brandNameField.setText(brand);
					}
				}
			}

			@Override
			public void afterTextChanged(Editable s) {

			}
		});
	}


	private boolean HasEmptyFields(EditText[] fields) {
		for (EditText field : fields) {
			if (field.getText().toString().equals("")) {
				return true;
			}
		}

		return false;
	}


	private void DisableFields(EditText[] fields) {
		for (EditText field : fields) {
			field.setEnabled(false);
		}
	}


	private void EnableFields(EditText[] fields) {
		for (EditText field : fields) {
			field.setEnabled(true);
		}
	}
}
