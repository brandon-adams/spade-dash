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
@RequestMapping("/spade/api/users")
public class UserService {
	
	private APIController apiController;
	private static final Logger LOG = LoggerFactory
			.getLogger(UserService.class);

	@Autowired
	public UserService(APIController api) {
		apiController = api;
	}
	
	@RequestMapping(value = "", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody ResponseEntity<String> listAllTemplates(@RequestBody String payload) {
		return new ResponseEntity<String>(apiController.addUser(payload),
				HttpStatus.OK);
	}
	
	@RequestMapping(value = "/{username}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody ResponseEntity<String> getUser(
			@PathVariable String username) {
		return new ResponseEntity<String>(apiController.getUser(username),
				HttpStatus.FOUND);
	}

	@RequestMapping(value = "/{username}", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody ResponseEntity<String> deleteTemplate(
			@PathVariable String username) {
		return new ResponseEntity<String>(
				apiController.deleteUser(username), HttpStatus.OK);
	}
}
