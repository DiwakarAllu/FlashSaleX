package com.flashsalex.user.exception;

public class ResourceNotFoundException extends RuntimeException {

	public ResourceNotFoundException(String resource) {
		super(resource + " was not found");
	}
}
