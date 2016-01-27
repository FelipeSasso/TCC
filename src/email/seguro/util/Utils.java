package email.seguro.util;

import java.io.ByteArrayInputStream;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

import org.apache.http.conn.ssl.AbstractVerifier;

import android.util.Base64;

public class Utils {
	public static byte[] cifraChave(byte[] keyBytes, byte[] senha)
			throws NoSuchAlgorithmException, NoSuchPaddingException,
			InvalidKeyException, IllegalBlockSizeException, BadPaddingException {

		SecretKeySpec secretKeySpec = new SecretKeySpec(senha, "AES");
		Cipher cipher = Cipher.getInstance("AES");
		cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
		byte[] chaveCifrada = cipher.doFinal(keyBytes);

		return chaveCifrada;
	}

	public static byte[] decifraChave(byte[] keyBytes, byte[] senha)
			throws NoSuchAlgorithmException, NoSuchPaddingException,
			InvalidKeyException, IllegalBlockSizeException, BadPaddingException {

		SecretKeySpec secretKeySpec = new SecretKeySpec(senha, "AES");
		Cipher cipher = Cipher.getInstance("AES");
		cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);
		byte[] chaveDecifrada = cipher.doFinal(keyBytes);

		return chaveDecifrada;
	}

	public static PrivateKey montaChave(byte[] bytesChave)
			throws NoSuchAlgorithmException, InvalidKeySpecException {
		PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(bytesChave);
		KeyFactory keyFactory = KeyFactory.getInstance("RSA");
		return keyFactory.generatePrivate(keySpec);
	}

	public static byte[] getHash(byte[] senha) {
		MessageDigest md = null;

		try {
			md = MessageDigest.getInstance("SHA256");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}

		md.update(senha);
		byte[] hash = md.digest();

		return hash;
	}

	public static boolean validaEmail(String email) {
		int tamanho;
		int indexArroba;
		int indexPonto;

		tamanho = email.length();
		indexArroba = email.indexOf("@");
		indexPonto = email.lastIndexOf(".");

		if (tamanho < 5 || indexArroba <= 0 || indexPonto <= 0
				|| indexPonto < indexArroba || indexPonto + 1 == tamanho) {
			return false;
		}
		return true;
	}

	public static String getCN(X509Certificate cert) {
		String cn = "";
		String str[] = AbstractVerifier.getCNs(cert);

		for (int i = 0; i < str.length; i++) {
			cn = cn + str[i] + " ";
		}

		return cn;
	}

	public static X509Certificate decodificaCertificado(String certBase64) {

		byte certByte[];
		ByteArrayInputStream inStream;
		CertificateFactory cf;
		X509Certificate cert = null;
		try {
			certByte = Base64.decode(certBase64, Base64.DEFAULT);
			inStream = new ByteArrayInputStream(certByte);
			cf = CertificateFactory.getInstance("X.509");
			cert = (X509Certificate) cf.generateCertificate(inStream);
			inStream.close();
		} catch (Exception e) {

		}
		return cert;
	}

	public static String codificaChave(String senha, Key chave)
			throws InvalidKeyException, NoSuchAlgorithmException,
			NoSuchPaddingException, IllegalBlockSizeException,
			BadPaddingException {

		byte[] senhaBytes = Utils.getHash(senha.getBytes());
		byte[] chavecifrada = Utils.cifraChave(chave.getEncoded(), senhaBytes);

		return Base64.encodeToString(chavecifrada, Base64.DEFAULT);
	}

	public static String convertHexToString(String hex) {
		StringBuilder sb = new StringBuilder();
		StringBuilder temp = new StringBuilder();

		for (int i = 0; i < hex.length() - 1; i += 2) {
			String output = hex.substring(i, (i + 2));
			int decimal = Integer.parseInt(output, 16);
			sb.append((char) decimal);
			temp.append(decimal);
		}
		return sb.toString();
	}

}
