package com.habosa.yoursquare.model;

import java.util.List;

public class Place {

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

    private String title;
    private String address;
    private List<String> tags;

    public Place() {}

    public Place(String title, String address, List<String> tags) {
        this.title = title;
        this.address = address;
        this.tags = tags;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
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
