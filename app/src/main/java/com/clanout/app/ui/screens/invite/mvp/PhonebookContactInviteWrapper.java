package com.clanout.app.ui.screens.invite.mvp;

import com.clanout.app.model.PhonebookContact;

public class PhonebookContactInviteWrapper
{
    private PhonebookContact phonebookContact;
    private boolean isSelected;

    public PhonebookContact getPhonebookContact()
    {
        return phonebookContact;
    }

    public void setPhonebookContact(PhonebookContact phonebookContact)
    {
        this.phonebookContact = phonebookContact;
    }

    public boolean isSelected()
    {
        return isSelected;
    }

    public void setSelected(boolean selected)
    {
        isSelected = selected;
    }
}
