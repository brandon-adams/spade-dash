package com.nwt.spade.controllers;

import java.io.StringReader;
import java.util.Date;
import java.util.TimerTask;

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

import com.nwt.spade.exceptions.KubernetesOperationException;

@Service
public class StackController {

	private MongoDBController db;
	private KubernetesController kc;

	private static final Logger LOG = LoggerFactory
			.getLogger(StackController.class);

	@Autowired
	public StackController(KubernetesController kc, MongoDBController db) {
		this.kc = kc;
		this.db = db;
	}

	public JsonArray createStack(String project, String template)
			throws KubernetesOperationException {
		JsonArrayBuilder arrBuild = Json.createArrayBuilder();
		JsonObjectBuilder objBuild = Json.createObjectBuilder();
		JsonObject jsonInput = Json.createReader(new StringReader(template))
				.readObject();

		String stackName = jsonInput.getString("name");
		String stackProj = jsonInput.getString("project");
		JsonArray controllers = jsonInput.getJsonArray("controllers");

		for (JsonValue cont : controllers) {
			String os = ((JsonObject) cont).getString("os").toLowerCase();
			String app = ((JsonObject) cont).getString("app").toLowerCase();
			String name = ((JsonObject) cont).getString("name").toLowerCase();
			//String stack = ((JsonObject) cont).getString("stack").toLowerCase();
			int replicas = ((JsonObject) cont).getInt("replicas");
			String imageName = db.getImage(project, os, app)
					.getJsonObject(0).getString("image");
			arrBuild.add(kc
					.createEnv(stackName, name, project, imageName, os, app,
							replicas).getJsonObject(0).getString("id"));
		}

		objBuild.add("id", stackName);
		objBuild.add("project", stackProj);
		objBuild.add("controllers", arrBuild.build());

		return db.updateStack(project, objBuild.build().toString());
	}

	public JsonArray getStack(String project, String id) {

		return db.getStack(project, id);
	}

	public JsonArray deleteStack(String project, String id) {

		return db.deleteStack(project, id);
	}

	public JsonArray getAllStacks(String project) {

		return db.getAllStacks(project);
	}

	public JsonArray createStackTemp(String project, String template) {

		return db.updateStackTemp(project, template);
	}

	public JsonArray getStackTemp(String project, String id) {

		return db.getStackTemp(project, id);
	}

	public JsonArray deleteStackTemp(String project, String id) {

		return db.deleteStackTemp(project, id);
	}

	public JsonArray getAllStackTemps(String project) {

		return db.getAllStackTemps(project);
	}

	public void updateAllStacks(){
		JsonArray dbPods = db.getAllPods("all");
		JsonArray dbConts = db.getAllControllers("all");
		JsonArray dbStacks = db.getAllStacks("all");
		
		for (JsonValue stack : dbStacks){
			JsonObjectBuilder objBuild = Json.createObjectBuilder();
			objBuild.add("id", ((JsonObject)stack).getString("id"));
			objBuild.add("project", ((JsonObject)stack).getString("project"));
			JsonArrayBuilder arrBuild = Json.createArrayBuilder();
			for (JsonValue pod : dbPods){
				String ownStack = ((JsonObject)pod).getJsonObject("labels").getString("stack");
				if (ownStack.equals(((JsonObject)stack).getString("id"))){
					arrBuild.add(((JsonObject)pod).getJsonObject("labels").getString("name"));
				}
			}
			objBuild.add("pods", arrBuild.build());
			
			for (JsonValue cont : dbConts){
				String ownStack = ((JsonObject)cont).getJsonObject("labels").getString("stack");
				if (ownStack.equals(((JsonObject)stack).getString("id"))){
					arrBuild.add(((JsonObject)cont).getJsonObject("labels").getString("name"));
				}
			}
			objBuild.add("controllers", arrBuild.build());
		}
	}

	public static class UpdateStatus extends TimerTask {

		private StackController stackCont;

		public UpdateStatus(StackController stackController) {
			super();
			stackCont = stackController;
		}

		@Override
		public void run() {
			stackCont.updateAllStacks();
		}
	}
}
