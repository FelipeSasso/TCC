package email.seguro;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateFactory;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import email.seguro.banco.Banco;
import email.seguro.controle.CertificadosActivity;
import email.seguro.controle.EnviarEmailActivity;
import email.seguro.controle.GerenciaContasActivity;
import email.seguro.tabelas.Certificados;
import email.seguro.tabelas.CertificadosRevogados;
import email.seguro.util.FileSelector;

public class MainActivity extends Activity {

	private Button btnCertificados;
	private Button btnEnviarEmail;
	private Button btnContas;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		SQLiteOpenHelper banco = new Banco(getApplicationContext());
		banco.getWritableDatabase();

		btnCertificados = (Button) findViewById(R.id.btnCertificados);
		btnEnviarEmail = (Button) findViewById(R.id.btnEnviarEmail);
		btnContas = (Button) findViewById(R.id.btnContas);

		btnCertificados.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				startActivity(new Intent(getApplicationContext(),
						CertificadosActivity.class));
			}
		});
		btnEnviarEmail.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				startActivity(new Intent(getApplicationContext(),
						EnviarEmailActivity.class));
			}
		});
		btnContas.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				startActivity(new Intent(getApplicationContext(),
						GerenciaContasActivity.class));
			}
		});

		banco.close();

		verificaRevogados(getApplicationContext());

	}

	public void abrirArquivo(String extensao, int acao) {
		Intent storeActivity = new Intent(getApplicationContext(),
				FileSelector.class);
		storeActivity.putExtra("ext", extensao);
		startActivityForResult(storeActivity, acao);

	}

	public void verificaRevogados(Context context) {
		int count = 0;
		int codigo = 0;

		Cursor c = getArrayCert(context);
		count = c.getCount();

		c.moveToFirst();

		if (count > 0) {

			for (int i = 0; i < count; i++) {
				codigo = c.getInt(0);
				X509Certificate cert = getCertificadoDecodificado(c
						.getString(1));

				try {
					cert.checkValidity();
				} catch (CertificateExpiredException e) {

				} catch (CertificateNotYetValidException e) {
					gravaCertificado(cert);
					deletaCertificado(codigo);
				}
				c.moveToNext();
			}
		}

		c.close();

	}

	public Cursor getArrayCert(Context context) {

		SQLiteOpenHelper banco = new Banco(context);
		SQLiteDatabase readableDatabase = banco.getReadableDatabase();
		Cursor query = readableDatabase.query(Certificados.NOME_TABELA,
				new String[] { Certificados._ID, Certificados.CERTIFICADO },
				null, null, null, null, null);
		startManagingCursor(query);
		readableDatabase.close();
		return query;
	}

	public static X509Certificate getCertificadoDecodificado(String certBase64) {

		byte certByte[];
		ByteArrayInputStream inStream;
		CertificateFactory cf;
		X509Certificate cert = null;

		certByte = Base64.decode(certBase64, Base64.DEFAULT);
		inStream = new ByteArrayInputStream(certByte);
		try {
			cf = CertificateFactory.getInstance("X.509");

			cert = (X509Certificate) cf.generateCertificate(inStream);
		} catch (CertificateException e) {
			e.printStackTrace();
		}
		try {
			inStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return cert;
	}

	public void gravaCertificado(X509Certificate cert) {
		String certEncoded = null;
		ContentValues content = new ContentValues();
		SQLiteOpenHelper bd = new Banco(this);
		SQLiteDatabase database = bd.getWritableDatabase();
		try {
			certEncoded = Base64.encodeToString(cert.getEncoded(),
					Base64.DEFAULT);

		} catch (CertificateEncodingException e) {
			e.printStackTrace();
		}

		content.put(CertificadosRevogados.CERTIFICADO, certEncoded);
		database.insert(Certificados.NOME_TABELA, null, content);
		database.close();
	}

	public void deletaCertificado(int codigo) {
		SQLiteOpenHelper bd = new Banco(this);
		SQLiteDatabase database = bd.getWritableDatabase();

		database.delete(CertificadosRevogados.NOME_TABELA,
				"_id" + "=" + codigo, null);
		database.close();
	}

}
