package com.nebula.app.network;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/** Minimal model of https://images-api.nasa.gov/search responses. */
public class NasaImagesResponse {
    public Collection collection;

    public static class Collection {
        public List<Item> items;
    }

    public static class Item {
        public List<Data> data;
        public List<Link> links;
    }

    public static class Data {
        public String title;
        public String description;
        @SerializedName("nasa_id") public String nasaId;
        @SerializedName("date_created") public String dateCreated;
        @SerializedName("media_type") public String mediaType;
        public String center;
        public String photographer;
        @SerializedName("secondary_creator") public String secondaryCreator;
    }

    public static class Link {
        public String href;
        public String rel;
    }
}
