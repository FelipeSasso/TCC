package email.seguro.controle;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.Principal;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateFactory;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import email.seguro.R;
import email.seguro.banco.Banco;
import email.seguro.tabelas.Certificados;
import email.seguro.tabelas.CertificadosRevogados;
import email.seguro.util.FileSelector;
import email.seguro.util.Utils;

public class GerenciaCertificadosActivity extends ListActivity {

	private static final int IMPORTAR_CERTIFICADO = 1;
	private static final int SELECIONAR_CERTIFICADO = 2;
	private static final int EXCLUIR = 3;
	private static final int VISUALIZAR = 4;

	static String[] CERTIFICADOS;
	static int[] CODIGOS;

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);

		listar();

		setListAdapter(new ArrayAdapter<String>(this, R.layout.lista_contas,
				CERTIFICADOS));

		ListView lv = getListView();
		lv.setTextFilterEnabled(true);

		registerForContextMenu(lv);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {

		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
		menu.setHeaderTitle(CERTIFICADOS[info.position]);

		menu.add(Menu.NONE, EXCLUIR, EXCLUIR, "Excluir");
		menu.add(Menu.NONE, VISUALIZAR, VISUALIZAR, "Detalhes");

	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item
				.getMenuInfo();
		switch (item.getItemId()) {
		case EXCLUIR:
			confirmacaoDialogo("Deseja excluir esse certificado?",
					CODIGOS[info.position]);

			break;
		case VISUALIZAR:
			Intent storeActivity = new Intent(getApplicationContext(),
					InfoCertificadoActivity.class);

			storeActivity.putExtra("certificado",
					getCertificado(CODIGOS[info.position]));
			startActivity(storeActivity);
		}
		return true;
	}

	public int excluirCertificado(int _id) {
		SQLiteOpenHelper banco = new Banco(this);
		SQLiteDatabase write = banco.getWritableDatabase();
		int count = write.delete(Certificados.NOME_TABELA, Certificados._ID
				+ " = " + _id, null);
		return count;
	}

	public String getCertificado(int _id) {
		String certBase64 = "";
		SQLiteOpenHelper banco = new Banco(this);
		SQLiteDatabase readableDatabase = banco.getReadableDatabase();
		Cursor query = readableDatabase.query(Certificados.NOME_TABELA,
				new String[] { Certificados.CERTIFICADO }, Certificados._ID
						+ " = " + _id, null, null, null, null);

		if (query.getCount() > 0) {
			query.moveToFirst();
			certBase64 = query.getString(0);
		}

		return certBase64;

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, IMPORTAR_CERTIFICADO, 0, "Importar Certificado");
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		String extension;
		switch (item.getItemId()) {

		case IMPORTAR_CERTIFICADO:
			Intent storeActivity = new Intent(getApplicationContext(),
					FileSelector.class);
			extension = "cer,crt,pem";
			storeActivity.putExtra("ext", extension);
			startActivityForResult(storeActivity, SELECIONAR_CERTIFICADO);
			break;
		default:
			return super.onOptionsItemSelected(item);
		}
		return true;
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case SELECIONAR_CERTIFICADO:
			if (resultCode == RESULT_OK) {
				String fileSelected = data.getStringExtra("file");
				try {
					carregaCertificado(fileSelected);
				} catch (CertificateException e) {
					mensagem("O certificado selecionado não é válido!");					
					e.printStackTrace();
				} catch (IOException e) {
					mensagem("Ocorreu um erro ao abrir o arquivo");
					e.printStackTrace();
				}
				listar();
			}
			break;
		}
	}

	public boolean carregaCertificado(String caminhoDoArquivo)
			throws CertificateException, IOException {
		InputStream inStream;
		CertificateFactory cf;
		X509Certificate cert = null;

		inStream = new FileInputStream(caminhoDoArquivo);
		cf = CertificateFactory.getInstance("X.509");
		cert = (X509Certificate) cf.generateCertificate(inStream);
		inStream.close();

		try {
			cert.checkValidity();
		} catch (CertificateExpiredException e) {
			mensagem("Certificado está com a data de validade expirada, e será armazenado em Certificados Revogados");
			gravaCertificadoRevogado(cert);
			return false;
		} catch (CertificateNotYetValidException e) {

		}

		gravaCertificado(cert);
		return true;
	}

	public void gravaCertificadoRevogado(X509Certificate cert) {
		String certEncoded = null;
		ContentValues content = new ContentValues();
		SQLiteOpenHelper bd = new Banco(this);
		SQLiteDatabase database = bd.getWritableDatabase();

		try {
			certEncoded = Base64.encodeToString(cert.getEncoded(),
					Base64.DEFAULT);

			content.put(CertificadosRevogados.CERTIFICADO, certEncoded);
			database.insert(CertificadosRevogados.NOME_TABELA, null, content);

		} catch (CertificateEncodingException e) {
			mensagem("Ocorreu um erro ao gravar o certificado!");
			e.printStackTrace();
		}

	}

	public void gravaCertificado(X509Certificate cert) {
		String certEncoded = null;
		String email = "";
		ContentValues content = new ContentValues();
		SQLiteOpenHelper bd = new Banco(this);
		SQLiteDatabase database = bd.getWritableDatabase();
		try {
			certEncoded = Base64.encodeToString(cert.getEncoded(),
					Base64.DEFAULT);

		} catch (CertificateEncodingException e) {
			mensagem("Ocorreu um erro ao gravar o certificado!");
			e.printStackTrace();
		}

		try {
			email = getEmail(cert);
		} catch (CertificateParsingException e) {
			Log.i("ERRO", e.getMessage());
		}

		content.put(Certificados.CERTIFICADO, certEncoded);		
		content.put(Certificados.EMAIL, email);
		database.insert(Certificados.NOME_TABELA, null, content);
	}

	private static String getEmail(X509Certificate certificate)
			throws CertificateParsingException {
		Principal principal = certificate.getSubjectDN();

		if (principal == null) {
			return null;
		}

		String dn = principal.getName();
		if (dn != null) {
			StringTokenizer tokenizer = new StringTokenizer(dn, ",");
			String token = null;
			while (tokenizer.hasMoreTokens()) {

				token = (String) tokenizer.nextToken();

				if (token.toLowerCase().startsWith("emailaddress=")) {
					int len = "emailaddress=".length();

					if (token.length() >= len) {
						return token.substring(len).toLowerCase().trim();
					}
				}

				if (token.toLowerCase().startsWith("1.2.840.113549.1.9.1=#")) {
					int len = "1.2.840.113549.1.9.1=#".length();

					if (token.length() >= len) {
						return Utils.convertHexToString(
								token.substring(len).toLowerCase()).trim();
					}
				}
			}
		}
		  Collection<List<?>> subjectAlternativeNames = certificate.getSubjectAlternativeNames();
	        if (subjectAlternativeNames == null) {
	            return "Sem e-mail";
	        }
	        Iterator<List<?>> iterator = subjectAlternativeNames.iterator();

	        while (iterator.hasNext()) {
	            List<?> next = iterator.next();

	            for (int i = 0; i < next.size(); i++) {
	                Object get = next.get(i);

	                if ("1".equals((String) get.toString())) {
	                    return next.get(i + 1).toString().trim();
	                }
	            }
	        }

	        return "Sem e-mail";
	}

	private void listar() {
		int count;
		Cursor arrayContas = getArrayCert();
		startManagingCursor(arrayContas);
		count = arrayContas.getCount();
		CERTIFICADOS = new String[count];
		CODIGOS = new int[count];
		arrayContas.moveToFirst();
		for (int i = 0; i < count; i++) {

			String cn = Utils.getCN(Utils.decodificaCertificado(arrayContas
					.getString(1)));

			CERTIFICADOS[i] = cn;
			CODIGOS[i] = arrayContas.getInt(0);
			arrayContas.moveToNext();
		}
		arrayContas.close();
		setListAdapter(new ArrayAdapter<String>(this, R.layout.lista_contas,
				CERTIFICADOS));

	}

	public void confirmacaoDialogo(String mensagem, final int _id) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(mensagem)
				.setCancelable(false)
				.setPositiveButton("Sim",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								int count = excluirCertificado(_id);
								if (count > 0) {
									mensagem("Excluído com sucesso.");
									listar();
								}
							}
						})
				.setNegativeButton("Não",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								return;
							}
						});
		AlertDialog alert = builder.create();
		alert.show();
	}

	public Cursor getArrayCert() {

		SQLiteOpenHelper banco = new Banco(this);
		SQLiteDatabase readableDatabase = banco.getReadableDatabase();
		Cursor query = readableDatabase.query(Certificados.NOME_TABELA,
				new String[] { Certificados._ID, Certificados.CERTIFICADO },
				null, null, null, null, null);
		startManagingCursor(query);		
		return query;
	}

	private void mensagem(String mensagem) {
		Toast toast = Toast.makeText(getApplicationContext(), mensagem,
				Toast.LENGTH_SHORT);
		toast.show();
	}

}
