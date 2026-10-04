package com.swadhyaydata.app.entity;

public class CommunityZipDTO {

    public CommunityZipDTO() {
    }

    public CommunityZipDTO(String communityname, String zipcode) {
        this.communityname = communityname;
        this.zipcode = zipcode;
    }

    private String communityname;

    private String zipcode;

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
}
