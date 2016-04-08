package com.clanout.app.api.google_places.response;

import com.google.gson.annotations.SerializedName;

public class GooglePlaceDetailsApiResponse
{
    @SerializedName("result")
    private Result result;

    public String getName()
    {
        return result.name;
    }

    public double getLatitude()
    {
        return result.geometry.location.latitude;
    }

    public double getLongitude()
    {
        return result.geometry.location.longitude;
    }

    private static class Result
    {
        @SerializedName("name")
        private String name;

        @SerializedName("geometry")
        private Geometry geometry;

        private static class Geometry
        {
            @SerializedName("location")
            private Location location;

            private static class Location
            {
                @SerializedName("lat")
                private double latitude;

                @SerializedName("lng")
                private double longitude;
            }
        }
    }
}
