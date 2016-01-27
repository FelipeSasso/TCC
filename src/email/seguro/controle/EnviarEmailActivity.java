package email.seguro.controle;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import email.seguro.R;
import email.seguro.banco.Banco;
import email.seguro.email.Email;
import email.seguro.email.EmailActivity;
import email.seguro.tabelas.ContasEmail;

public class EnviarEmailActivity extends Activity {
	String emailOrigem;
	String nome;
	String senha;

	Button btnEnviar;
	Button btnCancelar;

	CheckBox chAssinar;
	CheckBox chCriptografar;

	Spinner spinner;
	private int CODIGOS[];

	private static final int ENVIAR_EMAIL = 1;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.enviar_email);

		btnEnviar = (Button) findViewById(R.id.btn_enviar);
		btnCancelar = (Button) findViewById(R.id.btn_cancelar);

		btnEnviar.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// if (verificaConexao(getApplicationContext())) {
				enviarEmail();
				// } else {
				// mensagem("Não existe conexão com a internet!");

				// }

			}
		});

		btnCancelar.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				cancelar();
			}
		});

		chAssinar = (CheckBox) findViewById(R.id.checkAssinar);
		chAssinar.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (chAssinar.isChecked()) {
					chCriptografar.setChecked(false);
				}

			}
		});
		chCriptografar = (CheckBox) findViewById(R.id.checkCriptografar);
		chCriptografar.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (chCriptografar.isChecked()) {
					chAssinar.setChecked(false);
				}
			}
		});

		spinner = (Spinner) findViewById(R.id.spinEmail);

		String contas[] = carregarContas();

		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, contas);

		adapter.setDropDownViewResource(android.R.layout.select_dialog_item);
		spinner.setAdapter(adapter);

	}

	public boolean enviarEmail() {
		EditText txtDestino;
		EditText txtAssunto;
		EditText txtMensagem;
		String assunto;
		String destinatarios;
		String mensagem;

		txtDestino = (EditText) findViewById(R.id.txtDestino);
		txtAssunto = (EditText) findViewById(R.id.txtAssunto);
		txtMensagem = (EditText) findViewById(R.id.txtMensagem);

		if (txtDestino.getText().toString().trim().isEmpty()) {
			mensagem("Informe um destinatário.");
			txtDestino.requestFocus();
			return false;
		}

		assunto = txtAssunto.getText().toString();
		destinatarios = txtDestino.getText().toString();
		mensagem = txtMensagem.getText().toString();

		configConta(CODIGOS[(int) spinner.getSelectedItemId()]);
		emailOrigem = (String) spinner.getSelectedItem();

		final Email email = new Email();

		String[] toArr = destinatarios.split(",");
		email.setTo(toArr);
		email.setFrom("\"" + nome + "\"" + "<" + emailOrigem + ">");
		email.setSubject(assunto);
		email.setBody(mensagem);
		email.setAssina(chAssinar.isChecked());
		email.setCifra(chCriptografar.isChecked());

		Intent storeActivity = new Intent(getApplicationContext(),
				EmailActivity.class);
		storeActivity.putExtra("user", emailOrigem);
		storeActivity.putExtra("senha", senha);
		storeActivity.putExtra("email", email);
		storeActivity.putExtra("id",
				String.valueOf(CODIGOS[(int) spinner.getSelectedItemId()]));
		startActivityForResult(storeActivity, ENVIAR_EMAIL);
		return true;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case ENVIAR_EMAIL:
			if (resultCode == Activity.RESULT_OK) {
				mensagem("Email enviado com sucesso.");
				finish();
			}
			break;
		}

	}

	public String[] carregarContas() {
		int count;
		SQLiteOpenHelper banco = new Banco(this);
		SQLiteDatabase datab = banco.getReadableDatabase();

		Cursor cursor = datab.query(ContasEmail.NOME_TABELA, new String[] {
				ContasEmail._ID, ContasEmail.EMAIL }, null, null, null, null,
				null);
		count = cursor.getCount();

		if (count <= 0) {
			mensagem("É necessário configurar uma conta para envio!");
			cancelar();
		}

		cursor.moveToFirst();
		String contas[] = new String[count];
		CODIGOS = new int[count];

		for (int i = 0; i < count; i++) {
			CODIGOS[i] = cursor.getInt(0);
			contas[i] = cursor.getString(1);
			cursor.moveToNext();
		}

		datab.close();
		cursor.close();

		return contas;
	}

	public void configConta(final int _id) {
		SQLiteOpenHelper banco = new Banco(this);
		SQLiteDatabase datab = banco.getReadableDatabase();
		Cursor cursor = datab.query(ContasEmail.NOME_TABELA, new String[] {
				ContasEmail.NOME, ContasEmail.SENHA }, ContasEmail._ID + " = "
				+ _id, null, null, null, null);

		if (cursor.getCount() > 0) {
			cursor.moveToFirst();

			nome = cursor.getString(0);
			senha = cursor.getString(1);
		}

		datab.close();
		cursor.close();
	}

	private void cancelar() {
		Intent intentSalvo = new Intent();
		setResult(Activity.RESULT_CANCELED, intentSalvo);
		finish();
	}

	private void mensagem(String mensagem) {
		Context context = getApplicationContext();
		Toast toast = Toast.makeText(context, mensagem, Toast.LENGTH_SHORT);
		toast.show();
	}

	public boolean verificaConexao(Context context) {
		try {
			ConnectivityManager cm = (ConnectivityManager) context
					.getSystemService(Context.CONNECTIVITY_SERVICE);

			if (cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE)
					.isConnected()
					|| cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
							.isConnected()) {
				return true;
			} else {

				return false;
			}
		} catch (Exception e) {
			return false;
		}

	}

}
