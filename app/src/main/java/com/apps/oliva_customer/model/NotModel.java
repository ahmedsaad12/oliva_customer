package com.apps.oliva_customer.model;

import java.io.Serializable;

public class NotModel implements Serializable {
    private boolean newNotification;

    public NotModel(boolean newNotification) {
        this.newNotification = newNotification;
    }

    public boolean isNewNotification() {
        return newNotification;
    }

    public void setNewNotification(boolean newNotification) {
        this.newNotification = newNotification;
    }
}
