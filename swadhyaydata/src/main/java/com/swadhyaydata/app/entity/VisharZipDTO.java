package com.swadhyaydata.app.entity;

public class VisharZipDTO {

    public VisharZipDTO() {
    }

    public VisharZipDTO(String vishtar, String zipcode) {
        this.vishtar = vishtar;
        this.zipcode = zipcode;
    }

    private String vishtar;

    private String zipcode;

    public String getVishtar() {
        return vishtar;
    }

    public void setVishtar(String vishtar) {
        this.vishtar = vishtar;
    }

    public String getZipcode() {
        return zipcode;
    }

    public void setZipcode(String zipcode) {
        this.zipcode = zipcode;
    }
}
