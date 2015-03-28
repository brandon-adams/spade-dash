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

@Service
@RestController
@RequestMapping("/spade/api")
public class ProjectService {

	private APIController apiController;
	private static final Logger LOG = LoggerFactory
			.getLogger(TemplateService.class);

	@Autowired
	public ProjectService(APIController api) {
		apiController = api;
	}

	@RequestMapping(value = "/projects", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody ResponseEntity<String> addProject(
			@RequestBody String payload) {
		return new ResponseEntity<String>(apiController.addProject(payload),
				HttpStatus.CREATED);
	}

	@RequestMapping(value = "/{project}", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody ResponseEntity<String> deleteProject(
			@PathVariable String project) {
		return new ResponseEntity<String>(apiController.deleteProject(project),
				HttpStatus.OK);
	}

	@RequestMapping(value = "/{project}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody ResponseEntity<String> getProject(
			@PathVariable String project) {
		return new ResponseEntity<String>(apiController.getProject(project),
				HttpStatus.FOUND);
	}
}
