package com.mwendasoft.superme;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class SuperMeBackupHelper {

    private Context context;

    // Constant database name
    private static final String DATABASE_NAME = "SuperMeDB.db";

    // SharedPreferences file and key to check if app is already installed
    private static final String PREF_NAME = "superme_prefs";
    private static final String INSTALLED_KEY = "app_installed";

    // 16-byte secret key for AES encryption (keep this secret and safe)
    private static final String AES_KEY = "1234567890abcdef";

    public SuperMeBackupHelper(Context context) {
        this.context = context;
    }

    // Backup the database with AES encryption to ".superme/backups/" directory
    public boolean backupDatabase() {
        try {
            File dbFile = context.getDatabasePath(DATABASE_NAME);
            File backupDir = new File(Environment.getExternalStorageDirectory(), ".superme/.backups");

            if (!backupDir.exists()) {
                backupDir.mkdirs();
            }

            File backupFile = new File(backupDir, DATABASE_NAME + ".enc");
            encryptFile(dbFile, backupFile);
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // Restore the encrypted database only if the app was freshly installed
    public boolean restoreIfFreshInstall() {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        boolean installedBefore = prefs.getBoolean(INSTALLED_KEY, false);

        if (!installedBefore) {
            File backupFile = new File(Environment.getExternalStorageDirectory(), ".superme/.backups/" + DATABASE_NAME + ".enc");
            File dbFile = context.getDatabasePath(DATABASE_NAME);

            try {
                if (!dbFile.getParentFile().exists()) {
                    dbFile.getParentFile().mkdirs();
                }

                if (backupFile.exists()) {
                    decryptFile(backupFile, dbFile);

                    // Mark app as installed after restoring
                    prefs.edit().putBoolean(INSTALLED_KEY, true).apply();
                    return true;
                }

            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }

        return false;
    }

    // Encrypt a file using AES and save to a new file
    private void encryptFile(File inputFile, File outputFile) throws Exception {
        SecretKey secretKey = new SecretKeySpec(AES_KEY.getBytes(), "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);

        FileInputStream inStream = new FileInputStream(inputFile);
        CipherOutputStream outStream = new CipherOutputStream(new FileOutputStream(outputFile), cipher);

        byte[] buffer = new byte[1024];
        int length;
        while ((length = inStream.read(buffer)) > 0) {
            outStream.write(buffer, 0, length);
        }

        inStream.close();
        outStream.close();
    }

    // Decrypt a file using AES and save to the original database file
    private void decryptFile(File inputFile, File outputFile) throws Exception {
        SecretKey secretKey = new SecretKeySpec(AES_KEY.getBytes(), "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, secretKey);

        CipherInputStream inStream = new CipherInputStream(new FileInputStream(inputFile), cipher);
        FileOutputStream outStream = new FileOutputStream(outputFile);

        byte[] buffer = new byte[1024];
        int length;
        while ((length = inStream.read(buffer)) > 0) {
            outStream.write(buffer, 0, length);
        }

        inStream.close();
        outStream.close();
    }
}
