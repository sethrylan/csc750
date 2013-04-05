package edu.ncsu.soc.motivator.domain;

import java.util.List;

public class Nearby {
    
    public String next_page_token;
    public List<Result> results;
    
    public class Result {
        public Geometry geometry;
        public String icon;
        public String id;
        public String name;
        public Hours opening_hours;
        public List<Photo> photos;
        public double rating;
        public String reference;
        public List<String> types;
        public String vicinity;
        
        public class Geometry {
            public Location location;
            public class Location {
                public double lat;
                public double lng;
            }
        }
        
        public class Hours {
            public boolean open_now;
        }
        
        public class Photo {
            public int height;
            public int width;
            public String photo_reference;
        }
    }
}
