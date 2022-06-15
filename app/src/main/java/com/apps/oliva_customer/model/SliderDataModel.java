package com.apps.oliva_customer.model;

import java.io.Serializable;
import java.util.List;

public class SliderDataModel extends StatusResponse implements Serializable {
    private List<SliderModel> data;

    public List<SliderModel> getData() {
        return data;
    }

    public static class SliderModel implements Serializable {
        private String image;

        public String getImage() {
            return image;
        }
    }
}
