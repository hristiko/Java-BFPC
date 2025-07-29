package org.example;

import java.io.*;
import java.nio.file.Path;
import java.util.ArrayList;

public class DictionaryAttack {

    private ArrayList<String> commonPasswords = new ArrayList<String>();
    private ArrayList<String> commonPasswordsHashed = new ArrayList<String>();

    boolean typeHashMD5;

    DictionaryAttack(boolean typeHashMD5) {
        this.typeHashMD5 = typeHashMD5;
    }

    public void DictionaryAttackFun(){
        Path fileName = Path.of("PasswordDictionary.txt");

        try (BufferedReader buffer = new BufferedReader(new InputStreamReader(new FileInputStream(fileName.toFile()), "ISO-8859-1"))){
            String str;
            while ((str = buffer.readLine()) != null){
                commonPasswords.add(str);
                if (typeHashMD5){
                    String strToHash = MD5.toMD5crack(str);
                    commonPasswordsHashed.add(strToHash);
                } else {
                    String strToHash = SHA256.toSHA256(str);
                    commonPasswordsHashed.add(strToHash);
                }

            }
        }

        catch (IOException e){
            e.printStackTrace();
        }
    }


    public ArrayList<String> getCommonPasswordsHashed() {
        return commonPasswordsHashed;
    }

    public ArrayList<String> getCommonPasswords() {
        return commonPasswords;
    }
}
