package de.maxgb.loadoutsaver.util;


import de.maxgb.loadoutsaver.R;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences;
import android.webkit.WebView;

/**
 * Allows to easily show an InfoBox/AlertDialog.
 * Designed to show instructions, but only once or when they changed again
 * @author Max
 *
 */
public class InfoBox {
	/**
	 * Instruction enum to store different instructions, which can be shown in an InfoBox.
	 * Contain title, last change and instruction(in HTML format)
	 */
	public enum Instruction {
		
		MAIN("Loadout Saver",5,"<html><body>1. Enter your Battlog login information in the options menu, so the app can edit your loadout<br>2. Use the +-Button to save your currently equipped Loadout and choose a name and a type (Infantry/Vehicle/All) for it<br>3. Click on a Loadout to load it and watch it happen on Battlelog or in game<br>4. Long click on a Loadout to remove it.</body></html");

		public String activity_name;
		public int last_change;
		public String text;

		Instruction(String name, int last_change, String text) {
			this.activity_name = name;
			this.last_change = last_change;
			this.text = text;
		}

	}

	/**
	 * Shows an InfoBox (AlertDialog) with the given instruction
	 * @param context Context of the application
	 * @param instruction Instruction to be shown
	 */
	public static void showInstructionBox(Context context, Instruction instruction) {
		//get settings to check if instruction was already shown
		SharedPreferences settings = context
				.getSharedPreferences(Constants.INFO_BOX_PREF_NAME, 0);
		if (settings.getInt(instruction.activity_name, 0) < instruction.last_change) {
			//instruction is newer than the last shown -> Create a AlertDialog containing an Webview
			AlertDialog.Builder builder = new AlertDialog.Builder(context);
			builder.setTitle(context.getResources().getString(R.string.instruction));

			builder.setPositiveButton(context.getResources().getString(R.string.ok), new OnClickListener() {
				@Override
				public void onClick(DialogInterface i, int a) {
				}

			});
			WebView v = new WebView(context);
			v.loadData(instruction.text, "text/html; charset=UTF-8", null);
			builder.setView(v);

			AlertDialog dialog = builder.create();
			dialog.show();

			SharedPreferences.Editor editor = settings.edit();
			editor.putInt(instruction.activity_name, instruction.last_change);
			editor.commit();
		}
	}
	
	/**
	 * Shows an InfoBox (AlertDialog) with the given title and text
	 * @param context Context of the application
	 * @param title Title of the dialog
	 * @param text Text to be shown
	 */
	public static void showInfoBox(Context context, String title, String text) {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setTitle(title);
		builder.setPositiveButton(context.getResources().getString(R.string.ok), new OnClickListener() {
			@Override
			public void onClick(DialogInterface i, int a) {
			}

		});
		WebView v = new WebView(context);
		v.loadData(text, "text/html; charset=UTF-8", null);
		builder.setView(v);

		AlertDialog dialog = builder.create();
		dialog.show();
	}

}