package org.dancefire.anz;

import java.util.ArrayList;

import org.dancefire.anz.mobile.AnzMobileUtil;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class SelectionFragment extends BaseFragment {
	private OnItemClickListener m_listener;

	public static SelectionFragment newInstance(ArrayList<String> list) {
		Bundle args = new Bundle();
		args.putStringArrayList("list", list);
		SelectionFragment f = new SelectionFragment();
		f.setArguments(args);
		return f;
	}
	
	@Override
	public View onCreateView(android.view.LayoutInflater inflater,
			android.view.ViewGroup container, Bundle savedInstanceState) {
		AnzMobileUtil.logger.fine("SelectionFragment.onCreateView()");
		ArrayList<String> list_string = getArguments().getStringArrayList(
				"list");
		if (list_string != null) {
			View v = inflater.inflate(R.layout.selection_layout, container,
					false);
			ListView list_view = (ListView) v.findViewById(android.R.id.list);
			list_view.setAdapter(new ArrayAdapter<String>(getActivity(),
					R.layout.selectioin_item_layout, list_string));
			list_view.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> parent, View view,
						int position, long id) {
					AnzMobileUtil.logger.info("[SelectionFragment] Clicked " + position);
					if (m_listener != null) {
						m_listener.onItemClick(parent, view, position, id);
					}
					getFragmentManager().popBackStack();
				}
			});

			return v;
		}
		return null;
	}
	
	@Override
	public void onResume() {
		super.onResume();
		AnzMobileUtil.logger.fine("SelectionFragment.onResume()");
	}

	@Override
	public void onPause() {
		AnzMobileUtil.logger.fine("SelectionFragment.onPause()");
		super.onPause();
	}
	public void setOnItemClickListner(OnItemClickListener listener) {
		m_listener = listener;
	}
}
