package org.example;

import util.LogLevel;
import util.Logger;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class DictionaryAttackParallel {

    public static final int NUM_OF_THREADS = 20;
    private static final AtomicBoolean found = new AtomicBoolean(false);
    private static String foundPassword = "";
    private static int incrementer = 0;
    private final Gui window;
    private final String targetHash;
    private final boolean md5;
    private final List<String> allPasswords;
    private final List<String> candidatePasswords = new ArrayList<>();

    public DictionaryAttackParallel(String targetHash, boolean md5, Gui window) throws Exception {
        this.targetHash = targetHash;
        this.md5 = md5;
        this.window = window;

        allPasswords = new ArrayList<>();
        Path fileName = Path.of("PasswordDictionary.txt");
        try (BufferedReader buffer = new BufferedReader(
                new InputStreamReader(new FileInputStream(fileName.toFile()), StandardCharsets.ISO_8859_1))) {
            String str;
            while ((str = buffer.readLine()) != null) {
                allPasswords.add(str.trim());
            }
        }
    }

    public void dapAttack() {
        Logger.log("Parallel Dictionary Attack Started.", LogLevel.Status);

        Thread[] threads = new Thread[NUM_OF_THREADS];
        int total = allPasswords.size();
        int rangeSize = total / NUM_OF_THREADS;
        long startTime = System.currentTimeMillis();

        for (int i = 0; i < NUM_OF_THREADS; i++) {
            int startIndex = i * rangeSize;
            int endIndex;
            if (i == NUM_OF_THREADS - 1) {
                endIndex = total;
            } else {
                endIndex = startIndex + rangeSize;
            }

            DPWorker worker = new DPWorker(allPasswords.subList(startIndex, endIndex), targetHash, md5, candidatePasswords, window, total, startTime);

            threads[i] = new Thread(worker);
            threads[i].start();
        }

        for (int i = 0; i < NUM_OF_THREADS; i++) {
            try {
                threads[i].join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static synchronized void setFoundPassword(String password) {
        found.set(true);
        foundPassword = password;
    }

    public static boolean wasFound() {
        return found.get();
    }

    public static synchronized int increaseAndGet() {
        incrementer++;
        return incrementer;
    }

    public static synchronized int getIncrementer() {
        return incrementer;
    }

    public static synchronized String getFoundPassword() {
        return foundPassword;
    }
}
