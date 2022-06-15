package com.apps.oliva_customer.model;

import java.io.Serializable;

public class ShipModel extends StatusResponse implements Serializable {
    private String shipping;

    public String getShipping() {
        return shipping;
    }
}
