package com.nutrition.express.util;

import android.Manifest;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.pm.PackageManager;
import androidx.core.content.ContextCompat;
import android.util.Log;
import android.util.TypedValue;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.nutrition.express.BuildConfig;
import com.nutrition.express.R;
import com.nutrition.express.application.ExpressApplication;
import com.nutrition.express.model.event.EventPermission;

import org.greenrobot.eventbus.EventBus;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import okio.BufferedSink;
import okio.BufferedSource;
import okio.Okio;
import okio.Sink;
import okio.Source;

/**
 * Created by huang on 5/16/16.
 */
public class Utils {
    public static float dp2Pixels(Context context, int dp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
                context.getResources().getDisplayMetrics());
    }

    public static void copy2Clipboard(Context context, String string) {
        ClipboardManager clipboardManager =
                (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clipData = ClipData.newPlainText("Tumblr", string);
        clipboardManager.setPrimaryClip(clipData);
        Toast.makeText(context, R.string.copy_to_clipboard, Toast.LENGTH_SHORT).show();
    }

    public static boolean canWrite2Storage() {
        if (ContextCompat.checkSelfPermission(ExpressApplication.getApplication(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            EventBus.getDefault().post(new EventPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE));
            return false;
        }
        return true;
    }

    public static String md5sum(String input) {
        try {
            MessageDigest mdEnc = MessageDigest.getInstance("MD5");
            mdEnc.update(input.getBytes(), 0, input.length());
            String md5 = new BigInteger(1, mdEnc.digest()).toString(16);
            while ( md5.length() < 32 ) {
                md5 = "0" + md5;
            }
            return md5;
        } catch (NoSuchAlgorithmException e) {
            System.out.println("Exception while encrypting to md5");
            e.printStackTrace();
        }
        return input;
    }

    public static boolean store(File file, byte[] content) {
        if (content == null || content.length == 0) return false;
        try {
            ByteArrayInputStream in = new ByteArrayInputStream(content);
            Source source = Okio.source(in);
            Sink sink = Okio.sink(file);
            BufferedSink bufferedSink = Okio.buffer(sink);
            bufferedSink.writeAll(source);
            source.close();
            bufferedSink.close();
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public static byte[] read(File file) {
        if (!file.exists()) return null;
        try {
            Source source = Okio.source(file);
            byte[] bytes = new byte[(int) file.length()];
            BufferedSource bufferedSource = Okio.buffer(source);
            bufferedSource.read(bytes);
            return bytes;
        } catch (IOException e) {
            return null;
        }
    }

    /**
     *
     * @param name file name
     * @param object java bean object
     */
    public static void store(String name, Object object) {
        if (object == null) {
            File file = new File(ExpressApplication.getApplication().getFilesDir(), name);
            file.delete();
            return;
        }
        Gson gson = new Gson();
        String content  = gson.toJson(object);
        File file = new File(ExpressApplication.getApplication().getFilesDir(), name);
        store(file, content.getBytes());
    }

    /**
     *
     * @param name file name
     * @param typeOfT Example : new TypeToken<LinkedHashSet<String>>(){}.getType()
     * @return the target object, a java bean object.
     */
    public static <T> T read(String name, Type typeOfT) {
        File file = new File(ExpressApplication.getApplication().getFilesDir(), name);
        byte[] content = read(file);
        if (content == null) return null;
        String string = new String(content);
        if (BuildConfig.DEBUG) {
            Log.d("TAG", "getShortContent: " + string);
        }
        try {
            Gson gson = new Gson();
            return gson.fromJson(string, typeOfT);
        } catch (JsonParseException e) {
            e.printStackTrace();
            return null;
        }
    }

}
