package com.example.qqhideimgcreate;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 缓存的数据时间是以秒为单位计算的
 *
 * @author Michael Yang（www.yangfuhai.com�?update at 2013.08.07
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class ACache {
    public static final int TIME_HOUR = 60 * 60;
    public static final int TIME_DAY = TIME_HOUR * 24;
    private static final int MAX_SIZE = 1000 * 1000 * 50; // 50 mb
    private static final int MAX_COUNT = Integer.MAX_VALUE; // 不限制存放数据的数量
    private static final Map<String, ACache> mInstanceMap = new HashMap<>();
    private ACacheManager mCache;
    private static final String externalStoryCacheDir = Environment.getExternalStorageDirectory()
            + File.separator + ".ACache";
    private static String defaultCacheFilePath = externalStoryCacheDir;

    //**************        增加自定义静态方法    *****************//
    public static void setDefaultCacheDir(String dir) {
        if (externalStoryCacheDir.equals(defaultCacheFilePath))
            defaultCacheFilePath = dir;
        else
            throw new RuntimeException("Cannot set the cache path repeatedly");
    }

    private static ACache get(@TYPE.DATA_TYPE String type) {
        return get(new File(defaultCacheFilePath + File.separator + type));
    }

    public static String getString(String key) {
        return get(TYPE.STRING).getAsString(key);
    }

    public static void putString(String key, String value) {
        get(TYPE.STRING).put(key, value);
    }

    public static void putString(String key, String value, int time) {
        get(TYPE.STRING).put(key, value, time);
    }

    public static JSONObject getJSONObject(String key) {
        return get(TYPE.JSONOBJECT).getAsJSONObject(key);
    }

    public static void putJSONObject(String key, JSONObject jsonObject) {
        get(TYPE.JSONOBJECT).put(key, jsonObject);
    }

    public static void putJSONObject(String key, JSONObject jsonObject, int time) {
        get(TYPE.JSONOBJECT).put(key, jsonObject, time);
    }

    public static JSONArray getJSONArray(String key) {
        return get(TYPE.JSONARRAY).getAsJSONArray(key);
    }

    public static void putJSONArray(String key, JSONArray jsonArray) {
        get(TYPE.JSONARRAY).put(key, jsonArray);
    }

    public static void putJSONArray(String key, JSONArray jsonArray, int time) {
        get(TYPE.JSONARRAY).put(key, jsonArray, time);
    }

    public static Drawable getDrawable(String key) {
        return get(TYPE.DRAWABLE).getAsDrawable(key);
    }

    public static void putDrawable(String key, Drawable drawable) {
        get(TYPE.DRAWABLE).put(key, drawable);
    }

    public static void putDrawable(String key, Drawable drawable, int time) {
        get(TYPE.DRAWABLE).put(key, drawable, time);
    }

    public static byte[] getBinary(String key) {
        return get(TYPE.BYTE).getAsBinary(key);
    }

    public static void putBinary(String key, byte[] bytes) {
        get(TYPE.BYTE).put(key, bytes);
    }

    public static void putBinary(String key, byte[] bytes, int time) {
        get(TYPE.BYTE).put(key, bytes, time);
    }

    public static Bitmap getBitmap(String key) {
        return get(TYPE.BITMAP).getAsBitmap(key);
    }

    public static void putBitmap(String key, Bitmap bitmap) {
        get(TYPE.BITMAP).put(key, bitmap);
    }

    public static void putBitmap(String key, Bitmap bitmap, int time) {
        get(TYPE.BITMAP).put(key, bitmap, time);
    }

    public static Object getObject(String key) {
        return get(TYPE.OBJECT).getAsObject(key);
    }

    public static void putObject(String key, Serializable object) {
        get(TYPE.OBJECT).put(key, object);
    }

    public static void putObject(String key, Serializable object, int time) {
        get(TYPE.OBJECT).put(key, object, time);
    }

    public static void putBoolean(String key, boolean value) {
        get(TYPE.BOOLEAN).put(key, value);
    }

    public static void putBoolean(String key, boolean value, int time) {
        get(TYPE.BOOLEAN).put(key, value, time);
    }

    public static boolean getBoolean(String key, boolean defaultValue) {
        return get(TYPE.BOOLEAN).getAsBoolean(key, defaultValue);
    }

    public static boolean removeKey(String key, @TYPE.DATA_TYPE String type) {
        return get(type).remove(key);
    }

    public static void clearAll() {
        get(TYPE.STRING).clear();
        get(TYPE.BYTE).clear();
        get(TYPE.JSONOBJECT).clear();
        get(TYPE.JSONARRAY).clear();
        get(TYPE.DRAWABLE).clear();
        get(TYPE.BITMAP).clear();
        get(TYPE.OBJECT).clear();
        get(TYPE.BOOLEAN).clear();
    }

    public static void clear(@TYPE.DATA_TYPE String type) {
        get(type).clear();
    }

    public static File getFile(String key, @TYPE.DATA_TYPE String type) {
        return get(type).file(key);
    }

    //**************        增加自定义静态方法 end     *****************//

    public static ACache get(Context ctx) {
        return get(ctx, "ACache");
    }

    public static ACache get(Context ctx, String cacheName) {  //更改为非缓存
        File f = new File(ctx.getFilesDir(), cacheName);
        return get(f, MAX_SIZE, MAX_COUNT);
    }

    public static ACache get(File cacheDir) {
        return get(cacheDir, MAX_SIZE, MAX_COUNT);
    }

    public static ACache get(Context ctx, long max_zise, int max_count) {
        File f = new File(ctx.getFilesDir(), "ACache");
        return get(f, max_zise, max_count);
    }

    public static ACache get(File cacheDir, long max_zise, int max_count) {
        ACache manager = mInstanceMap.get(cacheDir.getAbsoluteFile() + myPid());
        if (manager == null) {
            manager = new ACache(cacheDir, max_zise, max_count);
            mInstanceMap.put(cacheDir.getAbsolutePath() + myPid(), manager);
        }
        return manager;
    }

    private static String myPid() {
        return "_" + android.os.Process.myPid();
    }

    private ACache(File cacheDir, long max_size, int max_count) {
        if (!cacheDir.exists() && !cacheDir.mkdirs()) {
            throw new RuntimeException("can't make dirs in "
                    + cacheDir.getAbsolutePath());
        }
        mCache = new ACacheManager(cacheDir, max_size, max_count);
    }

    // =======================================
    // ============ String数据 读写 ==============
    // =======================================

    /**
     * 保存 String数据 �?缓存�?
     *
     * @param key   保存的key
     * @param value 保存的String数据
     */
    public void put(String key, String value) {
        File file = mCache.newFile(key);
        BufferedWriter out = null;
        try {
            out = new BufferedWriter(new FileWriter(file), 1024);
            out.write(value);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (out != null) {
                try {
                    out.flush();
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            mCache.put(file);
        }
    }

    /**
     * 保存 String数据 �?缓存�?
     *
     * @param key      保存的key
     * @param value    保存的String数据
     * @param saveTime 保存的时间，单位：秒
     */
    public void put(String key, String value, int saveTime) {
        put(key, Utils.newStringWithDateInfo(saveTime, value));
    }

    /**
     * 读取 String数据
     *
     * @param key 读取的key
     * @return String 数据
     */
    public String getAsString(String key) {
        File file = mCache.get(key);
        if (!file.exists())
            return null;
        boolean removeFile = false;
        BufferedReader in = null;
        try {
            in = new BufferedReader(new FileReader(file));
            StringBuilder readString = new StringBuilder();
            String currentLine;
            while ((currentLine = in.readLine()) != null) {
                readString.append(currentLine);
            }
            if (!Utils.isDue(readString.toString())) {
                return Utils.clearDateInfo(readString.toString());
            } else {
                removeFile = true;
                return null;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (removeFile)
                remove(key);
        }
    }

    // =======================================
    // ============= JSONObject 数据 读写 ==============
    // =======================================

    /**
     * 保存 JSONObject数据 �?缓存�?
     *
     * @param key   保存的key
     * @param value 保存的JSON数据
     */
    public void put(String key, JSONObject value) {
        put(key, value.toString());
    }

    /**
     * 保存 JSONObject数据 �?缓存�?
     *
     * @param key      保存的key
     * @param value    保存的JSONObject数据
     * @param saveTime 保存的时间，单位：秒
     */
    public void put(String key, JSONObject value, int saveTime) {
        put(key, value.toString(), saveTime);
    }

    /**
     * 读取JSONObject数据
     *
     * @param key key
     * @return JSONObject数据
     */
    public JSONObject getAsJSONObject(String key) {
        String JSONString = getAsString(key);
        try {
            return new JSONObject(JSONString);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // =======================================
    // ============ JSONArray 数据 读写 =============
    // =======================================

    /**
     * 保存 JSONArray数据 �?缓存�?
     *
     * @param key   保存的key
     * @param value 保存的JSONArray数据
     */
    public void put(String key, JSONArray value) {
        put(key, value.toString());
    }

    /**
     * 保存 JSONArray数据 �?缓存�?
     *
     * @param key      保存的key
     * @param value    保存的JSONArray数据
     * @param saveTime 保存的时间，单位：秒
     */
    public void put(String key, JSONArray value, int saveTime) {
        put(key, value.toString(), saveTime);
    }

    /**
     * 读取JSONArray数据
     *
     * @param key key
     * @return JSONArray数据
     */
    public JSONArray getAsJSONArray(String key) {
        String JSONString = getAsString(key);
        try {
            return new JSONArray(JSONString);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // =======================================
    // ============== byte 数据 读写 =============
    // =======================================

    /**
     * 保存 byte数据 �?缓存�?
     *
     * @param key   保存的key
     * @param value 保存的数�?
     */
    public void put(String key, byte[] value) {
        File file = mCache.newFile(key);
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(file);
            out.write(value);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (out != null) {
                try {
                    out.flush();
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            mCache.put(file);
        }
    }

    /**
     * 保存 byte数据 �?缓存�?
     *
     * @param key      保存的key
     * @param value    保存的数�?
     * @param saveTime 保存的时间，单位：秒
     */
    public void put(String key, byte[] value, int saveTime) {
        put(key, Utils.newByteArrayWithDateInfo(saveTime, value));
    }

    /**
     * 获取 byte 数据
     *
     * @param key key
     * @return byte 数据
     */
    public byte[] getAsBinary(String key) {
        RandomAccessFile RAFile = null;
        boolean removeFile = false;
        try {
            File file = mCache.get(key);
            if (!file.exists())
                return null;
            RAFile = new RandomAccessFile(file, "r");
            byte[] byteArray = new byte[(int) RAFile.length()];
            RAFile.read(byteArray);
            if (!Utils.isDue(byteArray)) {
                return Utils.clearDateInfo(byteArray);
            } else {
                removeFile = true;
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            if (RAFile != null) {
                try {
                    RAFile.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (removeFile)
                remove(key);
        }
    }

    // =======================================
    // ============= 序列�?数据 读写 ===============
    // =======================================

    /**
     * 保存 Serializable数据 �?缓存�?
     *
     * @param key   保存的key
     * @param value 保存的value
     */
    public void put(String key, Serializable value) {
        put(key, value, -1);
    }


    /**
     * 保存 Serializable数据�?缓存�?
     *
     * @param key      保存的key
     * @param value    保存的value
     * @param saveTime 保存的时间，单位：秒
     */
    public void put(String key, Serializable value, int saveTime) {
        ByteArrayOutputStream baos;
        baos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(value);
            byte[] data = baos.toByteArray();
            if (saveTime != -1) {
                put(key, data, saveTime);
            } else {
                put(key, data);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 读取 Serializable数据
     *
     * @param key key
     * @return Serializable 数据
     */
    public Object getAsObject(String key) {
        byte[] data = getAsBinary(key);
        if (data != null) {
            ByteArrayInputStream bais = null;
            ObjectInputStream ois = null;
            try {
                bais = new ByteArrayInputStream(data);
                ois = new ObjectInputStream(bais);
                return ois.readObject();
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            } finally {
                try {
                    if (bais != null)
                        bais.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    if (ois != null)
                        ois.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;

    }

    public Boolean getAsBoolean(String key, Boolean defaultBoolean) {
        byte[] data = getAsBinary(key);
        if (data != null) {
            ByteArrayInputStream bais = null;
            ObjectInputStream ois = null;
            try {
                bais = new ByteArrayInputStream(data);
                ois = new ObjectInputStream(bais);
                Object reObject = ois.readObject();
                return (Boolean) reObject;
            } catch (Exception e) {
                e.printStackTrace();
                return defaultBoolean;
            } finally {
                try {
                    if (bais != null)
                        bais.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    if (ois != null)
                        ois.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return defaultBoolean;

    }

    // =======================================
    // ============== bitmap 数据 读写 =============
    // =======================================

    /**
     * 保存 bitmap �?缓存�?
     *
     * @param key   保存的key
     * @param value 保存的bitmap数据
     */
    public void put(String key, Bitmap value) {
        put(key, Utils.Bitmap2Bytes(value));
    }

    /**
     * 保存 bitmap �?缓存�?
     *
     * @param key      保存的key
     * @param value    保存�?bitmap 数据
     * @param saveTime 保存的时间，单位：秒
     */
    public void put(String key, Bitmap value, int saveTime) {
        put(key, Utils.Bitmap2Bytes(value), saveTime);
    }

    /**
     * 读取 bitmap 数据
     *
     * @param key key
     * @return bitmap 数据
     */
    public Bitmap getAsBitmap(String key) {
        if (getAsBinary(key) == null) {
            return null;
        }
        return Utils.Bytes2Bimap(getAsBinary(key));
    }

    // =======================================
    // ============= drawable 数据 读写 =============
    // =======================================

    /**
     * 保存 drawable �?缓存�?
     *
     * @param key   保存的key
     * @param value 保存的drawable数据
     */
    public void put(String key, Drawable value) {
        put(key, Utils.drawable2Bitmap(value));
    }

    /**
     * 保存 drawable �?缓存�?
     *
     * @param key      保存的key
     * @param value    保存�?drawable 数据
     * @param saveTime 保存的时间，单位：秒
     */
    public void put(String key, Drawable value, int saveTime) {
        put(key, Utils.drawable2Bitmap(value), saveTime);
    }

    /**
     * 读取 Drawable 数据
     *
     * @param key key
     * @return Drawable 数据
     */
    public Drawable getAsDrawable(String key) {
        if (getAsBinary(key) == null) {
            return null;
        }
        return Utils.bitmap2Drawable(Utils.Bytes2Bimap(getAsBinary(key)));
    }

    /**
     * 获取缓存文件
     *
     * @param key key
     * @return value 缓存的文�?
     */
    public File file(String key) {
        File f = mCache.newFile(key);
        if (f.exists())
            return f;
        return null;
    }

    /**
     * 移除某个key
     *
     * @param key 要移除的文件的key
     * @return 是否移除成功
     */
    public boolean remove(String key) {
        return mCache.remove(key);
    }

    /**
     * 清除�?��数据
     */
    public void clear() {
        mCache.clear();
    }

    /**
     * @author 杨福海（michael�?www.yangfuhai.com
     * @version 1.0
     */
    private class ACacheManager {
        private final AtomicLong cacheSize;
        private final AtomicInteger cacheCount;
        private final long sizeLimit;
        private final int countLimit;
        private final Map<File, Long> lastUsageDates = Collections
                .synchronizedMap(new HashMap<File, Long>());
        private File cacheDir;

        private ACacheManager(File cacheDir, long sizeLimit, int countLimit) {
            this.cacheDir = cacheDir;
            this.sizeLimit = sizeLimit;
            this.countLimit = countLimit;
            cacheSize = new AtomicLong();
            cacheCount = new AtomicInteger();
            calculateCacheSizeAndCacheCount();
        }

        /**
         * 计算 cacheSize和cacheCount
         */
        private void calculateCacheSizeAndCacheCount() {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    int size = 0;
                    int count = 0;
                    File[] cachedFiles = cacheDir.listFiles();
                    if (cachedFiles == null)
                        return;
                    for (File cachedFile : cachedFiles) {
                        size += calculateSize(cachedFile);
                        count += 1;
                        lastUsageDates.put(cachedFile,
                                cachedFile.lastModified());
                    }
                    cacheSize.set(size);
                    cacheCount.set(count);
                }
            }).start();
        }

        private void put(File file) {
            int curCacheCount = cacheCount.get();
            while (curCacheCount + 1 > countLimit) {
                long freedSize = removeNext();
                cacheSize.addAndGet(-freedSize);

                curCacheCount = cacheCount.addAndGet(-1);
            }
            cacheCount.addAndGet(1);

            long valueSize = calculateSize(file);
            long curCacheSize = cacheSize.get();
            while (curCacheSize + valueSize > sizeLimit) {
                long freedSize = removeNext();
                curCacheSize = cacheSize.addAndGet(-freedSize);
            }
            cacheSize.addAndGet(valueSize);

            Long currentTime = System.currentTimeMillis();
            if (file.setLastModified(currentTime))
                lastUsageDates.put(file, currentTime);
        }

        private File get(String key) {
            File file = newFile(key);
            Long currentTime = System.currentTimeMillis();
            if (file.setLastModified(currentTime))
                lastUsageDates.put(file, currentTime);

            return file;
        }

        private File newFile(String key) {
            return new File(cacheDir, String.valueOf(key.hashCode()));
        }

        private boolean remove(String key) {
            return deleteFileSafely(get(key));
        }

        private void clear() {
            lastUsageDates.clear();
            cacheSize.set(0);
            File[] files = cacheDir.listFiles();
            if (files != null)
                for (File f : files)
                    deleteFileSafely(f);
        }

        /**
         * 移除旧的文件
         *
         * @return 移除的文件的大小
         */
        private long removeNext() {
            if (lastUsageDates.isEmpty()) {
                return 0;
            }

            Long oldestUsage = null;
            File mostLongUsedFile = null;
            Set<Entry<File, Long>> entries = lastUsageDates.entrySet();
            synchronized (lastUsageDates) {
                for (Entry<File, Long> entry : entries) {
                    if (mostLongUsedFile == null) {
                        mostLongUsedFile = entry.getKey();
                        oldestUsage = entry.getValue();
                    } else {
                        Long lastValueUsage = entry.getValue();
                        if (lastValueUsage < oldestUsage) {
                            oldestUsage = lastValueUsage;
                            mostLongUsedFile = entry.getKey();
                        }
                    }
                }
            }

            long fileSize = mostLongUsedFile == null ? 0 : calculateSize(mostLongUsedFile);
            if (deleteFileSafely(mostLongUsedFile)) {
                lastUsageDates.remove(mostLongUsedFile);
            }
            return fileSize;
        }

        private long calculateSize(File file) {
            return file.length();
        }

        /**
         * @param file 要删除的文件
         */
        private boolean deleteFileSafely(File file) {
            if (file != null && file.exists()) {
                File tmp = getTmpFile(file, System.currentTimeMillis(), -1);
                return file.renameTo(tmp) ? tmp.delete() : file.delete();
            }
            return false;
        }


        private File getTmpFile(File file, long time, int index) {
            File tmp = index == -1 ? new File(file.getParent() + File.separator + time) :
                    new File(file.getParent() + File.separator + time + "(" + index + ")");
            return !tmp.exists() ? tmp : getTmpFile(file, time, index >= 1000 ? index : ++index);
        }
    }

    /**
     * @author 杨福海（michael�?www.yangfuhai.com
     * @version 1.0
     */
    private static class Utils {

        /**
         * 判断缓存的String数据是否到期
         *
         * @param str str数据
         * @return true：到期了 false：还没有到期
         */
        private static boolean isDue(String str) {
            return isDue(str.getBytes());
        }

        /**
         * 判断缓存的byte数据是否到期
         *
         * @param data byte 数据
         * @return true：到期了 false：还没有到期
         */
        private static boolean isDue(byte[] data) {
            String[] strs = getDateInfoFromDate(data);
            if (strs != null && strs.length == 2) {
                String saveTimeStr = strs[0];
                while (saveTimeStr.startsWith("0")) {
                    saveTimeStr = saveTimeStr
                            .substring(1, saveTimeStr.length());
                }
                long saveTime = Long.valueOf(saveTimeStr);
                long deleteAfter = Long.valueOf(strs[1]);
                return System.currentTimeMillis() > saveTime + deleteAfter * 1000;
            }
            return false;
        }

        private static String newStringWithDateInfo(int second, String strInfo) {
            return createDateInfo(second) + strInfo;
        }

        private static byte[] newByteArrayWithDateInfo(int second, byte[] data2) {
            byte[] data1 = createDateInfo(second).getBytes();
            byte[] retdata = new byte[data1.length + data2.length];
            System.arraycopy(data1, 0, retdata, 0, data1.length);
            System.arraycopy(data2, 0, retdata, data1.length, data2.length);
            return retdata;
        }

        private static String clearDateInfo(String strInfo) {
            if (strInfo != null && hasDateInfo(strInfo.getBytes())) {
                strInfo = strInfo.substring(strInfo.indexOf(mSeparator) + 1,
                        strInfo.length());
            }
            return strInfo;
        }

        private static byte[] clearDateInfo(byte[] data) {
            if (hasDateInfo(data)) {
                return copyOfRange(data, indexOf(data) + 1,
                        data.length);
            }
            return data;
        }

        private static boolean hasDateInfo(byte[] data) {
            return data != null && data.length > 15 && data[13] == '-'
                    && indexOf(data) > 14;
        }

        private static String[] getDateInfoFromDate(byte[] data) {
            if (hasDateInfo(data)) {
                String saveDate = new String(copyOfRange(data, 0, 13));
                String deleteAfter = new String(copyOfRange(data, 14,
                        indexOf(data)));
                return new String[]{saveDate, deleteAfter};
            }
            return null;
        }

        private static int indexOf(byte[] data) {
            for (int i = 0; i < data.length; i++)
                if (data[i] == mSeparator)
                    return i;
            return -1;
        }

        private static byte[] copyOfRange(byte[] original, int from, int to) {
            int newLength = to - from;
            if (newLength < 0)
                throw new IllegalArgumentException(from + " > " + to);
            byte[] copy = new byte[newLength];
            System.arraycopy(original, from, copy, 0,
                    Math.min(original.length - from, newLength));
            return copy;
        }

        private static final char mSeparator = ' ';

        private static String createDateInfo(int second) {
            StringBuilder currentTime = new StringBuilder(System.currentTimeMillis() + "");
            while (currentTime.length() < 13) {
                currentTime.insert(0, "0");
            }
            return currentTime + "-" + second + mSeparator;
        }

        /*
         * Bitmap �?byte[]
         */
        private static byte[] Bitmap2Bytes(Bitmap bm) {
            if (bm == null) {
                return null;
            }
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bm.compress(Bitmap.CompressFormat.PNG, 100, baos);
            return baos.toByteArray();
        }

        /*
         * byte[] �?Bitmap
         */
        private static Bitmap Bytes2Bimap(byte[] b) {
            if (b.length == 0)
                return null;
            return BitmapFactory.decodeByteArray(b, 0, b.length);
        }

        /*
         * Drawable �?Bitmap
         */
        private static Bitmap drawable2Bitmap(Drawable drawable) {
            if (drawable == null)
                return null;
            // �?drawable 的长�?
            int w = drawable.getIntrinsicWidth();
            int h = drawable.getIntrinsicHeight();
            // �?drawable 的颜色格�?
            Bitmap.Config config = drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888
                    : Bitmap.Config.RGB_565;
            // 建立对应 bitmap
            Bitmap bitmap = Bitmap.createBitmap(w, h, config);
            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, w, h);
            drawable.draw(canvas);
            return bitmap;
        }

        /*
         * Bitmap �?Drawable
         */
        private static Drawable bitmap2Drawable(Bitmap bm) {
            if (bm == null) {
                return null;
            }
            return new BitmapDrawable(null, bm);
        }
    }

    public static final class TYPE {
        public static final String STRING = "STRING";
        public static final String BYTE = "BYTE";
        public static final String JSONOBJECT = "JSONOBJECT";
        public static final String JSONARRAY = "JSONARRAY";
        public static final String DRAWABLE = "DRAWABLE";
        public static final String BITMAP = "BITMAP";
        public static final String OBJECT = "OBJECT";
        public static final String BOOLEAN = "BOOLEAN";

        @Retention(RetentionPolicy.SOURCE)
        public @interface DATA_TYPE {
        }
    }
}
