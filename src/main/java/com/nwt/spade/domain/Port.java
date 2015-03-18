package com.nwt.spade.domain;

import java.io.Serializable;

public class Port implements Serializable {

	private int hostPort;
	private int containerPort;
	/**
	 * @return the hostPort
	 */
	public int getHostPort() {
		return hostPort;
	}
	/**
	 * @param hostPort the hostPort to set
	 */
	public void setHostPort(int hostPort) {
		this.hostPort = hostPort;
	}
	/**
	 * @return the containerPort
	 */
	public int getContainerPort() {
		return containerPort;
	}
	/**
	 * @param containerPort the containerPort to set
	 */
	public void setContainerPort(int containerPort) {
		this.containerPort = containerPort;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + hostPort;
		return result;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof Port))
			return false;
		Port other = (Port) obj;
		if (hostPort != other.hostPort)
			return false;
		return true;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Port [hostPort=" + hostPort + ", containerPort="
				+ containerPort + "]";
	}
	
	
}
