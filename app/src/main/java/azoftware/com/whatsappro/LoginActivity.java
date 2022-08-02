package azoftware.com.whatsappro;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;


public class LoginActivity extends AppCompatActivity {
    private final String TAG = "loginactivity";
    EditText etEmail, etname;
   EditText etPassword, etPassword2;
    Button btnSignInUp;
     TextView tvSignInUp;
  TextInputLayout tilname, tilPassword2;
    public static int lang_selected;
    private DatabaseReference UserRef, RootRef;




    private FirebaseAuth mAuth;
    private boolean createNewAccount = true;
    CheckBox isbox1, isbox2;
    RelativeLayout show_lan_dialog;
    Context context;
    Resources resources;



    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        etEmail =(EditText) findViewById(R.id.etEmail);
        etname =(EditText) findViewById(R.id.etname);
        etPassword=(EditText)findViewById(R.id.etPassword);
        etPassword2=(EditText)findViewById(R.id.etPassword2);
        btnSignInUp = (Button)findViewById(R.id.btnSignInUp);
        tvSignInUp = (TextView) findViewById(R.id.tvSignInUp);
        isbox1 = (CheckBox) findViewById(R.id.box1);
        isbox2 = (CheckBox) findViewById(R.id.box2);
        tilname = (TextInputLayout)findViewById(R.id.tilname);
        tilPassword2 = (TextInputLayout)findViewById(R.id.tilPassword2);

        show_lan_dialog = (RelativeLayout)findViewById(R.id.showlangdialog);
        if(LocaleHelper.getLanguage(LoginActivity.this).equalsIgnoreCase("es"))
        {
            context = LocaleHelper.setLocale(LoginActivity.this,"es");
            resources =context.getResources();

            etEmail.setHint(resources.getString(R.string.prompt_email));
            etPassword.setHint(resources.getString(R.string.prompt_password));
            tilname.setHint(resources.getString(R.string.prompt_nombre));
            tilPassword2.setHint(resources.getString(R.string.prompt_confirm_password));
            btnSignInUp.setText(resources.getString(R.string.action_sign_up));

            tvSignInUp.setText(resources.getString(R.string.sign_in_free));
            isbox1.setText(resources.getString(R.string.box1));
            isbox2.setText(resources.getString(R.string.box2));
            lang_selected = 0;
        }else if(LocaleHelper.getLanguage(LoginActivity.this).equalsIgnoreCase("en")){
            context = LocaleHelper.setLocale(LoginActivity.this,"en");
            resources =context.getResources();
            //*etEmail.setHint(resources.getString(R.string.prompt_email));
           //* etPassword.setHint(resources.getString(R.string.prompt_password));
            btnSignInUp.setText(resources.getString(R.string.action_sign_up));
            etEmail.setHint(resources.getString(R.string.prompt_email));
            etPassword.setHint(resources.getString(R.string.prompt_password));
            tilname.setHint(resources.getString(R.string.prompt_nombre));
            tilPassword2.setHint(resources.getString(R.string.prompt_confirm_password));
            tvSignInUp.setText(resources.getString(R.string.sign_in_free));
            isbox1.setText(resources.getString(R.string.box1));
            isbox2.setText(resources.getString(R.string.box2));
            lang_selected =1;
        }



        initViews();
        setListners();
        mAuth = FirebaseAuth.getInstance();
    }

    private void initViews() {
        etEmail = findViewById(R.id.etEmail);
        etname = findViewById(R.id.etname);
        tilname = (TextInputLayout)findViewById(R.id.tilname);
        tilPassword2 = (TextInputLayout)findViewById(R.id.tilPassword2);
        etPassword = findViewById(R.id.etPassword);
        etPassword2 = findViewById(R.id.etPassword2);
        btnSignInUp = findViewById(R.id.btnSignInUp);
        tvSignInUp = findViewById(R.id.tvSignInUp);
        isbox1 = findViewById(R.id.box1);
        isbox2 = findViewById(R.id.box2);


        isbox1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (buttonView.isChecked()){
                    isbox2.setChecked(false);
                }

            }
        });




        isbox2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (buttonView.isChecked()){
                    isbox1.setChecked(false);
                }

            }
        });
    }






    private boolean checkCredentials(String email, String password, String name, String confirm) {



        if (!email.contains("@abtmexico") || email.length() < 6) {
            onInvalidEmail(R.string.error_invalid_email);
            return false;
        }
        else if (password.length() < 6) {
            onInvalidPassword(R.string.error_invalid_password);
            return false;
        }
        else if (password.length() != confirm.length()) {
            onInvalidcontraseñas(R.string.error_invalid_parent);
            return false;
        }
        else if(!name.toString().matches("[a-zA-Z ]+") ){
            onInvalidName(R.string.error_invalid_parent);
            return false;
        }
        return true;


    }

    private void onInvalidName(int idError) {
        etname.setError(getString(idError));
        etname.requestFocus();
    }

    private void onInvalidEmail(int idError) {
        etEmail.setError(getString(idError));
        etEmail.requestFocus();
    }

    private void onInvalidPassword(int idError) {
        etPassword.setError(getString(idError));
        etPassword.requestFocus();
    }

    private void onInvalidcontraseñas(int idError) {
        etPassword.setError(getString(idError));
        etPassword2.setError(getString(idError));
        etPassword.requestFocus();
        etPassword2.requestFocus();
    }

    private void registerNewUser(String email, String password, String name) {

        if (!(isbox2.isChecked() || isbox1.isChecked())){
            Toast.makeText(LoginActivity.this ,"Solo se puede una" , Toast.LENGTH_SHORT).show();
            return;

        }
        EditText inputPassword = (EditText) findViewById(R.id.etPassword);
        EditText inputPassword2 = (EditText) findViewById(R.id.etPassword2);

        String password1 = inputPassword.getText().toString();
        String password2 = inputPassword2.getText().toString();

       if(password1.equals(password2)) {


           mAuth.createUserWithEmailAndPassword(email, password)
                   .addOnCompleteListener(this, task -> {
                       if (task.isSuccessful()) { // Sign in success,
                           Log.d(TAG, "createUserWithEmail:success");
                           FirebaseUser user = mAuth.getCurrentUser();
                           sendEmailVerification(user);
                       } else { // If sign in fails
                           Log.w(TAG, "createUserWithEmail:failure", task.getException());
                           Toast.makeText(LoginActivity.this, "Error while registering new user", Toast.LENGTH_SHORT).show();
                       }
                   });


        }else {

           Toast.makeText(LoginActivity.this ,"Las contraseñas no son iguales", Toast.LENGTH_SHORT).show();
           return;

       }





    }

    private void sendEmailVerification(FirebaseUser user) {
        Log.d(TAG, "started Verification");
        user.sendEmailVerification()
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "Verification email sent to " + user.getEmail());
                        showConfirmationDialog(R.string.confirm_email,
                                getString(R.string.please_confirm_email, user.getEmail()));

                        /*Intent intent = new Intent(LoginActivity.this, MiperfilActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);*/
                    } else {
                        Log.e(TAG, "sendEmailVerification", task.getException());
                    }
                });
    }

    private void showConfirmationDialog(int title, String msg){
        AlertDialog.Builder dlg = new AlertDialog.Builder(this);
        dlg.setMessage(msg);
        dlg.setTitle(title);
        dlg.setPositiveButton(R.string.ok, null);
        dlg.show();
    }



    private void loginUser(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {  // Sign in success,
                    Log.d(TAG, "signInWithEmail:success");
                    FirebaseUser user = mAuth.getCurrentUser();
                    Toast.makeText(LoginActivity.this, "Ingresado con exito", Toast.LENGTH_SHORT).show();
                    EnviarAlInicio();


                    if (!user.isEmailVerified()) {
                        showConfirmationDialog(R.string.confirm_email,
                                getString(R.string.please_confirm_email, user.getEmail()));
                    } else {
                        updateUI(user);
                    }
                } else { // If sign in fails
                    Log.w(TAG, "signInWithEmail:failure", task.getException());
                    Toast.makeText(LoginActivity.this, "Error while login", Toast.LENGTH_SHORT).show();
                /*    Toast.makeText(LoginActivity.this, "Sign In", Toast.LENGTH_LONG).show();
                    EnviarAlInicio();
                } else {
                    Toast.makeText(LoginActivity.this, "Sign in failed", Toast.LENGTH_LONG).show();*/

                }

          }

        });
    }

    private void EnviarAlInicio() {
       //* if(bandera==1){
            Toast.makeText(LoginActivity.this, "Datos55", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();

}



    private void updateUI(FirebaseUser currentUser) { //send current user to next activity
        if (currentUser == null) return;
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void setListners() {
        tvSignInUp.setOnClickListener(view -> {
            createNewAccount = !createNewAccount;
            if (createNewAccount) { // if the user wants to create an account
                tilname.setVisibility(View.VISIBLE);
                tilPassword2.setVisibility(View.VISIBLE);
                etname.setVisibility(View.VISIBLE);
                etPassword2.setVisibility(View.VISIBLE);
                onChangeContent(R.string.create_account, R.string.sign_in_free);
            } else { // if the user wants to login
                tilname.setVisibility(View.INVISIBLE);
                tilPassword2.setVisibility(View.INVISIBLE);
               etname.setVisibility(View.INVISIBLE);
                etPassword2.setVisibility(View.INVISIBLE);

                onChangeContent(R.string.action_sign_in, R.string.create_account);
            }
        });
        btnSignInUp.setOnClickListener(view -> {

            if (createNewAccount) { // user wants to create a new acco
                if (checkCredentials(etEmail.getText().toString(), etPassword.getText().toString(),etname.getText().toString(),etPassword2.getText().toString())) {
                    registerNewUser(etEmail.getText().toString(), etPassword.getText().toString(), etname.getText().toString());

                }
            }
            else { // User wants to login in with an existing account
                    loginUser(etEmail.getText().toString(), etPassword.getText().toString());
                }

        });
    }

    private void onChangeContent(int btnTextId, int textViewTextId) {
        tvSignInUp.setText(textViewTextId);
        btnSignInUp.setText(btnTextId);
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);
        if (currentUser != null){
            EnviarAlInicio();
        }
    }
}














