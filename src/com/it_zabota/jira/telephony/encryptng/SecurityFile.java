package com.it_zabota.jira.telephony.encryptng;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;

public class SecurityFile {
	public static void encrypt(String key, InputStream is, OutputStream os, String isEncrypt) throws Throwable {
		encryptOrDecrypt(key, Cipher.ENCRYPT_MODE, is, os, isEncrypt);
	}

	public static void decrypt(String key, InputStream is, OutputStream os, String isEncrypt) throws Throwable {
		encryptOrDecrypt(key, Cipher.DECRYPT_MODE, is, os, isEncrypt);
	}

	public static void encryptOrDecrypt(String key, int mode, InputStream is, OutputStream os, String isEncrypt) throws Throwable {

		DESKeySpec dks = new DESKeySpec(key.getBytes());
		SecretKeyFactory skf = SecretKeyFactory.getInstance("DES");
		SecretKey desKey = skf.generateSecret(dks);
		Cipher cipher = Cipher.getInstance("DES"); // DES/ECB/PKCS5Padding for SunJCE
		
		if (mode == Cipher.ENCRYPT_MODE) {
			cipher.init(Cipher.ENCRYPT_MODE, desKey);
			if (isEncrypt.equalsIgnoreCase("ДА")) {
				CipherInputStream cis = new CipherInputStream(is, cipher);
				doCopy(cis, os);
			}
			else {
				InputStream cis = is;
				doCopy(cis, os);
			}
			
		} else if (mode == Cipher.DECRYPT_MODE) {
			cipher.init(Cipher.DECRYPT_MODE, desKey);
			if (isEncrypt.equalsIgnoreCase("ДА")) {
				CipherOutputStream cos = new CipherOutputStream(os, cipher);
				doCopy(is, cos);
			}
			else {
				OutputStream cos = os;
				doCopy(is, cos);
			}
			

		}
	}

	public static void doCopy(InputStream is, OutputStream os) throws IOException {
		byte[] bytes = new byte[64];
		int numBytes;
		while ((numBytes = is.read(bytes)) != -1) {
			os.write(bytes, 0, numBytes);
		}
		os.flush();
		os.close();
		is.close();
	}
}
