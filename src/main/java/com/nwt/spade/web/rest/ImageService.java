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
public class ImageService {

	private APIController apiController;
	private static final Logger LOG = LoggerFactory
			.getLogger(ImageService.class);

	@Autowired
	public ImageService(APIController api) {
		apiController = api;
	}

	@RequestMapping(value = "/{project}/images", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody ResponseEntity<String> listAllimages(
			@PathVariable String project) {
		return new ResponseEntity<String>(apiController.listAllImages(project),
				HttpStatus.OK);
	}

	@RequestMapping(value = "/{project}/images/{os}/{app}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody ResponseEntity<String> getImage(
			@PathVariable String project, @PathVariable String os,
			@PathVariable String app) {
		return new ResponseEntity<String>(apiController.getImage(project, os, app),
				HttpStatus.OK);
	}

	@RequestMapping(value = "/{project}/images/", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody ResponseEntity<String> addImage(
			@PathVariable String project, @RequestBody String payload) {
		return new ResponseEntity<String>(apiController.addImage(project, payload),
				HttpStatus.OK);
	}

	@RequestMapping(value = "/{project}/images/{os}/{app}", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody ResponseEntity<String> deleteImage(
			@PathVariable String project, @PathVariable String os,
			@PathVariable String app) {
		return new ResponseEntity<String>(apiController.deleteImage(project, os, app),
				HttpStatus.OK);
	}

}
