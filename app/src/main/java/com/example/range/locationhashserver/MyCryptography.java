package com.example.range.locationhashserver;

import android.util.Log;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.ArrayUtils;

import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;

class MyCryptography {
    private static byte[] X_bytes = new byte[32];
    private static String H_value = "B194BAC80A08F53B366D008E584A5DE48504FA9D1BB6C7AC252E72C202FDCE0D";
    private static String[] H_TABLE = {"10110001", "10010100", "10111010", "11001000", "00001010", "00001000", "11110101",
            "00111011", "00110110", "01101101", "00000000", "10001110", "01011000", "01001010", "01011101", "11100100",
            "10000101", "00000100", "11111010", "10011101", "00011011", "10110110", "11000111", "10101100", "00100101",
            "00101110", "01110010", "11000010", "00000010", "11111101", "11001110", "00001101", "01011011", "11100011",
            "11010110", "00010010", "00010111", "10111001", "01100001", "10000001", "11111110", "01100111", "10000110",
            "10101101", "01110001", "01101011", "10001001", "00001011", "01011100", "10110000", "11000000", "11111111",
            "00110011", "11000011", "01010110", "10111000", "00110101", "11000100", "00000101", "10101110", "11011000",
            "11100000", "01111111", "10011001", "11100001", "00101011", "11011100", "00011010", "11100010", "10000010",
            "01010111", "11101100", "01110000", "00111111", "11001100", "11110000", "10010101", "11101110", "10001101",
            "11110001", "11000001", "10101011", "01110110", "00111000", "10011111", "11100110", "01111000", "11001010",
            "11110111", "11000110", "11111000", "01100000", "11010101", "10111011", "10011100", "01001111", "11110011",
            "00111100", "01100101", "01111011", "01100011", "01111100", "00110000", "01101010", "11011101", "01001110",
            "10100111", "01111001", "10011110", "10110010", "00111101", "00110001", "00111110", "10011000", "10110101",
            "01101110", "00100111", "11010011", "10111100", "11001111", "01011001", "00011110", "00011000", "00011111",
            "01001100", "01011010", "10110111", "10010011", "11101001", "11011110", "11100111", "00101100", "10001111",
            "00001100", "00001111", "10100110", "00101101", "11011011", "01001001", "11110100", "01101111", "01110011",
            "10010110", "01000111", "00000110", "00000111", "01010011", "00010110", "11101101", "00100100", "01111010",
            "00110111", "00111001", "11001011", "10100011", "10000011", "00000011", "10101001", "10001011", "11110110",
            "10010010", "10111101", "10011011", "00011100", "11100101", "11010001", "01000001", "00000001", "01010100",
            "01000101", "11111011", "11001001", "01011110", "01001101", "00001110", "11110010", "01101000", "00100000",
            "10000000", "10101010", "00100010", "01111101", "01100100", "00101111", "00100110", "10000111", "11111001",
            "00110100", "10010000", "01000000", "01010101", "00010001", "10111110", "00110010", "10010111", "00010011",
            "01000011", "11111100", "10011010", "01001000", "10100000", "00101010", "10001000", "01011111", "00011001",
            "01001011", "00001001", "10100001", "01111110", "11001101", "10100100", "11010000", "00010101", "01000100",
            "10101111", "10001100", "10100101", "10000100", "01010000", "10111111", "01100110", "11010010", "11101000",
            "10001010", "10100010", "11010111", "01000110", "01010010", "01000010", "10101000", "11011111", "10110011",
            "01101001", "01110100", "11000101", "01010001", "11101011", "00100011", "00101001", "00100001", "11010100",
            "11101111", "11011001", "10110100", "00111010", "01100010", "00101000", "01110101", "10010001", "00010100",
            "00010000", "11101010", "01110111", "01101100", "11011010", "00011101"};
    private static byte[] s_bytes = new byte[16];
    private static byte[] h_bytes;

    static {
        try {
            h_bytes = Hex.decodeHex(H_value.toCharArray());
        }
        catch (DecoderException e) {
            Log.d("ERROR", e.getMessage());
        }
    }

    private static byte MASK = (byte) Integer.parseInt("11111111", 2);
    private static byte[] a_X1, b_X2, c_X3, d_X4, Y;
    private static long Two_In32 = 4294967296L, Two_In24 = 16777216L, Two_In16 = 65536L, Two_In8 = 256L;
    private static int T;
    private static int flag;
    private static long length;

    /**
     * calculate hash-value
     * @return hash string
     */
    public static String getHash(String videoFile) throws DecoderException {
        RandomAccessFile file = null;
        long point = 0;
        try {
            file = new RandomAccessFile(videoFile, "rw");
            length = file.length();
            point = ((int) (length / 32) * 32);
            while (file.getFilePointer() != length) {
                if (file.getFilePointer() == point) {
                    flag = file.read(X_bytes = new byte[(int) (length - file.getFilePointer())]);
                    madeMultiple();
                } else {
                    flag = file.read(X_bytes);
                }
                s_bytes = XOR(s_bytes, getDisplay1(ArrayUtils.addAll(X_bytes, h_bytes)));
                h_bytes = getDisplay2(ArrayUtils.addAll(X_bytes, h_bytes));
            }

        } catch (Throwable e) {
            e.printStackTrace();
        }
        long fileLength = length * 8;
        Y = getDisplay2(ArrayUtils.addAll(getWordForFileLength(fileLength), ArrayUtils.addAll(s_bytes, h_bytes)));
        String result = bytesToHex(Y);
        return result;

    }

    final private static char[] hexArray = "0123456789ABCDEF".toCharArray();
    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    private static void madeMultiple() {
        T = (X_bytes.length % 32);
        for (int i = 0; i < (32 - T); i++) {
            X_bytes = Arrays.copyOf(X_bytes, 32);
        }
    }

    /**
     * display 1 𝜎1(𝑢) = 𝐹𝑢1||𝑢2(𝑢3 ⊕ 𝑢4) ⊕ 𝑢3 ⊕ 𝑢4
     * XOR - ⊕
     * @param inputWord - input binary word (length 512 bit)
     * @return binary word (length 128 bit)
     */
    private static byte[] getDisplay1(byte[] inputWord) {
        byte[] u1_d1 = Arrays.copyOfRange(inputWord, 0, 16);
        byte[] u2_d1 = Arrays.copyOfRange(inputWord, 16, 32);
        byte[] u3_d1 = Arrays.copyOfRange(inputWord, 32, 48);
        byte[] u4_d1 = Arrays.copyOfRange(inputWord, 48, 64);
        byte[] encrypt_result = encrypt(ArrayUtils.addAll(u1_d1, u2_d1), XOR(u3_d1, u4_d1));

        return XOR((XOR(encrypt_result, u3_d1)), u4_d1);
    }

    /**
     * display 2 𝜎2(𝑢) = (𝐹𝜃1(𝑢1) ⊕ 𝑢1) || (𝐹𝜃2(𝑢2) ⊕ 𝑢2)
     * @param inputWord - input binary word (length 512 bit)
     * @return binary word (length 256 bit)
     */
    private static byte[] getDisplay2(byte[] inputWord) {
        byte[] u1 = Arrays.copyOfRange(inputWord, 0, 16);
        byte[] u2 = Arrays.copyOfRange(inputWord, 16, 32);
        byte[] key_1 = ArrayUtils.addAll(getDisplay1(inputWord), Arrays.copyOfRange(inputWord, 48, 64));//u4
        byte[] key_2 = ArrayUtils.addAll(XORWithOnes(getDisplay1(inputWord)), Arrays.copyOfRange(inputWord, 32, 48));//u3
        byte[] first_encrypt_path_result = XOR(encrypt(key_1, u1), u1);
        byte[] second_encrypt_path_result = XOR(encrypt(key_2, u2), u2);

        return ArrayUtils.addAll(first_encrypt_path_result, second_encrypt_path_result);
    }

    /**
     * addition on the module 2
     * @param u_bytes1 - bit sequence {0, 1}n
     * @param u_bytes2 - bit sequence {0, 1}n
     * @return bit sequence {0, 1}n
     */
    private static byte[] XOR(byte[] u_bytes1, byte[] u_bytes2) {
        byte[] result = new byte[u_bytes1.length];
        for (int i = 0; i < u_bytes1.length; i++)
            result[i] = (byte) (u_bytes1[i] ^ u_bytes2[i]);

        return result;
    }

    private static byte[] XORWithOnes(byte[] array) {
        byte[] result = new byte[array.length];
        int currentIndex;
        for (currentIndex = 0; currentIndex < array.length; currentIndex++)
            result[currentIndex] = (byte) (array[currentIndex] ^ MASK);

        return result;
    }

    /**
     * encryption algorithm block
     * word X = X1 || X2 || X3 || X4, where Xi ∈ {0, 1}32
     * key 𝜃 = 𝜃1 || 𝜃2 || ... || 𝜃8 ∈ {0, 1}32
     * a ← X1, b ← X2, c ← X3, d ← X4
     * b ← b ⊕ G5(a square_plus K(7i-6))
     * c ← c ⊕ G21(d square_plus K(7i-5))
     * a ← a square_minus G13(b square_plus K(7i-4))
     * e ← G21(b square_plus c square_plus K(7i-3)) ⊕ <i>32
     * b ← b square_plus e
     * c ← c square_minus e
     * d ← d square_plus G13(c square_plus K(7i-2))
     * b ← b ⊕ G21(a square_plus K(7i-1))
     * c ← c ⊕ G5(d square_plus K(7i))
     * a ↔ b
     * c ↔ d
     * b ↔ c
     * Y ← b || d || a || c
     * @param key - input binary key 𝜃 (length 256 bit)
     * @param value - input binary word 𝑋 (length 128 bit)
     * @return - output binary word 𝑌 (length 128 bit)
     */
    private static byte[] encrypt(byte key[], byte[] value) {
        ArrayList<byte[]> K;
        a_X1 = Arrays.copyOfRange(value, 0, 4);
        b_X2 = Arrays.copyOfRange(value, 4, 8);
        c_X3 = Arrays.copyOfRange(value, 8, 12);
        d_X4 = Arrays.copyOfRange(value, 12, 16);
        byte[] e;
        byte[] buff;
        K = getK(key);
        for (int i = 1; i <= 8; i++) {
            b_X2 = XOR(b_X2, G(5, square_plus(a_X1, K.get((7 * i - 6 - 1)))));
            c_X3 = XOR(c_X3, G(21, square_plus(d_X4, K.get((7 * i - 5 - 1)))));
            a_X1 = square_minus(a_X1, G(13, square_plus(b_X2, K.get(7 * i - 4 - 1))));
            e = XOR(G(21, square_plus(K.get(7 * i - 3 - 1), square_plus(b_X2, c_X3))), getWord(i));
            b_X2 = square_plus(b_X2, e);
            c_X3 = square_minus(c_X3, e);
            d_X4 = square_plus(d_X4, G(13, square_plus(c_X3, K.get(7 * i - 2 - 1))));
            b_X2 = XOR(b_X2, G(21, square_plus(a_X1, K.get((7 * i - 1 - 1)))));
            c_X3 = XOR(c_X3, G(5, square_plus(d_X4, K.get((7 * i - 1)))));

            buff = a_X1;
            a_X1 = b_X2;
            b_X2 = buff;

            buff = c_X3;
            c_X3 = d_X4;
            d_X4 = buff;

            buff = b_X2;
            b_X2 = c_X3;
            c_X3 = buff;

        }

        return ArrayUtils.addAll(ArrayUtils.addAll(b_X2, d_X4), ArrayUtils.addAll(a_X1, c_X3));
    }

    /**
     * splitting key
     * 𝜃 = 𝜃1 || 𝜃2 || ... || 𝜃8 ∈ {0, 1}32
     * @param key - input binary key (length 256 bit)
     * @return tact key K1 = 𝜃1, K2 = 𝜃2, ..., K8 = 𝜃8, K9 = 𝜃1, K10 = 𝜃2, ..., K56 = 𝜃8
     */
    private static ArrayList<byte[]> getK(byte key[]) {
        ArrayList<byte[]> K = new ArrayList<>();
        int pos = 0;
        for (int i = 0; i < 56; i++) {
            if (pos >= 8)
                pos = 0;
            K.add(Arrays.copyOfRange(key, pos * 4, (pos + 1) * 4));
            pos++;
        }

        return K;
    }

    /**
     * conversion
     * Gr: {0, 1}32 → {0, 1}32
     * equivalent word u = u1 || u2 || u3 || u4, ui ∈ {0, 1}32
     * to word Gr(u) = RotHi^r(H(u1) || H(u2) || H(u3) || H(u4))
     * @param r - 5, 13 or 21
     * @param value - input binary word (length 32 bit)
     * @return binary word (length 32 bit)
     */
    private static byte[] G(int r, byte[] value) {

        return RotHi(r, value);
    }

    private static int signedByteToInteger(byte b) {

        return b & 0xFF;
    }

    /**
     * equivalent of the word
     * @param value - bit sequence {0, 1}8n
     * @return 2^7*u1 + 2^6*u2 + ... + u8
     */
    private static long getAccordance(byte[] value) {
        long acc = signedByteToInteger(value[0]);
        acc += signedByteToInteger(value[1]) * Two_In8;
        acc += signedByteToInteger(value[2]) * Two_In16;
        acc += signedByteToInteger(value[3]) * Two_In24;

        return acc;
    }

    private static byte[] getWordForFileLength(long lg) {
        byte[] buff = ByteBuffer.allocate(8).putLong(lg).array();
        byte[] result = new byte[16];
        for (int i = 0; i < buff.length; i++)
            result[i] = buff[buff.length - i - 1];

        return result;
    }

    private static byte[] getWord(long acc) {
        acc = acc % Two_In32;
        byte[] buffer = ByteBuffer.allocate(8).putLong(acc).array();

        return new byte[]{buffer[7], buffer[6], buffer[5], buffer[4]};
    }

    /**
     * square_plus
     * @param u - bit sequence {0, 1}8n
     * @param v - bit sequence (o, 1)8n
     * @return bit sequence <u` + v`>8n
     */
    private static byte[] square_plus(byte[] u, byte[] v) {

        return getWord(getAccordance(u) + getAccordance(v));
    }

    /**
     * square_minus
     * u = v square_plus w
     * @param u - bit sequence {0, 1}8n
     * @param v - bit sequence (0, 1)8n
     * @return bit sequence w ∈ {0, 1}8n
     */
    private static byte[] square_minus(byte[] u, byte[] v) {

        return getWord(getAccordance(u) - getAccordance(v));
    }

    /**
     * RotHI for u ∈ {0, 1}8n word ShHi(u) ⊕ ShLo^(8n-1)(u)
     * @param cycle - 5, 13 or 21
     * @param val - input binary word (length 32 bit)
     * @return - output binary word (length 32 bit)
     */
    private static byte[] RotHi(int cycle, byte[] val) {
        String string = H_TABLE[signedByteToInteger(val[3])] +
                H_TABLE[signedByteToInteger(val[2])] +
                H_TABLE[signedByteToInteger(val[1])] +
                H_TABLE[signedByteToInteger(val[0])];
        String swap = string.substring(cycle) + string.substring(0, cycle);
        Long buff = Long.parseLong(swap, 2);
        byte[] b = Arrays.copyOfRange(ByteBuffer.allocate(8).putLong(buff).array(), 4, 8);

        return new byte[]{b[3], b[2], b[1], b[0]};
    }
}
