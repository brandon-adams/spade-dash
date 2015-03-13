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
@RequestMapping("/spade/api")
public class TemplateService {

	private APIController apiController;
	private static final Logger LOG = LoggerFactory
			.getLogger(TemplateService.class);

	@Autowired
	public TemplateService(APIController api) {
		apiController = api;
	}

	@RequestMapping(value = "/{project}/templates/{os}/{app}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody ResponseEntity<String> getTemplate(
			@PathVariable String project, @PathVariable String os,
			@PathVariable String app) {
		return new ResponseEntity<String>(apiController.getTemplate(project, os, app),
				HttpStatus.FOUND);
	}

	@RequestMapping(value = "/{project}/templates/{os}/{app}", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody ResponseEntity<String> deleteTemplate(
			@PathVariable String project, @PathVariable String os,
			@PathVariable String app) {
		return new ResponseEntity<String>(
				apiController.deleteTemplate(project, os, app), HttpStatus.OK);
	}

	@RequestMapping(value = "/{project}/templates", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody ResponseEntity<String> listAllTemplates(
			@PathVariable String project) {
		return new ResponseEntity<String>(apiController.listAllTemplates(project),
				HttpStatus.FOUND);
	}

}
