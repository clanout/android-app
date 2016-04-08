package com.clanout.app.model;

import com.clanout.app.api.core.GsonProvider;

public class User implements Model
{
    private String userId;
    private String firstname;
    private String lastname;
    private String email;
    private String mobileNumber;
    private String gender;
    private String username;
    private String locationZone;

    public String getMobileNumber()
    {
        return mobileNumber;
    }

    public void setMobileNumber(String mobileNumber)
    {
        this.mobileNumber = mobileNumber;
    }

    public String getId()
    {
        return userId;
    }

    public void setId(String id)
    {
        this.userId = id;
    }

    public String getName()
    {
        return String.format("%s %s", firstname, lastname);
    }

    public String getFirstname()
    {
        return firstname;
    }

    public void setFirstname(String firstname)
    {
        this.firstname = firstname;
    }

    public String getLastname()
    {
        return lastname;
    }

    public void setLastname(String lastname)
    {
        this.lastname = lastname;
    }

    public String getEmail()
    {
        return email;
    }

    public void setEmail(String email)
    {
        this.email = email;
    }

    public String getGender()
    {
        return gender;
    }

    public void setGender(String gender)
    {
        this.gender = gender;
    }

    public String getUsername()
    {
        return username;
    }

    public void setUsername(String username)
    {
        this.username = username;
    }

    public String getLocationZone()
    {
        return locationZone;
    }

    public void setLocationZone(String locationZone)
    {
        this.locationZone = locationZone;
    }

    @Override
    public String toString()
    {
        return GsonProvider.getGson().toJson(this);
    }
}
