package com.daissoda.easyshop.datamodel;

import java.io.Serializable;

public class ShopDataModel implements Serializable {
	String id;
	String shopName;
	String subLocation;
	String city;
	String latitude;
	String longitude;



	public ShopDataModel() {

	}

	public ShopDataModel(String id, String shopName, String subLocation, String city, String latitude, String longitude) {
		this.id = id;
		this.shopName = shopName;
		this.subLocation = subLocation;
		this.city = city;
		this.latitude = latitude;
		this.longitude = longitude;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getShopName() {
		return shopName;
	}

	public void setShopName(String shopName) {
		this.shopName = shopName;
	}

	public String getSubLocation() {
		return subLocation;
	}

	public void setSubLocation(String subLocation) {
		this.subLocation = subLocation;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getLatitude() {
		return latitude;
	}

	public void setLatitude(String latitude) {
		this.latitude = latitude;
	}

	public String getLongitude() {
		return longitude;
	}

	public void setLongitude(String longitude) {
		this.longitude = longitude;
	}
}
