package email.seguro.tabelas;

import android.provider.BaseColumns;

public class Certificados implements BaseColumns 
{
	public static final String NOME_TABELA = "certificados";
	public static final String _ID_tipo = "INTEGER PRIMARY KEY AUTOINCREMENT";
	public static final String CERTIFICADO = "certificado";
    public static final String CERTIFICADO_tipo = "TEXT";
    public static final String EMAIL = "email";
    public static final String EMAIL_tipo = "TEXT";
}
