package edu.ncsu.soc.motivator;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.maps.GeoPoint;

public class BusStop implements Parcelable {

	private String stopId;			// The stop id
	private String code;		// Passenger-facing stop identifier
	private String name;		// Passenger-facing name for the stop
	private double latitude;	// The latitude location of the stop
	private double longitude;	// The longitude location of the stop
	private String direction;	// The direction of travel for routes serving this stop
	private String locationType;	/* Corresponds to the stop location_type field 
									   defined in the GTFS spec found at OneBusAway website*/
	
	/**
	 * BusStop constructor.
	 */
	public BusStop() {
	}
	
	/**
	 * Constructs a new GeoPoint using this class's 
	 * latitude and longitude.
	 * @return
	 */
	public GeoPoint getGeoPoint() {
		return new GeoPoint((int)(latitude * 1e6), (int)(longitude * 1e6));
	}
	
	/**
	 * @return code of type string.
	 */
	public String getCode() {
		return code;
	}

	/**
	 * Set the class's code field to given code.
	 * @param code
	 */
	public void setCode(String code) {
		this.code = code;
	}

	/**
	 * @return name of bus stop.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Set name of bus stop.
	 * @param name
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return latitude of type double of this bus stop.
	 */
	public double getLatitude() {
		return latitude;
	}

	/**
	 * set latitude of this bus stop.
	 * @param latitude
	 */
	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	/**
	 * @return longitude of type double of this bus stop.
	 */
	public double getLongitude() {
		return longitude;
	}

	/**
	 * Set longitude of type double of this bus stop.
	 * @param longitude
	 */
	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	/**
	 * @return stopId of this bus stop.
	 */
	public String getStopId() {
		return stopId;
	}

	/**
	 * Set stopId of this bus stop.
	 * @param stopId
	 */
	public void setStopId(String stopId) {
		this.stopId = stopId;
	}
	
	/**
	 * @return direction that buses go when stopping at this bus stop.
	 */
	public String getDirection() {
		return direction;
	}

	/**
	 * Set direction that buses go when stopping at this bus stop.
	 * @param direction
	 */
	public void setDirection(String direction) {
		this.direction = direction;
	}

	/**
	 * @return locationType of this bus stop.
	 */
	public String getLocationType() {
		return locationType;
	}

	/**
	 * Set locationType of this bus stop.
	 * @param locationType
	 */
	public void setLocationType(String locationType) {
		this.locationType = locationType;
	}
	
	/**
	 * required by Parcelable but unused.
	 */
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	/**
	 * Writes a BusStop to a Parcel
	 */
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(stopId);
		dest.writeString(code);
		dest.writeString(name);
		dest.writeDouble(latitude);
		dest.writeDouble(longitude);
		dest.writeString(direction);
		dest.writeString(locationType);
	}
	
	/**
	 * Builds a BusStop from a Parcel
	 */
	public static final Parcelable.Creator<BusStop> CREATOR = new Parcelable.Creator<BusStop>() {

		public BusStop createFromParcel(Parcel source) {
			BusStop b = new BusStop();
			b.setStopId(source.readString());
			b.setCode(source.readString());
			b.setName(source.readString());
			b.setLatitude(source.readDouble());
			b.setLongitude(source.readDouble());
			b.setDirection(source.readString());
			b.setLocationType(source.readString());
			return b;
		}

		public BusStop[] newArray(int size) {
			return new BusStop[size];
		}
		
	};

	/**
	 * Compares this BusStop with given BusStop.
	 * Returns true if their stopId, code, direction, 
	 * latitude(with 100000 precision) and longitude(with 100000 precision).
	 * @param other
	 * @return
	 */
	public boolean equals(BusStop other) {
		if(!this.stopId.equals(other.stopId)) {
			return false;
		} else if(!this.code.equals(other.code)) {
			return false;
		} else if(!this.direction.equals(other.direction)) {
			return false;
		} else if(Math.round(this.latitude * 100000) != Math.round(other.latitude * 100000)) {
			return false;
		} else if(Math.round(this.longitude * 100000) != Math.round(other.longitude * 100000)) {
			return false;
		}
		
		return true;
	}
	
	@Override
	public String toString() {
		return name;
	}
}