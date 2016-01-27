package email.seguro.email;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.Security;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Date;
import java.util.Properties;

import javax.activation.CommandMap;
import javax.activation.MailcapCommandMap;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import javax.mail.AuthenticationFailedException;
import javax.mail.Authenticator;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.spongycastle.asn1.ASN1EncodableVector;
import org.spongycastle.asn1.cms.AttributeTable;
import org.spongycastle.asn1.cms.IssuerAndSerialNumber;
import org.spongycastle.asn1.smime.SMIMECapabilitiesAttribute;
import org.spongycastle.asn1.smime.SMIMECapability;
import org.spongycastle.asn1.smime.SMIMECapabilityVector;
import org.spongycastle.asn1.smime.SMIMEEncryptionKeyPreferenceAttribute;
import org.spongycastle.asn1.x500.X500Name;
import org.spongycastle.cms.jcajce.JcaSimpleSignerInfoGeneratorBuilder;
import org.spongycastle.jce.provider.BouncyCastleProvider;
import org.spongycastle.mail.smime.SMIMEEnvelopedGenerator;
import org.spongycastle.mail.smime.SMIMEException;
import org.spongycastle.mail.smime.SMIMESignedGenerator;
import org.spongycastle.operator.OperatorCreationException;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import email.seguro.R;
import email.seguro.banco.Banco;
import email.seguro.tabelas.Certificados;
import email.seguro.tabelas.ContasEmail;
import email.seguro.util.Utils;

public class EmailActivity extends Activity {

	private static final int DIALOG_SENHA = 0;

	static {
		Security.addProvider(new BouncyCastleProvider());
	}
	Email email;
	private static String _user;
	private static String _pass;
	private static String _id;
	private static byte[] chavePrivada;
	private static String senhaChave;
	private PrivateKey privKey;
	private MimeMultipart _multipart;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		_multipart = new MimeMultipart();

		addMailcap();

		Bundle extras = getIntent().getExtras();

		email = (Email) extras.getSerializable("email");
		_user = extras.getString("user");
		_pass = extras.getString("senha");
		_id = extras.getString("id");

		try {
			send(email);
		} catch (MessagingException e) {
			e.printStackTrace();
		}
	}

	private void addMailcap() {
		MailcapCommandMap mc = (MailcapCommandMap) CommandMap
				.getDefaultCommandMap();
		mc.addMailcap("text/html;; x-java-content-handler=com.sun.mail.handlers.text_html");
		mc.addMailcap("text/xml;; x-java-content-handler=com.sun.mail.handlers.text_xml");
		mc.addMailcap("text/plain;; x-java-content-handler=com.sun.mail.handlers.text_plain");
		mc.addMailcap("multipart/*;; x-java-content-handler=com.sun.mail.handlers.multipart_mixed");
		mc.addMailcap("message/rfc822;; x-java-content-handler=com.sun.mail.handlers.message_rfc822");

		mc.addMailcap("application/pkcs7-signature;; x-java-content-handler=org.spongycastle.mail.smime.handlers.pkcs7_signature");
		mc.addMailcap("application/pkcs7-mime;; x-java-content-handler=org.spongycastle.mail.smime.handlers.pkcs7_mime");
		mc.addMailcap("application/x-pkcs7-signature;; x-java-content-handler=org.spongycastle.mail.smime.handlers.x_pkcs7_signature");
		mc.addMailcap("application/x-pkcs7-mime;; x-java-content-handler=org.spongycastle.mail.smime.handlers.x_pkcs7_mime");
		mc.addMailcap("multipart/signed;; x-java-content-handler=org.spongycastle.mail.smime.handlers.multipart_signed");
		CommandMap.setDefaultCommandMap(mc);

	}

	public static Authenticator getAuthenticator() {

		Authenticator autenticacao = new Authenticator() {

			@Override
			public PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(_user, _pass);
			}
		};

		return autenticacao;
	}

	public String buscaChave() {
		SQLiteOpenHelper banco = new Banco(getApplicationContext());
		SQLiteDatabase db = banco.getReadableDatabase();
		String chave = "";

		Cursor query = db.query(ContasEmail.NOME_TABELA,
				new String[] { ContasEmail.CHAVE_PRIVADA }, ContasEmail._ID
						+ "='" + _id + "'", null, null, null, null);

		startManagingCursor(query);

		if (query.getCount() > 0) {
			query.moveToFirst();
			chavePrivada = Base64.decode(query.getString(0), Base64.DEFAULT);
			showDialog(DIALOG_SENHA);
		} else {
		}

		db.close();

		return chave;

	}

	public X509Certificate buscaCertificado(String email) {
		SQLiteOpenHelper banco = new Banco(getApplicationContext());
		SQLiteDatabase db = banco.getReadableDatabase();
		String certificado;
		X509Certificate cert = null;

		Cursor query = db.query(Certificados.NOME_TABELA,
				new String[] { Certificados.CERTIFICADO }, Certificados.EMAIL
						+ "='" + email + "'", null, null, null, null);
		startManagingCursor(query);

		if (query.getCount() > 0) {
			query.moveToFirst();
			certificado = query.getString(0);
			cert = Utils.decodificaCertificado(certificado);
			db.close();
			return cert;
		} else {
			db.close();
			return null;
		}

	}

	public boolean send(Email email) throws MessagingException {

		if (email.isAssina()) {
			if (existeChave()) {
				showDialog(DIALOG_SENHA);
			} else {
				mensagem("Não existe certificado associado à conta de e-mail.");
				retornaErro();
			}

		}

		if (email.isCifra()) {
			try {
				cifrarEmail();
			} catch (NoSuchAlgorithmException e) {				
				e.printStackTrace();
			} catch (NoSuchProviderException e) {				
				e.printStackTrace();
			} catch (SMIMEException e) {				
				e.printStackTrace();
			} catch (IOException e) {				
				e.printStackTrace();
			}

			Intent intentSalvo = new Intent();
			setResult(Activity.RESULT_OK, intentSalvo);
			finish();
		}

		if (!email.isAssina() && !email.isCifra()) {
			enviaEmailNormal();
			Intent intentSalvo = new Intent();
			setResult(Activity.RESULT_OK, intentSalvo);
			finish();
		}

		return true;

	}

	public void retornaErro() {
		Intent intentSalvo = new Intent();
		setResult(Activity.RESULT_CANCELED, intentSalvo);
		finish();

	}

	private void enviaEmailNormal() throws AddressException, MessagingException {
		Properties props = _setProperties();

		Session session = Session.getInstance(props, getAuthenticator());
		MimeMessage msg = new MimeMessage(session);

		msg.setFrom(new InternetAddress(email.getFrom()));

		InternetAddress[] addressTo = new InternetAddress[email.getTo().length];
		for (int i = 0; i < email.getTo().length; i++) {
			addressTo[i] = new InternetAddress(email.getTo()[i]);
		}
		msg.setRecipients(MimeMessage.RecipientType.TO, addressTo);

		msg.setSubject(email.getSubject());
		msg.setSentDate(new Date());

		BodyPart messageBodyPart = new MimeBodyPart();
		messageBodyPart.setText(email.getBody());
		_multipart.addBodyPart(messageBodyPart);
		msg.setContent(_multipart);

		try {
			Transport.send(msg);
		} catch (AuthenticationFailedException e) {
			mensagem("Nome de usuário ou senha incorretos.");
			retornaErro();
		}
	}

	private void assinaEmail() throws CertificateEncodingException,
			OperatorCreationException, MessagingException, SMIMEException {

		Properties props = _setProperties();

		Session session = Session.getInstance(props, getAuthenticator());

		ASN1EncodableVector signedAttrs = new ASN1EncodableVector();
		SMIMECapabilityVector caps = new SMIMECapabilityVector();

		caps.addCapability(SMIMECapability.dES_EDE3_CBC);
		caps.addCapability(SMIMECapability.rC2_CBC, 128);
		caps.addCapability(SMIMECapability.dES_CBC);

		signedAttrs.add(new SMIMECapabilitiesAttribute(caps));

		X509Certificate cert = getCertificadoAssinatura();

		IssuerAndSerialNumber issAndSer = new IssuerAndSerialNumber(
				new X500Name(cert.getSubjectX500Principal().toString()),
				cert.getSerialNumber());

		signedAttrs.add(new SMIMEEncryptionKeyPreferenceAttribute(issAndSer));
		SMIMESignedGenerator gen = new SMIMESignedGenerator();

		gen.addSignerInfoGenerator(new JcaSimpleSignerInfoGeneratorBuilder()
				.setProvider("SC")
				.setSignedAttributeGenerator(new AttributeTable(signedAttrs))
				.build(cert.getSigAlgName(), privKey, cert));

		MimeBodyPart msg = new MimeBodyPart();

		msg.setText(email.getBody());

		MimeMultipart mm = gen.generate(msg);

		InternetAddress[] addressTo = new InternetAddress[email.getTo().length];
		for (int i = 0; i < email.getTo().length; i++) {
			addressTo[i] = new InternetAddress(email.getTo()[i]);
		}

		MimeMessage body = new MimeMessage(session);
		body.setFrom(new InternetAddress(email.getFrom()));
		body.addRecipients(Message.RecipientType.TO, addressTo);
		body.setSubject(email.getSubject());
		body.setContent(mm, mm.getContentType());
		body.saveChanges();
		body.setContent(mm);

		try {
			Transport.send(body);
		} catch (AuthenticationFailedException e) {
			mensagem("Nome de usuário ou senha incorretos.");
			retornaErro();
		}

	}

	@SuppressWarnings("deprecation")
	private void cifrarEmail() throws MessagingException,
			NoSuchAlgorithmException, NoSuchProviderException, SMIMEException,
			IOException {

		Properties props = _setProperties();

		Session session = Session.getInstance(props, getAuthenticator());

		for (int i = 0; i < email.getTo().length; i++) {
			MimeMessage body = new MimeMessage(session);
			_multipart = new MimeMultipart();
			MimeBodyPart mp = null;
			MimeBodyPart msg = new MimeBodyPart();
			msg.setText(email.getBody());

			SMIMEEnvelopedGenerator gen = null;
			X509Certificate cert = buscaCertificado(email.getTo()[i].trim());

			if (cert != null) {
				gen = new SMIMEEnvelopedGenerator();
				gen.addKeyTransRecipient((X509Certificate) cert);
				mp = gen.generate(msg, SMIMEEnvelopedGenerator.AES192_CBC, "SC");
				body.setContent(mp.getContent(), mp.getContentType());
				_multipart.addBodyPart(mp);

				InternetAddress addressTo = new InternetAddress();
				addressTo = new InternetAddress(email.getTo()[i]);
				body.setFrom(new InternetAddress(email.getFrom()));
				body.addRecipient(Message.RecipientType.TO, addressTo);
				body.setSubject(email.getSubject());
				body.saveChanges();
				body.setContent(_multipart);
				try {
					Transport.send(body);
				} catch (AuthenticationFailedException e) {
					mensagem("Nome de usuário ou senha incorretos.");
					retornaErro();
				}

			} else {
				mensagem("Para os destinatários sem certificados importados, o e-mail não foi enviado.");
			}

		}

	}

	private void mensagem(String mensagem) {
		Context context = getApplicationContext();
		Toast toast = Toast.makeText(context, mensagem, Toast.LENGTH_SHORT);
		toast.show();
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		AlertDialog.Builder alert = new AlertDialog.Builder(this);

		switch (id) {
		case DIALOG_SENHA:
			alert.setTitle("Senha");
			alert.setCancelable(true);
			alert.setOnCancelListener(new DialogInterface.OnCancelListener() {

				@Override
				public void onCancel(DialogInterface dialog) {
					retornaErro();
				}
			});
			LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View view = inflater.inflate(R.layout.pass_phrase_chave, null);
			final EditText txtSenhaCert = (EditText) view
					.findViewById(R.id.senhaCertificado);
			alert.setView(view);

			alert.setPositiveButton(android.R.string.ok,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							buscaChave();
							senhaChave = txtSenhaCert.getText().toString();
							try {

								byte[] chaveDecifrada = decifraChave(
										chavePrivada,
										hashSenha(senhaChave.getBytes()));
								montaChave(chaveDecifrada);
								assinaEmail();

								Intent intentSalvo = new Intent();
								setResult(Activity.RESULT_OK, intentSalvo);
								finish();

							} catch (NoSuchAlgorithmException e) {
								e.printStackTrace();
							} catch (InvalidKeySpecException e) {
								e.printStackTrace();
							} catch (CertificateEncodingException e) {
								e.printStackTrace();
							} catch (OperatorCreationException e) {
								e.printStackTrace();
							} catch (MessagingException e) {
								e.printStackTrace();
							} catch (SMIMEException e) {
								e.printStackTrace();
							} catch (InvalidKeyException e) {
								e.printStackTrace();
							} catch (NoSuchPaddingException e) {
								e.printStackTrace();
							} catch (IllegalBlockSizeException e) {
								mensagem("Senha da chave inválida.");
								retornaErro();
							} catch (BadPaddingException e) {
								mensagem("Senha da chave inválida.");
								retornaErro();
							}

						}
					}

			);
			return alert.create();
		default: {
			return super.onCreateDialog(id);
		}
		}

	}

	public byte[] decifraChave(byte[] keyBytes, byte[] senha)
			throws NoSuchAlgorithmException, NoSuchPaddingException,
			InvalidKeyException, IllegalBlockSizeException, BadPaddingException {

		SecretKeySpec secretKeySpec = new SecretKeySpec(senha, "AES");
		Cipher cipher = null;

		cipher = Cipher.getInstance("AES");

		cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);

		byte[] chaveDecifrada = null;

		chaveDecifrada = cipher.doFinal(keyBytes);

		return chaveDecifrada;

	}

	private void montaChave(byte[] bytesChave) throws NoSuchAlgorithmException,
			InvalidKeySpecException {

		PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(bytesChave);
		KeyFactory keyFactory = KeyFactory.getInstance("RSA");
		privKey = keyFactory.generatePrivate(keySpec);

	}

	public byte[] hashSenha(byte[] senha) {
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

	public X509Certificate getCertificadoAssinatura() {
		SQLiteOpenHelper banco = new Banco(getApplicationContext());
		SQLiteDatabase db = banco.getReadableDatabase();
		String certificado;
		X509Certificate cert = null;

		Cursor query = db.query(ContasEmail.NOME_TABELA,
				new String[] { ContasEmail.CERTIFICADO }, ContasEmail._ID
						+ "='" + _id + "'", null, null, null, null);
		startManagingCursor(query);

		if (query.getCount() > 0) {
			query.moveToFirst();
			certificado = query.getString(0);
			cert = Utils.decodificaCertificado(certificado);
		}

		db.close();

		return cert;
	}

	public boolean existeChave() {
		SQLiteOpenHelper banco = new Banco(getApplicationContext());
		SQLiteDatabase db = banco.getReadableDatabase();

		Cursor query = db.query(ContasEmail.NOME_TABELA,
				new String[] { ContasEmail.CHAVE_PRIVADA }, ContasEmail._ID
						+ "='" + _id + "'", null, null, null, null);

		startManagingCursor(query);

		query.moveToFirst();
		if (query.getCount() > 0) {
			if (query.getString(0) == null || query.getString(0) == "") {
				db.close();
				return false;
			} else {
				db.close();
				return true;
			}

		} else {
			db.close();
			return false;
		}

	}

	private Properties _setProperties() {
		Properties props = new Properties();

		props.put("mail.smtp.host", "smtp.gmail.com");
		props.put("mail.debug", "false");
		props.put("mail.smtp.auth", "true");
		props.put("mail.transport.protocol", "smtp");
		props.put("mail.smtp.port", "465");
		props.put("mail.smtp.socketFactory.port", "465");
		props.put("mail.smtp.starttls.enable", "true");
		props.put("mail.smtp.socketFactory.class",
				"javax.net.ssl.SSLSocketFactory");
		props.put("mail.smtp.socketFactory.fallback", "false");

		return props;
	}

}
