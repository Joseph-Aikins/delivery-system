package io.deliverysystem.data;

import androidx.annotation.NonNull;

public class Receipt implements Model {
	private String name, uid;
	private long timestamp;
	
	public Receipt() {
	}
	
	public Receipt(String name, String uid) {
		this.name = name;
		this.uid = uid;
		this.timestamp = System.currentTimeMillis();
	}
	
	@Override
	public String getName() {
		return name;
	}
	
	@Override
	public String getUid() {
		return uid;
	}
	
	@Override
	public long getTimestamp() {
		return timestamp;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}
	
	public void setUid(String uid) {
		this.uid = uid;
	}
	
	@NonNull
	@Override
	public String toString() {
		return String.format("Receipt(%s,%s)", name, uid);
	}
}
