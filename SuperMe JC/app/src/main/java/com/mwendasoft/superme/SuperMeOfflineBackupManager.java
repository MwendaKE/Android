package com.mwendasoft.superme;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.util.Base64;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.InvalidKeyException;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Arrays;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import android.util.Log;

public class SuperMeOfflineBackupManager extends SQLiteOpenHelper {

    // Database configuration
    private static final String DATABASE_NAME = "SuperMeDB.db";
    private static final int DATABASE_VERSION = 1;
    private static final String BACKUP_PATH = "Android/data/com.mycompany.superme/files/.superme/backups/superme_backup.db";
    private static final String PREFS_NAME = "install_prefs";
    private static final String PREFS_INSTALL_FLAG = "is_installed";

    // Encryption configuration
    private static final String ENCRYPTION_ALGORITHM = "AES/CBC/PKCS5Padding";
    private static final String SECRET_KEY_ALGORITHM = "AES";
    private static final String IV = "abcdefghijklmnop"; // 16 bytes IV

    private Context context;
    private byte[] secretKey;
    private SharedPreferences prefs;

    // Constructor
    public SuperMeOfflineBackupManager(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
        this.secretKey = generateSecretKey(context.getPackageName());
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    // Generate encryption key (returns 16-byte array)
    private byte[] generateSecretKey(String packageName) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(packageName.getBytes());
            return Arrays.copyOf(hash, 16); // Ensure exactly 16 bytes
        } catch (NoSuchAlgorithmException e) {
            // Fallback: use package name padded to 16 bytes
            byte[] fallback = new byte[16];
            byte[] pkgBytes = packageName.getBytes();
            System.arraycopy(pkgBytes, 0, fallback, 0, Math.min(pkgBytes.length, 16));
            return fallback;
        }
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Only restore on fresh install
        if (isFreshInstall()) {
            restoreFromBackup(db);
            markAsInstalled();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Backup before upgrade
        backupDatabase();
    }

    private boolean isFreshInstall() {
        return !prefs.getBoolean(PREFS_INSTALL_FLAG, false);
    }

    private void markAsInstalled() {
        prefs.edit().putBoolean(PREFS_INSTALL_FLAG, true).apply();
    }

    public boolean backupDatabase() {
        try {
            File backupFile = getBackupFile();
            if (backupFile == null) {
                Log.e("Backup", "Failed to create backup file");
                return false;
            }

            File dbFile = context.getDatabasePath(DATABASE_NAME);
            if (!dbFile.exists()) {
                Log.e("Backup", "Database file doesn't exist");
                return false;
            }

            encryptFile(dbFile, backupFile);
            Log.d("Backup", "Backup saved to: " + backupFile.getAbsolutePath());
            return true;
        } catch (Exception e) {
            Log.e("Backup", "Backup error", e);
            return false;
        }
    }

    private boolean restoreFromBackup(SQLiteDatabase db) {
        try {
            File backupFile = getBackupFile();
            if (!backupFile.exists()) {
                Log.d("Backup", "No backup file found");
                return false;
            }

            File tempFile = new File(context.getCacheDir(), "temp_restore.db");
            decryptFile(backupFile, tempFile);

            db.close();
            copyFile(tempFile, context.getDatabasePath(DATABASE_NAME));
            tempFile.delete();

            db = getWritableDatabase();
            return true;
        } catch (Exception e) {
            Log.e("Backup", "Restore error", e);
            return false;
        }
    }

    private void encryptFile(File input, File output) throws Exception {
        doCrypto(Cipher.ENCRYPT_MODE, input, output);
    }

    private void decryptFile(File input, File output) throws Exception {
        doCrypto(Cipher.DECRYPT_MODE, input, output);
    }

    private void doCrypto(int mode, File input, File output) throws Exception {
        SecretKeySpec keySpec = new SecretKeySpec(secretKey, SECRET_KEY_ALGORITHM);
        AlgorithmParameterSpec ivSpec = new IvParameterSpec(IV.getBytes());

        Cipher cipher = Cipher.getInstance(ENCRYPTION_ALGORITHM);
        cipher.init(mode, keySpec, ivSpec);

        try (FileInputStream in = new FileInputStream(input);
		FileOutputStream out = new FileOutputStream(output)) {
            byte[] inputBytes = new byte[(int) input.length()];
            in.read(inputBytes);
            out.write(cipher.doFinal(inputBytes));
        }
    }

    private void copyFile(File src, File dst) throws Exception {
        try (FileInputStream in = new FileInputStream(src);
		FileOutputStream out = new FileOutputStream(dst)) {
            byte[] buffer = new byte[1024];
            int length;
            while ((length = in.read(buffer)) > 0) {
                out.write(buffer, 0, length);
            }
        }
    }

    private File getBackupFile() {
        File baseDir = context.getExternalFilesDir(null);
        if (baseDir == null) {
            Log.e("Backup", "External storage not available");
            return null;
        }

        File backupDir = new File(baseDir, ".superme/backups");
        if (!backupDir.exists() && !backupDir.mkdirs()) {
            Log.e("Backup", "Failed to create backup directory");
            return null;
        }

        return new File(backupDir, "superme_backup.db");
    }

    public void onDatabaseChanged() {
        new Thread(new Runnable() {
				@Override
				public void run() {
					backupDatabase();
				}
			}).start();
    }

    public boolean backupExists() {
        File backupFile = getBackupFile();
        return backupFile != null && backupFile.exists();
    }
}
