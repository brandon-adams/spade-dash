package com.nwt.spade.controllers;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

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
import org.springframework.web.bind.annotation.PathVariable;

import com.nwt.spade.domain.Container;
import com.nwt.spade.domain.Port;
import com.nwt.spade.domain.Template;
import com.nwt.spade.exceptions.KubernetesOperationException;

@Service
public class APIController {

	protected DockerController dockerController;
	protected KubernetesController kubeController;
	protected ProjectController projController;
	protected MesosController mesosController;
	protected StackController stackController;
	protected UserController userController;

	private static final Logger LOG = LoggerFactory
			.getLogger(APIController.class);

	@Autowired
	public APIController(DockerController dc, KubernetesController kc, ProjectController pr, UserController uc, MesosController mc, StackController sc) {
		dockerController = dc;
		kubeController = kc;
		projController = pr;
		userController = uc;
		mesosController = mc;
		stackController = sc;
	}
	
	public String createRepl(String temp){
		LOG.debug("Trying the new Template stuff" + temp);
		Template template = new Template();
		JsonObject json = Json.createReader(new StringReader(temp))
				.readObject();
		template.setId(json.getString("id"));
		template.setReplicas(json.getInt("replicas"));
		template.setSelect(json.getString("select"));
		template.setContainers(new ArrayList());
		for (JsonValue jval : json.getJsonArray("containers")){
			Container cont = new Container();
			cont.setOs(((JsonObject)jval).getString("os"));
			cont.setApp(((JsonObject)jval).getString("app"));
			cont.setName(((JsonObject)jval).getString("name"));
			cont.setPorts(new ArrayList());
			for (JsonValue port : ((JsonObject)jval).getJsonArray("ports")){
				Port pt = new Port();
				pt.setContainerPort(((JsonObject)port).getInt("containerPort"));
				pt.setHostPort(((JsonObject)port).getInt("hostPort"));
				cont.getPorts().add(pt);
			}
			template.getContainers().add(cont);
		}
		JsonArray jsonReturn = kubeController.createPod(template);
		
		JsonObjectBuilder objBuild = Json.createObjectBuilder();
		objBuild.add("api", "v0.0.4");
		objBuild.add("time", new Date().getTime());
		objBuild.add("type", "CreateEnv");
		objBuild.add("items", jsonReturn);
		return objBuild.build().toString();
	}

	public String addEnv(String project, String payload) {
		JsonObject jsonInput = Json.createReader(new StringReader(payload))
				.readObject();
		String os = jsonInput.getString("os").toLowerCase();
		String app = jsonInput.getString("app").toLowerCase();
		String name = jsonInput.getString("name").toLowerCase();
		String stack = jsonInput.getString("stack").toLowerCase();
		int replicas = jsonInput.getInt("replicas");
		/*
		 * try { System.out.println("Image used: " + template.getImageName());
		 * System.out.println("Path used: " + path);
		 * System.out.println("Building Docker image with Docker file provided..."
		 * ); String imageId = dockerController.buildImage(imageName, path);
		 * 
		 * System.out.print("Built and pushed the image with ID " + imageId +
		 * ".");
		 * 
		 * } catch (Exception ioe) {
		 * System.out.println(ioe.getStackTrace().toString()); System.exit(1); }
		 */
		String imageName = dockerController.getImage(project, os, app).getJsonArray("items").getJsonObject(0).getString("image");
		LOG.info("Launching the pod at the Kubernetes master.");
		JsonArrayBuilder arrBuild = Json.createArrayBuilder();
		JsonArray jsonReturn = arrBuild.build();
		try {
			jsonReturn = kubeController.createEnv(stack, name, project, imageName, os, app, replicas);
		} catch (KubernetesOperationException e) {
			// Insert error message into Json Object
			e.printStackTrace();
		}
		JsonObjectBuilder objBuild = Json.createObjectBuilder();
		objBuild.add("api", "v0.0.4");
		objBuild.add("time", new Date().getTime());
		objBuild.add("type", "CreateEnv");
		objBuild.add("items", jsonReturn);
		LOG.info("Pod creation request sent. Check dashboard for status.");
		return objBuild.toString();
	}

	public String deleteEnv(String project, String id) {
		JsonObjectBuilder objBuild = Json.createObjectBuilder();
		objBuild.add("api", "v0.0.4");
		objBuild.add("time", new Date().getTime());
		objBuild.add("type", "DeleteEnv");
		JsonArrayBuilder arrBuild = Json.createArrayBuilder();
		JsonArray jsonReturn = arrBuild.build();
		try {
			jsonReturn = kubeController.deleteEnv(project, id);
			objBuild.add("items", jsonReturn);
		} catch (KubernetesOperationException e) {
			// Insert error message into Json Object
			e.printStackTrace();
		}
		return objBuild.build().toString();
	}

	public String listAllEnvs(String project) {
		JsonObjectBuilder objBuild = Json.createObjectBuilder();
		objBuild.add("api", "v0.0.4");
		objBuild.add("time", new Date().getTime());
		objBuild.add("type", "GetEnvs");
		objBuild.add("items", kubeController.getAllEnvs(project));
		return objBuild.build().toString();
	}

	public String getEnv(String project, String id) {
		JsonObjectBuilder objBuild = Json.createObjectBuilder();
		objBuild.add("api", "v0.0.4");
		objBuild.add("time", new Date().getTime());
		objBuild.add("type", "GetEnv");
		objBuild.add("items", kubeController.getEnv(project, id.toLowerCase()));
		return objBuild.build().toString();
	}

	public String getImage(String project, String os, String app) {
		return dockerController.getImage(project, os.toLowerCase(), app.toLowerCase()).toString();
	}
	
	public String addImage(String project, String payload) {
		JsonObject jsonInput = Json.createReader(new StringReader(payload))
				.readObject();
		String name = jsonInput.getString("name");
		String os = jsonInput.getString("os").toLowerCase();
		String app = jsonInput.getString("app").toLowerCase();
		String type = jsonInput.getString("type").toLowerCase(); 
		return dockerController.addImage(project, name, os.toLowerCase(), app.toLowerCase()).toString();
	}
	
	public String deleteImage(String project, String os, String app){
		return dockerController.deleteImage(project, os.toLowerCase(), app.toLowerCase()).toString();
	}

	public String listAllImages(String project) {
		return dockerController.getAllImages(project).toString();
	}
	
	public String getStack(String project, String id) {
		JsonObjectBuilder objBuild = Json.createObjectBuilder();
		objBuild.add("api", "v0.0.4");
		objBuild.add("time", new Date().getTime());
		objBuild.add("type", "GetStack");
		objBuild.add("items", stackController.getStack(project, id));
		return objBuild.build().toString();
	}

	public String listAllStacks(String project) {
		JsonObjectBuilder objBuild = Json.createObjectBuilder();
		objBuild.add("api", "v0.0.4");
		objBuild.add("time", new Date().getTime());
		objBuild.add("type", "GetStacks");
		objBuild.add("items", stackController.getAllStacks(project));
		return objBuild.build().toString();
	}
	
	public String deleteStack(String project, String id){
		JsonObjectBuilder objBuild = Json.createObjectBuilder();
		objBuild.add("api", "v0.0.4");
		objBuild.add("time", new Date().getTime());
		objBuild.add("type", "DeleteStack");
		objBuild.add("items", stackController.deleteStack(project, id));
		return objBuild.build().toString();
	}
	
	public String addStack(String project, String payload) {
		JsonObjectBuilder objBuild = Json.createObjectBuilder();
		objBuild.add("api", "v0.0.4");
		objBuild.add("time", new Date().getTime());
		objBuild.add("type", "CreateStack");
		try {
			objBuild.add("items", stackController.createStack(project, payload));
		} catch (KubernetesOperationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return objBuild.build().toString();
	}
	
	public String getStackTemp(String project, String id) {
		JsonObjectBuilder objBuild = Json.createObjectBuilder();
		objBuild.add("api", "v0.0.4");
		objBuild.add("time", new Date().getTime());
		objBuild.add("type", "GetStackTemplate");
		objBuild.add("items", stackController.getStackTemp(project, id));
		return objBuild.build().toString();
	}

	public String listAllStackTemps(String project) {
		JsonObjectBuilder objBuild = Json.createObjectBuilder();
		objBuild.add("api", "v0.0.4");
		objBuild.add("time", new Date().getTime());
		objBuild.add("type", "GetStacks");
		objBuild.add("items", stackController.getAllStackTemps(project));
		return objBuild.build().toString();
	}
	
	public String deleteStackTemp(String project, String id){
		JsonObjectBuilder objBuild = Json.createObjectBuilder();
		objBuild.add("api", "v0.0.4");
		objBuild.add("time", new Date().getTime());
		objBuild.add("type", "DeleteStack");
		objBuild.add("items", stackController.deleteStackTemp(project, id));
		return objBuild.build().toString();
	}
	
	public String addStackTemp(String project, String template) {
		JsonObjectBuilder objBuild = Json.createObjectBuilder();
		objBuild.add("api", "v0.0.4");
		objBuild.add("time", new Date().getTime());
		objBuild.add("type", "CreateStack");
		objBuild.add("items", stackController.createStackTemp(project, template));
		return objBuild.build().toString();
	}

	public String getTemplate(String project, String os, String app) {
		String imageName = dockerController.getImage(project, os.toLowerCase(), app.toLowerCase()).getString("name");
		JsonObjectBuilder objBuild = Json.createObjectBuilder();
		objBuild.add("api", "v0.0.4");
		objBuild.add("time", new Date().getTime());
		objBuild.add("type", "GetTemplate");
		objBuild.add("items", kubeController.getTemplate(project, imageName));
		return objBuild.build().toString();
	}

	public String listAllTemplates(String project) {
		JsonObjectBuilder objBuild = Json.createObjectBuilder();
		objBuild.add("api", "v0.0.4");
		objBuild.add("time", new Date().getTime());
		objBuild.add("type", "GetTemplates");
		objBuild.add("items", kubeController.getAllTemplates(project));
		return objBuild.build().toString();
	}
	
	public String deleteTemplate(String project, String os, String app){
		String imageName = dockerController.getImage(project, os.toLowerCase(), app.toLowerCase()).getString("name");
		JsonObjectBuilder objBuild = Json.createObjectBuilder();
		objBuild.add("api", "v0.0.4");
		objBuild.add("time", new Date().getTime());
		objBuild.add("type", "DeleteTemplate");
		objBuild.add("items", kubeController.deleteTemplate(project, imageName));
		return objBuild.build().toString();
	}
	
	public String addProject(String project) {
		JsonObjectBuilder objBuild = Json.createObjectBuilder();
		objBuild.add("api", "v0.0.4");
		objBuild.add("time", new Date().getTime());
		objBuild.add("type", "AddProject");
		objBuild.add("items", projController.addProject(project));
		return objBuild.build().toString();
	}

	public String getProject(String project) {
		return projController.getProject(project).toString();
	}

	public String deleteProject(String project) {
		return projController.deleteProject(project).toString();
	}
	
	public String listAllProjects(String project){
		return projController.listAllProjects().toString();
	}
	
	public String addUser(String payload) {
		return userController.addUser(payload).toString();
	}

	public String getUser(String username) {
		return userController.getUser(username).toString();
	}

	public String deleteUser(String username) {
		return userController.deleteUser(username).toString();
	}
	
	public String listAllUsers(){
		return userController.listAllUsers().toString();
	}
	
	public String listAllPods(String project){
		JsonObjectBuilder objBuild = Json.createObjectBuilder();
		objBuild.add("api", "v0.0.4");
		objBuild.add("time", new Date().getTime());
		objBuild.add("type", "ListPods");
		objBuild.add("items", kubeController.getAllPods(project));
		return objBuild.build().toString();
	}
	
	public String listAllTasks(){
		JsonObjectBuilder objBuild = Json.createObjectBuilder();
		objBuild.add("api", "v0.0.4");
		objBuild.add("time", new Date().getTime());
		objBuild.add("type", "ListTasks");
		objBuild.add("items", mesosController.listAllTasks());
		return objBuild.build().toString();
	}
	
	public String listAllSlaves(){
		JsonObjectBuilder objBuild = Json.createObjectBuilder();
		objBuild.add("api", "v0.0.4");
		objBuild.add("time", new Date().getTime());
		objBuild.add("type", "ListSlaves");
		objBuild.add("items", mesosController.listAllSlaves());
		return objBuild.build().toString();
	}

	public API version() {
		return new API();
	}

	public class API {
		private final String version;
		private final String owner;
		private final String details;
		private Map<String, String> endpoints;

		public API() {
			details = "SPADE Api endpoints";
			version = "v0.0.4";
			owner = "bradams";
			endpoints = new HashMap<String, String>();
			endpoints.put("add env", "POST /spade/api/{project}/env/");
			endpoints.put("get env", "GET /spade/api/{project}/env/{name}");
			endpoints.put("delete env", "DELETE /spade/api/{project}/env/{name}");
			endpoints.put("list all envs", "GET /spade/api/env");
			endpoints.put("list project envs", "GET /spade/api/{project}/env");
			endpoints.put("add image", "POST /spade/api/images/{payload}");
			endpoints.put("get image", "GET /spade/api/images/{os}/{app}");
			endpoints.put("delete image", "DELETE /spade/api/images/{os}/{app}");
			endpoints.put("list all images", "GET /spade/api/images");
			//endpoints.put("add template", "POST /spade/api/templates/{template}");
			endpoints.put("get template", "GET /spade/api/{project}/templates/{os}/{app}");
			endpoints.put("delete template", "DELETE /spade/api/{project}/templates/{os}/{app}");
			endpoints.put("list project templates", "GET /spade/api/{project}/templates");
			endpoints.put("get project", "GET /spade/api/{name}");
			endpoints.put("delete project", "DELETE /spade/api/{name}");
			endpoints.put("list all projects", "GET /spade/api/projects");
		}

		public String getVersion() {
			return version;
		}

		public String getOwner() {
			return owner;
		}
		
		public String getDetails(){
			return details;
		}
		
		public Map getEndpoints(){
			return endpoints;
		}
	}

	/*
	 * public static void main(String[] args) { APIController api = new
	 * APIController(new DockerController( new MongoDBController()), new
	 * KubernetesController( new MongoDBController()));
	 * 
	 * System.out.println(api.listAllImages()); }
	 */
}
