package email.seguro.email;

import java.io.Serializable;

public class Email implements Serializable {

	private static final long serialVersionUID = 1L;
	private String _body;
	private String _to[];
	private String _from;
	private String _subject;	
	private boolean _assina;
	private boolean _cifra;

	public boolean isAssina() {
		return _assina;
	}

	public void setAssina(boolean _assina) {
		this._assina = _assina;
	}

	public boolean isCifra() {
		return _cifra;
	}

	public void setCifra(boolean _cifra) {
		this._cifra = _cifra;
	}

	public String getBody() {
		return _body;
	}

	public void setBody(String _body) {
		this._body = _body;
	}

	public String[] getTo() {
		return _to;
	}

	public void setTo(String[] _to) {
		this._to = _to;
	}

	public String getFrom() {
		return _from;
	}

	public void setFrom(String _from) {
		this._from = _from;
	}

	public String getSubject() {
		return _subject;
	}

	public void setSubject(String _subject) {
		this._subject = _subject;
	}

}
