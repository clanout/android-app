package com.clanout.app.model;

public class PhonebookContact
{
    private String name;
    private String phone;

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getPhone()
    {
        return phone;
    }

    public void setPhone(String phone)
    {
        this.phone = phone;
    }

    @Override
    public int hashCode()
    {
        return phone.hashCode();
    }

    @Override
    public boolean equals(Object o)
    {
        if (o == this)
        {
            return true;
        }

        if (!(o instanceof PhonebookContact))
        {
            return false;
        }
        else
        {
            PhonebookContact other = (PhonebookContact) o;
            return phone.equals(other.phone);
        }
    }
}
