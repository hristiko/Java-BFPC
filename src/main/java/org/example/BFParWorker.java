package org.example;

import util.LogLevel;
import util.Logger;

import java.util.ArrayList;
import java.util.Random;


public class BFParWorker implements Runnable {

    private final String pass;
    private final int length;
    private final boolean md5;
    private final ArrayList<String> charSet;
    private final ArrayList<String> candidatePasswords;
    private final Gui window;

    public BFParWorker(String pass, ArrayList<String> charSet, int length, boolean md5, ArrayList<String> candidatePasswords, Gui window){
        this.pass = pass;
        this.charSet = charSet;
        this.length = length;
        this.md5 = md5;
        this.candidatePasswords = candidatePasswords;
        this.window = window;
    }

    @Override
    public void run() {
        //Logger.log("Inside run()", LogLevel.Status);
        Random rand = new Random();
        long n = (long) Math.pow(charSet.size(), length );
        long startTime = System.currentTimeMillis();
        while (!BruteForceParallel.wasFound()){
            //Logger.log("Inside while...", LogLevel.Status);
            String generatedPass = "";
            int currentLength = 0;

            while (currentLength < length){
                int randIndex = rand.nextInt(charSet.size());
                generatedPass += charSet.get(randIndex);
                currentLength++;
            }

            synchronized (candidatePasswords){
                //Logger.log("Inside critical section...", LogLevel.Status);
                if (candidatePasswords.contains(generatedPass)){
                    continue;
                }

                candidatePasswords.add(generatedPass);
                int attempts = BruteForceParallel.increaseAndGet();
                double barI = (attempts * 100.0) / n;

                window.fill(barI);

                String hashed;
                if (md5) {
                    hashed = org.example.MD5.toMD5crack(generatedPass);
                } else {
                    hashed = org.example.SHA256.toSHA256(generatedPass);
                }

                if (hashed.equals(pass)) {
                    Logger.log("Total attempts: " + attempts, LogLevel.Status);
                    BruteForceParallel.setFoundPassword((generatedPass));
                    long endTime = System.currentTimeMillis();
                    long totalTime = endTime - startTime;

                    Logger.log("FOUND: " + generatedPass, LogLevel.Success);
                    window.finalPassword("The password is: " + generatedPass + ". Total time: " + totalTime + " ms.");
                    break;
                }
                if (barI == 100) {
                    Logger.log("NOT FOUND", LogLevel.Error);
                    window.finalPassword("Password not found!");
                }

            }
        }
    }
}
