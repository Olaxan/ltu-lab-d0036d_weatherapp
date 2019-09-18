package weatherapp;

public class Locality
{
	
	public Locality(String name, float latitude, float longitude, int altitude)
	{
		super();
		this.name = name;
		this.latitude = latitude;
		this.longitude = longitude;
		this.altitude = altitude;
	}
	
	public String name;
	public float latitude;
	public float longitude;
	public int altitude;
	public long cacheTimer = 0;
}
