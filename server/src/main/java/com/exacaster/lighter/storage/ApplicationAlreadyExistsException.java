package com.exacaster.lighter.storage;

public class ApplicationAlreadyExistsException extends RuntimeException {
    private final String applicationId;

    public ApplicationAlreadyExistsException(String applicationId) {
        super("Application already exists");
        this.applicationId = applicationId;
    }

    public String getApplicationId() {
        return applicationId;
    }

    @Override
    public String toString() {
        return "ApplicationAlreadyExistsException{" +
                "applicationId='" + applicationId + '\'' +
                '}';
    }
}
