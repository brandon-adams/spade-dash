package com.nwt.spade.controllers;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.Date;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.BuildImageCmd;
import com.github.dockerjava.api.command.PushImageCmd;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.DockerClientConfig;

@Service
//@ConfigurationProperties()
@PropertySource("config/application.properties")
public class DockerController {
	
	//@Inject
    //private Environment env;
	
	//private static final Logger LOG = LoggerFactory.getLogger(DockerController.class);

	private MongoDBController db;
	private DockerClient docker;
	@Value("${docker.host}")
	private String host;
	@Value("${docker.port}")
	private String port;
	@Value("${docker.user}")
	private String user;
	@Value("${docker.password}")
	private String password;
	@Value("${docker.email}")
	private String email;
	@Value("${docker.server}")
	private String server;

	@Autowired
	public DockerController(MongoDBController db) {
		this.db = db;
	}
	
	@PostConstruct
	public void init(){
		//host = env.getRequiredProperty("docker.host");
		//port = env.getRequiredProperty("docker.port");
		//user = env.getRequiredProperty("docker.user");
		//password = env.getRequiredProperty("docker.password");
		//server = env.getRequiredProperty("docker.server");
		DockerClientConfig config = DockerClientConfig
				.createDefaultConfigBuilder().withVersion("1.16")
				.withUri("http://"+host+":"+port).withUsername(user)
				.withPassword(password)
				.withEmail(email)
				.withServerAddress(server).build();
		docker = DockerClientBuilder.getInstance(config).build();
	}
	
	/*public String getImageName(String os, String app){
		return ((HashMap<String, Object>) images.get(os)).get(app).toString();
	}*/

	public String buildContainer(String container, String file) {
		String imageId = "Failed";
		try {
			File dockerFile = new File(file).getAbsoluteFile();
		
		
		System.out.println(dockerFile);
		BuildImageCmd bcmd = docker.buildImageCmd(dockerFile).withTag(container)
				.withRemove();
		InputStream bresult = bcmd.exec();
		StringWriter bwriter = new StringWriter();
		try {
			IOUtils.copy(bresult, bwriter, "UTF-8");
		} catch (IOException e) {
			e.printStackTrace();
		}
		String bresponse = bwriter.toString();
		System.out.println(bresponse);
		imageId = StringUtils.substringBetween(bresponse,
				"Successfully built ", "\\n\"}").trim();
		
		System.out.println(imageId);
		} catch (Exception e) {
			
		}
		
		String repo = container.contains(":") ? container.split(":")[0] : container;
		String tag = container.contains(":") ? container.split(":")[1] : "latest";
		
		PushImageCmd pcmd = docker.pushImageCmd(repo).withTag(tag);
		InputStream presult = pcmd.exec();
		StringWriter pwriter = new StringWriter();
		try {
			IOUtils.copy(presult, pwriter, "UTF-8");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String presponse = pwriter.toString();
		System.out.println(presponse);

		return imageId;
		
	}
	
	public JsonObject getImage(String project, String os, String app){
		JsonObjectBuilder objBuild = Json.createObjectBuilder();
		objBuild.add("api", "v0.0.4");
		objBuild.add("time", new Date().getTime());
		objBuild.add("label", "extra");
		objBuild.add("items", db.getImage(project, os, app));
		return objBuild.build();
	}
	
	public JsonObject deleteImage(String project, String os, String app){
		JsonObjectBuilder objBuild = Json.createObjectBuilder();
		objBuild.add("api", "v0.0.4");
		objBuild.add("time", new Date().getTime());
		objBuild.add("label", "extra");
		objBuild.add("items", db.deleteImage(project, os, app));
		return objBuild.build();
	}
	
	public JsonObject addImage(String project, String name, String os, String app){
		JsonObjectBuilder objBuild = Json.createObjectBuilder();
		objBuild.add("api", "v0.0.4");
		objBuild.add("time", new Date().getTime());
		objBuild.add("label", "extra");
		objBuild.add("items", db.addImage(project, name, os, app));
		return objBuild.build();
	}
	
	public JsonObject getAllImages(String project){
		JsonObjectBuilder objBuild = Json.createObjectBuilder();
		objBuild.add("api", "v0.0.4");
		objBuild.add("time", new Date().getTime());
		objBuild.add("label", "extra");
		objBuild.add("items", db.getAllImages(project));
		return objBuild.build();
	}

//	public static void main(String[] args) {
//
//		DockerController test = new DockerController(new MongoDBController(true));
//		System.out.println(test.getImage("ubuntu", "wildfly"));
//		//test.buildImage("bradams/devops:demo", "/home/badams/code/dashboard");
//	}
	
	/*@Configuration
	@PropertySource("classpath:/config/application.properties")
	public class DockerConfig {
		
		@Autowired
	    private Environment env;
		
		private String host;
		private String port;
		private String user;
		private String password;
		private String email;
		private String server;
		
		public void init(){
			
		}
		
	}*/

}
