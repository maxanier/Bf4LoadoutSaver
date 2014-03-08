package de.maxgb.loadoutsaver;

import de.maxgb.loadoutsaver.util.Constants;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;


public class LoadoutNameDialog extends DialogFragment {

	public interface  LoadoutNameDialogListener {
		public void addCurrentLoadout(String name,boolean weapon,boolean kit,boolean vehicle);

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
		builder.setTitle(Messages.getString("LoadoutNameDialog.SetName")); //$NON-NLS-1$
		LayoutInflater inflater = getActivity().getLayoutInflater();
		builder.setView(inflater.inflate(R.layout.add_loadout_menu,null));
		
		
		builder.setPositiveButton(Messages.getString("LoadoutNameDialog.Add"), //$NON-NLS-1$
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						String name=((EditText)thisDialog.findViewById(R.id.loadoutname)).getText().toString();
						if(name.equals("")){
							name="Loadout";
						}
						boolean w = ((CheckBox)thisDialog.findViewById(R.id.dialog_box_weapons)).isChecked();
						boolean k = ((CheckBox)thisDialog.findViewById(R.id.dialog_box_kits)).isChecked();
						boolean v=((CheckBox)thisDialog.findViewById(R.id.dialog_box_vehicles)).isChecked();
						if(!w&&!k&&!v){
							w=true;
							k=true;
							v=true;
						}
						mListener.addCurrentLoadout(name,w,k,v);

					}
				}).setNegativeButton(Messages.getString("LoadoutNameDialog.Abort"), //$NON-NLS-1$
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {

					}
				});
		if(android.os.Build.VERSION.SDK_INT<VERSION_CODES.HONEYCOMB){
			builder.setInverseBackgroundForced(true);
		}
		thisDialog=builder.create();
		return thisDialog;
	}
}