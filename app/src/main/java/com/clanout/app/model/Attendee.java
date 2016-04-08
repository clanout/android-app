package com.clanout.app.model;

/**
 * Created by harsh on 08/04/16.
 */
public class Attendee implements Model
{
    private String id;
    private String name;
    private String status;

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    @Override
    public int hashCode()
    {
        return id.hashCode();
    }

    @Override
    public boolean equals(Object o)
    {
        if (o == this) {
            return true;
        }

        if (!(o instanceof Attendee)) {
            return false;
        }
        else {
            Attendee other = (Attendee) o;
            if (id.equals(other.id)) {
                return true;
            }
            else {
                return false;
            }
        }
    }

}
