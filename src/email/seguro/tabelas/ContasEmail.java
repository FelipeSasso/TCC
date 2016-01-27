package email.seguro.tabelas;

import android.provider.BaseColumns;

public class ContasEmail implements BaseColumns {

	public static final String NOME_TABELA = "emails";
	public static final String _ID_tipo = "INTEGER PRIMARY KEY AUTOINCREMENT";
	public static final String NOME = "nome";
	public static final String NOME_tipo = "TEXT";
	public static final String EMAIL = "email";
	public static final String EMAIL_tipo = "TEXT";	
	public static final String SENHA = "senha";
	public static final String SENHA_tipo = "TEXT";
	public static final String CHAVE_PRIVADA = "chave_privada";
	public static final String CHAVE_PRIVADA_tipo = "TEXT";
	public static final String CERTIFICADO = "certificado";
	public static final String CERTIFICADO_tipo = "TEXT";
	
}
