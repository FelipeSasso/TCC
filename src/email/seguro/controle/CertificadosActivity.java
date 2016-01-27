package email.seguro.controle;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import email.seguro.R;

public class CertificadosActivity extends Activity {

	private Button btnCertificados;
	private Button btnCertificadosRevogados;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.certificados);

		btnCertificados = (Button) findViewById(R.id.btnCertificados);
		btnCertificadosRevogados = (Button) findViewById(R.id.btnCertificadosRevogados);

		btnCertificados.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				startActivity(new Intent(getApplicationContext(),
						GerenciaCertificadosActivity.class));
			}
		});
		btnCertificadosRevogados.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				startActivity(new Intent(getApplicationContext(),
						GerenciaCertificadosRevogadosActivity.class));

			}
		});
	}

}
