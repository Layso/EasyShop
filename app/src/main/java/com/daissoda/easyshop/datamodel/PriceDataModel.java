package com.daissoda.easyshop.datamodel;

public class PriceDataModel {
	private String id;
	private String abbreviation;
	private String productID;
	private String brandID;
	private String priceEuro;
	private String priceCent;



	public PriceDataModel() {

	}

	public PriceDataModel(String id, String abbreviation, String productID, String brandID, String priceEuro, String priceCent) {
		this.id = id;
		this.productID = productID;
		this.abbreviation = abbreviation;
		this.brandID = brandID;
		this.priceEuro = priceEuro;
		this.priceCent = priceCent;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getAbbreviation() {
		return abbreviation;
	}

	public void setAbbreviation(String abbreviation) {
		this.abbreviation = abbreviation;
	}

	public String getProductID() {
		return productID;
	}

	public void setProductID(String productID) {
		this.productID = productID;
	}

	public String getBrandID() {
		return brandID;
	}

	public void setBrandID(String brandID) {
		this.brandID = brandID;
	}

	public String getPriceEuro() {
		return priceEuro;
	}

	public void setPriceEuro(String priceEuro) {
		this.priceEuro = priceEuro;
	}

	public String getPriceCent() {
		return priceCent;
	}

	public void setPriceCent(String priceCent) {
		this.priceCent = priceCent;
	}
}
