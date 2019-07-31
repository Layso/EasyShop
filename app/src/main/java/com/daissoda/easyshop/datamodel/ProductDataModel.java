package com.daissoda.easyshop.datamodel;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public class ProductDataModel implements Parcelable {
	private String id;
	private String name;
	private int quantity;
	private String unit;



	public ProductDataModel() {

	}

	public ProductDataModel(String id, String name, int quantity, String unit) {
		this.id = id;
		this.name = name;
		this.quantity = quantity;
		this.unit = unit;
	}


	protected ProductDataModel(Parcel in) {
		id = in.readString();
		name = in.readString();
		quantity = in.readInt();
		unit = in.readString();
	}

	public static final Creator<ProductDataModel> CREATOR = new Creator<ProductDataModel>() {
		@Override
		public ProductDataModel createFromParcel(Parcel in) {
			return new ProductDataModel(in);
		}

		@Override
		public ProductDataModel[] newArray(int size) {
			return new ProductDataModel[size];
		}
	};

	@NonNull
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();

		builder.append(name);

		if (!id.equals("")) {
			builder.append(" - ");
			builder.append(quantity);
			builder.append(unit);
		}

		return builder.toString();
	}


	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getQuantity() {
		return quantity;
	}

	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}

	public String getUnit() {
		return unit;
	}

	public void setUnit(String unit) {
		this.unit = unit;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(id);
		dest.writeString(name);
		dest.writeInt(quantity);
		dest.writeString(unit);
	}
}
