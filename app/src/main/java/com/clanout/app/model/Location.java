package com.clanout.app.model;

import com.clanout.app.api.core.GsonProvider;

public class Location implements Model
{
    private Double longitude;
    private Double latitude;
    private String name;
    private String zone;

    public Double getLongitude()
    {
        return longitude;
    }

    public void setLongitude(Double longitude)
    {
        this.longitude = longitude;
    }

    public Double getLatitude()
    {
        return latitude;
    }

    public void setLatitude(Double latitude)
    {
        this.latitude = latitude;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getZone()
    {
        return zone;
    }

    public void setZone(String zone)
    {
        this.zone = zone;
    }

    @Override
    public String toString()
    {
        return GsonProvider.getGson().toJson(this);
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (!(o instanceof Location)) return false;

        Location location = (Location) o;

        if (longitude != null ? !longitude.equals(location.longitude) : location.longitude != null)
        {
            return false;
        }

        if (latitude != null ? !latitude.equals(location.latitude) : location.latitude != null)
        {
            return false;
        }

        if (name != null ? !name.equals(location.name) : location.name != null)
        {
            return false;
        }
        return zone.equals(location.zone);

    }

    @Override
    public int hashCode()
    {
        int result = longitude != null ? longitude.hashCode() : 0;
        result = 31 * result + (latitude != null ? latitude.hashCode() : 0);
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + zone.hashCode();
        return result;
    }
}