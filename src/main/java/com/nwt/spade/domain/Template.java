package com.nwt.spade.domain;

import io.fabric8.kubernetes.api.model.Container;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class Template extends AbstractAuditingEntity implements Serializable {
	
	private String name;
	private String id;
	private String version;
	private List<Container> containers;
	private Map<String, String> labels;

}
