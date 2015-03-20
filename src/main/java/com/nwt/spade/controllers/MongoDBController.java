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

	public JsonArray addTemplate(String project, String template, String imageName) {
		BasicDBObject doc = (BasicDBObject) JSON.parse(template);
		DBCollection coll = db.getCollection("templates");
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

	public JsonArray getTemplate(String project, String imageName) {
		BasicDBObject query = new BasicDBObject();
		BasicDBObject removeId = new BasicDBObject("_id", 0);
		query.put("desiredState.podTemplate.labels.image", imageName);
		query.put("desiredState.podTemplate.labels.project", project);
		DBCollection coll = db.getCollection("templates");
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

	public JsonArray deleteTemplate(String project, String imageName) {
		BasicDBObject query = new BasicDBObject();
		BasicDBObject removeId = new BasicDBObject("_id", 0);
		query.put("desiredState.podTemplate.labels.image", imageName);
		query.put("desiredState.podTemplate.labels.project", project);
		DBCollection coll = db.getCollection("templates");
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

	public JsonArray getAllTemplates(String project) {
		BasicDBObject query = new BasicDBObject();
		if (!project.equals("all")) query.put("desiredState.podTemplate.labels.project", project);
		BasicDBObject removeId = new BasicDBObject("_id", 0);
		DBCollection coll = db.getCollection("templates");
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

	public JsonArray addEnv(String project, String template) {
		BasicDBObject doc = (BasicDBObject) JSON.parse(template);
		LOG.debug("Template: " + template);
		
		doc.append("_id", doc.get("id"));
		DBCollection coll = db.getCollection("environments");
		BasicDBObject query = new BasicDBObject();
		query.put("_id", doc.get("_id"));
		query.put("desiredState.podTemplate.labels.project", project);
		DBCursor cursor = coll.find(query);
		JsonObjectBuilder objBuild = Json.createObjectBuilder();
		JsonArrayBuilder arrBuild = Json.createArrayBuilder();
		JsonObject json = objBuild.build();
		if (doc.containsValue("Status")) return arrBuild.add(json).build();
		if (cursor.count() > 0) {
			LOG.info("Environment Exists");
			json = Json
					.createReader(new StringReader(cursor.next().toString()))
					.readObject();
			return arrBuild.add(json).build();
		} else {
			LOG.info("Inserted: " + coll.insert(doc).toString());
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

	public JsonArray updateEnv(String project, String template) {
		BasicDBObject doc = (BasicDBObject) JSON.parse(template);
		//LOG.debug(template);
		doc.append("_id", doc.get("id"));
		DBCollection coll = db.getCollection("environments");
		BasicDBObject query = new BasicDBObject();
		query.put("_id", doc.get("id"));
		//query.put("desiredState.podTemplate.labels.project", project);
		DBCursor cursor = coll.find(query);
		JsonObjectBuilder objBuild = Json.createObjectBuilder();
		JsonArrayBuilder arrBuild = Json.createArrayBuilder();
		JsonObject json = objBuild.build();
		if (cursor.count() > 0) {
			LOG.info("Environment found and updated: "
					+ coll.update(query, doc, true, false));
			json = Json.createReader(new StringReader(template)).readObject();
			return arrBuild.add(json).build();
		} else {
			LOG.info("Environment inserted: " + coll.insert(doc));
			json = Json.createReader(new StringReader(template)).readObject();
			return arrBuild.add(json).build();
		}
	}

	public JsonArray getEnv(String project, String id) {
		BasicDBObject query = new BasicDBObject();
		BasicDBObject removeId = new BasicDBObject("_id", 0);
		query.put("id", id);
		//query.put("desiredState.podTemplate.labels.project", project);
		DBCollection coll = db.getCollection("environments");
		DBCursor cursor = coll.find(query, removeId);
		JsonObjectBuilder objBuild = Json.createObjectBuilder();
		JsonArrayBuilder arrBuild = Json.createArrayBuilder();
		JsonObject json = objBuild.build();
		while (cursor.hasNext()) {
			BasicDBObject found = (BasicDBObject) cursor.next();
			LOG.info("Found Environment: " + found.toString());
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

	public JsonArray deleteEnv(String project, String id) {
		BasicDBObject query = new BasicDBObject();
		BasicDBObject removeId = new BasicDBObject("_id", 0);
		query.put("id", id);
		//query.put("desiredState.podTemplate.labels.project", project);
		DBCollection coll = db.getCollection("environments");
		DBCursor cursor = coll.find(query, removeId);
		JsonObjectBuilder objBuild = Json.createObjectBuilder();
		JsonArrayBuilder arrBuild = Json.createArrayBuilder();
		JsonObject json = objBuild.build();
		while (cursor.hasNext()) {
			BasicDBObject found = (BasicDBObject) cursor.next();
			LOG.info("Environment removed: " + coll.remove(found));
			json = Json.createReader(new StringReader(found.toString()))
					.readObject();
			arrBuild.add(json);
		}
		return arrBuild.build();
	}

	public JsonArray getAllEnvs(String project) {
		LOG.debug("PROJECT: " + project);
		BasicDBObject query = new BasicDBObject();
		//query.put("desiredState.podTemplate.labels.project", project); NEED TO IMPLEMENT PROJECT BASED
		BasicDBObject removeId = new BasicDBObject("_id", 0);
		DBCollection coll = db.getCollection("environments");
		DBCursor cursor = coll.find(query, removeId);
		JsonObjectBuilder objBuild = Json.createObjectBuilder();
		JsonArrayBuilder arrBuild = Json.createArrayBuilder();
		JsonObject json = objBuild.build();
		while (cursor.hasNext()) {
			BasicDBObject found = (BasicDBObject) cursor.next();
			LOG.info("Found Environment: " + found.toString());
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
	
	/*
	 * public static void main(String[] args) {
	 * 
	 * MongoDBController mo = new MongoDBController(); String temp =
	 * "{\"id\":\"jboss-pod\",\"kind\":\"Pod\",\"apiVersion\":\"v1beta1\"," +
	 * "\"desiredState\":{\"manifest\":{\"version\":\"v1beta1\",\"id\":\"jboss-pod\","
	 * + "\"containers\":[{\"name\":\"jboss-wildfly\",\"image\":\"jboss-image\""
	 * + ",\"ports\":[{\"containerPort\":8080,\"hostPort\":31081}," +
	 * "{\"containerPort\":9990,\"hostPort\":31090}]}]}},\"labels\":" +
	 * "{\"name\":\"jboss\",\"type\":\"jboss-pod\", \"image\":\"jboss-image\"}}"
	 * ; // mo.addTemplate(temp, "jboss-image"); //
	 * mo.getTemplate("jboss-image"); String env =
	 * "{ \"kind\": \"Pod\", \"id\": \"apache-pod\", \"uid\": " +
	 * "\"d977ec0f-bb82-11e4-a5e9-fa163e737490\", \"creationTimestamp\": " +
	 * "\"2015-02-23T17:38:59Z\", \"selfLink\": " +
	 * "\"/api/v1beta1/pods/apache-pod?namespace=default\", \"resourceVersion\": 50186, "
	 * + "\"apiVersion\": \"v1beta1\", \"namespace\": \"default\", " +
	 * "\"labels\": { \"app\": \"apache\", \"image\": \"sewatech/modcluster\", "
	 * +
	 * "\"name\": \"apache-httpd\", \"os\": \"ubuntu\", \"type\": \"apache-pod\" }, "
	 * +
	 * "\"desiredState\": { \"manifest\": { \"version\": \"v1beta2\", \"id\": \"\", "
	 * + "\"volumes\": null, \"containers\": [ { \"name\": \"apache-httpd\"," +
	 * " \"image\": \"sewatech/modcluster\", \"ports\": [ { \"hostPort\": 31080,"
	 * +
	 * " \"containerPort\": 80, \"protocol\": \"TCP\" } ], \"imagePullPolicy\": \"\" } ], "
	 * +
	 * "\"restartPolicy\": { \"always\": {} }, \"dnsPolicy\": \"ClusterFirst\" } },"
	 * + " \"currentState\": { \"manifest\": { \"version\": \"\", \"id\": \"\","
	 * + " \"volumes\": null, \"containers\": null, \"restartPolicy\": {} }, " +
	 * "\"status\": \"Waiting\" } }"; //mo.addEnv(env);
	 * //System.out.println(mo.getImage("ubuntu", "wildfly"));
	 * System.out.println(mo.getAllImages()); // KubernetesController test = new
	 * KubernetesController();
	 * 
	 * }
	 */

}
