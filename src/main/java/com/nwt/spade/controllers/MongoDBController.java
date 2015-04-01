package com.nwt.spade.controllers;

import java.io.StringReader;
import java.net.UnknownHostException;
import java.util.Timer;
import java.util.TimerTask;

import javax.annotation.PostConstruct;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.util.JSON;

//import com.newwave.spade.docker.KubernetesController.UpdateStatus;

@Service
@PropertySource("config/application.properties")
public class MongoDBController {

	private static final Logger LOG = LoggerFactory
			.getLogger(MongoDBController.class);
	private MongoClient mongo;
	private DB db;

	@Value("${mongodb.host}")
	private String host;
	@Value("${mongodb.port}")
	private int port;
	@Value("${mongodb.db}")
	private String dbName;

	public MongoDBController() {

	}

	public MongoDBController(boolean on) {
		try {
			mongo = new MongoClient("localhost");
			db = mongo.getDB("demo");
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}

	@PostConstruct
	public void init() {
		try {
			mongo = new MongoClient(host, port);
			db = mongo.getDB(dbName);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}

	public JsonArray addContTemplate(String project, String template, String imageName) {
		BasicDBObject doc = (BasicDBObject) JSON.parse(template);
		doc.put("_id", doc.get("id"));
		DBCollection coll = db.getCollection("controller_templates");
		BasicDBObject query = new BasicDBObject();
		query.put("id", doc.get("id"));
		query.put("desiredState.podTemplate.labels.image", imageName);
		query.put("desiredState.podTemplate.labels.project", project);
		DBCursor cursor = coll.find(query);
		JsonObjectBuilder objBuild = Json.createObjectBuilder();
		JsonArrayBuilder arrBuild = Json.createArrayBuilder();
		JsonObject json = objBuild.build();
		if (cursor.count() > 0) {
			LOG.info("Template with image name " + imageName + " exists");
			json = Json
					.createReader(new StringReader(cursor.next().toString()))
					.readObject();
			return arrBuild.add(json).build();
		} else {
			LOG.info("Inserted Template: " + coll.insert(doc).toString());
			json = Json.createReader(new StringReader(template)).readObject();
			return arrBuild.add(json).build();
		}
	}

	public JsonArray getContTemplate(String project, String id) {
		BasicDBObject query = new BasicDBObject();
		BasicDBObject removeId = new BasicDBObject("_id", 0);
		query.put("id", id);
		//query.put("desiredState.podTemplate.labels.image", imageName);
		query.put("desiredState.podTemplate.labels.project", project);
		DBCollection coll = db.getCollection("controller_templates");
		DBCursor cursor = coll.find(query, removeId);
		JsonObjectBuilder objBuild = Json.createObjectBuilder();
		JsonArrayBuilder arrBuild = Json.createArrayBuilder();
		JsonObject json = objBuild.build();
		while (cursor.hasNext()) {
			BasicDBObject found = (BasicDBObject) cursor.next();
			LOG.info("Found Template: " + found.toString());
			json = Json.createReader(new StringReader(found.toString()))
					.readObject();
			arrBuild.add(json);
		}

		return arrBuild.build();
	}

	public JsonArray deleteContTemplate(String project, String id) {
		BasicDBObject query = new BasicDBObject();
		BasicDBObject removeId = new BasicDBObject("_id", 0);
		query.put("id", id);
		query.put("desiredState.podTemplate.labels.project", project);
		DBCollection coll = db.getCollection("controller_templates");
		DBCursor cursor = coll.find(query, removeId);
		JsonObjectBuilder objBuild = Json.createObjectBuilder();
		JsonArrayBuilder arrBuild = Json.createArrayBuilder();
		JsonObject json = objBuild.build();
		while (cursor.hasNext()) {
			BasicDBObject found = (BasicDBObject) cursor.next();
			LOG.info("Template removed: " + coll.remove(found));
			json = Json.createReader(new StringReader(found.toString())).readObject();
			arrBuild.add(json);
		}

		return arrBuild.build();
	}

	public JsonArray getAllContTemplates(String project) {
		BasicDBObject query = new BasicDBObject();
		if (!project.equals("all")) query.put("desiredState.podTemplate.labels.project", project);
		BasicDBObject removeId = new BasicDBObject("_id", 0);
		DBCollection coll = db.getCollection("controller_templates");
		DBCursor cursor = coll.find(query, removeId);
		JsonObjectBuilder objBuild = Json.createObjectBuilder();
		JsonArrayBuilder arrBuild = Json.createArrayBuilder();
		JsonObject json = objBuild.build();
		while (cursor.hasNext()) {
			BasicDBObject found = (BasicDBObject) cursor.next();
			LOG.info("Found Template: " + found.toString());
			json = Json.createReader(new StringReader(found.toString()))
					.readObject();
			arrBuild.add(json);
		}

		return arrBuild.build();
	}

	public JsonArray addController(String project, String template) {
		BasicDBObject doc = (BasicDBObject) JSON.parse(template);
		LOG.debug("Template: " + template);
		
		doc.append("_id", doc.get("id"));
		DBCollection coll = db.getCollection("controllers");
		BasicDBObject query = new BasicDBObject();
		query.put("_id", doc.get("_id"));
		query.put("desiredState.podTemplate.labels.project", project);
		DBCursor cursor = coll.find(query);
		JsonObjectBuilder objBuild = Json.createObjectBuilder();
		JsonArrayBuilder arrBuild = Json.createArrayBuilder();
		JsonObject json = objBuild.build();
		if (doc.containsValue("Status")) return arrBuild.add(json).build();
		if (cursor.count() > 0) {
			LOG.info("Controller Exists");
			json = Json
					.createReader(new StringReader(cursor.next().toString()))
					.readObject();
			return arrBuild.add(json).build();
		} else {
			LOG.info("Inserted Controller: " + coll.insert(doc).toString());
			json = Json
					.createReader(new StringReader(doc.toString()))
					.readObject();
			return arrBuild.add(json).build();
		}

	}
	
	public JsonArray updatePod(String project, String template) {
		BasicDBObject doc = (BasicDBObject) JSON.parse(template);
		doc.append("_id", doc.get("id"));
		DBCollection coll = db.getCollection("pods");
		BasicDBObject query = new BasicDBObject();
		//LOG.debug("ID: " + doc.getString("id"));
		query.put("_id", doc.get("id"));
		//query.put("labels.project", project);
		DBCursor cursor = coll.find(query);
		JsonObjectBuilder objBuild = Json.createObjectBuilder();
		JsonArrayBuilder arrBuild = Json.createArrayBuilder();
		JsonObject json = objBuild.build();
		if (cursor.count() > 0) {
			LOG.info("Pod found and updated: "
					+ coll.update(query, doc, true, false));
			json = Json.createReader(new StringReader(template)).readObject();
			return arrBuild.add(json).build();
		} else {
			LOG.info("Pod inserted: " + coll.insert(doc));
			json = Json.createReader(new StringReader(template)).readObject();
			return arrBuild.add(json).build();
		}
	}
	
	public JsonArray updateStack(String project, String template) {
		BasicDBObject doc = (BasicDBObject) JSON.parse(template);
		doc.append("_id", doc.get("id"));
		DBCollection coll = db.getCollection("stacks");
		BasicDBObject query = new BasicDBObject();
		//LOG.debug("ID: " + doc.getString("id"));
		query.put("_id", doc.get("id"));
		//query.put("labels.project", project);
		DBCursor cursor = coll.find(query);
		JsonObjectBuilder objBuild = Json.createObjectBuilder();
		JsonArrayBuilder arrBuild = Json.createArrayBuilder();
		JsonObject json = objBuild.build();
		if (cursor.count() > 0) {
			LOG.info("Stack found and updated: "
					+ coll.update(query, doc, true, false));
			json = Json.createReader(new StringReader(template)).readObject();
			return arrBuild.add(json).build();
		} else {
			LOG.info("Stack inserted: " + coll.insert(doc));
			json = Json.createReader(new StringReader(template)).readObject();
			return arrBuild.add(json).build();
		}
	}
	
	public JsonArray getStack(String project, String id) {
		BasicDBObject query = new BasicDBObject();
		BasicDBObject removeId = new BasicDBObject("_id", 0);
		query.put("id", id);
		//query.put("desiredState.podTemplate.labels.project", project);
		DBCollection coll = db.getCollection("stacks");
		DBCursor cursor = coll.find(query, removeId);
		JsonObjectBuilder objBuild = Json.createObjectBuilder();
		JsonArrayBuilder arrBuild = Json.createArrayBuilder();
		JsonObject json = objBuild.build();
		while (cursor.hasNext()) {
			BasicDBObject found = (BasicDBObject) cursor.next();
			LOG.info("Found Stack: " + found.toString());
			json = Json.createReader(new StringReader(found.toString()))
					.readObject();
			arrBuild.add(json);
		}
		return arrBuild.build();
	}
	
	public JsonArray deleteStack(String project, String id) {
		BasicDBObject query = new BasicDBObject();
		BasicDBObject removeId = new BasicDBObject("_id", 0);
		query.put("id", id);
		//query.put("desiredState.podTemplate.labels.project", project);
		DBCollection coll = db.getCollection("stacks");
		DBCursor cursor = coll.find(query, removeId);
		JsonObjectBuilder objBuild = Json.createObjectBuilder();
		JsonArrayBuilder arrBuild = Json.createArrayBuilder();
		JsonObject json = objBuild.build();
		while (cursor.hasNext()) {
			BasicDBObject found = (BasicDBObject) cursor.next();
			LOG.info("Stack removed: " + coll.remove(found));
			json = Json.createReader(new StringReader(found.toString()))
					.readObject();
			arrBuild.add(json);
		}
		return arrBuild.build();
	}
	
	public JsonArray getAllStacks(String project) {
		BasicDBObject query = new BasicDBObject();
		//query.put("desiredState.podTemplate.labels.project", project); NEED TO IMPLEMENT PROJECT BASED
		BasicDBObject removeId = new BasicDBObject("_id", 0);
		DBCollection coll = db.getCollection("stacks");
		DBCursor cursor = coll.find(query, removeId);
		JsonObjectBuilder objBuild = Json.createObjectBuilder();
		JsonArrayBuilder arrBuild = Json.createArrayBuilder();
		JsonObject json = objBuild.build();
		while (cursor.hasNext()) {
			BasicDBObject found = (BasicDBObject) cursor.next();
			LOG.info("Found Stack: " + found.toString());
			json = Json.createReader(new StringReader(found.toString()))
					.readObject();
			arrBuild.add(json);
		}
		return arrBuild.build();
	}
	
	public JsonArray updateStackTemp(String project, String template) {
		BasicDBObject doc = (BasicDBObject) JSON.parse(template);
		doc.append("_id", doc.get("id"));
		DBCollection coll = db.getCollection("stack_templates");
		BasicDBObject query = new BasicDBObject();
		//LOG.debug("ID: " + doc.getString("id"));
		query.put("_id", doc.get("id"));
		//query.put("labels.project", project);
		DBCursor cursor = coll.find(query);
		JsonObjectBuilder objBuild = Json.createObjectBuilder();
		JsonArrayBuilder arrBuild = Json.createArrayBuilder();
		JsonObject json = objBuild.build();
		if (cursor.count() > 0) {
			LOG.info("Stack found and updated: "
					+ coll.update(query, doc, true, false));
			json = Json.createReader(new StringReader(template)).readObject();
			return arrBuild.add(json).build();
		} else {
			LOG.info("Stack inserted: " + coll.insert(doc));
			json = Json.createReader(new StringReader(template)).readObject();
			return arrBuild.add(json).build();
		}
	}
	
	public JsonArray getStackTemp(String project, String id) {
		BasicDBObject query = new BasicDBObject();
		BasicDBObject removeId = new BasicDBObject("_id", 0);
		query.put("id", id);
		//query.put("desiredState.podTemplate.labels.project", project);
		DBCollection coll = db.getCollection("stack_template");
		DBCursor cursor = coll.find(query, removeId);
		JsonObjectBuilder objBuild = Json.createObjectBuilder();
		JsonArrayBuilder arrBuild = Json.createArrayBuilder();
		JsonObject json = objBuild.build();
		while (cursor.hasNext()) {
			BasicDBObject found = (BasicDBObject) cursor.next();
			LOG.info("Found Stack: " + found.toString());
			json = Json.createReader(new StringReader(found.toString()))
					.readObject();
			arrBuild.add(json);
		}
		return arrBuild.build();
	}
	
	public JsonArray deleteStackTemp(String project, String id) {
		BasicDBObject query = new BasicDBObject();
		BasicDBObject removeId = new BasicDBObject("_id", 0);
		query.put("id", id);
		//query.put("desiredState.podTemplate.labels.project", project);
		DBCollection coll = db.getCollection("stack_template");
		DBCursor cursor = coll.find(query, removeId);
		JsonObjectBuilder objBuild = Json.createObjectBuilder();
		JsonArrayBuilder arrBuild = Json.createArrayBuilder();
		JsonObject json = objBuild.build();
		while (cursor.hasNext()) {
			BasicDBObject found = (BasicDBObject) cursor.next();
			LOG.info("Stack removed: " + coll.remove(found));
			json = Json.createReader(new StringReader(found.toString()))
					.readObject();
			arrBuild.add(json);
		}
		return arrBuild.build();
	}
	
	public JsonArray getAllStackTemps(String project) {
		BasicDBObject query = new BasicDBObject();
		//query.put("desiredState.podTemplate.labels.project", project); NEED TO IMPLEMENT PROJECT BASED
		BasicDBObject removeId = new BasicDBObject("_id", 0);
		DBCollection coll = db.getCollection("stack_templates");
		DBCursor cursor = coll.find(query, removeId);
		JsonObjectBuilder objBuild = Json.createObjectBuilder();
		JsonArrayBuilder arrBuild = Json.createArrayBuilder();
		JsonObject json = objBuild.build();
		while (cursor.hasNext()) {
			BasicDBObject found = (BasicDBObject) cursor.next();
			LOG.info("Found Stack: " + found.toString());
			json = Json.createReader(new StringReader(found.toString()))
					.readObject();
			arrBuild.add(json);
		}
		return arrBuild.build();
	}

	public JsonArray updateController(String project, String template) {
		BasicDBObject doc = (BasicDBObject) JSON.parse(template);
		//LOG.debug(template);
		doc.append("_id", doc.get("id"));
		DBCollection coll = db.getCollection("controllers");
		BasicDBObject query = new BasicDBObject();
		query.put("_id", doc.get("id"));
		//query.put("desiredState.podTemplate.labels.project", project);
		DBCursor cursor = coll.find(query);
		JsonObjectBuilder objBuild = Json.createObjectBuilder();
		JsonArrayBuilder arrBuild = Json.createArrayBuilder();
		JsonObject json = objBuild.build();
		if (cursor.count() > 0) {
			LOG.info("Controller found and updated: "
					+ coll.update(query, doc, true, false));
			json = Json.createReader(new StringReader(template)).readObject();
			return arrBuild.add(json).build();
		} else {
			LOG.info("Controller inserted: " + coll.insert(doc));
			json = Json.createReader(new StringReader(template)).readObject();
			return arrBuild.add(json).build();
		}
	}

	public JsonArray getController(String project, String id) {
		BasicDBObject query = new BasicDBObject();
		BasicDBObject removeId = new BasicDBObject("_id", 0);
		query.put("id", id);
		LOG.debug("GETTING ID: " + id);
		//query.put("desiredState.podTemplate.labels.project", project);
		DBCollection coll = db.getCollection("controllers");
		DBCursor cursor = coll.find(query, removeId);
		JsonObjectBuilder objBuild = Json.createObjectBuilder();
		JsonArrayBuilder arrBuild = Json.createArrayBuilder();
		JsonObject json = objBuild.build();
		while (cursor.hasNext()) {
			BasicDBObject found = (BasicDBObject) cursor.next();
			LOG.info("Found Controller: " + found.toString());
			json = Json.createReader(new StringReader(found.toString()))
					.readObject();
			arrBuild.add(json);
		}
		return arrBuild.build();
	}
	
	public JsonArray getPod(String project, String id) {
		BasicDBObject query = new BasicDBObject();
		BasicDBObject removeId = new BasicDBObject("_id", 0);
		query.put("id", id);
		//query.put("desiredState.podTemplate.labels.project", project);
		DBCollection coll = db.getCollection("pods");
		DBCursor cursor = coll.find(query, removeId);
		JsonObjectBuilder objBuild = Json.createObjectBuilder();
		JsonArrayBuilder arrBuild = Json.createArrayBuilder();
		JsonObject json = objBuild.build();
		while (cursor.hasNext()) {
			BasicDBObject found = (BasicDBObject) cursor.next();
			LOG.info("Found Pod: " + found.toString());
			json = Json.createReader(new StringReader(found.toString()))
					.readObject();
			arrBuild.add(json);
		}
		return arrBuild.build();
	}

	public JsonArray deleteController(String project, String id) {
		BasicDBObject query = new BasicDBObject();
		BasicDBObject removeId = new BasicDBObject("_id", 0);
		query.put("id", id);
		//query.put("desiredState.podTemplate.labels.project", project);
		DBCollection coll = db.getCollection("controllers");
		DBCursor cursor = coll.find(query, removeId);
		JsonObjectBuilder objBuild = Json.createObjectBuilder();
		JsonArrayBuilder arrBuild = Json.createArrayBuilder();
		JsonObject json = objBuild.build();
		while (cursor.hasNext()) {
			BasicDBObject found = (BasicDBObject) cursor.next();
			LOG.info("Controller removed: " + coll.remove(found));
			json = Json.createReader(new StringReader(found.toString()))
					.readObject();
			arrBuild.add(json);
		}
		return arrBuild.build();
	}

	public JsonArray getAllControllers(String project) {
		BasicDBObject query = new BasicDBObject();
		//query.put("desiredState.podTemplate.labels.project", project); NEED TO IMPLEMENT PROJECT BASED
		BasicDBObject removeId = new BasicDBObject("_id", 0);
		DBCollection coll = db.getCollection("controllers");
		DBCursor cursor = coll.find(query, removeId);
		JsonObjectBuilder objBuild = Json.createObjectBuilder();
		JsonArrayBuilder arrBuild = Json.createArrayBuilder();
		JsonObject json = objBuild.build();
		while (cursor.hasNext()) {
			BasicDBObject found = (BasicDBObject) cursor.next();
			LOG.info("Found Controller: " + found.toString());
			json = Json.createReader(new StringReader(found.toString()))
					.readObject();
			arrBuild.add(json);
		}
		return arrBuild.build();
	}
	
	public JsonArray getAllPods(String project) {
		BasicDBObject query = new BasicDBObject();
		//query.put("desiredState.podTemplate.labels.project", project);
		BasicDBObject removeId = new BasicDBObject("_id", 0);
		DBCollection coll = db.getCollection("pods");
		DBCursor cursor = coll.find(query, removeId);
		JsonObjectBuilder objBuild = Json.createObjectBuilder();
		JsonArrayBuilder arrBuild = Json.createArrayBuilder();
		JsonObject json = objBuild.build();
		while (cursor.hasNext()) {
			BasicDBObject found = (BasicDBObject) cursor.next();
			LOG.info("Found Pod: " + found.toString());
			json = Json.createReader(new StringReader(found.toString()))
					.readObject();
			arrBuild.add(json);
		}
		return arrBuild.build();
	}
	
	public JsonArray deletePod(String project, String id) {
		BasicDBObject query = new BasicDBObject();
		BasicDBObject removeId = new BasicDBObject("_id", 0);
		query.put("id", id);
		//query.put("desiredState.podTemplate.labels.project", project);
		DBCollection coll = db.getCollection("pods");
		DBCursor cursor = coll.find(query, removeId);
		JsonObjectBuilder objBuild = Json.createObjectBuilder();
		JsonArrayBuilder arrBuild = Json.createArrayBuilder();
		JsonObject json = objBuild.build();
		while (cursor.hasNext()) {
			BasicDBObject found = (BasicDBObject) cursor.next();
			LOG.info("Pod removed: " + coll.remove(found));
			json = Json.createReader(new StringReader(found.toString()))
					.readObject();
			arrBuild.add(json);
		}
		return arrBuild.build();
	}

	public JsonArray addImage(String project, String imageName, String os, String app) {
		DBCollection coll = db.getCollection("images");
		BasicDBObject newImage = new BasicDBObject().append("image", imageName)
				.append("os", os).append("app", app);
		BasicDBObject query = new BasicDBObject();
		BasicDBObject removeId = new BasicDBObject("_id", 0);
		query.put("image", imageName);
		query.put("os", os);
		query.put("app", app);
		//if (!project.equals("all")) query.put("labels.project", project); ******* Come back to this
		DBCursor cursor = coll.find(query, removeId);
		JsonObjectBuilder objBuild = Json.createObjectBuilder();
		JsonArrayBuilder arrBuild = Json.createArrayBuilder();
		JsonObject json = objBuild.build();
		if (cursor.count() > 0) {
			LOG.info("Image already stored: " + imageName + ", " + os + ", "
					+ app + ", ");
			BasicDBObject found = (BasicDBObject) cursor.next();
			json = Json.createReader(new StringReader(found.toString()))
					.readObject();
			arrBuild.add(json);
		} else {
			LOG.info("New image inserted: " + coll.insert(newImage));
			json = Json.createReader(new StringReader(newImage.toString()))
					.readObject();
			arrBuild.add(json);
		}
		return arrBuild.build();
	}

	public JsonArray getImage(String project, String os, String app) {
		DBCollection coll = db.getCollection("images");
		BasicDBObject query = new BasicDBObject();
		BasicDBObject removeId = new BasicDBObject("_id", 0);
		query.put("os", os);
		query.put("app", app);
		//if (!project.equals("all")) query.put("labels.project", project); ******* Come back to this
		DBCursor cursor = coll.find(query, removeId);
		JsonObjectBuilder objBuild = Json.createObjectBuilder();
		JsonArrayBuilder arrBuild = Json.createArrayBuilder();
		JsonObject json = objBuild.build();
		while (cursor.hasNext()) {
			BasicDBObject found = (BasicDBObject) cursor.next();
			LOG.info("Found Image: " + found.toString());
			json = Json.createReader(new StringReader(found.toString()))
					.readObject();
			arrBuild.add(json);
		}
		return arrBuild.build();
	}

	public JsonArray deleteImage(String project, String os, String app) {
		BasicDBObject query = new BasicDBObject();
		BasicDBObject removeId = new BasicDBObject("_id", 0);
		query.put("os", os);
		query.put("app", app);
		//if (!project.equals("all")) query.put("labels.project", project); ******* Come back to this
		DBCollection coll = db.getCollection("images");
		DBCursor cursor = coll.find(query, removeId);
		JsonObjectBuilder objBuild = Json.createObjectBuilder();
		JsonArrayBuilder arrBuild = Json.createArrayBuilder();
		JsonObject json = objBuild.build();
		while (cursor.hasNext()) {
			BasicDBObject found = (BasicDBObject) cursor.next();
			LOG.info("Image removed: " + coll.remove(found));
			json = Json.createReader(new StringReader(found.toString()))
					.readObject();
			arrBuild.add(json);
		}
		return arrBuild.build();
	}

	public JsonArray getAllImages(String project) {
		BasicDBObject query = new BasicDBObject();
		//if (!project.equals("all")) query.put("labels.project", project); ******* Come back to this
		BasicDBObject removeId = new BasicDBObject("_id", 0);
		DBCollection coll = db.getCollection("images");
		DBCursor cursor = coll.find(query, removeId);
		JsonObjectBuilder objBuild = Json.createObjectBuilder();
		JsonArrayBuilder arrBuild = Json.createArrayBuilder();
		JsonObject json = objBuild.build();
		while (cursor.hasNext()) {
			BasicDBObject found = (BasicDBObject) cursor.next();
			//LOG.info("Found Image: " + found.toString());
			json = Json.createReader(new StringReader(found.toString()))
					.readObject();
			arrBuild.add(json);
		}
		return arrBuild.build();
	}

	public JsonArray addProject(String project) {
		DBCollection coll = db.getCollection("projects");
		BasicDBObject newProj = (BasicDBObject) JSON.parse(project);
		newProj.append("_id", newProj.getString("name"));
		BasicDBObject query = new BasicDBObject();
		query.append("name", newProj.getString("name"));
		BasicDBObject removeId = new BasicDBObject("_id", 0);
		DBCursor cursor = coll.find(query, removeId);
		JsonObjectBuilder objBuild = Json.createObjectBuilder();
		JsonArrayBuilder arrBuild = Json.createArrayBuilder();
		JsonObject json = objBuild.build();
		if (cursor.count() > 0) {
			LOG.info("Project already created: ");
			BasicDBObject found = (BasicDBObject) cursor.next();
			json = Json.createReader(new StringReader(found.toString()))
					.readObject();
			arrBuild.add(json);
		} else {
			LOG.info("New project created: " + coll.insert(newProj));
			json = Json.createReader(new StringReader(newProj.toString()))
					.readObject();
			arrBuild.add(json);
		}

		return arrBuild.build();
	}
	
	public JsonArray updateProj(String project) {
		BasicDBObject doc = (BasicDBObject) JSON.parse(project);
		doc.append("_id", doc.get("name"));
		DBCollection coll = db.getCollection("projects");
		BasicDBObject query = new BasicDBObject();
		query.put("_id", doc.get("name"));
		DBCursor cursor = coll.find(query);
		JsonObjectBuilder objBuild = Json.createObjectBuilder();
		JsonArrayBuilder arrBuild = Json.createArrayBuilder();
		JsonObject json = objBuild.build();
		if (cursor.count() > 0) {
			LOG.info("Project found and updated: "
					+ coll.update(query, doc, true, false));
			json = Json.createReader(new StringReader(project)).readObject();
			return arrBuild.add(json).build();
		} else {
			LOG.info("Project created: " + coll.insert(doc));
			json = Json.createReader(new StringReader(project)).readObject();
			return arrBuild.add(json).build();
		}
	}

	public JsonArray getProject(String project) {
		DBCollection coll = db.getCollection("projects");
		BasicDBObject query = new BasicDBObject();
		BasicDBObject removeId = new BasicDBObject("_id", 0);
		query.put("name", project);
		DBCursor cursor = coll.find(query, removeId);
		JsonObjectBuilder objBuild = Json.createObjectBuilder();
		JsonArrayBuilder arrBuild = Json.createArrayBuilder();
		JsonObject json = objBuild.build();
		while (cursor.hasNext()) {
			BasicDBObject found = (BasicDBObject) cursor.next();
			LOG.info("Found Project: " + found.toString());
			json = Json.createReader(new StringReader(found.toString()))
					.readObject();
			arrBuild.add(json);
		}
		return arrBuild.build();
	}

	public JsonArray deleteProject(String project) {
		BasicDBObject query = new BasicDBObject();
		BasicDBObject removeId = new BasicDBObject("_id", 0);
		query.put("name", project);
		DBCollection coll = db.getCollection("projects");
		DBCursor cursor = coll.find(query, removeId);
		JsonObjectBuilder objBuild = Json.createObjectBuilder();
		JsonArrayBuilder arrBuild = Json.createArrayBuilder();
		JsonObject json = objBuild.build();
		while (cursor.hasNext()) {
			BasicDBObject found = (BasicDBObject) cursor.next();
			LOG.info("Project removed: " + coll.remove(found));
			json = Json.createReader(new StringReader(found.toString()))
					.readObject();
			arrBuild.add(json);
		}
		return arrBuild.build();
	}

	public JsonArray getAllProjects() {
		BasicDBObject query = new BasicDBObject();
		BasicDBObject removeId = new BasicDBObject("_id", 0);
		DBCollection coll = db.getCollection("projects");
		DBCursor cursor = coll.find(query, removeId);
		JsonObjectBuilder objBuild = Json.createObjectBuilder();
		JsonArrayBuilder arrBuild = Json.createArrayBuilder();
		JsonObject json = objBuild.build();
		while (cursor.hasNext()) {
			BasicDBObject found = (BasicDBObject) cursor.next();
			LOG.info("Found Project: " + found.toString());
			json = Json.createReader(new StringReader(found.toString()))
					.readObject();
			arrBuild.add(json);
		}
		return arrBuild.build();
	}
	
	public JsonArray updateTask(String task) {
		BasicDBObject doc = (BasicDBObject) JSON.parse(task);
		doc.append("_id", doc.getString("id"));
		DBCollection coll = db.getCollection("mesos_tasks");
		BasicDBObject query = new BasicDBObject();
		LOG.debug("ID: " + doc.getString("id"));
		query.put("_id", doc.getString("id"));
		//query.put("labels.project", project);
		DBCursor cursor = coll.find(query);
		JsonObjectBuilder objBuild = Json.createObjectBuilder();
		JsonArrayBuilder arrBuild = Json.createArrayBuilder();
		JsonObject json = objBuild.build();
		if (cursor.count() > 0) {
			LOG.info("Task found and updated: "
					+ coll.update(query, doc, true, false));
			json = Json.createReader(new StringReader(task)).readObject();
			return arrBuild.add(json).build();
		} else {
			LOG.info("Task inserted: " + coll.insert(doc));
			json = Json.createReader(new StringReader(task)).readObject();
			return arrBuild.add(json).build();
		}
	}
	
	public JsonArray getAllTasks() {
		BasicDBObject query = new BasicDBObject();
		BasicDBObject removeId = new BasicDBObject("_id", 0);
		DBCollection coll = db.getCollection("mesos_tasks");
		DBCursor cursor = coll.find(query, removeId);
		JsonObjectBuilder objBuild = Json.createObjectBuilder();
		JsonArrayBuilder arrBuild = Json.createArrayBuilder();
		JsonObject json = objBuild.build();
		while (cursor.hasNext()) {
			BasicDBObject found = (BasicDBObject) cursor.next();
			LOG.info("Found Task: " + found.toString());
			json = Json.createReader(new StringReader(found.toString()))
					.readObject();
			arrBuild.add(json);
		}
		return arrBuild.build();
	}
	
	public JsonArray deleteTask(String id) {
		BasicDBObject query = new BasicDBObject();
		BasicDBObject removeId = new BasicDBObject("_id", 0);
		query.put("id", id);
		DBCollection coll = db.getCollection("mesos_tasks");
		DBCursor cursor = coll.find(query, removeId);
		JsonObjectBuilder objBuild = Json.createObjectBuilder();
		JsonArrayBuilder arrBuild = Json.createArrayBuilder();
		JsonObject json = objBuild.build();
		while (cursor.hasNext()) {
			BasicDBObject found = (BasicDBObject) cursor.next();
			LOG.info("Task removed: " + coll.remove(found));
			json = Json.createReader(new StringReader(found.toString()))
					.readObject();
			arrBuild.add(json);
		}
		return arrBuild.build();
	}
	
	public JsonArray updateSlave(String slave) {
		BasicDBObject doc = (BasicDBObject) JSON.parse(slave);
		doc.append("_id", doc.getString("id"));
		DBCollection coll = db.getCollection("mesos_slaves");
		BasicDBObject query = new BasicDBObject();
		LOG.debug("ID: " + doc.getString("id"));
		query.put("_id", doc.getString("id"));
		//query.put("labels.project", project);
		DBCursor cursor = coll.find(query);
		JsonObjectBuilder objBuild = Json.createObjectBuilder();
		JsonArrayBuilder arrBuild = Json.createArrayBuilder();
		JsonObject json = objBuild.build();
		if (cursor.count() > 0) {
			LOG.info("Slave found and updated: "
					+ coll.update(query, doc, true, false));
			json = Json.createReader(new StringReader(slave)).readObject();
			return arrBuild.add(json).build();
		} else {
			LOG.info("Slave inserted: " + coll.insert(doc));
			json = Json.createReader(new StringReader(slave)).readObject();
			return arrBuild.add(json).build();
		}
	}
	
	public JsonArray getAllSlaves() {
		BasicDBObject query = new BasicDBObject();
		BasicDBObject removeId = new BasicDBObject("_id", 0);
		DBCollection coll = db.getCollection("mesos_slaves");
		DBCursor cursor = coll.find(query, removeId);
		JsonObjectBuilder objBuild = Json.createObjectBuilder();
		JsonArrayBuilder arrBuild = Json.createArrayBuilder();
		JsonObject json = objBuild.build();
		while (cursor.hasNext()) {
			BasicDBObject found = (BasicDBObject) cursor.next();
			LOG.info("Found Slave: " + found.toString());
			json = Json.createReader(new StringReader(found.toString()))
					.readObject();
			arrBuild.add(json);
		}
		return arrBuild.build();
	}
	
	public JsonArray deleteSlave(String id) {
		BasicDBObject query = new BasicDBObject();
		BasicDBObject removeId = new BasicDBObject("_id", 0);
		query.put("id", id);
		DBCollection coll = db.getCollection("mesos_slaves");
		DBCursor cursor = coll.find(query, removeId);
		JsonObjectBuilder objBuild = Json.createObjectBuilder();
		JsonArrayBuilder arrBuild = Json.createArrayBuilder();
		JsonObject json = objBuild.build();
		while (cursor.hasNext()) {
			BasicDBObject found = (BasicDBObject) cursor.next();
			LOG.info("Slave removed: " + coll.remove(found));
			json = Json.createReader(new StringReader(found.toString()))
					.readObject();
			arrBuild.add(json);
		}
		return arrBuild.build();
	}
	
	public JsonArray updateUser(String user) {
		BasicDBObject doc = (BasicDBObject) JSON.parse(user);
		doc.append("_id", doc.getString("name"));
		DBCollection coll = db.getCollection("users");
		BasicDBObject query = new BasicDBObject();
		LOG.debug("NAME: " + doc.getString("name"));
		query.put("_id", doc.getString("name"));
		//query.put("labels.project", project);
		DBCursor cursor = coll.find(query);
		JsonObjectBuilder objBuild = Json.createObjectBuilder();
		JsonArrayBuilder arrBuild = Json.createArrayBuilder();
		JsonObject json = objBuild.build();
		if (cursor.count() > 0) {
			LOG.info("User found and updated: "
					+ coll.update(query, doc, true, false));
			json = Json.createReader(new StringReader(user)).readObject();
			return arrBuild.add(json).build();
		} else {
			LOG.info("User created: " + coll.insert(doc));
			json = Json.createReader(new StringReader(user)).readObject();
			return arrBuild.add(json).build();
		}
	}
	
	public JsonArray getUser(String username) {
		DBCollection coll = db.getCollection("users");
		BasicDBObject query = new BasicDBObject();
		BasicDBObject removeId = new BasicDBObject("_id", 0);
		query.put("name", username);
		DBCursor cursor = coll.find(query, removeId);
		JsonObjectBuilder objBuild = Json.createObjectBuilder();
		JsonArrayBuilder arrBuild = Json.createArrayBuilder();
		JsonObject json = objBuild.build();
		while (cursor.hasNext()) {
			BasicDBObject found = (BasicDBObject) cursor.next();
			LOG.info("Found User: " + found.toString());
			json = Json.createReader(new StringReader(found.toString()))
					.readObject();
			arrBuild.add(json);
		}
		return arrBuild.build();
	}

	public JsonArray deleteUser(String username) {
		BasicDBObject query = new BasicDBObject();
		BasicDBObject removeId = new BasicDBObject("_id", 0);
		query.put("name", username);
		DBCollection coll = db.getCollection("users");
		DBCursor cursor = coll.find(query, removeId);
		JsonObjectBuilder objBuild = Json.createObjectBuilder();
		JsonArrayBuilder arrBuild = Json.createArrayBuilder();
		JsonObject json = objBuild.build();
		while (cursor.hasNext()) {
			BasicDBObject found = (BasicDBObject) cursor.next();
			LOG.info("User removed: " + coll.remove(found));
			json = Json.createReader(new StringReader(found.toString()))
					.readObject();
			arrBuild.add(json);
		}
		return arrBuild.build();
	}

	public JsonArray getAllUsers() {
		BasicDBObject query = new BasicDBObject();
		BasicDBObject removeId = new BasicDBObject("_id", 0);
		DBCollection coll = db.getCollection("users");
		DBCursor cursor = coll.find(query, removeId);
		JsonObjectBuilder objBuild = Json.createObjectBuilder();
		JsonArrayBuilder arrBuild = Json.createArrayBuilder();
		JsonObject json = objBuild.build();
		while (cursor.hasNext()) {
			BasicDBObject found = (BasicDBObject) cursor.next();
			LOG.info("Found User: " + found.toString());
			json = Json.createReader(new StringReader(found.toString()))
					.readObject();
			arrBuild.add(json);
		}
		return arrBuild.build();
	}
	
//	public static void main(String[] args){
//		MongoDBController test = new MongoDBController(true);
//		System.out.println(test.getController("demo", "wildfly-wildfly-apache"));
//	}

}
