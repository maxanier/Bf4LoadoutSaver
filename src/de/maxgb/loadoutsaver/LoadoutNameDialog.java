package de.maxgb.loadoutsaver;

import de.maxgb.android.util.Logger;
import afzkl.development.colorpickerview.dialog.ColorPickerDialog;
import afzkl.development.colorpickerview.view.ColorPanelView;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

public class LoadoutNameDialog extends DialogFragment {
	
	private final String TAG="LoadoutNameDialog";

	public interface LoadoutNameDialogListener {
		public void addCurrentLoadout(String name, boolean weapon, boolean kit,
				boolean vehicle, int color);

	}

	LoadoutNameDialogListener mListener;
	AlertDialog thisDialog;
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			mListener = (LoadoutNameDialogListener) activity;
		} catch (ClassCastException e) {
			// Aktivity implementiert das Interface nicht
			throw new ClassCastException(activity.toString()
					+ " must implement NoticeDialogListener"); //$NON-NLS-1$
		}
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle("New Loadout");
		LayoutInflater inflater = getActivity().getLayoutInflater();
		View view=inflater.inflate(R.layout.add_loadout_menu, null);
		
		final ColorPanelView colorpicker=(ColorPanelView)view.findViewById(R.id.dialog_view_color);
		colorpicker.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				Logger.i(TAG, "Button clicked");
				final ColorPickerDialog colorDialog=new ColorPickerDialog(getActivity(),Color.BLACK);
				
				colorDialog.setAlphaSliderVisible(false);
				colorDialog.setTitle("Pick a color!");
				
				colorDialog.setButton(DialogInterface.BUTTON_POSITIVE, "Ok", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						Logger.i(TAG, "Selected: "+colorDialog.getColor());
						
						if(colorpicker!=null){
							colorpicker.setColor(colorDialog.getColor());
						}
					}
				});
				
				colorDialog.show();
				
			}
			
		});
		
		//Set dialog View
		builder.setView(view);
		
		//Set dialog finished button listener
		builder.setPositiveButton("Add", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				String name = ((EditText) thisDialog
						.findViewById(R.id.loadoutname)).getText().toString().trim();
				if (name.equals("")) {
					name = "Loadout";
				}
				boolean w = ((CheckBox) thisDialog
						.findViewById(R.id.dialog_box_weapons)).isChecked();
				boolean k = ((CheckBox) thisDialog
						.findViewById(R.id.dialog_box_kits)).isChecked();
				boolean v = ((CheckBox) thisDialog
						.findViewById(R.id.dialog_box_vehicles)).isChecked();
				int c=((ColorPanelView)thisDialog.findViewById(R.id.dialog_view_color)).getColor();
				
				
				if (!w && !k && !v) {
					w = true;
					k = true;
					v = true;
				}
				mListener.addCurrentLoadout(name, w, k, v,c);

			}
		}).setNegativeButton("Abort", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {

			}
		});
		if (android.os.Build.VERSION.SDK_INT < VERSION_CODES.HONEYCOMB) {
			builder.setInverseBackgroundForced(true);
		}
		thisDialog = builder.create();
		
		
		
		return thisDialog;
	}
	

}