package io.deliverysystem.data;

public class AccessToken {
	
	public String device;
	public String model;
	public String token;
	public long timestamp;
	
	public AccessToken() {
	}
	
	public AccessToken(String device, String model, String token) {
		this.device = device;
		this.model = model;
		this.token = token;
		this.timestamp = System.currentTimeMillis();
	}
	
	
}
