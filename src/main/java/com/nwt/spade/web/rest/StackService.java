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
public class StackService {

	private APIController apiController;
	private static final Logger LOG = LoggerFactory
			.getLogger(StackService.class);

	@Autowired
	public StackService(APIController api) {
		apiController = api;
	}

	@RequestMapping(value = "/{project}/stacks/{name}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody ResponseEntity<String> getStack(@PathVariable String project,
			@PathVariable String name) {
		return new ResponseEntity<String>(apiController.getStack(project, name),
				HttpStatus.OK);
	}
	
	@RequestMapping(value = "/{project}/stacks", method = RequestMethod.POST, produces = MediaType.TEXT_HTML_VALUE)
	public @ResponseBody ResponseEntity<String> createStack(@RequestBody String template) {
		return new ResponseEntity<String>(apiController.addStack("demo", template),
				HttpStatus.OK);
	}
	
	@RequestMapping(value = "/{project}/stacks/{name}", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody ResponseEntity<String> deleteTemplate(@PathVariable String project,
			@PathVariable String name) {
		return new ResponseEntity<String>(apiController.deleteStack(project, name), HttpStatus.OK);
	}

	@RequestMapping(value = "/{project}/stacks", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody ResponseEntity<String> listAllTemplates(
			@PathVariable String project) {
		return new ResponseEntity<String>(apiController.listAllTemplates(project),
				HttpStatus.OK);
	}

}
