package com.guohe.selector.utils;

import com.intellij.ide.util.PropertiesComponent;

/**
 * Created by wff on 2017/10/26.
 */
public class PropertiesUtil {

    private static final String CUSTOM_FILE_NAME_STATE = "custom_file_name_state";
    private static final String AUTO_OPEN_FILE_CHECKBOX = "auto_open_file_checkbox";



    public static void saveCustomFileNameState(boolean selected){
        PropertiesComponent.getInstance().setValue(CUSTOM_FILE_NAME_STATE, selected);
    }

    public static boolean getCustomFileNameState(){
        if (PropertiesComponent.getInstance().isValueSet(CUSTOM_FILE_NAME_STATE)) {
            return Boolean.valueOf(PropertiesComponent.getInstance().getValue(CUSTOM_FILE_NAME_STATE));
        } else {
            return false;
        }
    }

    public static void saveAutoOpenFileCheckBox(boolean selected){
        PropertiesComponent.getInstance().setValue(AUTO_OPEN_FILE_CHECKBOX, selected);
    }

    public static boolean getAutoOpenFileCheckBox(){
        if (PropertiesComponent.getInstance().isValueSet(AUTO_OPEN_FILE_CHECKBOX)) {
            return Boolean.valueOf(PropertiesComponent.getInstance().getValue(AUTO_OPEN_FILE_CHECKBOX));
        } else {
            return false;
        }
    }

}
