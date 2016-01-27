package email.seguro.controle;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Enumeration;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import email.seguro.R;
import email.seguro.banco.Banco;
import email.seguro.tabelas.ContasEmail;
import email.seguro.util.FileSelector;
import email.seguro.util.Utils;

public class ContasActivity extends Activity {

	private static SQLiteDatabase DATABASE = null;
	private SQLiteOpenHelper helper;
	private Button btnSalvar;
	private Button btnCancelar;
	private Button btnBrowser;
	private TextView txtVwEmail;
	private TextView txtVwNome;
	private TextView txtVwSenha;
	private TextView txtVwCertificado;
	private int _id;
	private String email;
	private String nome;
	private String senha;
	private String chavePrivada;
	private String certificado;
	private static final int ADICIONAR = 3;
	private static final int EDITAR = 1;
	private final int SELECIONAR_CHAVE = 1;
	private final int DIALOG_SENHA = 4;

	private String caminhoArquivo;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		setContentView(R.layout.cadastro_conta);

		final int codigo_opcao[];
		final int opcao;
		super.onCreate(savedInstanceState);

		Bundle extras = getIntent().getExtras();

		if (extras != null) {
			codigo_opcao = extras.getIntArray("opcao");
		} else {
			codigo_opcao = new int[] { -1, -1 };

		}

		iniciaComponentes();

		_id = codigo_opcao[0];
		opcao = codigo_opcao[1];

		if (opcao == EDITAR) {
			carregaDadosEdicao(_id);
		}

		btnSalvar.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				getInformacoes();
				if (!validaInformacoes()) {
					return;
				}
				switch (opcao) {
				case ADICIONAR:
					salvarNovo();
					mensagem("Adicionado com sucesso.");
					Intent intent = new Intent();
					setResult(Activity.RESULT_OK, intent);
					finish();
					break;
				case EDITAR:
					salvarAtualizar();
					mensagem("Salvo com sucesso.");
					Intent intentSalvo = new Intent();
					setResult(Activity.RESULT_OK, intentSalvo);
					finish();
					break;
				}
			}
		});

		btnCancelar.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				Intent intentSalvo = new Intent();
				setResult(Activity.RESULT_CANCELED, intentSalvo);
				finish();
			}
		});

		btnBrowser.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				selecionaChave();
			}
		});
	}

	private void selecionaChave() {
		String extension;
		Intent storeActivity = new Intent(getApplicationContext(),
				FileSelector.class);
		extension = "p12,pfx";
		storeActivity.putExtra("ext", extension);
		startActivityForResult(storeActivity, SELECIONAR_CHAVE);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case SELECIONAR_CHAVE:
			if (resultCode == RESULT_OK) {
				caminhoArquivo = data.getStringExtra("file");
				showDialog(DIALOG_SENHA);
			}
			break;
		}
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		AlertDialog.Builder alert = new AlertDialog.Builder(this);
		switch (id) {
		case DIALOG_SENHA:
			alert.setTitle("Senha");
			LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View view = inflater.inflate(R.layout.pass_phrase, null);
			final EditText txtSenhaCert = (EditText) view
					.findViewById(R.id.senhaCertificado);
			final EditText txtSenhaChave = (EditText) view
					.findViewById(R.id.senhaChave);
			final EditText txtSenhaChaveRpt = (EditText) view
					.findViewById(R.id.senhaChaveRpt);
			alert.setView(view);

			alert.setPositiveButton(android.R.string.ok,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							String senhaCerti = "" + txtSenhaCert.getText();
							String senhaChave = "" + txtSenhaChave.getText();
							String senhaChaveRpt = ""
									+ txtSenhaChaveRpt.getText();
							if (!senhaChave.equals(senhaChaveRpt)) {
								mensagem("Senhas não são iguais.");
								txtSenhaCert.setText("");
								txtSenhaChave.setText("");
								txtSenhaChaveRpt.setText("");
								return;
							}

							if (senhaCerti.equals("")) {
								mensagem("Favor informar a senha.");
								txtSenhaCert.setText("");
								txtSenhaChave.setText("");
								txtSenhaChaveRpt.setText("");
								return;
							}
							try {
								carregaKeyStore(senhaCerti, senhaChave);
							} catch (KeyStoreException e) {
								mensagem("1" + e.getMessage());
							} catch (UnrecoverableKeyException e) {
								mensagem("2" + e.getMessage());
							} catch (NoSuchAlgorithmException e) {
								mensagem("3" + e.getMessage());
							} catch (InvalidKeyException e) {
								mensagem("4" + e.getMessage());
							} catch (NoSuchPaddingException e) {
								mensagem("5" + e.getMessage());
							} catch (IllegalBlockSizeException e) {
								mensagem("6" + e.getMessage());
							} catch (BadPaddingException e) {
								mensagem("7" + e.getMessage());
							} catch (CertificateEncodingException e) {
								mensagem("8" + e.getMessage());
							} catch (CertificateException e) {
								mensagem("9" + e.getMessage());
							} catch (IOException e) {
								mensagem("Senha inválida para o certificado importado.");
								txtSenhaCert.setText("");
								txtSenhaChave.setText("");
								txtSenhaChaveRpt.setText("");
							}
						}
					});

			alert.setNegativeButton(android.R.string.cancel,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							// removeDialog(Id.dialog.new_pass_phrase);
						}
					});
			return alert.create();
		default: {
			return super.onCreateDialog(id);
		}
		}

	}

	public void carregaKeyStore(String senhaCert, String senhaChave)
			throws KeyStoreException, UnrecoverableKeyException,
			NoSuchAlgorithmException, InvalidKeyException,
			NoSuchPaddingException, IllegalBlockSizeException,
			BadPaddingException, CertificateException, IOException {
		Key chave = null;
		KeyStore ks = KeyStore.getInstance("PKCS12");
		FileInputStream fileInput = null;

		fileInput = new FileInputStream(caminhoArquivo);

		ks.load(fileInput, senhaCert.toCharArray());

		Enumeration<String> aliases = ks.aliases();
		X509Certificate cert;

		String alias = aliases.nextElement();
		chave = ks.getKey(alias, senhaCert.toCharArray());
		cert = (X509Certificate) ks.getCertificate(alias);
		certificado = Base64.encodeToString(cert.getEncoded(), Base64.DEFAULT);
		if (certificado != null) {
			txtVwCertificado.setText(Utils.getCN(cert));
		}
		chavePrivada = Utils.codificaChave(senhaChave, chave);

	}

	public void carregaDadosEdicao(int codigo) {

		SQLiteOpenHelper banco = new Banco(this);
		SQLiteDatabase readableDatabase = banco.getReadableDatabase();
		Cursor query = readableDatabase.query(ContasEmail.NOME_TABELA,
				new String[] { ContasEmail.EMAIL, ContasEmail.NOME,
						ContasEmail.SENHA, ContasEmail.CERTIFICADO,
						ContasEmail.CHAVE_PRIVADA }, ContasEmail._ID + " = "
						+ codigo, null, null, null, null);

		query.moveToFirst();

		txtVwEmail.setText(query.getString(0));
		txtVwNome.setText(query.getString(1));
		txtVwSenha.setText(query.getString(2));
		certificado = query.getString(3);
		if (certificado != null) {
			txtVwCertificado.setText(Utils.getCN(Utils
					.decodificaCertificado(certificado)));
		}
		chavePrivada = query.getString(4);
	}

	public void iniciaComponentes() {
		btnSalvar = (Button) findViewById(R.id.btn_salvar);
		btnCancelar = (Button) findViewById(R.id.btn_cancelar);
		btnBrowser = (Button) findViewById(R.id.btn_browseChave);

		txtVwEmail = (TextView) findViewById(R.id.txtEmail);
		txtVwNome = (TextView) findViewById(R.id.txtNome);
		txtVwSenha = (TextView) findViewById(R.id.txtSenha);
		txtVwCertificado = (TextView) findViewById(R.id.txtChavePrivada);
	}

	private boolean validaInformacoes() {

		if (!Utils.validaEmail(email)) {
			mensagem("Favor informar um e-mail válido.");
			txtVwEmail.requestFocus();
			return false;
		}

		if (nome.length() <= 0) {
			mensagem("Favor informar um nome.");
			txtVwNome.requestFocus();
			return false;
		}

		if (senha.length() < 1) {
			mensagem("Informe a senha!");
			return false;
		}

		return true;
	}

	private void mensagem(String mensagem) {
		Context context = getApplicationContext();
		Toast toast = Toast.makeText(context, mensagem, Toast.LENGTH_SHORT);
		toast.show();
	}

	public void getInformacoes() {
		email = txtVwEmail.getText().toString();
		nome = txtVwNome.getText().toString();
		senha = txtVwSenha.getText().toString();
	}

	public void salvarNovo() {
		ContentValues values = new ContentValues();
		helper = new Banco(getApplicationContext());
		DATABASE = helper.getWritableDatabase();
		values.put(ContasEmail.EMAIL, email.toLowerCase());
		values.put(ContasEmail.NOME, nome);
		values.put(ContasEmail.SENHA, senha.toLowerCase());
		values.put(ContasEmail.CHAVE_PRIVADA, chavePrivada);
		values.put(ContasEmail.CERTIFICADO, certificado);
		DATABASE.insert(ContasEmail.NOME_TABELA, null, values);
	}

	public void salvarAtualizar() {
		ContentValues values = new ContentValues();
		helper = new Banco(getApplicationContext());
		DATABASE = helper.getWritableDatabase();
		values.put(ContasEmail.EMAIL, email.toLowerCase());
		values.put(ContasEmail.NOME, nome);
		values.put(ContasEmail.SENHA, senha.toLowerCase());
		values.put(ContasEmail.CHAVE_PRIVADA, chavePrivada);
		values.put(ContasEmail.CERTIFICADO, certificado);
		DATABASE.update(ContasEmail.NOME_TABELA, values, ContasEmail._ID + "="
				+ _id, null);
	}

}
