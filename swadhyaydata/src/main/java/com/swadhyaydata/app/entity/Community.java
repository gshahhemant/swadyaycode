package com.swadhyaydata.app.entity;


import jakarta.persistence.*;

@Entity
@Table(name = "communities", schema = "public")
public class Community {

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Auto-generate the primary key value
    private Long id; // Primary key field
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	private String cityname;

    private String communityname;
    
    private String zipcode;

    private String state;

    private String vishtar;

    @Column(name = "swadhyay_kendra_name")
    private String swadhyayKendraName;

	public String getCityname() {
		return cityname;
	}

	public void setCityname(String cityname) {
		this.cityname = cityname;
	}

	public String getCommunityname() {
		return communityname;
	}

	public void setCommunityname(String communityname) {
		this.communityname = communityname;
	}

	public String getZipcode() {
		return zipcode;
	}

	public void setZipcode(String zipcode) {
		this.zipcode = zipcode;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public String getVishtar() {
		return vishtar;
	}

	public void setVishtar(String vishtar) {
		this.vishtar = vishtar;
	}

	public String getSwadhyayKendraName() {
		return swadhyayKendraName;
	}

	public void setSwadhyayKendraName(String swadhyayKendraName) {
		this.swadhyayKendraName = swadhyayKendraName;
	}
    
    
}
