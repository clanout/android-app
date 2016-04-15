package com.clanout.app.model;

public class Friend implements Model
{
    private String userId;
    private String name;
    private String locationZone;
    private boolean isBlocked;
    private boolean isNew;

    public String getId()
    {
        return userId;
    }

    public void setUserId(String userId)
    {
        this.userId = userId;
    }

    public String getName()
    {
        return name;
    }

    public String getLocationZone()
    {
        return locationZone;
    }

    public boolean isBlocked()
    {
        return isBlocked;
    }

    public boolean isNew()
    {
        return isNew;
    }

    public void setIsNew(boolean isNew)
    {
        this.isNew = isNew;
    }

    public void setIsBlocked(boolean isBlocked)
    {
        this.isBlocked = isBlocked;
    }

    @Override
    public int hashCode()
    {
        return userId.hashCode();
    }

    @Override
    public boolean equals(Object o)
    {
        if (o == this) {
            return true;
        }

        if (!(o instanceof Friend)) {
            return false;
        }
        else {
            Friend other = (Friend) o;
            if (userId.equals(other.userId)) {
                return true;
            }
            else {
                return false;
            }
        }
    }

    public static Friend of(String id)
    {
        Friend friend = new Friend();
        friend.userId = id;
        return friend;
    }
}

