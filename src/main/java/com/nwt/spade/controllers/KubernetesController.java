package com.nwt.spade.controllers;

import io.fabric8.kubernetes.api.KubernetesHelper;
import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.ContainerManifest;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodState;
import io.fabric8.kubernetes.api.model.PodTemplate;
import io.fabric8.kubernetes.api.model.Port;
import io.fabric8.kubernetes.api.model.ReplicationController;
import io.fabric8.kubernetes.api.model.ReplicationControllerState;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import javax.annotation.PostConstruct;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.nwt.spade.exceptions.KubernetesOperationException;

@Service
@PropertySource("config/application.properties")
public class KubernetesController {

	private static final Logger LOG = LoggerFactory
			.getLogger(KubernetesController.class);

	private MongoDBController db;
	@Value("${kubernetes.host}")
	private String host;
	@Value("${kubernetes.api.port}")
	private String port;
	@Value("${kubernetes.api.endpoint}")
	private String endpoint;

	public KubernetesController() {
		db = new MongoDBController(true);
		host = "192.168.4.52";
		port = "8888";
		endpoint = "/api/v1beta1/pods";
	}

	@Autowired
	public KubernetesController(MongoDBController db) {
		this.db = db;
	}

	@PostConstruct
	public void init() {
		TimerTask updateTask = new UpdateStatus(this);
		Timer timer = new Timer(true);
		LOG.info("Setting TimerTask in KubernetesController");
		// scheduling the task at fixed rate delay
		timer.scheduleAtFixedRate(updateTask, 15 * 1000, 10 * 1000);
	}

	public void createTemplate(String project, String imageName, String os,
			String app) {

	}

	public JsonArray createEnv(String name, String project, String imageName,
			String os, String app, int replicas)
			throws KubernetesOperationException {
		String payload = null;
		// Pod pod = new Pod();PodState ps = new PodState();
		// ContainerManifest cm = new ContainerManifest();
		// Container c = new Container();
		switch (imageName) {
		case "sewatech/modcluster":
			payload = createApacheJSON(name, project, imageName, os, app,
					replicas);
			break;
		case "bradams/devops:nginx-ubuntu":
			payload = createNginxJSON(name, project, imageName, os, app,
					replicas);
			break;
		case "bradams/devops:wildfly-ubuntu":
			payload = createJbossJSON(name, project, imageName, os, app,
					replicas);
			break;
		case "bradams/devops:tomcat-ubuntu":
			payload = createTomcatJSON(name, project, imageName, os, app,
					replicas);
			break;
		case "partlab/ubuntu-mongodb":
			payload = createMongoDBJSON(name, project, imageName, os, app,
					replicas);
			break;
		case "bradams/devops:mysql-ubuntu":
			payload = createMySQLJSON(name, project, imageName, os, app,
					replicas);
			break;
		case "bradams/devops:apache-fedora":
			payload = createApacheJSON(name, project, imageName, os, app,
					replicas);
			break;
		case "bradams/devops:nginx-fedora":
			payload = createNginxJSON(name, project, imageName, os, app,
					replicas);
			break;
		case "bradams/devops:cluster":
			payload = createJbossJSON(name, project, imageName, os, app,
					replicas);
			break;
		case "bradams/devops:tomcat-fedora":
			payload = createTomcatJSON(name, project, imageName, os, app,
					replicas);
			break;
		case "bradams/devops:mongodb-fedora":
			payload = createMongoDBJSON(name, project, imageName, os, app,
					replicas);
			break;
		case "jdeathe/centos-ssh-mysql":
			payload = createMySQLJSON(name, project, imageName, os, app,
					replicas);
			break;
		default:
			payload = "{}";
		}

		// payload = db.getTemplate(imageName).getString("0");
		JsonArray added = db.addTemplate(project, payload, imageName);
		LOG.debug("Added template: " + added.getJsonObject(0).toString());

		String jsonString = kubeApiRequest("POST", endpoint
				+ "/replicationControllers/", payload);

		LOG.debug("Payload: " + payload);
		LOG.debug("Return from Kube: " + jsonString);
		return db.addEnv(project, jsonString);
	}

	public JsonObject updateEnv(String project, String id)
			throws KubernetesOperationException {
		JsonArray env = db.getEnv(project, id);
		JsonObjectBuilder objBuild = Json.createObjectBuilder();
		objBuild.add("api", "v0.0.4");
		objBuild.add("time", new Date().getTime());
		objBuild.add("type", "UpdateEnv");
		// JsonArray val = objBuild.build();

		String selfLink = env.getJsonObject(0).getString("selfLink");
		String jsonString = kubeApiRequest("GET", selfLink, null);

		try {
			JsonArray val = db.updateEnv(project, jsonString.toString());
			objBuild.add("items", val);
			// LOG.debug("STATUS: "
			// + val.getJsonObject(0).getJsonObject("currentState")
			// .getString("status"));
		} catch (NullPointerException ne) {
			ne.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return objBuild.build();
	}

	public JsonObject updateAllEnvs(String project)
			throws KubernetesOperationException {// COME BACK TO THIS ONE
		// AGAIN

		JsonObjectBuilder objBuild = Json.createObjectBuilder();
		// JsonObject val = objBuild.build();

		String envListStr = kubeApiRequest("GET", endpoint
				+ "/replicationControllers/", null);
		JsonArray envList = Json.createReader(new StringReader(envListStr))
				.readObject().getJsonArray("items");
		if (!envList.isEmpty()) {
			for (JsonValue jval : envList) {
				// String proj = ((JsonObject)
				// jval).getJsonObject("labels").getString("project");
				JsonArray val = db.updateEnv(project, jval.toString());
				objBuild.add("items", val);
				LOG.info("Synched repl cont -> env with KubeApi: " + val);
			}
		} else {
			LOG.info("No replication controllers running on the master");
		}

		String podListStr = kubeApiRequest("GET", endpoint + "/pods/", null);
		JsonArray podList = Json.createReader(new StringReader(podListStr))
				.readObject().getJsonArray("items");
		if (!podList.isEmpty()) {
			for (JsonValue jval : podList) {
				String podString = jval.toString().replaceAll(
						"k8s.mesosphere.io", "k8s_mesosphere_io");
				JsonArray val = db.updatePod(project, podString);
				objBuild.add("items", val);
				LOG.info("Synched pod -> pod with KubeApi: " + val);
			}
		} else {
			LOG.info("No pods running on the master");
		}

		JsonArray dbEnvs = db.getAllEnvs(project);
		if (!dbEnvs.isEmpty()) {
			for (JsonValue jval : dbEnvs) {
				// if(((JsonObject)jval).getString("kind").equals("Status"))
				// break;
				boolean remove = false;
				LOG.debug("DBENV ID: " + ((JsonObject) jval).toString());
				for (JsonValue envval : envList) {
					LOG.debug("KBENV ID: "
							+ ((JsonObject) envval).getString("id"));
					if (((JsonObject) envval).getString("id").equals(
							((JsonObject) jval).getString("id"))) {
						remove = true;
					}
				}
				if (!remove) {
					LOG.debug("Deleting leftover env: "
							+ db.deleteEnv(project,
									((JsonObject) jval).getString("id")));
				}
			}

		}

		JsonArray dbPods = db.getAllPods(project);
		if (!dbPods.isEmpty()) {
			for (JsonValue jval : dbPods) {
				boolean remove = false;
				// LOG.debug("DBPOD ID: " + ((JsonObject)
				// jval).getString("id"));
				for (JsonValue podval : podList) {
					// LOG.debug("KBPOD ID: " + ((JsonObject)
					// podval).getString("id"));
					if (((JsonObject) podval).getString("id").equals(
							((JsonObject) jval).getString("id"))) {
						remove = true;
					}
				}
				if (!remove) {
					LOG.debug("Deleting leftover pod: "
							+ db.deletePod(project,
									((JsonObject) jval).getString("id")));
				}
			}
		}

		return objBuild.build();
	}

	public JsonArray getTemplate(String project, String imageName) {

		return db.getTemplate(project, imageName);
	}

	public JsonArray deleteTemplate(String project, String imageName) {

		return db.deleteTemplate(project, imageName);
	}

	public JsonArray getAllTemplates(String project) {

		return db.getAllTemplates(project);
	}

	public JsonArray getEnv(String project, String id) {

		return db.getEnv(project, id);
	}

	public JsonArray deleteEnv(String project, String id)
			throws KubernetesOperationException {
		JsonObject env = db.getEnv(project, id).getJsonObject(0);
		String selfLink = env.getString("selfLink");
		String selector = env.getJsonObject("desiredState")
				.getJsonObject("replicaSelector").getString("type");
		JsonArray pods = db.getAllPods(project);
		for (JsonValue jval : pods) {
			if (((JsonObject) jval).getJsonObject("labels").getString("type")
					.equalsIgnoreCase(selector)) {
				LOG.debug("Deleting pod: "
						+ ((JsonObject) jval).getString("id"));
				kubeApiRequest("DELETE",
						((JsonObject) jval).getString("selfLink"), null);
				db.deletePod(project, ((JsonObject) jval).getString("id"));
			}
		}
		String result = kubeApiRequest("DELETE", selfLink, null);
		// Need to check that the element is actually deleted in the response,
		// otherwise throw Exception
		return db.deleteEnv(project, id);
	}

	public JsonArray getAllEnvs(String project) {

		return db.getAllEnvs(project);
	}

	public JsonArray getAllPods(String project) {

		return db.getAllPods(project);
	}

	private String createMongoDBJSON(String name, String project,
			String imageName, String os, String app, int replicas) {
		JsonObject mongoPod = Json
				.createObjectBuilder()
				.add("id", name)
				.add("kind", "ReplicationController")
				.add("apiVersion", "v1beta1")
				.add("labels", Json.createObjectBuilder().add("name", name))
				.add("desiredState",
						Json.createObjectBuilder()
								.add("replicas", replicas)
								.add("replicaSelector",
										Json.createObjectBuilder().add("type",
												"mongodb-pod"))
								.add("podTemplate",
										Json.createObjectBuilder()
												.add("desiredState",
														Json.createObjectBuilder()
																.add("manifest",
																		Json.createObjectBuilder()
																				.add("version",
																						"v1beta1")
																				.add("id",
																						"mongodb-pod")
																				.add("containers",
																						Json.createArrayBuilder()
																								.add(Json
																										.createObjectBuilder()
																										.add("name",
																												"mongodb")
																										.add("image",
																												imageName)
																										.add("cpu",
																												1000)
																										.add("ports",
																												Json.createArrayBuilder()
																														.add(Json
																																.createObjectBuilder()
																																.add("containerPort",
																																		27017)
																																.add("hostPort",
																																		31017)))))))
												.add("labels",
														Json.createObjectBuilder()
																.add("name",
																		"mongodb")
																.add("type",
																		"mongodb-pod")
																.add("image",
																		imageName)
																.add("os", os)
																.add("app", app)
																.add("project",
																		project))))
				.build();
		return mongoPod.toString();
	}

	private String createMySQLJSON(String name, String project,
			String imageName, String os, String app, int replicas) {
		JsonObject mysqlPod = Json
				.createObjectBuilder()
				.add("id", name)
				.add("kind", "ReplicationController")
				.add("apiVersion", "v1beta1")
				.add("labels", Json.createObjectBuilder().add("name", name))
				.add("desiredState",
						Json.createObjectBuilder()
								.add("replicas", replicas)
								.add("replicaSelector",
										Json.createObjectBuilder().add("type",
												"mysql-pod"))
								.add("podTemplate",
										Json.createObjectBuilder()
												.add("desiredState",
														Json.createObjectBuilder()
																.add("manifest",
																		Json.createObjectBuilder()
																				.add("version",
																						"v1beta1")
																				.add("id",
																						"mysql-pod")
																				.add("containers",
																						Json.createArrayBuilder()
																								.add(Json
																										.createObjectBuilder()
																										.add("name",
																												"mysql")
																										.add("image",
																												imageName)
																										.add("cpu",
																												1000)
																										.add("ports",
																												Json.createArrayBuilder()
																														.add(Json
																																.createObjectBuilder()
																																.add("containerPort",
																																		3306)
																																.add("hostPort",
																																		31306)))))))
												.add("labels",
														Json.createObjectBuilder()
																.add("name",
																		"mysql")
																.add("type",
																		"mysql-pod")
																.add("image",
																		imageName)
																.add("os", os)
																.add("app", app)
																.add("project",
																		project))))
				.build();
		return mysqlPod.toString();
	}

	private String createJbossJSON(String name, String project,
			String imageName, String os, String app, int replicas) {
		JsonObject jbossPod = Json
				.createObjectBuilder()
				.add("id", name)
				.add("kind", "ReplicationController")
				.add("apiVersion", "v1beta1")
				.add("labels", Json.createObjectBuilder().add("name", name))
				.add("desiredState",
						Json.createObjectBuilder()
								.add("replicas", replicas)
								.add("replicaSelector",
										Json.createObjectBuilder().add("type",
												"jboss-pod"))
								.add("podTemplate",
										Json.createObjectBuilder()
												.add("desiredState",
														Json.createObjectBuilder()
																.add("manifest",
																		Json.createObjectBuilder()
																				.add("version",
																						"v1beta1")
																				.add("id",
																						"jboss-pod")
																				.add("containers",
																						Json.createArrayBuilder()
																								.add(Json
																										.createObjectBuilder()
																										.add("name",
																												"jboss")
																										.add("image",
																												imageName)
																										.add("cpu",
																												1000)
																										.add("ports",
																												Json.createArrayBuilder()
																														.add(Json
																																.createObjectBuilder()
																																.add("containerPort",
																																		8080)
																																.add("hostPort",
																																		31081))
																														.add(Json
																																.createObjectBuilder()
																																.add("containerPort",
																																		9990)
																																.add("hostPort",
																																		31090)))))))
												.add("labels",
														Json.createObjectBuilder()
																.add("name",
																		"jboss")
																.add("type",
																		"jboss-pod")
																.add("image",
																		imageName)
																.add("os", os)
																.add("app", app)
																.add("project",
																		project))))
				.build();
		return jbossPod.toString();
	}

	private String createTomcatJSON(String name, String project,
			String imageName, String os, String app, int replicas) {
		JsonObject tomcatPod = Json
				.createObjectBuilder()
				.add("id", name)
				.add("kind", "ReplicationController")
				.add("apiVersion", "v1beta1")
				.add("labels", Json.createObjectBuilder().add("name", name))
				.add("desiredState",
						Json.createObjectBuilder()
								.add("replicas", replicas)
								.add("replicaSelector",
										Json.createObjectBuilder().add("type",
												"tomcat-pod"))
								.add("podTemplate",
										Json.createObjectBuilder()
												.add("desiredState",
														Json.createObjectBuilder()
																.add("manifest",
																		Json.createObjectBuilder()
																				.add("version",
																						"v1beta1")
																				.add("id",
																						"tomcat-pod")
																				.add("containers",
																						Json.createArrayBuilder()
																								.add(Json
																										.createObjectBuilder()
																										.add("name",
																												"tomcat")
																										.add("image",
																												imageName)
																										.add("cpu",
																												1000)
																										.add("ports",
																												Json.createArrayBuilder()
																														.add(Json
																																.createObjectBuilder()
																																.add("containerPort",
																																		8080)
																																.add("hostPort",
																																		31081)))))))
												.add("labels",
														Json.createObjectBuilder()
																.add("name",
																		"tomcat")
																.add("type",
																		"tomcat-pod")
																.add("image",
																		imageName)
																.add("os", os)
																.add("app", app)
																.add("project",
																		project))))
				.build();
		return tomcatPod.toString();
	}

	private String createApacheJSON(String name, String project,
			String imageName, String os, String app, int replicas) {
		JsonObject apachePod = Json
				.createObjectBuilder()
				.add("id", name)
				.add("kind", "ReplicationController")
				.add("apiVersion", "v1beta1")
				.add("labels", Json.createObjectBuilder().add("name", name))
				.add("desiredState",
						Json.createObjectBuilder()
								.add("replicas", replicas)
								.add("replicaSelector",
										Json.createObjectBuilder().add("type",
												"apache-pod"))
								.add("podTemplate",
										Json.createObjectBuilder()
												.add("desiredState",
														Json.createObjectBuilder()
																.add("manifest",
																		Json.createObjectBuilder()
																				.add("version",
																						"v1beta1")
																				.add("id",
																						"apache-pod")
																				.add("containers",
																						Json.createArrayBuilder()
																								.add(Json
																										.createObjectBuilder()
																										.add("name",
																												"apache")
																										.add("image",
																												imageName)
																										.add("cpu",
																												1000)
																										.add("ports",
																												Json.createArrayBuilder()
																														.add(Json
																																.createObjectBuilder()
																																.add("containerPort",
																																		80)
																																.add("hostPort",
																																		31080)))))))
												.add("labels",
														Json.createObjectBuilder()
																.add("name",
																		"apache")
																.add("type",
																		"apache-pod")
																.add("image",
																		imageName)
																.add("os", os)
																.add("app", app)
																.add("project",
																		project))))
				.build();
		return apachePod.toString();
	}

	private String createNginxJSON(String name, String project,
			String imageName, String os, String app, int replicas) {
		JsonObject nginxPod = Json
				.createObjectBuilder()
				.add("id", name)
				.add("kind", "ReplicationController")
				.add("apiVersion", "v1beta1")
				.add("labels", Json.createObjectBuilder().add("name", name))
				.add("desiredState",
						Json.createObjectBuilder()
								.add("replicas", replicas)
								.add("replicaSelector",
										Json.createObjectBuilder().add("type",
												"nginx-pod"))
								.add("podTemplate",
										Json.createObjectBuilder()
												.add("desiredState",
														Json.createObjectBuilder()
																.add("manifest",
																		Json.createObjectBuilder()
																				.add("version",
																						"v1beta1")
																				.add("id",
																						"nginx-pod")
																				.add("containers",
																						Json.createArrayBuilder()
																								.add(Json
																										.createObjectBuilder()
																										.add("name",
																												"nginx")
																										.add("image",
																												imageName)
																										.add("cpu",
																												1000)
																										.add("ports",
																												Json.createArrayBuilder()
																														.add(Json
																																.createObjectBuilder()
																																.add("containerPort",
																																		8080)
																																.add("hostPort",
																																		31080)))))))
												.add("labels",
														Json.createObjectBuilder()
																.add("name",
																		"nginx")
																.add("type",
																		"nginx-pod")
																.add("image",
																		imageName)
																.add("os", os)
																.add("app", app)
																.add("project",
																		project))))
				.build();
		return nginxPod.toString();
	}

	private String kubeApiRequest(String method, String link, String payload) {
		String line;
		StringBuffer jsonString = new StringBuffer();
		try {
			URL url = new URL("http://" + host + ":" + port + link);

			// LOG.debug("HOST: " + host);
			HttpURLConnection connection = (HttpURLConnection) url
					.openConnection();

			connection.setDoInput(true);
			connection.setDoOutput(true);
			connection.setRequestMethod(method.toUpperCase());
			connection.setRequestProperty("Accept", "application/json");
			connection.setRequestProperty("Content-Type",
					"application/json; charset=UTF-8");
			if (method.equalsIgnoreCase("POST")) {
				OutputStreamWriter writer = new OutputStreamWriter(
						connection.getOutputStream(), "UTF-8");
				writer.write(payload);
				writer.close();
			}
			BufferedReader br = new BufferedReader(new InputStreamReader(
					connection.getInputStream()));
			while ((line = br.readLine()) != null) {
				jsonString.append(line);
			}
			br.close();
			connection.disconnect();
		} catch (Exception e) {
			// e.printStackTrace();
			// throw new RuntimeException(e.getMessage());
		}

		return jsonString.toString();
	}

	private void podTest() {
		ReplicationController rep = new ReplicationController();
		rep.setId("mongodb-controller");
		rep.setKind("ReplicationController");
		Map<String, String> repLabels = new HashMap<String, String>();
		repLabels.put("name", "mongodb-controller");
		rep.setLabels(repLabels);

		ReplicationControllerState repState = new ReplicationControllerState();
		repState.setReplicas(1);
		Map<String, String> repSelect = new HashMap<String, String>();
		repSelect.put("type", "mongodb-pod");
		repState.setReplicaSelector(repSelect);
		
		PodTemplate podTemp = new PodTemplate();
		PodState podState = new PodState();

		ContainerManifest manifest = new ContainerManifest();
		manifest.setVersion("v1beta1");
		manifest.setId("mongo-pod");
		List<Container> containers = new ArrayList<>();
		for (int i = 0; i < 1; i++) {
			Container mongodb = new Container();
			mongodb.setName("mongodb");
			mongodb.setImage("partlab/ubuntu-mongodb");
			List<Port> ports = new ArrayList<Port>();
			Port port = new Port();
			port.setContainerPort(27017);
			port.setHostPort(31017);
			ports.add(port);
			mongodb.setPorts(ports);
			containers.add(mongodb);
		}

		manifest.setContainers(containers);
		podState.setManifest(manifest);
		podTemp.setDesiredState(podState);

		Map<String, String> labels = new HashMap<String, String>();
		labels.put("name", "mongodb");
		labels.put("type", "mongodb-pod");
		labels.put("image", "partlab/ubuntu-mongodb");
		labels.put("os", "ubuntu");
		labels.put("app", "mongodb");
		podTemp.setLabels(labels);
		repState.setPodTemplate(podTemp);
		rep.setDesiredState(repState);
		/*String json = createMongoDBJSON("mongodb-controller", "demo",
				"partlab/ubuntu-mongodb", "ubuntu", "mongodb", 1);
		try {
			pod = (Pod) KubernetesHelper.loadJson(json);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		try {
			System.out.println(KubernetesHelper.toJson(rep));
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		KubernetesController test = new KubernetesController();
		test.podTest();
	}

	public static class UpdateStatus extends TimerTask {

		private KubernetesController kubeCont;

		public UpdateStatus(KubernetesController kubernetesController) {
			super();
			kubeCont = kubernetesController;
		}

		@Override
		public void run() {
			try {
				kubeCont.updateAllEnvs("demo");
			} catch (KubernetesOperationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
