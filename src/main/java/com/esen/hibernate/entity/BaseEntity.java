package com.esen.hibernate.entity;

import java.io.Serializable;

import javax.persistence.GeneratedValue;
import javax.persistence.MappedSuperclass;
import javax.persistence.Version;

import org.hibernate.annotations.GenericGenerator;

/**
 * 提取所有实体的id生成策略,another
 * @author sam
 *
 */
@MappedSuperclass
public class BaseEntity implements Serializable{
	protected String id;
	
	protected long version;

	public void setId(String id) {
		this.id = id;
	}

	@javax.persistence.Id
	@GeneratedValue(generator = "uuid")
	@GenericGenerator(name = "uuid", strategy = "uuid")
	public String getId() {
		return id;
	}
	
	@Version
	public long getVersion() {
		return version;
	}

	public void setVersion(long version) {
		this.version = version;
	}

}
