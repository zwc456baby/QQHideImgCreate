package com.example.qqhideimgcreate.Utils;

import java.math.BigDecimal;

/**
 * double 计算工具类，支持小数点计算
 * Created by zhouwenchao on 2017-02-27.
 */
public class ArithUtil {
    private static final int DEF_DIV_SCALE = 10;

    //相加
    public static double add(double d1, double d2) {
        BigDecimal b1 = new BigDecimal(d1);
        BigDecimal b2 = new BigDecimal(d2);
        return b1.add(b2).doubleValue();
    }

    //相减
    public static double sub(double d1, double d2) {
        BigDecimal b1 = new BigDecimal(d1);
        BigDecimal b2 = new BigDecimal(d2);
        return b1.subtract(b2).doubleValue();

    }

    //相乘
    public static double mul(double d1, double d2) {
        BigDecimal b1 = new BigDecimal(d1);
        BigDecimal b2 = new BigDecimal(d2);
        return b1.multiply(b2).doubleValue();
    }

    //相除
    public static double div(double d1, double d2) {
        return div(d1, d2, DEF_DIV_SCALE);
    }

    public static double div(double d1, double d2, int scale) {
        if (scale < 0) {
            throw new IllegalArgumentException("The scale must be a positive integer or zero");
        }
        BigDecimal b1 = new BigDecimal(d1);
        BigDecimal b2 = new BigDecimal(d2);
        return b1.divide(b2, scale, BigDecimal.ROUND_HALF_UP).doubleValue();
    }


    /* Float */
    //相加
    public static float add(float d1, float d2) {
        BigDecimal b1 = new BigDecimal(Float.toString(d1));
        BigDecimal b2 = new BigDecimal(Float.toString(d2));
        return b1.add(b2).floatValue();
    }

    //相减
    public static float sub(float d1, float d2) {
        BigDecimal b1 = new BigDecimal(d1);
        BigDecimal b2 = new BigDecimal(d2);
        return b1.subtract(b2).floatValue();

    }

    //相乘
    public static float mul(float d1, float d2) {
        BigDecimal b1 = new BigDecimal(d1);
        BigDecimal b2 = new BigDecimal(d2);
        return b1.multiply(b2).floatValue();
    }

    //相除
    public static float div(float d1, float d2) {
        return div(d1, d2, DEF_DIV_SCALE);
    }

    public static float div(float d1, float d2, int scale) {
        if (scale < 0) {
            throw new IllegalArgumentException("The scale must be a positive integer or zero");
        }
        BigDecimal b1 = new BigDecimal(d1);
        BigDecimal b2 = new BigDecimal(d2);
        return b1.divide(b2, scale, BigDecimal.ROUND_HALF_UP).floatValue();
    }

    //10进制转16进制
    /*更换自定义方法为java系统提供的库*/
    public static String IntToHex(int n) {
        return Integer.toHexString(n);
//        char[] ch = new char[20];
//        int nIndex = 0;
//        while (true) {
//            Log.v(new String(ch));
//            int m = n / 16;
//            int k = n % 16;
//            if (k == 15)
//                ch[nIndex] = 'F';
//            else if (k == 14)
//                ch[nIndex] = 'E';
//            else if (k == 13)
//                ch[nIndex] = 'D';
//            else if (k == 12)
//                ch[nIndex] = 'C';
//            else if (k == 11)
//                ch[nIndex] = 'B';
//            else if (k == 10)
//                ch[nIndex] = 'A';
//            else
//                ch[nIndex] = (char) ('0' + k);
//            nIndex++;
//            if (m == 0)
//                break;
//            n = m;
//        }
//        StringBuffer sb = new StringBuffer();
//        sb.append(ch, 0, nIndex);
//        sb.reverse();
//        String strHex = "0x";
//        strHex += sb.toString();
//        return strHex;
    }

    //16进制转10进制
    /*更换方法为系统提供的库*/
    public static int HexToInt(String strHex) {
        return Integer.parseInt(strHex.replaceAll("^0[x|X]", ""), 16);
//        int nResult = 0;
//        if (!IsHex(strHex))
//            return nResult;
//        String str = strHex.toUpperCase();
//        if (str.length() > 2) {
//            if (str.charAt(0) == '0' && str.charAt(1) == 'X' ||
//                    str.charAt(0) == '0' && str.charAt(1) == 'x') {
//                str = str.substring(2);
//            }
//        }
//        int nLen = str.length();
//        for (int i = 0; i < nLen; ++i) {
//            char ch = str.charAt(nLen - i - 1);
//            try {
//                nResult += (GetHex(ch) * GetPower(16, i));
//            } catch (Exception e) {
//                // TODO Auto-generated catch block
//                e.printStackTrace();
//            }
//        }
//        return nResult;
    }

    //计算16进制对应的数值
    public static int GetHex(char ch) throws Exception {
        if (ch >= '0' && ch <= '9')
            return (int) (ch - '0');
        if (ch >= 'a' && ch <= 'f')
            return (int) (ch - 'a' + 10);
        if (ch >= 'A' && ch <= 'F')
            return (int) (ch - 'A' + 10);
        throw new Exception("error param");
    }

    //计算幂
    public static int GetPower(int nValue, int nCount) throws Exception {
        if (nCount < 0)
            throw new Exception("nCount can't small than 1!");
        if (nCount == 0)
            return 1;
        int nSum = 1;
        for (int i = 0; i < nCount; ++i) {
            nSum = nSum * nValue;
        }
        return nSum;
    }

    //判断是否是16进制数
    public static boolean IsHex(String strHex) {
        int i = 0;
        if (strHex.length() > 2) {
            if (strHex.charAt(0) == '0' && (strHex.charAt(1) == 'X' || strHex.charAt(1) == 'x')) {
                i = 2;
            }
        }
        for (; i < strHex.length(); ++i) {
            char ch = strHex.charAt(i);
            if ((ch >= '0' && ch <= '9') || (ch >= 'A' && ch <= 'F') || (ch >= 'a' && ch <= 'f'))
                continue;
            return false;
        }
        return true;
    }
}
