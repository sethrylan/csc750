package edu.ncsu.soc.motivator;

public class Forecast {

    public double latitude;
    public double longitude;
    public String timezone;
    public int offset;
    public Currently currently;
    
    public class Currently {
        public int time;
        public String summary;
        public double precipIntensity;
        public double precipProbability;
        public double temperature;
        
        public double windSpeed;
        public double windBearing;
        public double cloudCover;
        public double humidity;
        public double pressure;
        public double visibility;
    }
}
