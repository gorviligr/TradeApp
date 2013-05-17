package com.mnt.utils;

public class Hex{
    private static final char[] DIGITS_LOWER =
    {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
    public static String encodeHexString(byte[] data) {
           return new String(encodeHex(data));
       }
    public static char[] encodeHex(byte[] data) {
           return encodeHex(data, true);
       }
    public static char[] encodeHex(byte[] data, boolean toLowerCase) {
           return encodeHex(data,  DIGITS_LOWER);
       }
    
    protected static char[] encodeHex(byte[] data, char[] toDigits) {
           int l = data.length;
           char[] out = new char[l << 1];
           // two characters form the hex value.
           for (int i = 0, j = 0; i < l; i++) {
               out[j++] = toDigits[(0xF0 & data[i]) >>> 4];
               out[j++] = toDigits[0x0F & data[i]];
           }
           return out;
       }
}