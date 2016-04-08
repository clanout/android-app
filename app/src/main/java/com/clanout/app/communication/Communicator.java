package com.clanout.app.communication;

import com.squareup.otto.Bus;

public class Communicator
{
    private static Communicator instance;

    private Bus bus;

    private Communicator(Bus bus)
    {
        this.bus = bus;
    }

    public static Communicator getInstance()
    {
        if (instance == null)
        {
            throw new IllegalStateException();
        }

        return instance;
    }

    public static void init(Bus bus)
    {
        instance = new Communicator(bus);
    }

    public Bus getBus()
    {
        return bus;
    }
}
