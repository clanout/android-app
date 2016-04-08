package com.clanout.app.model.util;

import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;

public class PhoneUtils
{
    private static PhoneNumberUtil phoneNumberUtil = PhoneNumberUtil.getInstance();

    public static String sanitize(String number, String defaultCountryCode)
    {
        String phone = parsePhone(number, defaultCountryCode);
        if (phone == null)
        {
            return number;
        }
        else
        {
            return phone;
        }
    }

    public static String parsePhone(String number, String countryCode)
    {
        try
        {
            String regionCode = phoneNumberUtil
                    .getRegionCodeForCountryCode(Integer.parseInt(countryCode));
            Phonenumber.PhoneNumber phoneNumber = phoneNumberUtil
                    .parseAndKeepRawInput(number, regionCode);
            if (phoneNumberUtil.isValidNumber(phoneNumber))
            {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("+");
                stringBuilder.append(phoneNumber.getCountryCode());
                stringBuilder.append(phoneNumber.getNationalNumber());
                return stringBuilder.toString();
            }
        }
        catch (Exception e)
        {
        }
        return null;
    }
}