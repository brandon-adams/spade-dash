package com.nwt.spade.controllers;

import java.util.Date;
import java.util.Map.Entry;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ProjectController {
	
	private MongoDBController db;
	
	private static final Logger LOG = LoggerFactory
			.getLogger(ProjectController.class);

	@Autowired
	public ProjectController(MongoDBController db){
		this.db = db;
	}
	
	public JsonArray addProject(String payload){
		
		return db.addProject(payload);
	}
	
	public JsonObject getProject(String payload){
		JsonObjectBuilder objBuild = Json.createObjectBuilder();
		objBuild.add("api", "v0.0.4");
		objBuild.add("time", new Date().getTime());
		objBuild.add("type", "GetProject");
		objBuild.add("items", db.getProject(payload));
		return objBuild.build();
	}
	
	public JsonObject deleteProject(String payload){
		JsonObjectBuilder objBuild = Json.createObjectBuilder();
		objBuild.add("api", "v0.0.4");
		objBuild.add("time", new Date().getTime());
		objBuild.add("type", "DeleteProject");
		objBuild.add("items", db.deleteProject(payload));
		return objBuild.build();
	}
	
	public JsonObject listAllProjects(){
		JsonObjectBuilder objBuild = Json.createObjectBuilder();
		objBuild.add("api", "v0.0.4");
		objBuild.add("time", new Date().getTime());
		objBuild.add("type", "ListProjects");
		objBuild.add("items", db.getAllProjects());
		return objBuild.build();
	}
	
	public void updateProjects(){
		JsonArray projArr = db.getAllProjects();
		JsonArray imgArr = db.getAllImages("demo");
		JsonArray userArr = db.getAllUsers();
		
		for(JsonValue proj: projArr){
			JsonObjectBuilder objBuild = Json.createObjectBuilder();
			for (String key: ((JsonObject)proj).keySet()){
				objBuild.add(key, ((JsonObject)proj).get(key));
			}
			
			//LOG.debug("PROJECT: " + ((JsonObject)proj).getString("name"));
			JsonArray envArr = db.getAllEnvs(((JsonObject)proj).getString("name"));
			JsonArrayBuilder tmp = Json.createArrayBuilder();
			for(JsonValue env: envArr){
				//LOG.debug("ENV: " + ((JsonObject)env).getString("id"));
				if(!((JsonObject)proj).getJsonArray("environments").contains(((JsonObject)env).getString("id"))){
					tmp.add(((JsonObject)env).get("id"));
				}
			}
			objBuild.add("environments", tmp.build());
			
			tmp = Json.createArrayBuilder();
			for(JsonValue img: imgArr){
				if(!((JsonObject)proj).getJsonArray("images").contains(((JsonObject)img).getString("image"))){
					//LOG.debug(((JsonObject)img).getString("image"));
					tmp.add(((JsonObject)img).get("image"));
				}
			}
			objBuild.add("images", tmp.build());
			
			tmp = Json.createArrayBuilder();
			for(JsonValue user: userArr){
				LOG.debug(((JsonObject)user).getString("name"));
				if(((JsonObject)user).getJsonArray("projects").contains(((JsonObject)proj).getString("name"))){
					LOG.debug(((JsonObject)user).getString("name"));
					tmp.add(((JsonObject)user).get("name"));
				}
			}
			objBuild.add("users", tmp.build());
			LOG.info("Project updated: "+ db.updateProj(objBuild.build().toString()));
		}
	}
	
//	public static void main(String[] args){
//		ProjectController test = new ProjectController(new MongoDBController(true));
//		test.updateProjects();
//	}
	
}
