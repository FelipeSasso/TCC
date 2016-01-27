package email.seguro.controle;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
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
import email.seguro.tabelas.ContasEmail;

public class GerenciaContasActivity extends ListActivity {

	private static final int EDITAR = 1;
	private static final int EXCLUIR = 2;
	private static final int ADICIONAR = 3;

	static String[] CONTAS;
	static int[] CODIGOS;

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);

		listar();

		setListAdapter(new ArrayAdapter<String>(this, R.layout.lista_contas,
				CONTAS));

		ListView lv = getListView();
		lv.setTextFilterEnabled(true);

		registerForContextMenu(lv);

	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {

		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
		menu.setHeaderTitle(CONTAS[info.position]);

		menu.add(Menu.NONE, EDITAR, EDITAR, "Editar");
		menu.add(Menu.NONE, EXCLUIR, EXCLUIR, "Excluir");

	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item
				.getMenuInfo();
		switch (item.getItemId()) {
		case EDITAR:
			editarConta(CODIGOS[info.position]);
			break;
		case EXCLUIR:
			confirmacaoDialogo("Deseja excluir essa conta?",
					CODIGOS[info.position]);

			break;
		}
		return true;
	}

	public Cursor getArrayContas() {

		SQLiteOpenHelper banco = new Banco(this);
		SQLiteDatabase readableDatabase = banco.getReadableDatabase();
		Cursor query = readableDatabase.query(ContasEmail.NOME_TABELA,
				new String[] { ContasEmail._ID, ContasEmail.EMAIL }, null,
				null, null, null, null);
		
		//banco.close();		
		return query;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, ADICIONAR, 0, "Adicionar");
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {

		case ADICIONAR:
			adicionarConta();
			break;
		default:
			return super.onOptionsItemSelected(item);
		}
		return true;
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == ADICIONAR) {
			if (resultCode == Activity.RESULT_OK) {
				listar();
			}
		}
	}

	private void listar() {
		int count;
		Cursor arrayContas = getArrayContas();
		count = arrayContas.getCount();
		CONTAS = new String[count];
		CODIGOS = new int[count];
		arrayContas.moveToFirst();
		for (int i = 0; i < count; i++) {
			CONTAS[i] = arrayContas.getString(1);
			CODIGOS[i] = arrayContas.getInt(0);
			arrayContas.moveToNext();
		}
		setListAdapter(new ArrayAdapter<String>(this, R.layout.lista_contas,
				CONTAS));

	}

	public void adicionarConta() {
		Intent storeActivity = new Intent(getApplicationContext(),
				ContasActivity.class);

		int codigo_opcao[] = new int[] { -1, ADICIONAR };

		storeActivity.putExtra("opcao", codigo_opcao);
		startActivityForResult(storeActivity, ADICIONAR);
	}

	public void editarConta(int _id) {
		int codigo_opcao[] = new int[] { _id, EDITAR };

		Intent storeActivity = new Intent(getApplicationContext(),
				ContasActivity.class);

		storeActivity.putExtra("opcao", codigo_opcao);
		startActivityForResult(storeActivity, EDITAR);
	}

	public int excluirConta(int _id) {
		SQLiteOpenHelper banco = new Banco(this);
		SQLiteDatabase write = banco.getWritableDatabase();
		int count = write.delete(ContasEmail.NOME_TABELA, ContasEmail._ID
				+ " = " + _id, null);
		return count;
	}

	public void confirmacaoDialogo(String mensagem, final int _id) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(mensagem)
				.setCancelable(false)
				.setPositiveButton("Sim",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								int count = excluirConta(_id);							
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

	private void mensagem(String mensagem) {
		Context context = getApplicationContext();
		Toast toast = Toast.makeText(context, mensagem, Toast.LENGTH_SHORT);
		toast.show();
	}
}
