package com.theandroidsuit.bytheway.error;

/**
 * Created by Virginia Hernández on 21/01/15.
 */
public class BTWOperationError extends Exception {
    private String name;
    private String description;
    private Exception original;

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

    public Exception getOriginal() {
        return original;
    }

    public void setOriginal(Exception original) {
        this.original = original;
    }
}
