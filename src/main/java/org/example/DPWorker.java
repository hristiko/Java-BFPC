package org.example;

import util.LogLevel;
import util.Logger;

import java.util.List;

public class DPWorker implements Runnable {

    private final List<String> passwords;
    private final String targetHash;
    private final boolean md5;
    private final List<String> candidatePasswords;
    private final Gui window;
    private final int total;
    private final long startTime;

    public DPWorker(List<String> passwords, String targetHash, boolean md5, List<String> candidatePasswords, Gui window, int total, long startTime) {
        this.passwords = passwords;
        this.targetHash = targetHash;
        this.md5 = md5;
        this.candidatePasswords = candidatePasswords;
        this.window = window;
        this.total = total;
        this.startTime = startTime;
    }

    @Override
    public void run() {
        int index = 0;

        while (!DictionaryAttackParallel.wasFound() && index < passwords.size()) {
            String candidate = passwords.get(index);
            index++;

            int attempts = DictionaryAttackParallel.increaseAndGet();
            double barI = (attempts * 100.0) / total;

            window.fill(barI);

            String hashed;
            if (md5) {
                hashed = org.example.MD5.toMD5crack(candidate);
            } else {
                hashed = org.example.SHA256.toSHA256(candidate);
            }

            if (hashed.equals(targetHash)) {
                long endTime = System.currentTimeMillis();
                long totalTime = endTime - startTime;

                DictionaryAttackParallel.setFoundPassword(candidate);

                Logger.log("Parallel Dictionary: FOUND " + candidate, LogLevel.Success);
                window.fill(100);
                window.finalPassword("The password is: " + candidate + ". Total time: " + totalTime + " ms.");
                break;
            }

            if (barI >= 100 && !DictionaryAttackParallel.wasFound()) {
                Logger.log("NOT FOUND", LogLevel.Error);
                window.finalPassword("Password not found!");
            }
        }
    }
}
