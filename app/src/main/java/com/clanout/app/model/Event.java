package com.clanout.app.model;

import org.joda.time.DateTime;

import java.util.List;

public class Event implements Model
{
    public enum Type implements Model
    {
        OPEN,
        SECRET;
    }

    public enum RSVP implements Model
    {
        YES,
        NO,
        SEEN,
        DEFAULT;
    }

    private String id;
    private String title;
    private Type type;
    private String category;
    private String creatorId;

    private List<String> visibilityZones;

    private String description;
    private DateTime startTime;
    private DateTime endTime;
    private Location location;

    private DateTime createdAt;
    private DateTime updatedAt;

    private List<Attendee> attendees;

    /* Contextual Data */
    private RSVP rsvp;
    private String status;
    private List<String> friends;
    private List<String> inviter;
    private List<String> invitee;

    public String getId()
    {
        return id;
    }

    public String getTitle()
    {
        return title;
    }

    public Type getType()
    {
        return type;
    }

    public String getCategory()
    {
        return category;
    }

    public String getCreatorId()
    {
        return creatorId;
    }

    public List<String> getVisibilityZones()
    {
        return visibilityZones;
    }

    public String getDescription()
    {
        return description;
    }

    public DateTime getStartTime()
    {
        return startTime;
    }

    public DateTime getEndTime()
    {
        return endTime;
    }

    public Location getLocation()
    {
        if(location == null)
        {
            return new Location();
        }

        return location;
    }

    public DateTime getCreatedAt()
    {
        return createdAt;
    }

    public DateTime getUpdatedAt()
    {
        return updatedAt;
    }

    public List<Attendee> getAttendees()
    {
        return attendees;
    }

    public RSVP getRsvp()
    {
        return rsvp;
    }

    public String getStatus()
    {
        return status;
    }

    public List<String> getFriends()
    {
        return friends;
    }

    public List<String> getInviter()
    {
        return inviter;
    }

    public List<String> getInvitee()
    {
        return invitee;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public void setTitle(String title)
    {
        this.title = title;
    }

    public void setType(Type type)
    {
        this.type = type;
    }

    public void setCategory(String category)
    {
        this.category = category;
    }

    public void setCreatorId(String creatorId)
    {
        this.creatorId = creatorId;
    }

    public void setVisibilityZones(List<String> visibilityZones)
    {
        this.visibilityZones = visibilityZones;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public void setStartTime(DateTime startTime)
    {
        this.startTime = startTime;
    }

    public void setEndTime(DateTime endTime)
    {
        this.endTime = endTime;
    }

    public void setLocation(Location location)
    {
        this.location = location;
    }

    public void setCreatedAt(DateTime createdAt)
    {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(DateTime updatedAt)
    {
        this.updatedAt = updatedAt;
    }

    public void setAttendees(List<Attendee> attendees)
    {
        this.attendees = attendees;
    }

    public void setRsvp(RSVP rsvp)
    {
        this.rsvp = rsvp;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public void setFriends(List<String> friends)
    {
        this.friends = friends;
    }

    public void setInviter(List<String> inviter)
    {
        this.inviter = inviter;
    }

    public void setInvitee(List<String> invitee)
    {
        this.invitee = invitee;
    }

    public int getFriendCount()
    {
        if(friends == null)
        {
            return 0;
        }

        return friends.size();
    }

    public int getInviterCount()
    {
        if(inviter == null)
        {
            return 0;
        }

        return inviter.size();
    }


    @Override
    public int hashCode()
    {
        return id.hashCode();
    }

    @Override
    public boolean equals(Object o)
    {
        if (o == this)
        {
            return true;
        }

        if (!(o instanceof Event))
        {
            return false;
        }
        else
        {
            Event other = (Event) o;
            return id.equals(other.id);
        }
    }

    @Override
    public String toString()
    {
        return title;
    }

    public boolean isExpired()
    {
        if(DateTime.now().isAfter(endTime))
        {
            return true;
        }else{

            return false;
        }
    }
}
