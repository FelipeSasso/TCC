package email.seguro.util;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import email.seguro.R;
import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Collections;

public class FileSelector extends Activity {

	private String selectedFile = null;
	private String rootDir = Environment.getExternalStorageDirectory()
			.getAbsolutePath();
	private String currentDir = null;
	private String[] extFilter;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.fileopen);

		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			extFilter = extras.getString("ext").split(",");
		} else {
			extFilter = null;
		}
		draw(rootDir);
	}

	private void draw(String dir) {
		ListView lv = (ListView) findViewById(R.id.listOpenFile);
		TextView tv = (TextView) findViewById(R.id.textOpenFile);

		tv.setText(dir);
		currentDir = dir;
		File currentFile = new File(dir);
		File[] fileList = currentFile.listFiles(filter);

		ArrayList<String> fileStringList = new ArrayList<String>();
		final ArrayList<String> dirStringList = new ArrayList<String>();
		final ArrayList<String> allStringList = new ArrayList<String>();

		for (File f : fileList) {
			if (f.isDirectory()) {
				dirStringList.add(f.getName());
			}
			if (f.isFile()) {
				fileStringList.add(f.getName());
			}
		}

		Collections.sort(fileStringList);
		Collections.sort(dirStringList);
		if (!dir.equals(rootDir)) {
			allStringList.add("..");
		}
		allStringList.addAll(dirStringList);
		allStringList.addAll(fileStringList);

		ArrayAdapter<String> fileListAdapter = new ArrayAdapter<String>(
				getBaseContext(), R.layout.listview_item, allStringList);
		lv.setAdapter(fileListAdapter);

		lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> adapterView, View view,
					int position, long id) {
				String selection = allStringList.get(position);
				if (dirStringList.contains(selection)) {
					draw(currentDir + "/" + selection);
				} else if ((selection.equalsIgnoreCase("..") && (!currentDir
						.equalsIgnoreCase(rootDir)))) {
					draw(currentDir.substring(0, currentDir.lastIndexOf("/")));
				} else {
					selectedFile = currentDir + "/" + selection;
					exit();
				}

			}
		});
	}

	private void exit() {
		Intent intent = new Intent();
		if (selectedFile != null) {
			intent.putExtra("file", selectedFile);
			Log.i("FileSelector", "Returning Filepath: " + selectedFile);
			setResult(Activity.RESULT_OK, intent);
		} else {
			setResult(Activity.RESULT_CANCELED, intent);
		}
		finish();
	}

	FileFilter filter = new FileFilter() {

		@Override
		public boolean accept(File pathname) {
			String[] supportedFileExtensions = extFilter;
			if (pathname.isHidden()) {
				return false;
			}
			if (!pathname.canRead()) {
				return false;
			}

			if (pathname.isDirectory()) {
				return true;
			}

			if (supportedFileExtensions == null) {
				return true;
			}

			String fileName = pathname.getName();
			String fileExtension;
			int mid = fileName.lastIndexOf(".");
			fileExtension = fileName.substring(mid + 1, fileName.length());
			for (String s : supportedFileExtensions) {
				if (s.contentEquals(fileExtension)) {
					return true;
				}
			}
			return false;
		}
	};

	@Override
	public void onBackPressed() {
		if (currentDir.equalsIgnoreCase(rootDir)) {
			exit();
		} else {
			draw(currentDir.substring(0, currentDir.lastIndexOf("/")));
		}

	}
}
