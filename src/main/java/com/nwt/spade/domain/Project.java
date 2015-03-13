package com.nwt.spade.domain;

import java.io.Serializable;
import java.util.Set;

public class Project extends AbstractAuditingEntity implements Serializable {

	private String name;
	private String description;
	private Set<String> environments;
	private Set<SpringUser> users;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public Set<String> getEnvironments() {
		return environments;
	}
	public void setEnvironments(Set<String> environments) {
		this.environments = environments;
	}
	public Set<SpringUser> getUsers() {
		return users;
	}
	public void setUsers(Set<SpringUser> users) {
		this.users = users;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Project other = (Project) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}
	@Override
	public String toString() {
		return "Project [name=" + name + ", description=" + description
				+ ", environments=" + environments + ", users=" + users + "]";
	}
	
	
	
}
