package org.ruiyun.Util;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class PropUtil {
    private static Properties prop = null;

    private PropUtil() {
    }

    static {
        try {
            prop = new Properties();
            prop.load(new FileInputStream(PropUtil.class.getClassLoader().getResource("saltInvIX").getPath()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Properties getProperty() {
        return prop;
    }

    public static String getProperty(String str) {
//        System.out.println(prop.getProperty(str));
        return prop.getProperty(str);
    }

    public static void setProperty(String key, String val) {
        prop.setProperty(key, val);
    }
}
