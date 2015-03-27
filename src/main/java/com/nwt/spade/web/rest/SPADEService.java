package com.nwt.spade.web.rest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.nwt.spade.controllers.APIController;
import com.nwt.spade.controllers.APIController.API;

@Service
@RestController
@RequestMapping("/spade")
public class SPADEService {

	private APIController apiController;
	private static final Logger LOG = LoggerFactory
			.getLogger(SPADEService.class);

	@Autowired
	public SPADEService(APIController api) {
		apiController = api;
	}
	
	@RequestMapping(value = "/test", method = RequestMethod.POST, produces = MediaType.TEXT_HTML_VALUE)
	public @ResponseBody ResponseEntity<String> test(@RequestBody String temp) {
		return new ResponseEntity<String>(apiController.createRepl(temp),
				HttpStatus.OK);
	}

	@RequestMapping(value = "", method = RequestMethod.GET, produces = MediaType.TEXT_HTML_VALUE)
	public @ResponseBody ResponseEntity<String> spade() {
		return new ResponseEntity<String>("Welcome to SPADE Provisioning",
				HttpStatus.OK);
	}

	@RequestMapping(value = "/api", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody ResponseEntity<API> api() {
		return new ResponseEntity<API>(apiController.version(), HttpStatus.OK);
	}
	
	@RequestMapping(value = "/api/templates", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody ResponseEntity<String> listAllTemplates() {
		return new ResponseEntity<String>(apiController.listAllTemplates("all"),
				HttpStatus.OK);
	}

	@RequestMapping(value = "/api/images", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody ResponseEntity<String> listAllimages() {
		return new ResponseEntity<String>(apiController.listAllImages("all"),
				HttpStatus.OK);
	}

	@RequestMapping(value = "/api/env", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody ResponseEntity<String> listAllEnvs() {
		return new ResponseEntity<String>(apiController.listAllEnvs("all"),
				HttpStatus.OK);
	}
	
	@RequestMapping(value = "/api/pods", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody ResponseEntity<String> listAllPods() {
		return new ResponseEntity<String>(apiController.listAllPods("all"),
				HttpStatus.OK);
	}

	@RequestMapping(value = "/api/proj", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody ResponseEntity<String> listAllProjects() {
		return new ResponseEntity<String>(apiController.listAllProjects("all"),
				HttpStatus.OK);
	}
	
	@RequestMapping(value = "/api/tasks", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody ResponseEntity<String> listAllTasks() {
		return new ResponseEntity<String>(apiController.listAllTasks(),
				HttpStatus.OK);
	}
	
	@RequestMapping(value = "/api/slaves", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody ResponseEntity<String> listAllSlaves() {
		return new ResponseEntity<String>(apiController.listAllSlaves(),
				HttpStatus.OK);
	}
	
	@RequestMapping(value = "/api/users", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody ResponseEntity<String> listAllUsers() {
		return new ResponseEntity<String>(apiController.listAllUsers(),
				HttpStatus.OK);
	}
	
	@RequestMapping(value = "/api/stacks", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody ResponseEntity<String> listAllStacks() {
		return new ResponseEntity<String>(apiController.listAllStacks("all"),
				HttpStatus.OK);
	}

}
