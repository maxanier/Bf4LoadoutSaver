package de.maxgb.loadoutsaver.dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import de.maxgb.loadoutsaver.R;

public class EnterChallengeCodeDialog extends DialogFragment {
	public interface EnterChallengeDialogListener {
		public void onCanceledCode();

		public void onEnteredCode(String token, String code);
	}

	String code;
	String token;
	int layout=R.layout.dialog_challenge_code;
	EnterChallengeDialogListener mListener;

	public EnterChallengeCodeDialog() {

	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		// Verify that the host activity implements the callback interface
		try {
			// Instantiate the NoticeDialogListener so we can send events to the
			// host
			mListener = (EnterChallengeDialogListener) activity;
		} catch (ClassCastException e) {
			// The activity doesn't implement the interface, throw exception
			throw new ClassCastException(activity.toString()
					+ " must implement EnterChallengeDialogListener");
		}
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

		// Get the layout inflater
		LayoutInflater inflater = getActivity().getLayoutInflater();
		// Inflate and set the layout for the dialog
		// Pass null as the parent view because its going in the dialog layout
		View view = inflater.inflate(layout, null);

		TextView codeview = (TextView) view
				.findViewById(R.id.dialog_challenge_code_view);

		codeview.setText(code);

		builder.setView(view);

		builder.setPositiveButton("Done",
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						mListener.onEnteredCode(token, code);
					}
				});
		return builder.create();
	}

	@Override
	public void setArguments(Bundle arguments) {
		super.setArguments(arguments);
		code = arguments.getString("code");
		token = arguments.getString("token");
		if(arguments.getBoolean("again")){
			layout=R.layout.dialog_challenge_code_again;
		}
	}

}
