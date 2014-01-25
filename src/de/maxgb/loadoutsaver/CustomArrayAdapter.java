package de.maxgb.loadoutsaver;

import java.util.HashMap;
import java.util.List;

import de.maxgb.android.util.Logger;
import de.maxgb.loadoutsaver.util.Constants;
import de.maxgb.loadoutsaver.util.Loadout;
import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;


/**
 * CustomArrayAdapter with stable Ids, which manages the Loadout ListItemViews.
 * Delivers Views with Loadoutname and an icon for its type.
 * @author Max
 *
 */
public class CustomArrayAdapter extends ArrayAdapter<Loadout>  {
	private final String TAG="CustomArrayAdapter";
	HashMap<Loadout, Integer> idMap = new HashMap<Loadout, Integer>();
    List<Loadout> data;
    Context context;
    int layoutViewResourceId;
    int counter;
	
    public CustomArrayAdapter(Context context, int layoutViewResourceId, List<Loadout> data){
    	super(context,layoutViewResourceId,data);
    	this.data=data;
    	this.context=context;
    	this.layoutViewResourceId=layoutViewResourceId;
    	updateStableIds();
    }
    
    public long getItemId(int position) {
        Loadout item = getItem(position);
        if (idMap.containsKey(item)) {
            return idMap.get(item);
        }
        return -1;
    }

    public void updateStableIds() {
        idMap.clear();
        counter = 0;
        for (int i = 0; i < data.size(); ++i) {
            idMap.put(data.get(i), counter++);
        }
    }
    
    public void addStableIdForDataAtPosition(int position) {
        idMap.put(data.get(position), ++counter);
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }
    
    @Override
    public View getView(int position, View convertView, ViewGroup parent){
    	Loadout obj = data.get(position);
    	if(convertView == null){
    		LayoutInflater inflater = ((Activity)context).getLayoutInflater();
            convertView = inflater.inflate(layoutViewResourceId, parent, false);
    	}
    	int cellHeight = (int)(context.getResources().getDimension(R.dimen.cell_height));
    	convertView.setLayoutParams(new ListView.LayoutParams(ListView.LayoutParams
                .MATCH_PARENT, cellHeight));
    	
    	TextView nameView = (TextView) convertView.findViewById(R.id.item_name_view);
    	ImageView imageView = (ImageView) convertView.findViewById(R.id.item_image_view);
    	
    	
    	nameView.setText(obj.getName());
    	//Logger.i(TAG,"Creating View for Loadout Type: "+obj.getType());
    	
    	imageView.setImageDrawable(context.getResources().getDrawable(obj.getDrawableId()));
    	
    	return convertView;
    }
}
