package org.example;

import util.LogLevel;
import util.Logger;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class BruteForceParallel {

    public static final int NUM_OF_THREADS = 20;
    private static final AtomicBoolean found = new AtomicBoolean(false);
    private static String foundPassword = "";
    private static int incrementer = 0;
    ArrayList<String> charSet = new ArrayList<>();
    ArrayList<String> candidatePasswords = new ArrayList<>();
    private final Gui window;

    String pass;
    int length;
    boolean upperCase;
    boolean lowerCase;
    boolean digits;
    boolean specialCharsBool;
    boolean md5;
    boolean start;


    BruteForceParallel(String pass, int length, boolean upperCase, boolean lowerCase, boolean digits, boolean specialCharsBool, boolean md5, boolean start, Gui window){
        this.pass = pass;
        this.length = length;
        this.upperCase = upperCase;
        this.lowerCase = lowerCase;
        this.digits = digits;
        this.specialCharsBool = specialCharsBool;
        this.md5 = md5;
        this.start = start;
        this.window = window;
    }

    public void bfParCrack(){

        Logger.log("Brute Force Parallel Started: " + start, LogLevel.Status);

        ArrayList<String> UpperLetters =new ArrayList<>();
        for (char c = 'A'; c<='Z'; c++){
            UpperLetters.add(String.valueOf(c));
        }

        ArrayList<String> LowerLetters =new ArrayList<>();
        for (char c = 'a'; c <= 'z'; c++) {
            LowerLetters.add(String.valueOf(c));
        }

        ArrayList<String> Digits =new ArrayList<>();
        for (int i = 0; i <= 9; i++) {
            Digits.add(String.valueOf(i));
        }

        ArrayList<String> SPchars = new ArrayList<>();
        String specialChars = "!@#$%^&*()-_=+[]{}|;:'\",.<>?/";
        for (int i=0; i<specialChars.length(); i++){
            SPchars.add(String.valueOf(specialChars.charAt(i)));
        }

        if (upperCase){
            charSet.addAll(UpperLetters);
            setCharSet(charSet);
            Logger.log("Brute force parallel: upper case letters ALLOWED:" + getCharSet(), LogLevel.Success);
        }

        if (digits){
            charSet.addAll(Digits);
            setCharSet(charSet);
            Logger.log("Brute force parallel: DIGITS ALLOWED:" + getCharSet(), LogLevel.Success);
        }

        if (lowerCase){
            charSet.addAll(LowerLetters);
            setCharSet(charSet);
            Logger.log("Brute force parallel: lower case letters ALLOWED: " + getCharSet(), LogLevel.Success);
        }

        if (specialCharsBool){
            charSet.addAll(SPchars);
            setCharSet(charSet);
        }

        Thread[] threads = new Thread[NUM_OF_THREADS];

        for (int i=0; i<NUM_OF_THREADS; i++){
            BFParWorker worker = new BFParWorker(pass, charSet, length, md5, candidatePasswords, window);
            threads[i] = new Thread(worker);
            threads[i].start();
        }

        for (int i=0; i<NUM_OF_THREADS; i++){
            try {
                threads[i].join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

    }


    public static synchronized void setFoundPassword(String pass){
        found.set(true);
        foundPassword = pass;
    }

    public static boolean wasFound(){
        return found.get();
    }

    public static synchronized int increaseAndGet(){
        incrementer++;
        return incrementer;
    }

    public static int getIncrementer() {
        return incrementer;
    }

    public void setCharSet(ArrayList<String> charSet) {
        this.charSet = charSet;
    }

    public ArrayList<String> getCharSet() {
        return charSet;
    }

}
