package com.cfm.pullrefresh;

import java.util.ArrayList;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.app.Activity;

public class MainActivity extends Activity {
	
	private ListView list;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		list = (ListView) findViewById(R.id.list);
		ArrayList<String> arrayList = new ArrayList<String>();
		arrayList.add("your group");
		arrayList.add("my group");
		arrayList.add("his group");
		arrayList.add("her group");
		arrayList.add("your group");
		arrayList.add("my group");
		arrayList.add("his group");
		arrayList.add("her group");
		arrayList.add("your group");
		arrayList.add("my group");
		arrayList.add("his group");
		arrayList.add("her group");
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, arrayList);
		list.setAdapter(adapter);
	}

}
