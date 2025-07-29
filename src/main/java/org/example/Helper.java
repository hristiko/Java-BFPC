package org.example;

import util.LogLevel;
import util.Logger;

import java.io.*;

public class Helper {

    public boolean targetMD5 = false;
    public boolean targetSHA256 = false;
    public boolean UpperCase = false;
    public boolean LowerCase = false;
    public boolean Digits = false;
    public boolean SpecialChars = false;
    public boolean seq = false;
    public boolean par = false;
    public boolean dis = false;
    public boolean attackTypeBF = false;
    public boolean attackTypeDA = false;
    public int maxLength;

    public static String pass;

    private boolean start;


    public Helper() throws Exception {



        Gui window = new Gui();
        //DictionaryAttack da = new DictionaryAttack();

        while (!window.isStart()){
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Logger.log("Waiting...", LogLevel.Status);
            }
        }

        start = window.isStart();
        setStart(start);
        Logger.log("Helper STARTED: " + isWindowStart(), LogLevel.Status);



        //Logger.log("HELPER: MD5 " + window.isTargetMD5());
        if (window.isTargetMD5()){
            Logger.log("HELPER: MD5 TRUE");
            targetMD5 = true;
            setTargetMD5(targetMD5);
        }

        if (window.isCharacterSetSpecialCharacters()){
            //bf.setCharacterSetSpecialCharacters(true);
            SpecialChars = true;
            setSpecialChars(SpecialChars);
        }


        if (window.isCharacterSetDigits()){
            Logger.log("HELPER: DIGITS TRUE");
            Digits = true;
            setDigits(Digits);
        }

        if (window.isCharacterSetUpperCase()){
            UpperCase = true;
            setUpperCase(UpperCase);
        }

        if (window.isCharacterSetLowerCase()){
            LowerCase = true;
            setLowerCase(LowerCase);
        }


        pass = window.getPasswordInput();
        Logger.log("HELPER: "+ pass);
        maxLength = window.getMaxPasswordLength();
        setMaxLength(maxLength);
        Logger.log("HELPER maxLength: "+ getMaxLength());


        //Logger.log("BruteForceCracking START set to: " + isWindowStart(), LogLevel.Status);


        if (window.isBfType()){

            if (window.isSeqType()){

                long startTime = System.currentTimeMillis();

                BruteForce bruteForce = new BruteForce(pass, getMaxLength(), isUpperCase(), isLowerCase(), isDigits(), isSpecialChars(), isTargetMD5(), isWindowStart());
                bruteForce.BruteForceCracking();

                Logger.log("Size: " + bruteForce.getCandidatePasswordsHashed().size());
                Logger.log("STARTED CRACKING");
                double increment = 100. /bruteForce.getCandidatePasswordsHashed().size();
                double barI = 0;

                for (int i=0; i<bruteForce.getCandidatePasswordsHashed().size(); i++){
                    Logger.log("Cracking ... ", LogLevel.Status);
                    window.fill(barI);
                    barI+=increment;
                    Logger.log("Incrementer: " + barI, LogLevel.Status);
                    if (pass.equals(bruteForce.getCandidatePasswordsHashed().get(i))){
                        Logger.log("Total attempts: " + bruteForce.getCandidatePasswordsHashed().size(), LogLevel.Status);
                        long endTime = System.currentTimeMillis();
                        long totalTime = endTime - startTime;
                        System.out.println("FOUND: " + bruteForce.getCandidatePasswords().get(i));
                        window.fill(100);
                        window.finalPassword("The password is: " + bruteForce.getCandidatePasswords().get(i) + ". Total time: " + totalTime + " ms.");
                        Logger.log("The password is: " + bruteForce.getCandidatePasswords().get(i) + ". Total time: " + totalTime + " ms.", LogLevel.Success);
                        break;
                    }
                    else {
                        if (i == bruteForce.getCandidatePasswordsHashed().size()-1){
                            barI=100;
                            window.fill(100);
                            window.finalPassword("Password not found!");
                            System.out.println("NOT FOUND!");
                        }
                    }
                }
            }

            if (window.isParType()) {

                Logger.log("STARTED CRACKING before calling methods");

                BruteForceParallel bruteForceParallel = new BruteForceParallel(pass, getMaxLength(), isUpperCase(), isLowerCase(), isDigits(), isSpecialChars(), isTargetMD5(), isWindowStart(), window);
                bruteForceParallel.bfParCrack();

                window.fill(100);

            }

            if (window.isDisType()){
                Logger.log("CALLING MPJ for distributed cracking");

                try {
                    FileWriter fw = new FileWriter("distributed_input.txt");
                    fw.write(pass + "\n");
                    fw.write(getMaxLength() + "\n");
                    fw.write(isUpperCase() + "\n");
                    fw.write(isLowerCase() + "\n");
                    fw.write(isDigits() + "\n");
                    fw.write(isSpecialChars() + "\n");
                    fw.write(isTargetMD5() + "\n");
                    fw.close();

                    String mpjProject = System.getenv("MPJ_HOME");
                    String projectClassPath = "D:\\H.K\\FAX\\3rd Semester\\Prog3DONE\\Project\\Pass1\\out\\production\\main";

                    //Logger.log("Checking for class file at: " + projectClassPath + "\\org\\example\\BruteForceDistributed.class");
                    File f = new File(projectClassPath + "\\org\\example\\BruteForceDistributed.class");
                    //Logger.log("Exists? " + f.exists());

                    ProcessBuilder pb = new ProcessBuilder(
                            "java",
                            "-jar", mpjProject + "\\lib\\starter.jar",
                            "-np", "4",
                            "-cp", projectClassPath,
                            "org.example.BruteForceDistributed"
                    );

                    new File("distributed_progress.txt").delete();
                    new File("distributed_result.txt").delete();
                    pb.inheritIO();
                    Process mpjProcess = pb.start();

                    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                        Logger.log("GUI closed. Destroying distributed process...", LogLevel.Warn);
                        mpjProcess.destroy();
                    }));


                    boolean resultShown = false;
                    File progressFile = new File("distributed_progress.txt");
                    File resultFile = new File("distributed_result.txt");

                    while (!resultShown) {
                        try {
                            Thread.sleep(50);
                            if (progressFile.exists()) {
                                BufferedReader br = new BufferedReader(new FileReader(progressFile));
                                String line;

                                double[] rankProgress = new double[4];
                                boolean[] rankSeen = new boolean[4];

                                while ((line = br.readLine()) != null) {
                                    if (line.contains(":")) {
                                        String[] parts = line.split(":");
                                        int rankId = Integer.parseInt(parts[0].trim());
                                        double percent = Double.parseDouble(parts[1].trim());

                                        if (rankId >= 1 && rankId < 4) {
                                            rankProgress[rankId] = percent;
                                            rankSeen[rankId] = true;
                                        }
                                    }
                                }
                                br.close();


                                double total = 0;
                                int count = 0;
                                for (int i = 1; i <= 3; i++) {
                                    if (rankSeen[i]) {
                                        total += rankProgress[i];
                                        count++;
                                    }
                                }

                                double barValue = count > 0 ? total / count : 0;
                                if (barValue > 100.0) barValue = 100.0;
                                window.fill(barValue);
                            }

                            if (resultFile.exists()) {
                                BufferedReader br = new BufferedReader(new FileReader(resultFile));
                                String line = br.readLine();
                                br.close();

                                if (line != null && line.trim().startsWith("FOUND:")) {
                                    String[] parts = line.trim().substring(6).split(",");
                                    String foundPassword = parts[0];
                                    String timeTaken = (parts.length > 1) ? parts[1] : "N/A";
                                    window.fill(100);
                                    window.finalPassword("The password is: " + foundPassword + ". Total time: " + timeTaken + " ms.");
                                    Logger.log("Distributed result: FOUND: " + foundPassword, LogLevel.Success);
                                } else {
                                    window.fill(100);
                                    window.finalPassword("Password not found.");
                                    Logger.log("Distributed result: NOT FOUND", LogLevel.Warn);
                                }
                                resultShown = true;
                            }

                        } catch (Exception e) {
                            Logger.log("ERROR: " + e.getMessage(), LogLevel.Warn);
                        }
                    }


                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

            }
        }

        if(window.isDaType()){

            if (window.isSeqType()){
                long startTime = System.currentTimeMillis();

                DictionaryAttack dictionaryAttack = new DictionaryAttack(isTargetMD5());
                dictionaryAttack.DictionaryAttackFun();

                double increment = 100. /dictionaryAttack.getCommonPasswordsHashed().size();
                double barI = 0;

                for (int i=0; i<dictionaryAttack.getCommonPasswordsHashed().size(); i++){
                    window.fill(barI);
                    barI+=increment;
                    Logger.log("Progressbar: " + barI);
                    if (pass.equals(dictionaryAttack.getCommonPasswordsHashed().get(i))){
                        System.out.println("FOUND! " + dictionaryAttack.getCommonPasswords().get(i));
                        long endTime = System.currentTimeMillis();
                        long totalTime = endTime - startTime;
                        window.fill(100);
                        window.finalPassword("The password is: " + dictionaryAttack.getCommonPasswords().get(i) + ". Total time: " + totalTime + " ms.");
                        Logger.log("The password is: " + dictionaryAttack.getCommonPasswords().get(i) + ". Total time: " + totalTime + " ms.", LogLevel.Success);
                        break;
                    } else{
                        if (i == dictionaryAttack.getCommonPasswords().size()-1){
                            System.out.println("NOT FOUND!");
                        }
                    }
                }
            }

            if (window.isParType()){
                DictionaryAttackParallel dictionaryAttackParallel = new DictionaryAttackParallel(pass, isTargetMD5(), window);
                dictionaryAttackParallel.dapAttack();
            }

            if (window.isDisType()) {

                FileWriter fw = new FileWriter("dictionary_input.txt");
                fw.write(pass + "\n");
                fw.write(isTargetMD5() + "\n");
                fw.close();

                String mpjProject = System.getenv("MPJ_HOME");
                String projectClassPath = "D:\\H.K\\FAX\\3rd Semester\\Prog3DONE\\Project\\Pass1\\out\\production\\main";

                ProcessBuilder pb = new ProcessBuilder(
                        "java",
                        "-jar", mpjProject + "\\lib\\starter.jar",
                        "-np", "4",
                        "-cp", projectClassPath,
                        "org.example.DictionaryAttackDistributed"
                );

                new File("distributed_progress.txt").delete();
                new File("distributed_result.txt").delete();

                pb.inheritIO();
                Process mpjProcess = pb.start();

                Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                    Logger.log("GUI closed. Destroying distributed process...", LogLevel.Warn);
                    mpjProcess.destroy();
                }));

                boolean resultShown = false;
                File progressFile = new File("distributed_progress.txt");
                File resultFile = new File("distributed_result.txt");

                while (!resultShown) {
                    try {

                        Thread.sleep(50);

                        if (progressFile.exists()) {
                            BufferedReader br = new BufferedReader(new FileReader(progressFile));
                            String line;

                            double[] rankProgress = new double[4];     // supports rank 1–3
                            boolean[] rankSeen = new boolean[4];

                            while ((line = br.readLine()) != null) {
                                if (line.contains(":")) {
                                    String[] parts = line.split(":");
                                    int rankId = Integer.parseInt(parts[0].trim());
                                    double percent = Double.parseDouble(parts[1].trim());

                                    if (rankId >= 1 && rankId < 4) {
                                        rankProgress[rankId] = percent;
                                        rankSeen[rankId] = true;
                                    }
                                }
                            }
                            br.close();

                            double total = 0;
                            int count = 0;
                            for (int i = 1; i <= 3; i++) {
                                if (rankSeen[i]) {
                                    total += rankProgress[i];
                                    count++;
                                }
                            }

                            double barValue = count > 0 ? total / count : 0;
                            if (barValue > 100.0) barValue = 100.0;
                            window.fill(barValue);
                        }


                        if (resultFile.exists()) {
                            BufferedReader br = new BufferedReader(new FileReader(resultFile));
                            String line = br.readLine();
                            br.close();

                            if (line != null && line.trim().startsWith("FOUND:")) {
                                String[] parts = line.trim().substring(6).split(",");
                                String foundPassword = parts[0];
                                String timeTaken = (parts.length > 1) ? parts[1] : "N/A";
                                window.fill(100);
                                window.finalPassword("The password is: " + foundPassword + ". Total time: " + timeTaken + " ms.");
                                Logger.log("Distributed Dictionary: FOUND: " + foundPassword, LogLevel.Success);
                            } else {
                                window.fill(100);
                                window.finalPassword("Password not found.");
                                Logger.log("Distributed Dictionary: NOT FOUND", LogLevel.Warn);
                            }

                            resultShown = true;
                        }

                    } catch (Exception e) {
                        Logger.log("Monitor error: " + e.getMessage(), LogLevel.Warn);
                    }
                }
            }

        }

    }

    public boolean isDigits() {
        return Digits;
    }

    public void setDigits(boolean digits) {
        Digits = digits;
    }

    public boolean isLowerCase() {
        return LowerCase;
    }

    public void setLowerCase(boolean lowerCase) {
        LowerCase = lowerCase;
    }

    public boolean isSpecialChars() {
        return SpecialChars;
    }

    public void setSpecialChars(boolean specialChars) {
        SpecialChars = specialChars;
    }

    public boolean isTargetMD5() {
        return targetMD5;
    }

    public void setTargetMD5(boolean targetMD5) {
        this.targetMD5 = targetMD5;
    }

    public boolean isTargetSHA256() {
        return targetSHA256;
    }

    public void setTargetSHA256(boolean targetSHA256) {
        this.targetSHA256 = targetSHA256;
    }

    public boolean isAttackTypeBF() {
        return attackTypeBF;
    }

    public boolean isAttackTypeDA() {
        return attackTypeDA;
    }

    public void setAttackTypeBF(boolean attackTypeBF) {
        this.attackTypeBF = attackTypeBF;
    }

    public void setAttackTypeDA(boolean attackTypeDA) {
        this.attackTypeDA = attackTypeDA;
    }

    public boolean isUpperCase() {
        return UpperCase;
    }

    public void setUpperCase(boolean upperCase) {
        UpperCase = upperCase;
    }

    public void setMaxLength(int maxLength) {
        this.maxLength = maxLength;
    }

    public int getMaxLength() {
        return maxLength;
    }

    public boolean isWindowStart() {
        return start;
    }

    public void setStart(boolean start) {
        this.start = start;
    }
}
