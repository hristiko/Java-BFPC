package org.example;

import util.LogLevel;
import util.Logger;

import java.util.ArrayList;
import java.util.Random;

public class BruteForce {

    ArrayList<String> charSet = new ArrayList<>();
    ArrayList<String> candidatePasswords = new ArrayList<>();
    ArrayList<String> candidatePasswordsHashed = new ArrayList<>();

    String pass;
    int length;
    boolean upperCase;
    boolean lowerCase;
    boolean digits;
    boolean specialCharsBool;
    boolean md5;
    boolean start;

    BruteForce(String pass, int length, boolean upperCase, boolean lowerCase, boolean digits, boolean specialCharsBool, boolean md5, boolean start){
        this.pass = pass;
        this.length = length;
        this.upperCase = upperCase;
        this.lowerCase = lowerCase;
        this.digits = digits;
        this.specialCharsBool = specialCharsBool;
        this.md5 = md5;
        this.start = start;
    }

    public void BruteForceCracking() {

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


        Logger.log("Brute Force Started: " + start, LogLevel.Status);

        if (upperCase){
            charSet.addAll(UpperLetters);
            setCharSet(charSet);
        }

        if (digits){
            charSet.addAll(Digits);
            setCharSet(charSet);
            Logger.log("Brute force helper: DIGITS ALLOWED:" + getCharSet(), LogLevel.Success);
        }

        if (lowerCase){
            charSet.addAll(LowerLetters);
            setCharSet(charSet);
            Logger.log("Brute force: lower case letters ALLOWED: " + getCharSet(), LogLevel.Success);
        }

        if (specialCharsBool){
            charSet.addAll(SPchars);
            setCharSet(charSet);
        }

        //Logger.log("MD5 from BF: " + isMD5());
        if (md5) {
            Logger.log("MD5 from BF: " + md5);
        }


        // BruteForceCracking bruteForceAttack = new BruteForceCracking();
        int r = length;        //length of the password
        /*long rFact = r;
        for (int i=r-1; i>0; i--){
            rFact = rFact*i;
        }*/

        int n = getCharSet().size();        //Length of the char set used
        /*int nFact = n-1;
        for (int i=n-2; i>0; i--){
            nFact = nFact*i;
        }*/

        /*int divident = n+r-1;
        long dividentFact = divident;
        for (int i=divident-1; i>0; i--){
            dividentFact = dividentFact*i;
        }

        long divider = rFact * nFact;

        long combinationsWithRepetitions = dividentFact / divider;*/

        Logger.log("There are: " + Math.pow(n,r) + " combinations", LogLevel.Status);
        //Logger.log("Entering loop to generate combinations", LogLevel.Status);
        Random rand = new Random();
        long maxCombinations = (long) Math.pow(n,r);

        /*for (int i=0; i<maxCombinations; i++){
            //Logger.log("Creating combination", LogLevel.Status);
            String pass = "";
            int length = 0;
            while (length<r){
                int randInt = rand.nextInt(getCharSet().size());
                if (pass.isBlank()){
                    pass = getCharSet().get(randInt);
                }else{
                    pass = pass.concat((getCharSet().get(randInt)));
                }
                length++;
            }

            if (!getCandidatePasswords().contains(pass)){
                candidatePasswords.add(pass);
                setCandidatePasswords(candidatePasswords);
            }else {
                i--;
            }
        }

        Logger.log("Hashing the combinations", LogLevel.Status);
        for (int i=0; i<getCandidatePasswords().size(); i++){
            if (md5){
                candidatePasswordsHashed.add(org.example.MD5.toMD5crack(getCandidatePasswords().get(i)));
                setCandidatePasswordsHashed(candidatePasswordsHashed);
            }else {
                candidatePasswordsHashed.add(org.example.SHA256.toSHA256(getCandidatePasswords().get(i)));
                setCandidatePasswordsHashed(candidatePasswordsHashed);
            }
        }*/
        Logger.log("Hashing the combinations", LogLevel.Status);
        for (int i = 0; i < maxCombinations; i++) {
            String pass = "";
            int length = 0;
            while (length < r) {
                int randInt = rand.nextInt(getCharSet().size());
                pass += getCharSet().get(randInt);
                length++;
            }

            if (!getCandidatePasswords().contains(pass)) {
                candidatePasswords.add(pass);
                setCandidatePasswords(candidatePasswords);

                if (md5) {
                    candidatePasswordsHashed.add(org.example.MD5.toMD5crack(pass));
                } else {
                    candidatePasswordsHashed.add(org.example.SHA256.toSHA256(pass));
                }
                setCandidatePasswordsHashed(candidatePasswordsHashed);
            } else {
                i--;
            }
        }


        Logger.log("Brute force: Size of array: " + getCandidatePasswordsHashed().size());


    }

    public void setCharSet(ArrayList<String> charSet) {
        this.charSet = charSet;
    }

    public ArrayList<String> getCharSet() {
        return charSet;
    }

    public void setCandidatePasswords(ArrayList<String> candidatePasswords) {
        this.candidatePasswords = candidatePasswords;
    }

    public ArrayList<String> getCandidatePasswords() {
        return candidatePasswords;
    }

    public ArrayList<String> getCandidatePasswordsHashed() {
        return candidatePasswordsHashed;
    }

    public void setCandidatePasswordsHashed(ArrayList<String> candidatePasswordsHashed) {
        this.candidatePasswordsHashed = candidatePasswordsHashed;
    }

}
