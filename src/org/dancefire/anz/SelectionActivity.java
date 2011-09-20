package org.dancefire.anz;

import java.util.ArrayList;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class SelectionActivity extends BaseActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		ArrayList<String> list_string = getIntent().getStringArrayListExtra("item_list");
		if (list_string != null) {
			setContentView(R.layout.selection_layout);
			ListView list_view = (ListView) findViewById(android.R.id.list);
			list_view.setAdapter(new ArrayAdapter<String>(this, R.layout.selectioin_item_layout, list_string));
			list_view.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> parent, View view,
						int position, long id) {
					Intent intent = new Intent();
					intent.putExtra("selection", position);
					setResult(RESULT_OK, intent);
					finish();
				}
			});
		}
	}
}
