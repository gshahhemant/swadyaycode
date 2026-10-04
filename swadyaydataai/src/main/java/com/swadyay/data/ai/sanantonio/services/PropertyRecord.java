package com.swadyay.data.ai.sanantonio.services;

public class PropertyRecord {
    private String propertyId;
    private String geographicId;
    private String type;
    private String propertyAddress;
    private String legalDescription;
    private String ownerName;
    private String doingBusinessAs;
    private String city;
    private String indianName;
    private String zip;
    
    
    
	public String getZip() {
		return zip;
	}
	public void setZip(String zip) {
		this.zip = zip;
	}
	public String getIndianName() {
		return indianName;
	}
	public void setIndianName(String indianName) {
		this.indianName = indianName;
	}
	public String getCity() {
		return city;
	}
	public void setCity(String city) {
		this.city = city;
	}
	public String getPropertyId() {
		return propertyId;
	}
	public void setPropertyId(String propertyId) {
		this.propertyId = propertyId;
	}
	public String getGeographicId() {
		return geographicId;
	}
	public void setGeographicId(String geographicId) {
		this.geographicId = geographicId;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getPropertyAddress() {
		return propertyAddress;
	}
	public void setPropertyAddress(String propertyAddress) {
		this.propertyAddress = propertyAddress;
	}
	public String getLegalDescription() {
		return legalDescription;
	}
	public void setLegalDescription(String legalDescription) {
		this.legalDescription = legalDescription;
	}
	public String getOwnerName() {
		return ownerName;
	}
	public void setOwnerName(String ownerName) {
		this.ownerName = ownerName;
	}
	public String getDoingBusinessAs() {
		return doingBusinessAs;
	}
	public void setDoingBusinessAs(String doingBusinessAs) {
		this.doingBusinessAs = doingBusinessAs;
	}
	@Override
	public String toString() {
		return "PropertyRecord [propertyId=" + propertyId + ", geographicId=" + geographicId + ", type=" + type
				+ ", propertyAddress=" + propertyAddress + ", legalDescription=" + legalDescription + ", ownerName="
				+ ownerName + ", doingBusinessAs=" + doingBusinessAs + "]";
	}
	

    
}
