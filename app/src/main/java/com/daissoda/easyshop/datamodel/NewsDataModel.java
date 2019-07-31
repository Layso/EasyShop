package com.daissoda.easyshop.datamodel;

public class NewsDataModel {
	private String info;
	private String shopID;


	public NewsDataModel()  {

	}

	public NewsDataModel(String info, String shopID) {
		this.info = info;
		this.shopID = shopID;
	}


	public String getInfo() {
		return info;
	}

	public void setInfo(String info) {
		this.info = info;
	}

	public String getShopID() {
		return shopID;
	}

	public void setShopID(String shop) {
		this.shopID = shop;
	}
}
