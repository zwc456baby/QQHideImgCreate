package com.example.qqhideimgcreate.Utils;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Message;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.ref.Reference;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.RetentionPolicy.SOURCE;

//引用到其他包时，可能导入其他包时会异常，更改全包路径


/**
 * Created by hasee on 2017/6/9.
 * 入口类
 */
@SuppressWarnings("all")
public final class Log {
    private Log() {
    }

    private static Logger printer = new Logger();
    private static LogConfigImpl logConfig = LogConfigImpl.getInstance();

    public static LogConfig getLogConfig() {
        return logConfig;
    }

    public static String get(Object object) {
        return printer.get(object);
    }

    public static void v(Object object) {
        printer.v(object);
    }


    public static void d(Object object) {
        printer.d(object);
    }


    public static void i(Object object) {
        printer.i(object);
    }


    public static void w(Object object) {
        printer.w(object);
    }


    public static void e(Object object) {
        printer.e(object);
    }


    public static void wtf(Object object) {
        printer.wtf(object);
    }

    public static void json(String json) {
        printer.json(json);
    }

    public static void xml(String xml) {
        printer.xml(xml);
    }

    public static void v(String tag, Object object) {
        printer.setTag(tag).v(object);
    }


    public static void d(String tag, Object object) {
        printer.setTag(tag).d(object);
    }


    public static void i(String tag, Object object) {
        printer.setTag(tag).i(object);
    }


    public static void w(String tag, Object object) {
        printer.setTag(tag).w(object);
    }


    public static void e(String tag, Object object) {
        printer.setTag(tag).e(object);
    }


    public static void wtf(String tag, Object object) {
        printer.setTag(tag).wtf(object);
    }

    public static void json(String tag, String json) {
        printer.setTag(tag).json(json);
    }

    public static void xml(String tag, String xml) {
        printer.setTag(tag).xml(xml);
    }

    /**
     * Created by pengwei08 on 2015/7/20.
     */
    private static class Logger implements Printer {
        private LogConfigImpl mLogConfig;
        private final ThreadLocal<String> localTags = new ThreadLocal<String>();

        private Logger() {
            mLogConfig = LogConfigImpl.getInstance();
            mLogConfig.addParserClass(Constant.DEFAULT_PARSE_CLASS);
        }


        public Printer setTag(String tag) {
            if (!TextUtils.isEmpty(tag) && mLogConfig.isEnable())
                localTags.set(tag);

            return this;
        }

        private void logString(@LogLevel.LogLevelType int type, String tag, String msg) {
            if (msg.length() > Constant.LINE_MAX) {
                for (String subMsg : Utils.largeStringToList(msg))
                    printLog(type, tag, subMsg);

                return;
            }
            printLog(type, tag, msg);
        }

        private void logObject(@LogLevel.LogLevelType int type, Object objecttmp) {
            if (!mLogConfig.isEnable())
                return;

            if (type < mLogConfig.getLogLevel())
                return;

            String tempTag = localTags.get();
            if (!TextUtils.isEmpty(tempTag)) {
                localTags.remove();
                logString(type, tempTag, ObjectUtil.objectToString(objecttmp));
            } else {
                logString(type, mLogConfig.getTagPrefix(), ObjectUtil.objectToString(objecttmp));
            }
        }

        private StackTraceElement getCurrentStackTrace() {
            StackTraceElement[] trace = Thread.currentThread().getStackTrace();
            int stackOffset = getStackOffset(trace, Log.class);
            return stackOffset == -1 ? null : trace[stackOffset];
        }

        private int getStackOffset(StackTraceElement[] trace, Class<Log> cla) {
            String claName = cla.getName();
            for (int i = Constant.MIN_STACK_OFFSET; i < trace.length; i++) {
                if (trace[i].getClassName().equals(claName)) return ++i;
            }
            return -1;
        }

        private String getTopStackInfo() {
            StackTraceElement caller = getCurrentStackTrace();
            if (caller == null) return "Null Stack Trace";
            String stackTrace = caller.toString();
            stackTrace = stackTrace.substring(stackTrace.lastIndexOf('('), stackTrace.length());
            String tag = "%s.%s%s";
            String callerClazzName = caller.getClassName();
            callerClazzName = callerClazzName.substring(callerClazzName.lastIndexOf(".") + 1);
            tag = String.format(tag, callerClazzName, caller.getMethodName(), stackTrace);
            return tag;
        }

        @Override
        public void d(Object object) {
            logObject(LogLevel.TYPE_DEBUG, object);
        }


        @Override
        public void e(Object object) {
            logObject(LogLevel.TYPE_ERROR, object);
        }


        @Override
        public void w(Object object) {
            logObject(LogLevel.TYPE_WARM, object);
        }


        @Override
        public void i(Object object) {
            logObject(LogLevel.TYPE_INFO, object);
        }


        @Override
        public void v(Object object) {
            logObject(LogLevel.TYPE_VERBOSE, object);
        }


        @Override
        public void wtf(Object object) {
            logObject(LogLevel.TYPE_WTF, object);
        }

        @Override
        public void json(String json) {
            int indent = 4;
            if (TextUtils.isEmpty(json)) {
                logObject(LogLevel.TYPE_DEBUG, "JSON{json is empty}");
                return;
            }
            try {
                if (json.startsWith("{")) {
                    JSONObject jsonObject = new JSONObject(json);
                    String msg = jsonObject.toString(indent);
                    logObject(LogLevel.TYPE_DEBUG, msg);
                } else if (json.startsWith("[")) {
                    JSONArray jsonArray = new JSONArray(json);
                    String msg = jsonArray.toString(indent);
                    logObject(LogLevel.TYPE_DEBUG, msg);
                }
            } catch (JSONException e) {
                logObject(LogLevel.TYPE_ERROR, e.toString() + "\n\njson = " + json);
            }
        }

        @Override
        public void xml(String xml) {
            if (TextUtils.isEmpty(xml)) {
                logObject(LogLevel.TYPE_DEBUG, "XML{xml is empty}");
                return;
            }
            try {
                Source xmlInput = new StreamSource(new StringReader(xml));
                StreamResult xmlOutput = new StreamResult(new StringWriter());
                Transformer transformer = TransformerFactory.newInstance().newTransformer();
                transformer.setOutputProperty(OutputKeys.INDENT, "yes");
                transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
                transformer.transform(xmlInput, xmlOutput);
                logObject(LogLevel.TYPE_DEBUG, xmlOutput.getWriter().toString().replaceFirst(">", ">\n"));
            } catch (TransformerException e) {
                logObject(LogLevel.TYPE_ERROR, e.toString() + "\n\nxml = " + xml);
            }
        }

        private final String mSeparator1 = ": ";

        private void printLog(@LogLevel.LogLevelType int type, String tag, String msg) {
            msg = getTopStackInfo() + mSeparator1 + msg;
            switch (type) {
                case LogLevel.TYPE_VERBOSE:
                    android.util.Log.v(tag, msg);
                    savaLogToFile("V", tag, msg);
                    break;
                case LogLevel.TYPE_DEBUG:
                    android.util.Log.d(tag, msg);
                    savaLogToFile("D", tag, msg);
                    break;
                case LogLevel.TYPE_INFO:
                    android.util.Log.i(tag, msg);
                    savaLogToFile("I", tag, msg);
                    break;
                case LogLevel.TYPE_WARM:
                    android.util.Log.w(tag, msg);
                    savaLogToFile("W", tag, msg);
                    break;
                case LogLevel.TYPE_ERROR:
                    android.util.Log.e(tag, msg);
                    savaLogToFile("E", tag, msg);
                    break;
                case LogLevel.TYPE_WTF:
                    android.util.Log.wtf(tag, msg);
                    savaLogToFile("WTF", tag, msg);
                    break;
                default:
                    break;
            }
        }

        private void savaLogToFile(String level, String tag, String msg) {
            LogS.getInstance().addOnlineLog(level, tag, msg);
        }

        @Override
        public String get(Object object) {
            return ObjectUtil.objectToString(object);
        }

    }

    //********************** **********************************************************************
    //**********************  以下为配置类  ********************************************
    //********************** **********************************************************************

    /**
     * Created by pengwei on 16/3/4.
     * Log config
     */
    private static final class LogConfigImpl implements LogConfig {

        private boolean enable = true;
        private String tagPrefix;
        @LogLevel.LogLevelType
        private int logLevel = LogLevel.TYPE_VERBOSE;
        private List<Parser> parseList;
        private static LogConfigImpl singleton;

        private LogConfigImpl() {
            parseList = new ArrayList<>();
        }

        private static LogConfigImpl getInstance() {
            if (singleton == null) {
                synchronized (LogConfigImpl.class) {
                    if (singleton == null)
                        singleton = new LogConfigImpl();
                }
            }
            return singleton;
        }

        @Override
        public LogConfig configAllowLog(boolean allowLog) {
            this.enable = allowLog;
            return this;
        }

        @Override
        public LogConfig configTagPrefix(String prefix) {
            this.tagPrefix = prefix;
            return this;
        }


        @Override
        public LogConfig configLevel(@LogLevel.LogLevelType int logLevel) {
            this.logLevel = logLevel;
            return this;
        }

        @Override
        public LogConfig addParserClass(Class<? extends Parser>... classes) {
            for (Class<? extends Parser> cla : classes) {
                try {
                    parseList.add(0, cla.newInstance());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return this;
        }


        private boolean isEnable() {
            return enable;
        }

        private String getTagPrefix() {
            if (TextUtils.isEmpty(tagPrefix))
                return "LogUtils";


            return tagPrefix;
        }

        private int getLogLevel() {
            return logLevel;
        }

        private List<Parser> getParseList() {
            return parseList;
        }
    }
    //********************** **********************************************************************
    //**********************  以下为常量类  ********************************************
    //********************** **********************************************************************

    /**
     * Created by pengwei on 16/4/18.
     */
    private static class Constant {

        private static final String STRING_OBJECT_NULL = "Object[object is null]";

        // 每行最大日志长度
        private static final int LINE_MAX = 1024 * 3;

        // 解析属性最大层级
        private static final int MAX_CHILD_LEVEL = 2;

        private static final int MIN_STACK_OFFSET = 5;

        // 换行符
        private static final String BR = System.getProperty("line.separator");

        // 默认支持解析库
        private static final Class<? extends Parser>[] DEFAULT_PARSE_CLASS = new Class[]{
                BundleParse.class, IntentParse.class, CollectionParse.class,
                MapParse.class, ThrowableParse.class, ReferenceParse.class, MessageParse.class
        };


        private static List<Parser> getParsers() {
            return LogConfigImpl.getInstance().getParseList();
        }
    }
    //********************** **********************************************************************
    //**********************  以下为工具类  ********************************************
    //********************** **********************************************************************

    /**
     * Created by pengwei08 on 2015/7/20.
     */
    private static final class ObjectUtil {

        private static String objectToString(Object object) {
            return objectToString(object, 0);
        }

        private static boolean isStaticInnerClass(Class cla) {
            if (cla != null && cla.isMemberClass()) {
                int modifiers = cla.getModifiers();
                if ((modifiers & Modifier.STATIC) == Modifier.STATIC)
                    return true;

            }
            return false;
        }

        private static String objectToString(Object object, int childLevel) {
            if (object == null)
                return Constant.STRING_OBJECT_NULL;

            if (childLevel > Constant.MAX_CHILD_LEVEL)
                return object.toString();

            if (Constant.getParsers() != null && Constant.getParsers().size() > 0)
                for (Parser parser : Constant.getParsers())
                    if (parser.parseClassType().isAssignableFrom(object.getClass()))
                        return parser.parseString(object);

            if (ArrayUtil.isArray(object))
                return ArrayUtil.parseArray(object);

            if (object.toString().startsWith(object.getClass().getName() + "@")) {
                StringBuilder builder = new StringBuilder();
                getClassFields(object.getClass(), builder, object, false, childLevel);
                Class superClass = object.getClass().getSuperclass();
                while (!superClass.equals(Object.class)) {
                    getClassFields(superClass, builder, object, true, childLevel);
                    superClass = superClass.getSuperclass();
                }
                return builder.toString();
            } else
                // 若对象重写toString()方法默认走toString()
                return object.toString();
        }

        private static void getClassFields(Class cla, StringBuilder builder, Object o, boolean isSubClass,
                                           int childOffset) {
            if (cla.equals(Object.class))
                return;

            if (isSubClass)
                builder.append(Constant.BR).append(Constant.BR).append("=> ");

            String breakLine = "";
            builder.append(cla.getSimpleName()).append(" {");
            Field[] fields = cla.getDeclaredFields();
            for (int i = 0; i < fields.length; ++i) {
                Field field = fields[i];
                field.setAccessible(true);
                if (cla.isMemberClass() && !isStaticInnerClass(cla) && i == 0)
                    continue;

                Object subObject = null;
                try {
                    subObject = field.get(o);
                } catch (IllegalAccessException e) {
                    subObject = e;
                } finally {
                    if (subObject != null) {
                        // 解决Instant Run情况下内部类死循环的问题
                        if (!(!isStaticInnerClass(cla) && (field.getName().equals("$change")
                                || field.getName().equalsIgnoreCase("this$0")))) {
                            if (subObject instanceof String)
                                subObject = "\"" + subObject + "\"";
                            else if (subObject instanceof Character)
                                subObject = "\'" + subObject + "\'";

                            if (childOffset < Constant.MAX_CHILD_LEVEL)
                                subObject = objectToString(subObject, childOffset + 1);
                        }
                    }
                    String formatString = breakLine + "%s = %s, ";
                    builder.append(String.format(formatString, field.getName(),
                            subObject == null ? "null" : subObject.toString()));
                }
            }
            if (builder.toString().endsWith("{"))
                builder.append("}");
            else
                builder.replace(builder.length() - 2, builder.length() - 1, breakLine + "}");

        }
    }

    /**
     * Created by pengwei on 16/4/19.
     */
    private static final class Utils {

        private static List<String> largeStringToList(String msg) {
            List<String> stringList = new ArrayList<>();
            int index = 0;
            int maxLength = Constant.LINE_MAX;
            int countOfSub = msg.length() / maxLength;
            if (countOfSub > 0) {
                for (int i = 0; i < countOfSub; i++) {
                    String sub = msg.substring(index, index + maxLength);
                    stringList.add(sub);
                    index += maxLength;
                }
                stringList.add(msg.substring(index, msg.length()));
            } else
                stringList.add(msg);

            return stringList;
        }
    }
//***********************  ArrayUtil  ****************************

    /**
     * Created by pengwei08 on 2015/7/25.
     * Thanks to zhutiantao for providing an array of analytical methods.
     */
    private static final class ArrayUtil {

        private static int getArrayDimension(Object object) {
            int dim = 0;
            for (int i = 0; i < object.toString().length(); ++i) {
                if (object.toString().charAt(i) == '[')
                    ++dim;
                else
                    break;

            }
            return dim;
        }

        /**
         * 是否为数组
         *
         * @param object object
         * @return 是否为数组
         */
        private static boolean isArray(Object object) {
            return object.getClass().isArray();
        }

        private static char getType(Object object) {
            if (isArray(object)) {
                String str = object.toString();
                return str.substring(str.lastIndexOf("[") + 1, str.lastIndexOf("[") + 2).charAt(0);
            }
            return 0;
        }

        private static void traverseArray(StringBuilder result, Object array) {
            if (isArray(array)) {
                if (getArrayDimension(array) == 1) {
                    switch (getType(array)) {
                        case 'I':
                            result.append(Arrays.toString((int[]) array));
                            break;
                        case 'D':
                            result.append(Arrays.toString((double[]) array));
                            break;
                        case 'Z':
                            result.append(Arrays.toString((boolean[]) array));
                            break;
                        case 'B':
                            result.append(Arrays.toString((byte[]) array));
                            break;
                        case 'S':
                            result.append(Arrays.toString((short[]) array));
                            break;
                        case 'J':
                            result.append(Arrays.toString((long[]) array));
                            break;
                        case 'F':
                            result.append(Arrays.toString((float[]) array));
                            break;
                        case 'L':
                            Object[] objects = (Object[]) array;
                            result.append("[");
                            for (int i = 0; i < objects.length; ++i) {
                                result.append(ObjectUtil.objectToString(objects[i]));
                                if (i != objects.length - 1) {
                                    result.append(",");
                                }
                            }
                            result.append("]");
                            break;
                        default:
                            result.append(Arrays.toString((Object[]) array));
                            break;
                    }
                } else {
                    result.append("[");
                    for (int i = 0; i < ((Object[]) array).length; i++) {
                        traverseArray(result, ((Object[]) array)[i]);
                        if (i != ((Object[]) array).length - 1)
                            result.append(",");

                    }
                    result.append("]");
                }
            } else
                result.append("not a array!!");

        }

        /**
         * 将数组内容转化为字符串
         *
         * @param array 数组
         * @return 字符串
         */
        private static String parseArray(Object array) {
            StringBuilder result = new StringBuilder();
            traverseArray(result, array);
            return result.toString();
        }
    }
    //********************** **********************************************************************
    //**********************  以下为接口类  ********************************************
    //********************** **********************************************************************

    /**
     * Created by pengwei on 16/3/4.
     */
    public interface LogConfig {

        LogConfig configAllowLog(boolean allowLog);

        LogConfig configTagPrefix(String prefix);


        LogConfig configLevel(@LogLevel.LogLevelType int logLevel);

        LogConfig addParserClass(Class<? extends Parser>... classes);

    }

//**********************************  Printer ************************************

    /**
     * Created by pengwei08 on 2015/7/20.
     */
    public interface Printer {

        void d(Object object);

        void e(Object object);

        void w(Object object);

        void i(Object object);

        void v(Object object);

        void wtf(Object object);

        void json(String json);

        void xml(String xml);

        String get(Object object);

    }

    /**
     * Created by pengwei on 16/3/8.
     * 格式化对象
     */
    public interface Parser<T> {

        String LINE_SEPARATOR = Constant.BR;

        Class<T> parseClassType();

        String parseString(T t);
    }

    /**
     * Created by pengwei on 16/3/3.
     */
    public static final class LogLevel {
        public static final int TYPE_VERBOSE = 1;
        public static final int TYPE_DEBUG = 2;
        public static final int TYPE_INFO = 3;
        public static final int TYPE_WARM = 4;
        public static final int TYPE_ERROR = 5;
        public static final int TYPE_WTF = 6;

        @mIntDef({TYPE_VERBOSE, TYPE_DEBUG, TYPE_INFO, TYPE_WARM, TYPE_ERROR, TYPE_WTF})
        @Retention(RetentionPolicy.SOURCE)
        public @interface LogLevelType {
        }
    }


    @Retention(SOURCE)
    @Target({ANNOTATION_TYPE})
    private @interface mIntDef {
        /**
         * Defines the allowed constants for this element
         */
        long[] value() default {};

        /**
         * Defines whether the constants can be used as a flag, or just as an enum (the default)
         */
        boolean flag() default false;
    }


    //********************** **********************************************************************
    //**********************  以下为默认支持的解析类  ********************************************
    //********************** **********************************************************************

    /**
     * Created by pengwei on 16/3/8.
     */
    static final class BundleParse implements Parser<Bundle> {

        @Override
        public Class<Bundle> parseClassType() {
            return Bundle.class;
        }

        @Override
        public String parseString(Bundle bundle) {
            if (bundle != null) {
                StringBuilder builder = new StringBuilder(bundle.getClass().getName() + " [" + LINE_SEPARATOR);
                for (String key : bundle.keySet()) {
                    builder.append(String.format("'%s' => %s " + LINE_SEPARATOR,
                            key, ObjectUtil.objectToString(bundle.get(key))));
                }
                builder.append("]");
                return builder.toString();
            }
            return null;
        }
    }

    /**
     * Created by pengwei on 16/3/8.
     */
    static final class CollectionParse implements Parser<Collection> {

        @Override
        public Class<Collection> parseClassType() {
            return Collection.class;
        }

        @Override
        public String parseString(Collection collection) {
            String simpleName = collection.getClass().getName();
            StringBuilder msg = new StringBuilder("%s size = %d [" + Constant.BR);
            msg = new StringBuilder(String.format(msg.toString(), simpleName, collection.size()));
            if (!collection.isEmpty()) {
                Iterator iterator = collection.iterator();
                int flag = 0;
                while (iterator.hasNext()) {
                    String itemString = "[%d]:%s%s";
                    Object item = iterator.next();
                    msg.append(String.format(Locale.getDefault(), itemString, flag, ObjectUtil.objectToString(item),
                            flag++ < collection.size() - 1 ? "," + LINE_SEPARATOR : LINE_SEPARATOR));
                }
            }
            return msg + "]";
        }
    }

    /**
     * Created by pengwei on 16/3/8.
     */
    static final class IntentParse implements Parser<Intent> {

        private static HashMap<Integer, String> flagMap = new HashMap();

        static {

            Class cla = Intent.class;
            Field[] fields = cla.getDeclaredFields();
            for (Field field : fields) {
                field.setAccessible(true);
                if (field.getName().startsWith("FLAG_")) {
                    int value = 0;
                    try {
                        Object object = field.get(cla);
                        if (object instanceof Integer || object.getClass().getSimpleName().equals("int"))
                            value = (int) object;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if (flagMap.get(value) == null)
                        flagMap.put(value, field.getName());
                }
            }
        }

        @Override
        public Class<Intent> parseClassType() {
            return Intent.class;
        }

        @Override
        public String parseString(Intent intent) {
            String builder = parseClassType().getSimpleName() + " [" + LINE_SEPARATOR + String.format("%s = %s" + LINE_SEPARATOR, "Scheme", intent.getScheme()) +
                    String.format("%s = %s" + LINE_SEPARATOR, "Action", intent.getAction()) +
                    String.format("%s = %s" + LINE_SEPARATOR, "DataString", intent.getDataString()) +
                    String.format("%s = %s" + LINE_SEPARATOR, "Type", intent.getType()) +
                    String.format("%s = %s" + LINE_SEPARATOR, "Package", intent.getPackage()) +
                    String.format("%s = %s" + LINE_SEPARATOR, "ComponentInfo", intent.getComponent()) +
                    String.format("%s = %s" + LINE_SEPARATOR, "Flags", getFlags(intent.getFlags())) +
                    String.format("%s = %s" + LINE_SEPARATOR, "Categories", intent.getCategories()) +
                    String.format("%s = %s" + LINE_SEPARATOR, "Extras",
                            new BundleParse().parseString(intent.getExtras()));
            return builder + "]";
        }

        /**
         * 获取flag的值
         * 感谢涛哥提供的方法(*^__^*)
         */
        private String getFlags(int flags) {
            StringBuilder builder = new StringBuilder();
            for (int flagKey : flagMap.keySet()) {
                if ((flagKey & flags) == flagKey) {
                    builder.append(flagMap.get(flagKey));
                    builder.append(" | ");
                }
            }
            if (TextUtils.isEmpty(builder.toString()))
                builder.append(flags);
            else if (builder.indexOf("|") != -1)
                builder.delete(builder.length() - 2, builder.length());

            return builder.toString();
        }
    }

    /**
     * Created by pengwei on 16/3/8.
     */
    static final class MapParse implements Parser<Map> {
        @Override
        public Class<Map> parseClassType() {
            return Map.class;
        }

        @Override
        public String parseString(Map map) {
            StringBuilder msg = new StringBuilder(map.getClass().getName() + " [" + LINE_SEPARATOR);
            Set keys = map.keySet();
            for (Object key : keys) {
                String itemString = "%s -> %s" + LINE_SEPARATOR;
                Object value = map.get(key);
                if (value != null) {
                    if (value instanceof String)
                        value = "\"" + value + "\"";
                    else if (value instanceof Character)
                        value = "\'" + value + "\'";

                }
                msg.append(String.format(itemString, ObjectUtil.objectToString(key),
                        ObjectUtil.objectToString(value)));
            }
            return msg + "]";
        }
    }

    /**
     * Created by pengwei on 2017/3/29.
     */

    static final class MessageParse implements Parser<Message> {
        @Override
        public Class<Message> parseClassType() {
            return Message.class;
        }

        @Override
        public String parseString(Message message) {
            if (message == null)
                return null;

            return message.getClass().getName() + " [" + LINE_SEPARATOR + String.format("%s = %s", "what", message.what) + LINE_SEPARATOR +
                    String.format("%s = %s", "when", message.getWhen()) + LINE_SEPARATOR +
                    String.format("%s = %s", "arg1", message.arg1) + LINE_SEPARATOR +
                    String.format("%s = %s", "arg2", message.arg2) + LINE_SEPARATOR +
                    String.format("%s = %s", "data",
                            new BundleParse().parseString(message.getData())) +
                    LINE_SEPARATOR +
                    String.format("%s = %s", "obj",
                            ObjectUtil.objectToString(message.obj)) +
                    LINE_SEPARATOR +
                    "]";
        }
    }

    /**
     * Created by pengwei on 16/3/22.
     */
    static final class ReferenceParse implements Parser<Reference> {
        @Override
        public Class<Reference> parseClassType() {
            return Reference.class;
        }

        @Override
        public String parseString(Reference reference) {
            Object actual = reference.get();
            String builder = reference.getClass().getSimpleName() + "<"
                    + actual.getClass().getSimpleName() + "> {" + "→" + ObjectUtil.objectToString(actual);
            return builder + "}";
        }
    }

    /**
     * Created by pengwei on 16/3/8.
     */
    static final class ThrowableParse implements Parser<Throwable> {
        @Override
        public Class<Throwable> parseClassType() {
            return Throwable.class;
        }

        @Override
        public String parseString(Throwable throwable) {
            return android.util.Log.getStackTraceString(throwable);
        }
    }

    private static class TextUtils {
        private static boolean isEmpty(String str) {
            return str == null || str.equals("");
        }
    }

//    保存日志功能

    private static class LogS {
        private FileOutputStream outStream = null;
        private final Object obj = new Object();

        private long currentFileTime = 0;
        private final long newFileTime = 86400000;
        private final String dayType = "yyyy-MM-dd";
        private SimpleDateFormat day = new SimpleDateFormat(dayType, Locale.getDefault());

        private final String LogDir = Environment.getExternalStorageDirectory().toString()
                + File.separator + "LogUtilsFile" + File.separator;
        private final boolean canWrite = new File(Environment
                .getExternalStorageDirectory().getAbsolutePath()).canWrite();

        private final long MaxFileSize = 100 * 1024 * 1024;
        private Calendar c = Calendar.getInstance();
        private File currentLogFile;
        private String timeTitle;
        private final String enter = "\n";
        private final String separator = File.separator;
        private final String speter = " ";

        private byte[] errorLog;

        private static LogS logclass;

        private LogS() {
            InitStream();
        }

        private static LogS getInstance() {
            if (logclass == null) {
                synchronized (LogS.class) {
                    if (logclass == null) {
                        logclass = new LogS();
                    }
                }
            }
            return logclass;
        }

        private void InitStream() {
            c.setTimeInMillis(System.currentTimeMillis());
            timeTitle = (c.get(Calendar.YEAR)
                    + "/"
                    + (c.get(Calendar.MONTH) + 1)
                    + "/"
                    + c.get(Calendar.DATE)
                    + speter
                    + c.get(Calendar.HOUR_OF_DAY)
                    + ":"
                    + c.get(Calendar.MINUTE))
                    + ":"
                    + c.get(Calendar.SECOND)
            ;
            if (JudgeNewStream()) {  //循环判断是否需要创建新的文件流
                synchronized (obj) {
                    if (JudgeNewStream()) {
                        closeOutPutStream(); //如果需要,则关闭当前文件流
                        outStream = newOutPutStream();  //重新创建文件流
                    }
                }
            }
            createStreamTime = System.currentTimeMillis();
        }

        private void addOnlineLog(String level, String tag, String msg) {
            if (!canWrite) return;
            if (createStreamTime < (System.currentTimeMillis() - 1000)) {
                InitStream();
            }
            String logs = timeTitle + separator + level + separator + tag + speter + msg + enter;
            savaLineLog(logs.getBytes());
        }

        private long createStreamTime = System.currentTimeMillis();

        private void savaLineLog(byte[] logs) {
            try {
                outStream.write(logs);
                outStream.flush();
            } catch (Exception e) {
                synchronized (obj) {
                    if (Arrays.equals(logs, errorLog))
                        return;
                    errorLog = logs;
                    InitStream();
                    savaLineLog(errorLog);
                }
            }
        }

        private FileOutputStream newOutPutStream() {
            if (!canWrite)
                return null;
            try {
                deleteFiles(LogDir, 7, 0);
                String createfileTime = day.format(c.getTime());
                currentFileTime = stringToLong(createfileTime);
                currentLogFile = new File(LogDir + createfileTime + "Log.txt");
                if (!currentLogFile.exists()) {
                    File dir = new File(currentLogFile.getParent());
                    boolean result = createDir(dir) && currentLogFile.createNewFile();
                    if (!result) return null;
                }
                return new FileOutputStream(currentLogFile, true);  //追加模式
            } catch (Exception ignored) {
                return null;
            }
        }

        private boolean JudgeNewStream() {
            return currentFileTime < (System.currentTimeMillis() - newFileTime) || outStream == null || JudgeLogFileState();
        }

        private boolean JudgeLogFileState() {
            return currentLogFile == null || !currentLogFile.exists() || currentLogFile != null && currentLogFile.length() > MaxFileSize;
        }

        private void closeOutPutStream() {
            if (outStream != null) {
                try {
                    outStream.flush();
                    outStream.close();
                } catch (Exception ignored) {
                } finally {
                    outStream = null;
                }
            }
        }

        private void deleteFiles(String path, int day, long allFileSize) {
            File dir = new File(path);
            if (dir.exists() && dir.isDirectory()) {
                File[] files = dir.listFiles();
                if (files == null || files.length == 0)
                    return;

                for (File file : files) {
                    if (path.equals(LogDir))
                        if (file.getName().equals("true") && file.isFile()
                                || file.getName().equals("false") && file.isFile())
                            continue;

                    if (file.isDirectory()) {
                        deleteFiles(file.getAbsolutePath(), day, allFileSize);
                        if (!deleteFileSafely(file)) allFileSize += file.length();
                    } else if (file.isFile() && file.lastModified() < System.currentTimeMillis() - (newFileTime * day)) {
                        if (!deleteFileSafely(file)) allFileSize += file.length();
                    } else {
                        allFileSize += file.length();
                    }
                }
                if (allFileSize > MaxFileSize)  //限定总文件大小为100Mb
                    deleteFiles(path, --day, 0);
            }
        }

        private boolean createDir(File dir) {
            if (!dir.exists()) {
                return dir.mkdirs();
            } else if (dir.isFile()) {
                return deleteFileSafely(dir) && dir.mkdirs();
            }
            return true;
        }

        /**
         * @param file 要删除的文件
         */
        private boolean deleteFileSafely(File file) {
            if (file != null && file.exists()) {
                File tmp = getTmpFile(file, System.currentTimeMillis(), -1);
                if (file.renameTo(tmp)) {    // 将源文件重命名
                    return tmp.delete();  //  删除重命名后的文件
                } else {
                    return file.delete();
                }
            }
            return false;
        }

        private File getTmpFile(File file, long time, int index) {
            File tmp;
            if (index == -1) {
                tmp = new File(file.getParent() + separator + time);
            } else {
                tmp = new File(file.getParent() + separator + time + "(" + index + ")");
            }
            if (!tmp.exists()) {
                return tmp;
            } else {
                return getTmpFile(file, time, index >= 1000 ? index : ++index);
            }
        }


        private long stringToLong(String strTime) {
            Date date = stringToDate(strTime); // String类型转成date类型
            if (date == null) {
                return 0;
            } else {
                return date.getTime();
            }
        }

        private Date stringToDate(String strTime) {
            try {
                return day.parse(strTime);
            } catch (ParseException e) {
                return null;
            }
        }
    }
}