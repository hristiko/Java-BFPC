package org.example;

import mpi.MPI;
import util.LogLevel;
import util.Logger;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;

public class BruteForceDistributed {

    public static void main(String[] args) {
        MPI.Init(args);
        int rank = MPI.COMM_WORLD.Rank();
        int size = MPI.COMM_WORLD.Size();

        Logger.log("Started!", LogLevel.Info);
        long startTime = System.currentTimeMillis();

        String pass = "";
        int length = 0;
        boolean md5 = false;
        ArrayList<Character> charSet = new ArrayList<>();

        if (rank == 0) {
            Logger.log("Inside root", LogLevel.Info);

            try{
                BufferedReader reader = new BufferedReader(new FileReader("distributed_input.txt"));
                pass = reader.readLine();
                length = Integer.parseInt(reader.readLine());
                boolean upperCase = Boolean.parseBoolean(reader.readLine());
                boolean lowerCase = Boolean.parseBoolean(reader.readLine());
                boolean digits = Boolean.parseBoolean(reader.readLine());
                boolean specialCharsBool = Boolean.parseBoolean(reader.readLine());
                md5 = Boolean.parseBoolean(reader.readLine());
                reader.close();

                if (upperCase) {
                    for (char c = 'A'; c <= 'Z'; c++) {
                        charSet.add(c);
                    }
                }

                if (lowerCase) {
                    for (char c = 'a'; c <= 'z'; c++) {
                        charSet.add(c);
                    }
                }

                if (digits) {
                    for (char c = '0'; c <= '9'; c++) {
                        charSet.add(c);
                    }
                }

                if (specialCharsBool) {
                    String specialChars = "!@#$%^&*()-_=+[]{}|;:'\",.<>?/";
                    for (int i = 0; i < specialChars.length(); i++) {
                        charSet.add(specialChars.charAt(i));
                    }
                }

                Logger.log("Final charset size: " + charSet.size(), LogLevel.Info);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        }


        int[] maxLength = new int[1];
        if (rank == 0) {
            maxLength[0] = length;
        }
        MPI.COMM_WORLD.Bcast(maxLength, 0, 1, MPI.INT, 0);
        length = maxLength[0];


        boolean[] useMD5 = new boolean[1];
        if (rank == 0) {
            useMD5[0] = md5;
        }
        MPI.COMM_WORLD.Bcast(useMD5, 0, 1, MPI.BOOLEAN, 0);
        md5 = useMD5[0];


        int[] charSetLength = new int[1];
        if (rank == 0) {
            charSetLength[0] = charSet.size();
        }
        MPI.COMM_WORLD.Bcast(charSetLength, 0, 1, MPI.INT, 0);


        char[] charSetArray = new char[charSetLength[0]];
        if (rank == 0) {
            for (int i = 0; i < charSet.size(); i++) {
                charSetArray[i] = charSet.get(i);
            }
        }
        MPI.COMM_WORLD.Bcast(charSetArray, 0, charSetArray.length, MPI.CHAR, 0);


        int hashLength;
        if (md5 == true) {
            hashLength = 32;
        } else {
            hashLength = 64;
        }

        char[] passChars = new char[hashLength];
        if (rank == 0) {
            char[] temp = pass.toCharArray();
            for (int i = 0; i < hashLength; i++) {
                passChars[i] = temp[i];
            }
        }
        MPI.COMM_WORLD.Bcast(passChars, 0, hashLength, MPI.CHAR, 0);
        pass = new String(passChars);


        MPI.COMM_WORLD.Barrier();


        boolean[] localFound = new boolean[1];
        boolean[] globalFound = new boolean[1];


        if (rank != 0) {

            int attempts = 0;
            long totalCombinations = (long) Math.pow(charSetArray.length, length);

            int workerCount = size - 1;
            int workerIndex = rank - 1;

            long rangeSize = (totalCombinations + workerCount - 1) / workerCount;
            long startIndex = workerIndex * rangeSize;
            long endIndex = startIndex + rangeSize;
            if (endIndex > totalCombinations) {
                endIndex = totalCombinations;
            }

            Logger.log("Rank " + rank + " working on range [" + startIndex + ", " + endIndex + ")", LogLevel.Info);

            char[] resultChars = new char[0];
            int[] lengthArr = new int[0];

            for (long i = startIndex; i < endIndex; i++) {
                attempts++;
                String candidate = generator(i, length, charSetArray);
                String hashed;

                if (md5 == true) {
                    hashed = MD5.toMD5crack(candidate);
                } else {
                    hashed = SHA256.toSHA256(candidate);
                }


                if (hashed.equals(pass)) {
                    Logger.log("Rank " + rank + " FOUND password: " + candidate, LogLevel.Success);
                    Logger.log("Rank " + rank + " total attempts: " + attempts, LogLevel.Status);
                    if (localFound[0] == false){
                        long endTime = System.currentTimeMillis();
                        long totalTime = endTime - startTime;

                        localFound[0] = true;
                        //MPI.COMM_WORLD.Allreduce(localFound, 0, localFound, 0, 1, MPI.BOOLEAN, MPI.LOR);
                        String result = candidate + "," + totalTime;
                        Logger.log(result, LogLevel.Success);
                        resultChars = result.toCharArray();
                        lengthArr = new int[]{resultChars.length};
                        Logger.log("result chars: " + new String(resultChars), LogLevel.Success);
                        Logger.log("length array: " + Arrays.toString(lengthArr), LogLevel.Success);

                    }
                    //MPI.COMM_WORLD.Barrier();
                }


                MPI.COMM_WORLD.Allreduce(localFound, 0, globalFound, 0, 1, MPI.BOOLEAN, MPI.LOR);

                if (localFound[0]){
                    MPI.COMM_WORLD.Send(lengthArr, 0, 1, MPI.INT, 0, 1);
                    MPI.COMM_WORLD.Send(resultChars, 0, resultChars.length, MPI.CHAR, 0, 2);
                }
                if (globalFound[0] == true) {
                    break;
                }

                if (i % 100_000 == 0){
                    Logger.log("Rank " + rank + " trying: " + candidate, LogLevel.Status);
                }

                if (i % 1000 == 0) {

                    double localProgress = ((i - startIndex) * 100.0 / (rangeSize)) / 3.0;

                    try {
                        FileWriter fw = new FileWriter("distributed_progress.txt", true);
                        fw.write(rank + ":" + localProgress + "\n");
                        fw.close();
                    } catch (IOException e) {
                        Logger.log("Error writing progress line", LogLevel.Warn);
                    }
                }

            }

        } else {

            boolean finished = false;

            Logger.log("Inside ROOT, before WHILE", LogLevel.Info);

            while (!finished) {
                //Logger.log("Inside while", LogLevel.Info);
                localFound[0] = false;

                //Logger.log(String.valueOf(globalFound[0]), LogLevel.Info);
                MPI.COMM_WORLD.Allreduce(localFound, 0, globalFound, 0, 1, MPI.BOOLEAN, MPI.LOR);
                if (globalFound[0] == true) {
                    Logger.log("Root: Password found, exiting.", LogLevel.Info);
                    finished = true;
                }

            }



            //Logger.log("Inside ROOT, before TRY", LogLevel.Info);

            try {
                int[] msgLength = new int[1];
                MPI.COMM_WORLD.Recv(msgLength, 0, 1, MPI.INT, MPI.ANY_SOURCE, 1);
                Logger.log("recv masglength: " + Arrays.toString(msgLength), LogLevel.Success);

                char[] resultChars = new char[msgLength[0]];
                MPI.COMM_WORLD.Recv(resultChars, 0, msgLength[0], MPI.CHAR, MPI.ANY_SOURCE, 2);
                Logger.log("recv resChars: " + Arrays.toString(resultChars), LogLevel.Success);

                String resultStr = new String(resultChars);
                String record = "FOUND:" + resultStr + "\n";

                try (BufferedWriter writer = new BufferedWriter(new FileWriter("distributed_result.txt"))){
                    writer.write(record);
                    writer.flush();
                }

                Logger.log("Root: wrote FOUND result", LogLevel.Success);
            } catch (Exception e){
                Logger.log("Root: ERROR", LogLevel.Error);
            }

        }

        MPI.Finalize();
    }

    private static String generator(long index, int length, char[] charSetArray) {
        char[] result = new char[length];
        int size = charSetArray.length;

        for (int i = length - 1; i >= 0; i--) {
            int pos = (int) (index % size);
            result[i] = charSetArray[pos];
            index = index / size;
        }

        return new String(result);
    }
}





