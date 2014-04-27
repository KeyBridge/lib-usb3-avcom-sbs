package com.keybridgeglobal.sensor.util;

import java.util.Random;

@SuppressWarnings("ValueOfIncrementOrDecrementUsed")
public class ByteUtil {

  /**
   * All methods are static. Hide the constructor to prevent bad code.
   */
  private ByteUtil() {
  }

  /**
   * Returns a byte array containing the two's-complement representation of the
   * integer. The byte array will be in big-endian byte-order with a fixes
   * length of 4 (the least significant byte is in the 4th element).
   * <p>
   * <b>Example:</b>
   * <code>intToByteArray(258)</code> will return { 0, 0, 1, 2 },
   * <code>BigInteger.valueOf(258).toByteArray()</code> returns { 1, 2 }.
   * <p>
   * @param integer The integer to be converted.
   * @return The byte array of length 4.
   */
  public static byte[] intToByteArray(final int integer) {
    int byteNum = (40 - Integer.numberOfLeadingZeros(integer < 0 ? ~integer : integer)) / 8;
    byte[] byteArray = new byte[4];

    for (int n = 0; n < byteNum; n++) {
      byteArray[3 - n] = (byte) (integer >>> (n * 8));
    }
    return (byteArray);
  }

  /**
   * Tests whether two byte arrays are equal
   * <p>
   * @param left
   * @param right
   * @return
   */
  public static boolean equals(byte[] left, byte[] right) {
    if ((left == null) || (right == null)) {
      return false;
    }
    if (left.length != right.length) {
      return false;
    }
    for (int i = 0; i < left.length; i++) {
      if (left[i] != right[i]) {
        return false;
      }
    }
    return true;
  }

  /**
   * Get one bit back from a bit string within a multi-byte array at the
   * specified position:
   * <p>
   * @param data the Byte to study
   * @param pos  the bit position to get
   * @return returns zero or one
   */
  public static int getBit(byte[] data, int pos) {
    int posByte = pos / 8;
    int posBit = pos % 8;
    byte valByte = data[posByte];
    int valInt = valByte >> (8 - (posBit + 1)) & 0x0001;
    return valInt;
  }

  /**
   * Get one bit back from a bit string within a single byte at the specified
   * position:
   * <p>
   * @param data the Byte to study
   * @param pos  the bit position to get
   * @return returns zero or one
   */
  public static int getBit(byte data, int pos) {
    //    int posByte = pos / 8;
    int posBit = pos % 8;
    //    byte valByte = data;
    int valInt = data >> (8 - (posBit + 1)) & 0x0001;
    return valInt;
  }

  /**
   * To set one bit to a bit string at the specified position with the specified
   * bit value:
   * <p>
   * @param data
   * @param pos
   * @param val
   */
  public static void setBit(byte[] data, int pos, int val) {
    int posByte = pos / 8;
    int posBit = pos % 8;
    byte oldByte = data[posByte];
    oldByte = (byte) (((0xFF7F >> posBit) & oldByte) & 0x00FF);
    byte newByte = (byte) ((val << (8 - (posBit + 1))) | oldByte);
    data[posByte] = newByte;
  }

  /**
   * Unsigned int from two bytes
   * <p>
   * @param bytes
   * @param index
   * @return
   */
  public static int twoByteIntFromBytes(byte[] bytes, int index) {
    int idx = index;
    int i = bytes[idx++] << 8 & 0x0000FF00;
    i |= bytes[idx];
    return i;
  }

  /**
   * Get an unsigned integer from a 4-byte word
   * <p>
   * @param bytes
   * @param index
   * @return
   */
  public static long intFrom4Bytes(byte[] bytes, int index) {
    return intFrom4Bytes(bytes, index, false);
  }

  /**
   * Get a signed or unsigned integer from a 4-byte word.
   * <p>
   * @param bytes
   * @param index
   * @param signed
   * @return
   */
  public static long intFrom4Bytes(byte[] bytes, int index, boolean signed) {
    int idx = index;
    long val = 0;
    /*
     * byte high = bytes[idx++]; i |= (high << 24) & 0x00000000FF000000; i |=
     * (bytes[idx++] << 16) & 0x0000000000FF0000; i |= (bytes[idx++] << 8) &
     * 0x000000000000FF00; i |= bytes[idx++] & 0x00000000000000FF; if(signed) {
     * if((high & 0x80) == 1) i*=-1; }
     */
    for (int i = 0; i < 4; i++) {
      if (i < 3) {
        val |= (((long) bytes[idx + i] & 0x000000ff) << (32 - ((i + 1) * 8)));
      } else {
        val |= ((long) bytes[idx + i] & 0x000000ff);
      }

    }
    return val;
  }

  /**
   * Get an unsigned integer from a single byte Java assumes all Byte integers
   * are signed and range from -128 to 128. This returns an integer from 0 to
   * 255
   * <p>
   * @param byteValue
   * @return
   */
  public static int intFromByte(byte byteValue) {
    int val = 0;
    val |= byteValue & 0x000000ff;
    return val;
  }

  /**
   * Get an unsigned integer from two bytes sampled from within a byte stream,
   * starting at the specified index
   * <p>
   * @param bytes
   * @param index
   * @return
   */
  public static int intFrom2Bytes(byte[] bytes, int index) {
    return ((bytes[index] << 8) + bytes[index + 1]);
  }

  //----------------------------------------------------------------------------
  // From http://snippets.dzone.com/posts/show/93
  //  public static final byte[] intToByteArray(int value) {
  //        return new byte[] {
  //                (byte)(value >>> 24),
  //                (byte)(value >>> 16),
  //                (byte)(value >>> 8),
  //                (byte)value};
  //}
  //  public static final int byteArrayToInt(byte [] b) {
  //        return (b[0] << 24)
  //                + ((b[1] & 0xFF) << 16)
  //                + ((b[2] & 0xFF) << 8)
  //                + (b[3] & 0xFF);
  //}
  //----------------------------------------------------------------------------
  /**
   * Get an unsigned integer from a two bytes
   * <p>
   * @param bytes
   * @return
   */
  public static int intFrom2Bytes(byte[] bytes) {
    return intFrom2Bytes(bytes, 0);
  }

  /**
   * Unsigned Long from 8 bytes
   * <p>
   * @param bytes
   * @param index
   * @return
   */
  public static long longFromBytes(byte[] bytes, int index) {
    if ((bytes == null) || (bytes.length < 7)) {
      return -1;
    }
    int idx = index;
    long val = 0;
    for (int i = 0; i < 8; i++) {
      if (i < 7) {
        val |= (((long) bytes[idx + i] & 0x000000ff) << (64 - ((i + 1) * 8)));
      } else {
        val |= ((long) bytes[idx + i] & 0x000000ff);
      }
    }
    return val;
  }

  /**
   * Tests whether one byte array contains another
   * <p>
   * @param a          the byte array to examin
   * @param b          the byte array we're looking for
   * @param startIndex the index in a to begin looking
   * @return
   */
  public static boolean contains(byte[] a, byte[] b, int startIndex) {
    boolean isEqual = false;
    if ((a != null) && (b != null)) {
      for (int i = startIndex; (i < b.length) || (startIndex + i < a.length); i++) {
        isEqual = a[i] == b[i];
        if (!isEqual) {
          break;
        }
      }
    }
    return isEqual;
  }

  /**
   * Unsigned Long to 8 bytes
   * <p>
   * @param l
   * @param arr
   * @param startIdx
   */
  public static void longToBytes(long l, byte[] arr, int startIdx) {
    if (arr == null) {
      return;
    }
    int idx = startIdx;
    arr[idx++] = (byte) ((l >>> 56) & 0x00000000000000ff);
    arr[idx++] = (byte) ((l >>> 48) & 0x00000000000000ff);
    arr[idx++] = (byte) ((l >>> 40) & 0x00000000000000ff);
    arr[idx++] = (byte) ((l >>> 32) & 0x00000000000000ff);
    arr[idx++] = (byte) ((l >>> 24) & 0x00000000000000ff);
    arr[idx++] = (byte) ((l >>> 16) & 0x00000000000000ff);
    arr[idx++] = (byte) ((l >>> 8) & 0x00000000000000ff);
    arr[idx] = (byte) (l & 0x00000000000000ff);
  }

  /**
   * Convert an Integer to four bytes
   * <p>
   * @param number           the number to convert to a 4 byte integer
   * @param destination      the destination byte array to insert the new
   *                         4-bytes into
   * @param destinationIndex the index location where the number should be
   *                         copies
   */
  public static void intToBytes(int number, byte[] destination, int destinationIndex) {
    if (destination == null) {
      return;
    }
    int idx = destinationIndex;
    final int MASK = 0x000000ff;
    destination[idx++] = (byte) ((number >>> 24) & MASK);
    destination[idx++] = (byte) ((number >>> 16) & MASK);
    destination[idx++] = (byte) ((number >>> 8) & MASK);
    destination[idx] = (byte) (number & MASK);
  }

  /**
   * reverse the order of bytes attempt at big/little endian
   * <p>
   * @param in
   * @return
   */
  public static byte[] reverse(byte[] in) {
    int len = in.length;
    byte[] out = new byte[len];
    for (int i = 0; i < len; i++) {
      out[len - i - 1] = in[i];
    }
    return out;
  }

  /**
   * converts a byte stream to a printable string of bytes, optionally with
   * spaces
   * <p>
   * @param bytes
   * @param spaces
   * @return
   */
  public static String toString(byte[] bytes, boolean spaces) {
    String str = "";
    for (byte b : bytes) {
      str += Integer.toHexString(b & 0x000000FF);
      if (spaces) {
        str += " ";
      }
    }
    return str;
  }

  /**
   * converts a byte stream to a printable string of bytes with spaces
   * <p>
   * @param bytes
   * @return
   */
  public static String toString(byte[] bytes) {
    return toString(bytes, true);
  }

  public static String toString(int number) {
    return Integer.toHexString(number);
  }

  public static byte[] randomBytes(int size) {
    byte[] bytes = new byte[size];
    Random random = new Random();
    for (int i = 0; i < size; i++) {
      bytes[i] = (byte) random.nextInt();
    }
    return bytes;
  }

  /**
   * copy the pattern of bytes iteration times. returns a byte[] that is pattern
   * * iteration
   * <p>
   * @param pattern
   * @param iterations
   * @return
   */
  public static byte[] patternBytes(byte[] pattern, int iterations) {
    byte[] bytes = new byte[pattern.length * iterations];
    for (int i = 0; i < iterations; i++) {
      System.arraycopy(pattern, 0, bytes, pattern.length * i, pattern.length);
    }
    return bytes;
  }

  //----------------------------------------------------------------------------
  // Byte Conversion Utilities 
  // from: http://www.daniweb.com/code/snippet216874.html#

  /*
   * "primitive type --> byte[] data" Methods
   */
  public static byte[] toByteArray(byte data) {
    return new byte[]{data};
  }

  public static byte[] toByteArray(byte[] data) {
    return data;
  }

  public static byte[] toByteArray(short data) {
    return new byte[]{(byte) ((data >> 8) & 0xff), (byte) ((data) & 0xff),};
  }

  public static byte[] toByteArray(short[] data) {
    if (data == null) {
      return null;
    }
    // ----------
    byte[] byts = new byte[data.length * 2];
    for (int i = 0; i < data.length; i++) {
      System.arraycopy(toByteArray(data[i]), 0, byts, i * 2, 2);
    }
    return byts;
  }

  public static byte[] toByteArray(char data) {
    return new byte[]{(byte) ((data >> 8) & 0xff), (byte) ((data >> 0) & 0xff),};
  }

  public static byte[] toByteArray(char[] data) {
    if (data == null) {
      return null;
    }
    // ----------
    byte[] byts = new byte[data.length * 2];
    for (int i = 0; i < data.length; i++) {
      System.arraycopy(toByteArray(data[i]), 0, byts, i * 2, 2);
    }
    return byts;
  }

  public static byte[] toByteArray(int data) {
    return new byte[]{(byte) ((data >> 24) & 0xff), (byte) ((data >> 16) & 0xff), (byte) ((data >> 8) & 0xff), (byte) ((data >> 0) & 0xff),};
  }

  public static byte[] toByteArray(int[] data) {
    if (data == null) {
      return null;
    }
    // ----------
    byte[] byts = new byte[data.length * 4];
    for (int i = 0; i < data.length; i++) {
      System.arraycopy(toByteArray(data[i]), 0, byts, i * 4, 4);
    }
    return byts;
  }

  public static byte[] toByteArray(long data) {
    return new byte[]{(byte) ((data >> 56) & 0xff), (byte) ((data >> 48) & 0xff), (byte) ((data >> 40) & 0xff), (byte) ((data >> 32) & 0xff),
                      (byte) ((data >> 24) & 0xff), (byte) ((data >> 16) & 0xff), (byte) ((data >> 8) & 0xff), (byte) ((data >> 0) & 0xff),};
  }

  public static byte[] toByteArray(long[] data) {
    if (data == null) {
      return null;
    }
    // ----------
    byte[] byts = new byte[data.length * 8];
    for (int i = 0; i < data.length; i++) {
      System.arraycopy(toByteArray(data[i]), 0, byts, i * 8, 8);
    }
    return byts;
  }

  public static byte[] toByteArray(float data) {
    return toByteArray(Float.floatToRawIntBits(data));
  }

  public static byte[] toByteArray(float[] data) {
    if (data == null) {
      return null;
    }
    // ----------
    byte[] byts = new byte[data.length * 4];
    for (int i = 0; i < data.length; i++) {
      System.arraycopy(toByteArray(data[i]), 0, byts, i * 4, 4);
    }
    return byts;
  }

  public static byte[] toByteArray(Double data) {
    return toByteArray(Double.doubleToRawLongBits(data));
  }

  public static byte[] toByteArray(Double[] data) {
    if (data == null) {
      return null;
    }
    // ----------
    byte[] byts = new byte[data.length * 8];
    for (int i = 0; i < data.length; i++) {
      System.arraycopy(toByteArray(data[i]), 0, byts, i * 8, 8);
    }
    return byts;
  }

  public static byte[] toByteArray(double data) {
    return toByteArray(Double.doubleToRawLongBits(data));
  }

  public static byte[] toByteArray(double[] data) {
    if (data == null) {
      return null;
    }
    // ----------
    byte[] byts = new byte[data.length * 8];
    for (int i = 0; i < data.length; i++) {
      System.arraycopy(toByteArray(data[i]), 0, byts, i * 8, 8);
    }
    return byts;
  }

  public static byte[] toByteArray(boolean data) {
    return new byte[]{(byte) (data ? 0x01 : 0x00)}; // bool -> {1 byte}
  }

  public static byte[] toByteArray(boolean[] data) {
    // Advanced Technique: The byte array containts information
    // about how many boolean values are involved, so the exact
    // array is returned when later decoded.
    // ----------
    if (data == null) {
      return null;
    }
    // ----------
    int len = data.length;
    byte[] lena = toByteArray(len); // int conversion; length array = lena
    byte[] byts = new byte[lena.length + (len / 8) + (len % 8 != 0 ? 1 : 0)];
    // (Above) length-array-length + sets-of-8-booleans +? byte-for-remainder
    System.arraycopy(lena, 0, byts, 0, lena.length);
    // ----------
    // (Below) algorithm by Matthew Cudmore: boolean[] -> bits -> byte[]
    for (int i = 0, j = lena.length, k = 7; i < data.length; i++) {
      byts[j] |= (data[i] ? 1 : 0) << k--;
      if (k < 0) {
        j++;
        k = 7;
      }
    }
    // ----------
    return byts;
  }

  public static byte[] toByteArray(String data) {
    return (data == null) ? null : data.getBytes();
  }

  public static byte[] toByteArray(String[] data) {
    // Advanced Technique: Generates an indexed byte array
    // which contains the array of Strings. The byte array
    // contains information about the number of Strings and
    // the length of each String.
    // ----------
    if (data == null) {
      return null;
    }
    // ---------- flags:
    int totalLength = 0; // Measure length of final byte array
    int bytesPos = 0; // Used later
    // ----- arrays:
    byte[] dLen = toByteArray(data.length); // byte array of data length
    totalLength += dLen.length;
    int[] sLens = new int[data.length]; // String lengths = sLens
    totalLength += (sLens.length * 4);
    byte[][] strs = new byte[data.length][]; // array of String bytes
    // ----- pack strs:
    for (int i = 0; i < data.length; i++) {
      if (data[i] != null) {
        strs[i] = toByteArray(data[i]);
        sLens[i] = strs[i].length;
        totalLength += strs[i].length;
      } else {
        sLens[i] = 0;
        strs[i] = new byte[0]; // prevent null entries
      }
    }
    // ----------
    byte[] bytes = new byte[totalLength]; // final array
    System.arraycopy(dLen, 0, bytes, 0, 4);
    byte[] bsLens = toByteArray(sLens); // byte version of String sLens
    System.arraycopy(bsLens, 0, bytes, 4, bsLens.length);
    // -----
    bytesPos += 4 + bsLens.length; // mark position
    // -----
    for (byte[] sba : strs) {
      System.arraycopy(sba, 0, bytes, bytesPos, sba.length);
      bytesPos += sba.length;
    }
    // ----------
    return bytes;
  }


  /*
   * "byte[] data --> primitive type" Methods
   */
  public static byte toByte(byte[] data) {
    return ((data == null) || (data.length == 0)) ? 0x0 : data[0];
  }

  public static short toShort(byte[] data) {
    if ((data == null) || (data.length != 2)) {
      return 0x0;
    }
    // ----------
    return (short) ((0xff & data[0]) << 8 | (0xff & data[1]) << 0);
  }

  public static short[] toShortArray(byte[] data) {
    if ((data == null) || (data.length % 2 != 0)) {
      return null;
    }
    // ----------
    short[] shts = new short[data.length / 2];
    for (int i = 0; i < shts.length; i++) {
      shts[i] = toShort(new byte[]{data[(i * 2)], data[(i * 2) + 1]});
    }
    return shts;
  }

  public static char toChar(byte[] data) {
    if ((data == null) || (data.length != 2)) {
      return 0x0;
    }
    // ----------
    return (char) ((0xff & data[0]) << 8 | (0xff & data[1]) << 0);
  }

  public static char[] toCharArray(byte[] data) {
    if ((data == null) || (data.length % 2 != 0)) {
      return null;
    }
    // ----------
    char[] chrs = new char[data.length / 2];
    for (int i = 0; i < chrs.length; i++) {
      chrs[i] = toChar(new byte[]{data[(i * 2)], data[(i * 2) + 1],});
    }
    return chrs;
  }

  public static int toInt(byte[] data) {
    if ((data == null) || (data.length != 4)) {
      return 0x0;
    }
    // ----------
    return ( // NOTE: type cast not necessary for int
      (0xff & data[0]) << 24 | (0xff & data[1]) << 16 | (0xff & data[2]) << 8 | (0xff & data[3]) << 0);
  }

  public static int[] toIntArray(byte[] data) {
    if ((data == null) || (data.length % 4 != 0)) {
      return null;
    }
    // ----------
    int[] ints = new int[data.length / 4];
    for (int i = 0; i < ints.length; i++) {
      ints[i] = toInt(new byte[]{data[(i * 4)], data[(i * 4) + 1], data[(i * 4) + 2], data[(i * 4) + 3],});
    }
    return ints;
  }

  public static long toLong(byte[] data) {
    if ((data == null) || (data.length != 8)) {
      return 0x0;
    }
    // ----------
    return ( /**
       * (Below) convert to longs before shift because digits are lost with ints
       * beyond the 32-bit limit
       */
      (long) (0xff & data[0]) << 56 | (long) (0xff & data[1]) << 48 | (long) (0xff & data[2]) << 40 | (long) (0xff & data[3]) << 32
      | (long) (0xff & data[4]) << 24 | (long) (0xff & data[5]) << 16 | (long) (0xff & data[6]) << 8 | (long) (0xff & data[7]) << 0);
  }

  public static long[] toLongArray(byte[] data) {
    if ((data == null) || (data.length % 8 != 0)) {
      return null;
    }
    // ----------
    long[] lngs = new long[data.length / 8];
    for (int i = 0; i < lngs.length; i++) {
      lngs[i] = toLong(new byte[]{data[(i * 8)], data[(i * 8) + 1], data[(i * 8) + 2], data[(i * 8) + 3], data[(i * 8) + 4], data[(i * 8) + 5],
                                  data[(i * 8) + 6], data[(i * 8) + 7],});
    }
    return lngs;
  }

  public static float toFloat(byte[] data) {
    if ((data == null) || (data.length != 4)) {
      return 0x0;
    }
    return Float.intBitsToFloat(toInt(data));
  }

  public static float[] toFloatArray(byte[] data) {
    if ((data == null) || (data.length % 4 != 0)) {
      return null;
    }
    float[] flts = new float[data.length / 4];
    for (int i = 0; i < flts.length; i++) {
      flts[i] = toFloat(new byte[]{data[(i * 4)], data[(i * 4) + 1], data[(i * 4) + 2], data[(i * 4) + 3],});
    }
    return flts;
  }

  public static double toDouble(byte[] data) {
    if ((data == null) || (data.length != 8)) {
      return 0x0;
    }
    return Double.longBitsToDouble(toLong(data));
  }

  public static double[] toDoubleArray(byte[] data) {
    if (data == null) {
      return null;
    }
    if (data.length % 8 != 0) {
      return null;
    }
    double[] dbls = new double[data.length / 8];
    for (int i = 0; i < dbls.length; i++) {
      dbls[i] = toDouble(new byte[]{data[(i * 8)], data[(i * 8) + 1], data[(i * 8) + 2], data[(i * 8) + 3], data[(i * 8) + 4], data[(i * 8) + 5],
                                    data[(i * 8) + 6], data[(i * 8) + 7],});
    }
    return dbls;
  }

  public static boolean toBoolean(byte[] data) {
    return ((data == null) || (data.length == 0)) ? false : data[0] != 0x00;
  }

  public static boolean[] toBooleanArray(byte[] data) {
    /**
     * Advanced Technique: Extract the boolean array's length from the first
     * four bytes in the char array, and then read the boolean array.
     */
    if ((data == null) || (data.length < 4)) {
      return null;
    }
    int len = toInt(new byte[]{data[0], data[1], data[2], data[3]});
    boolean[] bools = new boolean[len];
    /**
     * pack booleans.
     */
    for (int i = 0, j = 4, k = 7; i < bools.length; i++) {
      bools[i] = ((data[j] >> k--) & 0x01) == 1;
      if (k < 0) {
        j++;
        k = 7;
      }
    }
    return bools;
  }

//  public static String toString(byte[] data) {    return (data == null) ? null : new String(data);  }
  /**
   * Extract the String array length from the first four bytes in the char
   * array, and then read the int array denoting the String lengths, and then
   * read the Strings.
   * <p>
   * @param data
   * @return
   */
  public static String[] toStringArray(byte[] data) {
    if ((data == null) || (data.length < 4)) {
      return null;
    }
    byte[] bBuff = new byte[4];
    System.arraycopy(data, 0, bBuff, 0, 4);
    int saLen = toInt(bBuff);
    if (data.length < (4 + (saLen * 4))) {
      return null;
    }

    bBuff = new byte[saLen * 4];
    System.arraycopy(data, 4, bBuff, 0, bBuff.length);
    int[] sLens = toIntArray(bBuff);
    if (sLens == null) {
      return null;
    }

    String[] strs = new String[saLen];
    for (int i = 0, dataPos = 4 + (saLen * 4); i < saLen; i++) {
      if (sLens[i] > 0) {
        if (data.length >= (dataPos + sLens[i])) {
          bBuff = new byte[sLens[i]];
          System.arraycopy(data, dataPos, bBuff, 0, sLens[i]);
          dataPos += sLens[i];
          strs[i] = toString(bBuff);
        } else {
          return null;
        }
      }
    }

    return strs;
  }

}
