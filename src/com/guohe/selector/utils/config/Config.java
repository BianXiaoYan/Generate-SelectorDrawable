package com.guohe.selector.utils.config;

import java.util.Locale;
import java.util.ResourceBundle;

public class Config
{
    static String CONFIG_NAME ="/config/res";
    static ResourceBundle bundle=null;
    static
    {
        Locale locale=Locale.getDefault();
       // Locale locale=Locale.US;
        bundle = ResourceBundle.getBundle(CONFIG_NAME, locale);
    }

    public static String getString(String key)
    {
       return bundle.getString(key);
    }
}
