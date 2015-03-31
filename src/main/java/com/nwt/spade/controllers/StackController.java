package com.nwt.spade.controllers;

import java.io.StringReader;
import java.util.Timer;
import java.util.TimerTask;

import javax.annotation.PostConstruct;
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

	@PostConstruct
	public void init() {
		TimerTask updateTask = new UpdateStacks(this);
		Timer timer = new Timer(true);
		LOG.info("Setting TimerTask in StackController");
		// scheduling the task at fixed rate delay
		timer.scheduleAtFixedRate(updateTask, 15 * 1000, 10 * 1000);
	}

	public JsonArray createStack(String project, String template)
			throws KubernetesOperationException {
		JsonArrayBuilder arrBuild = Json.createArrayBuilder();
		JsonObjectBuilder objBuild = Json.createObjectBuilder();
		JsonObject jsonInput = Json.createReader(new StringReader(template))
				.readObject();

		String stackName = jsonInput.getString("id").toLowerCase();
		String stackProj = jsonInput.getString("project").toLowerCase();
		JsonArray controllers = jsonInput.getJsonArray("controllers");

		for (JsonValue cont : controllers) {
			String os = ((JsonObject) cont).getString("os").toLowerCase();
			String app = ((JsonObject) cont).getString("app").toLowerCase();
			String name = ((JsonObject) cont).getString("name").toLowerCase();
			// String stack = ((JsonObject)
			// cont).getString("stack").toLowerCase();
			int replicas = ((JsonObject) cont).getInt("replicas");
			String imageName = db.getImage(project, os, app).getJsonObject(0)
					.getString("image");
			arrBuild.add(kc
					.createController(stackName, name, project, imageName, os,
							app, replicas).getJsonObject(0).getString("id"));
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
		JsonObject dbStack = db.getStack(project, id).getJsonObject(0);
		for (JsonValue jval : dbStack.getJsonArray("controllers")) {
			try {
				kc.deleteController(project, jval.toString());
			} catch (KubernetesOperationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

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

	public void updateAllStacks() {
		JsonArray dbPods = db.getAllPods("all");
		JsonArray dbConts = db.getAllControllers("all");
		JsonArray dbStacks = db.getAllStacks("all");
		JsonObjectBuilder objBuild = Json.createObjectBuilder();
		if (!dbStacks.isEmpty()) {
			for (JsonValue stack : dbStacks) {
				LOG.info("Found Stack: " + stack);
				objBuild = Json.createObjectBuilder();
				objBuild.add("id", ((JsonObject) stack).getString("id"));
				objBuild.add("project",
						((JsonObject) stack).getString("project"));
				JsonArrayBuilder arrBuild = Json.createArrayBuilder();
				for (JsonValue pod : dbPods) {
					String ownStack = ((JsonObject) pod)
							.getJsonObject("labels").getString("stack");
					if (ownStack.equals(((JsonObject) stack).getString("id"))) {
						arrBuild.add(((JsonObject) pod).getJsonObject("labels")
								.getString("name"));
					}
				}
				objBuild.add("pods", arrBuild.build());

				for (JsonValue cont : dbConts) {
					String ownStack = ((JsonObject) cont).getJsonObject(
							"labels").getString("stack");
					if (ownStack.equals(((JsonObject) stack).getString("id"))) {
						arrBuild.add(((JsonObject) cont)
								.getJsonObject("labels").getString("name"));
					}
				}
				objBuild.add("controllers", arrBuild.build());
				LOG.info("Stack updated: "
						+ db.updateStack(
								((JsonObject) stack).getString("project"),
								objBuild.build().toString()));
			}
		} else {
			LOG.info("No Stacks found");
			LOG.info("Containers found");
			LOG.debug("NO STACKS AND SOME CONTROLLERS");
			for (JsonValue cont : dbConts) {
				objBuild = Json.createObjectBuilder();
				JsonArrayBuilder arrBuild = Json.createArrayBuilder();
				String ownStack = ((JsonObject) cont).getJsonObject("labels")
						.getString("stack");
				String ownProj = ((JsonObject) cont).getJsonObject("labels")
						.getString("stack");
				arrBuild.add(((JsonObject) cont).getJsonObject("labels")
						.getString("name"));
				LOG.debug("CREATING NEW STACK");
				objBuild.add("id", ownStack);
				objBuild.add("project", ownProj);
				objBuild.add("controllers", arrBuild.build());
				String payload = objBuild.build().toString();
				LOG.info("Stack payload: " + payload);
				LOG.info("Stack updated: " + db.updateStack(ownProj, payload));
			}
		}
		if (dbConts.isEmpty() && !dbStacks.isEmpty()) {
			for (JsonValue stack : dbStacks) {
				LOG.info("Empty Stack deleted: "
						+ db.deleteStack(
								((JsonObject) stack).getString("project"),
								((JsonObject) stack).getString("id")));
			}
		}
	}

//	public static void main(String[] args) {
//		StackController test = new StackController(null, new MongoDBController(
//				true));
//
//		test.updateAllStacks();
//	}

	public static class UpdateStacks extends TimerTask {

		private StackController stackCont;

		public UpdateStacks(StackController stackController) {
			super();
			stackCont = stackController;
		}

		@Override
		public void run() {
			stackCont.updateAllStacks();
		}
	}
}
