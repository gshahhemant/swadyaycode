package com.swadyay.data.ai.austin.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CommentsAustinEntity {

	@JsonProperty("note")
	private String note;
	@JsonProperty("year")
	private String year;
	@JsonProperty("month")
	private String month;

	public String getNote() {
		return note;
	}

	public void setNote(String note) {
		this.note = note;
	}

	public String getYear() {
		return year;
	}

	public void setYear(String year) {
		this.year = year;
	}

	public String getMonth() {
		return month;
	}

	public void setMonth(String month) {
		this.month = month;
	}

}
