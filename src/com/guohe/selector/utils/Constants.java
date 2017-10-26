package com.guohe.selector.utils;

import java.util.regex.Pattern;

public interface Constants
{
    public static final String RES="res";
    public static final String POINT9SUFFX=".9";
    public static final String DRAWABLE="drawable";
    public static final String COLOR="color";
    public static Pattern VALID_FOLDER_PATTERN = Pattern.compile("^drawable(-[a-zA-Z0-9]+)*$");
    public static final String SELECTOR_XML="_selector.xml";

}
