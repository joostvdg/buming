package com.github.joostvdg.buming.sorting;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class SomethingElse {

    public static void main(String[] args) throws UnsupportedEncodingException, NoSuchAlgorithmException {
        byte[] data1 = "0123456adasdasdasdasd789".getBytes("UTF-8");

        MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
        byte[] digest = messageDigest.digest(data1);
        System.out.println("Digest: " + new String(digest));
        System.out.println("Digest length: " + digest.length);
    }
}
