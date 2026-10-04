package com.swadyay.data.ai.sanantonio.services;

import java.time.LocalDateTime;
import java.util.List;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;




@Entity
@Table(name = "contacts_sanantonio", schema = "public")
public class Contact {

    @EmbeddedId
    private ContactSanAntonioId id;

    private String name;

    private String property_address;
    
    private String community_name;
    
    private String details_url;
    
    
    @Column(columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private List<Comments> comments;
    
    
  private LocalDateTime updated_datetime;
    
    private LocalDateTime created_datetime;
    
    private String user_id;
    
    private String home_stead;

    // Getters and setters
    
    
    

    public String getCommunity_name() {
		return community_name;
	}

	public String getHome_stead() {
		return home_stead;
	}

	public void setHome_stead(String home_stead) {
		this.home_stead = home_stead;
	}

	public LocalDateTime getUpdated_datetime() {
		return updated_datetime;
	}

	public void setUpdated_datetime(LocalDateTime updated_datetime) {
		this.updated_datetime = updated_datetime;
	}

	public LocalDateTime getCreated_datetime() {
		return created_datetime;
	}

	public void setCreated_datetime(LocalDateTime created_datetime) {
		this.created_datetime = created_datetime;
	}

	public String getUser_id() {
		return user_id;
	}

	public void setUser_id(String user_id) {
		this.user_id = user_id;
	}

	public ContactSanAntonioId getId() {
		return id;
	}

	public void setId(ContactSanAntonioId id) {
		this.id = id;
	}

	public String getZip() {
		return id == null ? null : id.getZip();
	}

	public void setZip(String zip) {
		if (id == null) {
			id = new ContactSanAntonioId();
		}
		id.setZip(zip);
	}

	

	public List<Comments> getComments() {
		return comments;
	}

	public void setComments(List<Comments> comments) {
		this.comments = comments;
	}

	public void setCommunity_name(String community_name) {
		this.community_name = community_name;
	}

	public String getProperty_id() {
        return id == null ? null : id.getPropertyId();
    }

    public void setProperty_id(String property_id) {
        if (id == null) {
            id = new ContactSanAntonioId();
        }
        id.setPropertyId(property_id);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProperty_address() {
        return property_address;
    }

    public void setProperty_address(String property_address) {
        this.property_address = property_address;
    }

	public String getDetails_url() {
		return details_url;
	}

	public void setDetails_url(String details_url) {
		this.details_url = details_url;
	}
    
    
}
