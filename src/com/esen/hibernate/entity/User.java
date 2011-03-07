package com.esen.hibernate.entity;

import javax.persistence.Entity;

@Entity
public class User extends BaseId {
	private String name;

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

}
