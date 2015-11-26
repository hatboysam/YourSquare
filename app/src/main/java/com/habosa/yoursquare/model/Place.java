package com.habosa.yoursquare.model;

import java.util.ArrayList;
import java.util.List;

public class Place {

    private long id;
    private String googlePlaceId;
    private String name;
    private String address;
    private List<String> tags = new ArrayList<>();

    public static class Decorator {

        private Place place;

        public Decorator(Place place) {
            this.place = place;
        }

        public String getTagsString() {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < place.getTags().size(); i++) {
                sb.append(place.getTags().get(i));
                if (i != place.getTags().size() - 1) {
                    sb.append(", ");
                }
            }

            return sb.toString();
        }

    }

    public Place() {}

    public Place(String name, String address, List<String> tags) {
        this.name = name;
        this.address = address;
        this.tags = tags;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getGooglePlaceId() {
        return googlePlaceId;
    }

    public void setGooglePlaceId(String googlePlaceId) {
        this.googlePlaceId = googlePlaceId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }
}
