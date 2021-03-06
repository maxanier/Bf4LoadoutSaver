package de.maxgb.loadoutsaver;

import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import de.maxgb.loadoutsaver.util.Loadout;

/**
 * CustomArrayAdapter with stable Ids, which manages the Loadout ListItemViews.
 * Delivers Views with Loadoutname and an icon for its type.
 * 
 * @author Max
 * 
 */
public class CustomArrayAdapter extends ArrayAdapter<Loadout> {
	private final String TAG = "CustomArrayAdapter";
	HashMap<Loadout, Integer> idMap = new HashMap<Loadout, Integer>();
	List<Loadout> data;
	Context context;
	int layoutViewResourceId;
	int counter;

	public CustomArrayAdapter(Context context, int layoutViewResourceId,
			List<Loadout> data) {
		super(context, layoutViewResourceId, data);
		this.data = data;
		this.context = context;
		this.layoutViewResourceId = layoutViewResourceId;
		updateStableIds();
	}

	public void addStableIdForDataAtPosition(int position) {
		idMap.put(data.get(position), ++counter);
	}

	@Override
	public long getItemId(int position) {
		Loadout item = getItem(position);
		if (idMap.containsKey(item)) {
			return idMap.get(item);
		}
		return -1;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Loadout obj = data.get(position);
		if (convertView == null) {
			LayoutInflater inflater = ((Activity) context).getLayoutInflater();
			convertView = inflater.inflate(layoutViewResourceId, parent, false);
		}
		int cellHeight = (int) (context.getResources()
				.getDimension(R.dimen.cell_height));
		convertView.setLayoutParams(new ListView.LayoutParams(
				LayoutParams.MATCH_PARENT, cellHeight));

		TextView nameView = (TextView) convertView
				.findViewById(R.id.item_name_view);
		ImageView imageView = (ImageView) convertView
				.findViewById(R.id.item_image_view);

		nameView.setText(obj.getName());
		// Logger.i(TAG,"Creating View for Loadout Type: "+obj.getType());

		imageView.setImageBitmap(obj.getImage(context));
		
		convertView.setBackgroundColor(obj.getColor());

		return convertView;
	}

	@Override
	public boolean hasStableIds() {
		return true;
	}

	public void updateStableIds() {
		idMap.clear();
		counter = 0;
		for (int i = 0; i < data.size(); ++i) {
			idMap.put(data.get(i), counter++);
		}
	}
}
