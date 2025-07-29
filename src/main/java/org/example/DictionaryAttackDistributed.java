package org.example;

import mpi.MPI;
import util.LogLevel;
import util.Logger;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Arrays;

public class DictionaryAttackDistributed {

    public static void main(String[] args) throws IOException {
        MPI.Init(args);
        int rank = MPI.COMM_WORLD.Rank();
        int size = MPI.COMM_WORLD.Size();

        Logger.log("Started!", LogLevel.Info);
        long startTime = System.currentTimeMillis();

        String pass = "";
        boolean md5 = false;

        if (rank == 0) {
            Logger.log("Root: Reading dictionary_input.txt", LogLevel.Info);
            List<String> inputLines = java.nio.file.Files.readAllLines(
                    java.nio.file.Paths.get("dictionary_input.txt"));
            pass = inputLines.get(0).trim();
            md5 = Boolean.parseBoolean(inputLines.get(1).trim());
        }


        boolean[] useMD5 = new boolean[1];
        if (rank == 0) {
            useMD5[0] = md5;
        }
        MPI.COMM_WORLD.Bcast(useMD5, 0, 1, MPI.BOOLEAN, 0);
        md5 = useMD5[0];

        int hashLength;
        if (md5 == true) {
            hashLength = 32;
        } else {
            hashLength = 64;
        }

        char[] passChars = new char[hashLength];
        if (rank == 0) {
            char[] tempChars = pass.toCharArray();
            for (int i = 0; i < tempChars.length; i++) {
                passChars[i] = tempChars[i];
            }
        }
        MPI.COMM_WORLD.Bcast(passChars, 0, passChars.length, MPI.CHAR, 0);
        pass = new String(passChars).trim();

        List<String> allPasswords = java.nio.file.Files.readAllLines(
                java.nio.file.Paths.get("PasswordDictionary.txt"),
                StandardCharsets.ISO_8859_1);

        int totalPasswords = allPasswords.size();
        int workerCount = size - 1;

        boolean[] localFound = new boolean[1];
        boolean[] globalFound = new boolean[1];

        if (rank != 0) {
            int workerIndex = rank - 1;
            long rangeSize = (totalPasswords + workerCount - 1) / workerCount;
            long startIndex = workerIndex * rangeSize;
            long endIndex = startIndex + rangeSize;
            if (endIndex > totalPasswords) {
                endIndex = totalPasswords;
            }

            Logger.log("Rank " + rank + " range: [" + startIndex + ", " + endIndex + ")", LogLevel.Info);

            char[] resultChars = new char[0];
            int[] lengthArr = new int[0];

            for (long i = startIndex; i < endIndex; i++) {
                String candidate = allPasswords.get((int) i).trim();
                String hashed;

                if (md5 == true) {
                    hashed = MD5.toMD5crack(candidate);
                } else {
                    hashed = SHA256.toSHA256(candidate);
                }

                if (hashed.equals(pass)) {
                    Logger.log("Rank " + rank + " FOUND password: " + candidate, LogLevel.Success);

                    if (!localFound[0]) {
                        long endTime = System.currentTimeMillis();
                        long totalTime = endTime - startTime;
                        String result = candidate + "," + totalTime;

                        localFound[0] = true;

                        resultChars = result.toCharArray();
                        lengthArr = new int[]{resultChars.length};
                        Logger.log("Result chars: " + Arrays.toString(resultChars), LogLevel.Success);
                        Logger.log("Length array: " + Arrays.toString(lengthArr), LogLevel.Success);
                    }
                }

                MPI.COMM_WORLD.Allreduce(localFound, 0, globalFound, 0, 1, MPI.BOOLEAN, MPI.LOR);

                if (localFound[0]) {
                    MPI.COMM_WORLD.Send(lengthArr, 0, 1, MPI.INT, 0, 1);
                    MPI.COMM_WORLD.Send(resultChars, 0, resultChars.length, MPI.CHAR, 0, 2);
                }

                if (globalFound[0]) {
                    Logger.log("Rank " + rank + " exiting loop", LogLevel.Info);
                    break;
                }

                if (i % 1000 == 0) {
                    double localProgress = ((i - startIndex) * 100.0) / rangeSize;
                    FileWriter fw = new FileWriter("distributed_progress.txt", true);
                    fw.write(rank + ":" + localProgress + "\n");
                    fw.close();
                }
            }
        } else {
            Logger.log("Root waiting for result...", LogLevel.Info);

            boolean finished = false;

            while (!finished) {
                localFound[0] = false;
                MPI.COMM_WORLD.Allreduce(localFound, 0, globalFound, 0, 1, MPI.BOOLEAN, MPI.LOR);
                if (globalFound[0]) {
                    Logger.log("Root: password was found.", LogLevel.Info);
                    finished = true;
                }
            }

            try {
                int[] msgLength = new int[1];
                MPI.COMM_WORLD.Recv(msgLength, 0, 1, MPI.INT, MPI.ANY_SOURCE, 1);
                Logger.log("recv msgLength: " + Arrays.toString(msgLength), LogLevel.Success);

                char[] resultChars = new char[msgLength[0]];
                MPI.COMM_WORLD.Recv(resultChars, 0, msgLength[0], MPI.CHAR, MPI.ANY_SOURCE, 2);
                Logger.log("recv resultChars: " + Arrays.toString(resultChars), LogLevel.Success);

                String resultStr = new String(resultChars);
                String record = "FOUND:" + resultStr + "\n";

                try (BufferedWriter writer = new BufferedWriter(new FileWriter("distributed_result.txt"))) {
                    writer.write(record);
                    writer.flush();
                }

                Logger.log("Root: wrote FOUND result", LogLevel.Success);

            } catch (Exception e) {
                Logger.log("Root: ERROR - " + e.getMessage(), LogLevel.Error);
            }
        }

        MPI.Finalize();
    }
}
