package edu.ncsu.soc.motivator;

public enum WeatherFactor {
    CLOUD ("too cloudy"),
    PRECIPITATION ("too wet"),
    TEMPERATURE_HIGH ("too hot"),
    TEMPERATURE_LOW ("too cold"),
    WIND ("too windy"),
    VISIBILITY ("too hazy/fogy"); 
    
    private final String text;
    
    private WeatherFactor(String text) {
        this.text = text;
    }
    
    public String getText() {
        return this.text;
    }
}
