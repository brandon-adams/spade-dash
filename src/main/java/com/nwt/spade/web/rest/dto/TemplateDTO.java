package com.nwt.spade.web.rest.dto;

import java.util.Map;

public class TemplateDTO {

	private String id;
	private String kind;
	private String apiVersion;
	private Map desiredState;
	private Map labels;
	
	// These are for the simplified version
	private String imageName;
	private String os;
	private String app;
	
	public TemplateDTO(){}
	
	public TemplateDTO(String imageName, String os, String app){
		this.imageName = imageName;
		this.os = os;
		this.app = app;
	}
	
	public String getImageName() {
		return imageName;
	}

	public void setImageName(String imageName) {
		this.imageName = new String(imageName);
	}

	public String getOs() {
		return os;
	}

	public void setOs(String os) {
		this.os = new String(os);
	}

	public String getApp() {
		return app;
	}

	public void setApp(String app) {
		this.app = new String(app);
	}
	
}
