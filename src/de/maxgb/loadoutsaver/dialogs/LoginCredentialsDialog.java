package de.maxgb.loadoutsaver.dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import de.maxgb.loadoutsaver.R;
import de.maxgb.loadoutsaver.util.Constants;

public class LoginCredentialsDialog extends DialogFragment {
	public interface LoginCredentialsDialogListener {
		public void onCanceled();

		public void onEntered(String email, String password);
	}

	private LoginCredentialsDialogListener mListener;
	private EditText editEmail;
	private EditText editPassword;

	private String getEmail() {
		return editEmail.getText().toString().trim();

	}

	private String getPass() {
		return editPassword.getText().toString().trim();

	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		// Verify that the host activity implements the callback interface
		try {
			// Instantiate the NoticeDialogListener so we can send events to the
			// host
			mListener = (LoginCredentialsDialogListener) activity;
		} catch (ClassCastException e) {
			// The activity doesn't implement the interface, throw exception
			throw new ClassCastException(activity.toString()
					+ " must implement LoginCredentialsDialogListener");
		}
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		// Get the layout inflater
		LayoutInflater inflater = getActivity().getLayoutInflater();
		// Inflate and set the layout for the dialog
		// Pass null as the parent view because its going in the dialog layout
		View view = inflater.inflate(R.layout.dialog_login_credentials, null);

		final SharedPreferences pref = getActivity().getSharedPreferences(
				Constants.PREF_NAME, 0);

		String email = pref.getString(Constants.EMAIL_KEY, "");
		String pass = pref.getString(Constants.PASSWORD_KEY, "");

		editEmail = (EditText) view.findViewById(R.id.login_dialog_email);
		editPassword = (EditText) view.findViewById(R.id.login_dialog_password);

		editEmail.setText(email);
		editPassword.setText(pass);

		builder.setView(view)
				// Add action buttons
				.setPositiveButton("Login",
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int id) {
								pref.edit()
										.putString(Constants.EMAIL_KEY,
												getEmail())
										.putString(Constants.PASSWORD_KEY,
												getPass()).apply();
								mListener.onEntered(getEmail(), getPass());
							}
						})
				.setNegativeButton("Cancel",
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int id) {
								mListener.onCanceled();
								// LoginCredentialsDialog.this.getDialog().cancel();
							}
						});

		AlertDialog dialog = builder.create();

		return dialog;
	}

}
