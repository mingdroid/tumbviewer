package com.nutrition.express.model.helper;

import android.util.Log;

import com.google.gson.Gson;
import com.nutrition.express.BuildConfig;
import com.nutrition.express.application.TumbApp;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;

/**
 * Created by hm on 8/17/16.
 */
public class LocalPersistenceHelper {

    /**
     *
     * @param name store file name
     * @param content write to file
     */
    private static void storeShortContent(File name, String content) {
        FileWriter writer = null;
        try {
            writer = new FileWriter(name);
            writer.write(content);
        } catch (IOException e) {
        } finally {
            try {
                if (writer != null) {
                    writer.close();
                }
            } catch (IOException e) {
            }
        }
    }

    /**
     *
     * @param name store file name
     */
    private static String getShortContent(File name) {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(name));
            StringBuilder builder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
            return builder.toString();
        } catch (IOException e) {
            return "";
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {

            }
        }

    }

    /**
     *
     * @param name file name
     * @param object java bean object
     */
    public static void storeShortContent(String name, Object object) {
        Gson gson = new Gson();
        String content  = gson.toJson(object);
        File file = new File(TumbApp.Companion.getApp().getFilesDir(), name);
        storeShortContent(file, content);
    }

    /**
     *
     * @param name file name
     * @param typeOfT Example : new TypeToken<LinkedHashSet<String>>(){}.getType()
     * @return the target object, a java bean object.
     */
    public static <T> T getShortContent(String name, Type typeOfT) {
        File file = new File(TumbApp.Companion.getApp().getFilesDir(), name);
        String content = getShortContent(file);
        if (BuildConfig.DEBUG) {
            Log.d("TAG", "getShortContent: " + content);
        }
        Gson gson = new Gson();
        return gson.fromJson(content, typeOfT);
    }

}
