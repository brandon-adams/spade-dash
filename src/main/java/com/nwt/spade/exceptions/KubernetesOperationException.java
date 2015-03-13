package com.nwt.spade.exceptions;

public class KubernetesOperationException extends Exception {

	private final String message;
	
	public KubernetesOperationException(String m) {
		message=m;
	}
	
	@Override
	public String getLocalizedMessage() {
		return "Kubernetes has returned the following error: " + message;
	}
	
}