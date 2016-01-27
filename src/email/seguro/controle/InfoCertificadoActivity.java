package email.seguro.controle;

import java.io.ByteArrayInputStream;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.ExpandableListActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

public class InfoCertificadoActivity extends ExpandableListActivity {

	ExpandableListAdapter mAdapter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Bundle extras = getIntent().getExtras();

		String certBase64 = null;

		if (extras != null) {
			certBase64 = extras.getString("certificado");
		} else {
			finish();
		}

		X509Certificate cert = constroiCert(Base64.decode(certBase64,
				Base64.DEFAULT));

		mAdapter = new ExpandableListAdapter(cert);
		setListAdapter(mAdapter);
		registerForContextMenu(getExpandableListView());
	}

	public X509Certificate constroiCert(byte certByte[]) {

		ByteArrayInputStream inStream;
		CertificateFactory cf;
		X509Certificate cert = null;
		try {
			inStream = new ByteArrayInputStream(certByte);
			cf = CertificateFactory.getInstance("X.509");
			cert = (X509Certificate) cf.generateCertificate(inStream);
			inStream.close();
		} catch (Exception e) {
			Log.i("CERT", e.getMessage());
		}

		return cert;

	}

	public class ExpandableListAdapter extends BaseExpandableListAdapter {
		private String[] groups = { "Versão", "Número de Série",
				"Algorítmo de Assinatura", "Emissor", "Válido a partir de",
				"Válido até", "Requerente" };
		private String[][] children;

		public ExpandableListAdapter(X509Certificate cert) {
			String aux[][] = { { String.valueOf(cert.getVersion()) },
					{ String.valueOf(cert.getSerialNumber()) },
					{ String.valueOf(cert.getSigAlgName()) },
					{ String.valueOf(cert.getIssuerX500Principal()) },
					{ getData(cert.getNotBefore()) },
					{ getData(cert.getNotAfter()) },
					{ String.valueOf(cert.getSubjectDN()) }, };

			children = aux.clone();
		}

		public String getData(Date date) {

			String dataFormat = new SimpleDateFormat("hh:mm.ss - dd/MM/yyyy")
					.format(date);

			return dataFormat;
		}

		public Object getChild(int groupPosition, int childPosition) {
			return children[groupPosition][childPosition];
		}

		public long getChildId(int groupPosition, int childPosition) {
			return childPosition;
		}

		public int getChildrenCount(int groupPosition) {
			return children[groupPosition].length;
		}

		public TextView getGenericView() {

			AbsListView.LayoutParams lp = new AbsListView.LayoutParams(
					ViewGroup.LayoutParams.MATCH_PARENT, 64);

			TextView textView = new TextView(InfoCertificadoActivity.this);
			textView.setLayoutParams(lp);

			textView.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);
			textView.setPadding(36, 0, 0, 0);
			return textView;
		}

		public View getChildView(int groupPosition, int childPosition,
				boolean isLastChild, View convertView, ViewGroup parent) {
			TextView textView = getGenericView();
			textView.setText(getChild(groupPosition, childPosition).toString());
			return textView;
		}

		public Object getGroup(int groupPosition) {
			return groups[groupPosition];
		}

		public int getGroupCount() {
			return groups.length;
		}

		public long getGroupId(int groupPosition) {
			return groupPosition;
		}

		public View getGroupView(int groupPosition, boolean isExpanded,
				View convertView, ViewGroup parent) {
			TextView textView = getGenericView();
			textView.setText(getGroup(groupPosition).toString());
			return textView;
		}

		public boolean isChildSelectable(int groupPosition, int childPosition) {
			return true;
		}

		public boolean hasStableIds() {
			return true;
		}

	}
}