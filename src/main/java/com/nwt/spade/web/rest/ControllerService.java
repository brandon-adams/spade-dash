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
public class ControllerService {

	private APIController apiController;
	private static final Logger LOG = LoggerFactory
			.getLogger(ControllerService.class);

	@Autowired
	public ControllerService(APIController api) {
		apiController = api;
	}

	@RequestMapping(value = "/{project}/env", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody ResponseEntity<String> addEnv(
			@PathVariable String project, @RequestBody String payload) {
		return new ResponseEntity<String>(apiController.addEnv(project, payload),
				HttpStatus.OK);
	}

	@RequestMapping(value = "/{project}/env/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody ResponseEntity<String> getEnv(
			@PathVariable String project, @PathVariable String id) {
		return new ResponseEntity<String>(apiController.getEnv(project, id),
				HttpStatus.OK);
	}

	@RequestMapping(value = "/{project}/env/{id}", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody ResponseEntity<String> deleteEnv(
			@PathVariable String project, @PathVariable String id) {
		return new ResponseEntity<String>(apiController.deleteEnv(project, id),
				HttpStatus.OK);
	}

	@RequestMapping(value = "/{project}/env", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody ResponseEntity<String> listAllEnvs(
			@PathVariable String project) {
		return new ResponseEntity<String>(apiController.listAllEnvs(project),
				HttpStatus.OK);
	}

}
