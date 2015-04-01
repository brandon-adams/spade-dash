package com.nwt.spade.controllers;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;

@Service
@PropertySource("config/application.properties")
public class MesosController {

	private static final Logger LOG = LoggerFactory
			.getLogger(MesosController.class);

	private MongoDBController db;
	@Value("${mesos.master.host}")
	private String masterHost;
	@Value("${mesos.master.port}")
	private String masterPort;
	@Value("${mesos.slave.port}")
	private String slavePort;
	@Value("${mesos.slave.endpoint}")
	private String slaveEndpoint;
	@Value("${mesos.master.endpoint}")
	private String masterEndpoint;
	@Value("${mesos.slave.hosts}")
	private String[] slaves;

	public MesosController() {
		db = new MongoDBController(true);
		masterHost = "192.168.4.40";
		slavePort = "5051";
		masterPort = "5050";
		// endpoint = "/metrics";
	}

	@Autowired
	public MesosController(MongoDBController db) {
		this.db = db;
	}

	@PostConstruct
	public void init() {
		TimerTask updateTask = new UpdateTasks(this);
		Timer timer = new Timer(true);
		LOG.info("Setting TimerTask in MesosController");
		// scheduling the task at fixed rate delay
		timer.scheduleAtFixedRate(updateTask, 15 * 1000, 10 * 1000);
	}

	public JsonArray listAllTasks() {

		return db.getAllTasks();
	}

	public JsonArray listAllSlaves() {
		return db.getAllSlaves();
	}

	public void updateMesosStats() {
		JsonObjectBuilder objBuild = Json.createObjectBuilder();
		JsonArrayBuilder slaveArrBuild = Json.createArrayBuilder();
		JsonArrayBuilder taskArrBuild = Json.createArrayBuilder();

		String slavesPayload = mesosApiRequest(masterHost, masterPort,
				masterEndpoint + "/registry");
		JsonObject slavesJson = Json.createReader(
				new StringReader(slavesPayload)).readObject();

		JsonArray dbPods = db.getAllPods("all");

		for (String slave : slaves) {
			String tasksPayload = mesosApiRequest(slave, slavePort,
					slaveEndpoint + "/state.json");
			JsonObject tasksJson = Json.createReader(
					new StringReader(tasksPayload)).readObject();

			String statsPayload = mesosApiRequest(slave, slavePort,
					"/metrics/snapshot");
			JsonObject statsJson = Json.createReader(
					new StringReader(statsPayload)).readObject();

			if (!tasksJson.getJsonArray("frameworks").isEmpty()) {
				LOG.debug("FRAMEWORKS NOT EMPTY");
				for (JsonValue framework : tasksJson.getJsonArray("frameworks")) {
					if (!((JsonObject) framework).getJsonArray("executors")
							.isEmpty()) {
						LOG.debug("FOUND EXECUTOR: "
								+ tasksJson.getJsonArray("executors"));
						for (JsonValue executor : ((JsonObject) framework)
								.getJsonArray("executors")) {
							if (((JsonObject) executor).getString("id").equals(
									"KubeleteExecutorID")) {
								JsonArray kubeTasks = ((JsonObject) executor)
										.getJsonArray("tasks");
								if (!kubeTasks.isEmpty()) {
									LOG.debug("TASKS NOT EMPTY");
									for (JsonValue jval : kubeTasks) {
										JsonObjectBuilder taskBuild = Json
												.createObjectBuilder();
										String taskName = ((JsonObject) jval)
												.getString("name");
										String id = ((JsonObject) jval)
												.getString("id");
										String state = ((JsonObject) jval)
												.getString("state");
										String slaveId = ((JsonObject) jval)
												.getString("slave_id");
										double cpu = Double
												.parseDouble(((JsonObject) jval)
														.getJsonObject(
																"resources")
														.get("cpus").toString())
												/ Double.parseDouble(statsJson
														.get("slave/cpus_total")
														.toString());
										double disk = Double
												.parseDouble(((JsonObject) jval)
														.getJsonObject(
																"resources")
														.get("disk").toString())
												/ Double.parseDouble(statsJson
														.get("slave/disk_total")
														.toString());
										double mem = Double
												.parseDouble(((JsonObject) jval)
														.getJsonObject(
																"resources")
														.get("mem").toString())
												/ Double.parseDouble(statsJson
														.get("slave/mem_total")
														.toString());

										taskBuild.add("name", taskName);
										taskBuild.add("id", id);
										taskBuild.add("state", state);
										taskBuild.add("slaveId", slaveId);
										taskBuild.add("cpuPercent", cpu * 100);
										taskBuild
												.add("diskPercent", disk * 100);
										taskBuild.add("memPercent", mem * 100);
										for (JsonValue pod : dbPods) {
											try {
											String sid = ((JsonObject) pod)
													.getJsonObject(
															"annotations")
													.getString(
															"k8s_mesosphere_io/taskId");
											String podName = ((JsonObject) pod)
													.getJsonObject("labels").getString("name");
											if (id.equalsIgnoreCase(sid))
												taskBuild.add("podName",
														podName);
											} catch (Exception e){
												LOG.error(e.getLocalizedMessage());
											}
										}

										JsonObject taskJson = taskBuild.build();
										LOG.info("Synched task -> task with Mesos: "
												+ db.updateTask(taskJson
														.toString()));
										taskArrBuild.add(taskJson);
									}
								} else {
									LOG.info("No Tasks running on the Mesos Slave");
								}
							}
						}
					}
				}
			}
		}
		JsonArray tasks = taskArrBuild.build();

		for (JsonValue jval : slavesJson.getJsonObject("slaves").getJsonArray(
				"slaves")) {
			JsonObject info = ((JsonObject) jval).getJsonObject("info");
			String id = info.getJsonObject("id").getString("value");
			String hostname = info.getString("hostname");
			JsonArray resources = info.getJsonArray("resources");
			JsonArrayBuilder recArrBuild = Json.createArrayBuilder();
			for (JsonValue rec : resources) {
				recArrBuild.add(rec);
			}
			objBuild.add("id", id);
			objBuild.add("hostname", hostname);
			objBuild.add("resources", recArrBuild.build());
			JsonObject slaveObj = objBuild.build();
			slaveArrBuild.add(slaveObj);
			LOG.info("Synched slave -> slave with Mesos: "
					+ db.updateSlave(slaveObj.toString()));
		}

		JsonArray slaves = slaveArrBuild.build();

		JsonArray dbSlaves = db.getAllSlaves();
		if (!dbSlaves.isEmpty()) {
			for (JsonValue jval : dbSlaves) {
				boolean remove = false;
				for (JsonValue slaveval : slaves) {
					if (((JsonObject) slaveval).getString("id").equals(
							((JsonObject) jval).getString("id"))) {
						remove = true;
					}
				}
				if (!remove) {
					LOG.info("Deleting leftover slave: "
							+ db.deleteSlave(((JsonObject) jval)
									.getString("id")));
				}
			}

		}

		JsonArray dbTasks = db.getAllTasks();
		if (!dbTasks.isEmpty()) {
			for (JsonValue jval : dbTasks) {
				boolean remove = false;
				for (JsonValue taskval : tasks) {
					if (((JsonObject) taskval).getString("id").equals(
							((JsonObject) jval).getString("id"))) {
						remove = true;
					}
				}
				if (!remove) {
					LOG.info("Deleting leftover task: "
							+ db.deleteTask(((JsonObject) jval).getString("id")));
				}
			}
		}
		objBuild.add("items", tasks);

		// return objBuild.build();
	}

	private String mesosApiRequest(String host, String port, String link) {
		String line;
		StringBuffer jsonString = new StringBuffer();
		try {
			URL url = new URL("http://" + host + ":" + port + link);

			HttpURLConnection connection = (HttpURLConnection) url
					.openConnection();

			connection.setDoInput(true);
			connection.setDoOutput(true);
			connection.setRequestMethod("GET");
			connection.setRequestProperty("Accept", "application/json");
			connection.setRequestProperty("Content-Type",
					"application/json; charset=UTF-8");
			BufferedReader br = new BufferedReader(new InputStreamReader(
					connection.getInputStream()));
			while ((line = br.readLine()) != null) {
				jsonString.append(line);
			}
			br.close();
			connection.disconnect();
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e.getMessage());
		}

		return jsonString.toString();
	}

	// public static void main(String[] args){
	// MesosController mesos = new MesosController();
	// System.out.println(mesos.taskRequest("10251", "/debug/registry/tasks"));
	// //System.out.println(mesos.mesosApiRequest("/stats.json"));
	// }

	public static class UpdateTasks extends TimerTask {

		private MesosController mesosCont;

		public UpdateTasks(MesosController mesosController) {
			super();
			mesosCont = mesosController;
		}

		@Override
		public void run() {
			mesosCont.updateMesosStats();
		}
	}

}
