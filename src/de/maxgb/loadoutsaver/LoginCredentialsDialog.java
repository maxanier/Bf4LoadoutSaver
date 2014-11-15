package de.maxgb.loadoutsaver;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.widget.EditText;



public class LoginCredentialsDialog extends DialogFragment{
	public interface LoginCredentialsDialogListener{
		public void onEntered(String email,String password);
		public void onCanceled();
	}
	
	private LoginCredentialsDialogListener mListener;
	private EditText editEmail;
	private EditText editPassword;
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
	    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
	    // Get the layout inflater
	    LayoutInflater inflater = getActivity().getLayoutInflater();

	    // Inflate and set the layout for the dialog
	    // Pass null as the parent view because its going in the dialog layout
	    builder.setView(inflater.inflate(R.layout.login_credentials_dialog, null))
	    // Add action buttons
	           .setPositiveButton("Login", new DialogInterface.OnClickListener() {
	               @Override
	               public void onClick(DialogInterface dialog, int id) {
	                   mListener.onEntered(getEmail(),getPass());
	               }
	           })
	           .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
	               public void onClick(DialogInterface dialog, int id) {
	            	   mListener.onCanceled();
	                   //LoginCredentialsDialog.this.getDialog().cancel();
	               }
	           });      
	    
	    AlertDialog dialog=builder.create();
	    
	    editEmail=(EditText)dialog.findViewById(R.id.login_dialog_email);
	    editPassword=(EditText)dialog.findViewById(R.id.login_dialog_password);
	    
	    return dialog;
	}
	
	@Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            mListener = (LoginCredentialsDialogListener) activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement LoginCredentialsDialogListener");
        }
    }
	
	private String getEmail(){
		return editEmail.getText().toString().trim();
		
	}
	
	private String getPass(){
		return editPassword.getText().toString().trim();
		
	}

}
