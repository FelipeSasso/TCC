package email.seguro.banco;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import email.seguro.tabelas.Certificados;
import email.seguro.tabelas.CertificadosRevogados;
import email.seguro.tabelas.ContasEmail;

public class Banco extends SQLiteOpenHelper {

	private static final int BD_VERSAO = 1;
	public static final String BD_NOME = "database";

	public Banco(Context context) {
		super(context, BD_NOME, null, BD_VERSAO);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {

		db.execSQL("CREATE TABLE IF NOT EXISTS " + ContasEmail.NOME_TABELA
				+ " (" + ContasEmail._ID + " " + ContasEmail._ID_tipo + ","
				+ ContasEmail.EMAIL + " " + ContasEmail.EMAIL_tipo + ","
				+ ContasEmail.NOME + " " + ContasEmail.NOME_tipo + ","
				+ ContasEmail.SENHA + " " + ContasEmail.SENHA_tipo + ","
				+ ContasEmail.CHAVE_PRIVADA + " "
				+ ContasEmail.CHAVE_PRIVADA_tipo + ","
				+ ContasEmail.CERTIFICADO + " " + ContasEmail.CERTIFICADO_tipo
				+ ");");

		db.execSQL("CREATE TABLE IF NOT EXISTS " + Certificados.NOME_TABELA
				+ " (" + Certificados._ID + " " + Certificados._ID_tipo + ","
				+ Certificados.CERTIFICADO + " "
				+ Certificados.CERTIFICADO_tipo + "," + Certificados.EMAIL
				+ " " + Certificados.EMAIL_tipo + ");");	

		db.execSQL("CREATE TABLE IF NOT EXISTS "
				+ CertificadosRevogados.NOME_TABELA + " ("
				+ CertificadosRevogados._ID + " "
				+ CertificadosRevogados._ID_tipo + ","
				+ CertificadosRevogados.CERTIFICADO + " "
				+ CertificadosRevogados.CERTIFICADO_tipo + ");");
	}
}
