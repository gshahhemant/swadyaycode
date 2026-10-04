package com.swadyay.data.ai.sanantonio.services;

import java.io.Serializable;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class ContactSanAntonioId implements Serializable {

    @Column(name = "property_id", nullable = false)
    private String propertyId;

    @Column(name = "zip", nullable = false)
    private String zip;

    public ContactSanAntonioId() {}

    public ContactSanAntonioId(String propertyId, String zip) {
        this.propertyId = propertyId;
        this.zip = zip;
    }

    public String getPropertyId() { return propertyId; }
    public void setPropertyId(String propertyId) { this.propertyId = propertyId; }

    public String getZip() { return zip; }
    public void setZip(String zip) { this.zip = zip; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ContactSanAntonioId)) return false;
        ContactSanAntonioId that = (ContactSanAntonioId) o;
        return Objects.equals(propertyId, that.propertyId) &&
               Objects.equals(zip, that.zip);
    }

    @Override
    public int hashCode() {
        return Objects.hash(propertyId, zip);
    }
}
