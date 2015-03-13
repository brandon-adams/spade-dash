package com.nwt.spade.controllers;

import java.util.Date;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserController {
	
private MongoDBController db;
	
	private static final Logger LOG = LoggerFactory
			.getLogger(UserController.class);

	@Autowired
	public UserController(MongoDBController db){
		this.db = db;
	}
	
	public JsonObject addUser(String payload){
		JsonObjectBuilder objBuild = Json.createObjectBuilder();
		objBuild.add("api", "v0.0.4");
		objBuild.add("time", new Date().getTime());
		objBuild.add("type", "UpdateUser");
		objBuild.add("items", db.updateUser(payload));
		return objBuild.build();
	}
	
	public JsonObject getUser(String username){
		JsonObjectBuilder objBuild = Json.createObjectBuilder();
		objBuild.add("api", "v0.0.4");
		objBuild.add("time", new Date().getTime());
		objBuild.add("type", "GetUser");
		objBuild.add("items", db.getUser(username));
		return objBuild.build();
	}
	
	public JsonObject deleteUser(String username){
		JsonObjectBuilder objBuild = Json.createObjectBuilder();
		objBuild.add("api", "v0.0.4");
		objBuild.add("time", new Date().getTime());
		objBuild.add("type", "DeleteUser");
		objBuild.add("items", db.deleteUser(username));
		return objBuild.build();
	}
	
	public JsonObject listAllUsers(){
		JsonObjectBuilder objBuild = Json.createObjectBuilder();
		objBuild.add("api", "v0.0.4");
		objBuild.add("time", new Date().getTime());
		objBuild.add("type", "ListUsers");
		objBuild.add("items", db.getAllUsers());
		return objBuild.build();
	}	
}
