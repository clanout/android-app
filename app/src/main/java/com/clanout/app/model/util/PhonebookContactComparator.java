package com.clanout.app.model.util;

import com.clanout.app.model.PhonebookContact;

import java.util.Comparator;

public class PhonebookContactComparator implements Comparator<PhonebookContact>
{
    @Override
    public int compare(PhonebookContact lhs, PhonebookContact rhs)
    {
        return lhs.getName().toLowerCase().compareTo(rhs.getName().toLowerCase());
    }
}
