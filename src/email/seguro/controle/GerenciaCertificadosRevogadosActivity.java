package email.seguro.controle;

import android.app.AlertDialog;
import android.app.ListActivity;
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
import email.seguro.tabelas.CertificadosRevogados;
import email.seguro.util.Utils;

public class GerenciaCertificadosRevogadosActivity extends ListActivity {
	
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
		int count = write.delete(CertificadosRevogados.NOME_TABELA,
				CertificadosRevogados._ID + " = " + _id, null);
		return count;
	}

	public String getCertificado(int _id) {
		String certBase64 = "";
		SQLiteOpenHelper banco = new Banco(this);
		SQLiteDatabase readableDatabase = banco.getReadableDatabase();
		Cursor query = readableDatabase
				.query(CertificadosRevogados.NOME_TABELA,
						new String[] { CertificadosRevogados.CERTIFICADO },
						CertificadosRevogados._ID + " = " + _id, null, null,
						null, null);

		if (query.getCount() > 0) {
			query.moveToFirst();
			certBase64 = query.getString(0);
		}

		return certBase64;

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
		arrayContas.close();
		

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
		Cursor query = readableDatabase.query(
				CertificadosRevogados.NOME_TABELA, new String[] {
						CertificadosRevogados._ID,
						CertificadosRevogados.CERTIFICADO }, null, null, null,
				null, null);
		startManagingCursor(query);
		return query;
	}

	private void mensagem(String mensagem) {
		Toast toast = Toast.makeText(getApplicationContext(), mensagem,
				Toast.LENGTH_SHORT);
		toast.show();
	}
	
}
