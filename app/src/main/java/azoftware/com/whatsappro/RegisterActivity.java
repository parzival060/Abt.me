package azoftware.com.whatsappro;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class RegisterActivity extends AppCompatActivity {
    private final String TAG = "loginactivity";
    EditText etEmail;
    EditText etPassword;
    Button btnSignInUp;
    TextView tvSignInUp;
    private FirebaseAuth mAuth;
    private boolean createNewAccount = true;
    CheckBox isbox1, isbox2;
    RelativeLayout show_lan_dialog;
    Context context;
    Resources resources;
    int lang_selected;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        etEmail =(EditText) findViewById(R.id.etEmail);
        etPassword=(EditText)findViewById(R.id.etPassword);
        btnSignInUp = (Button)findViewById(R.id.btnSignInUp);
        tvSignInUp = (TextView) findViewById(R.id.tvSignInUp);
        isbox1 = (CheckBox) findViewById(R.id.box1);
        isbox2 = (CheckBox) findViewById(R.id.box2);

        show_lan_dialog = (RelativeLayout)findViewById(R.id.showlangdialog);
        if(LocaleHelper.getLanguage(RegisterActivity.this).equalsIgnoreCase("es"))
        {
            context = LocaleHelper.setLocale(RegisterActivity.this,"es");
            resources =context.getResources();


            btnSignInUp.setText(resources.getString(R.string.action_sign_up));

            tvSignInUp.setText(resources.getString(R.string.sign_in_free));
            isbox1.setText(resources.getString(R.string.box1));
            isbox2.setText(resources.getString(R.string.box2));


            lang_selected = 0;
        }else if(LocaleHelper.getLanguage(RegisterActivity.this).equalsIgnoreCase("en")){
            context = LocaleHelper.setLocale(RegisterActivity.this,"en");
            resources =context.getResources();
            //*etEmail.setHint(resources.getString(R.string.prompt_email));
            //* etPassword.setHint(resources.getString(R.string.prompt_password));
            btnSignInUp.setText(resources.getString(R.string.action_sign_up));

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
        etPassword = findViewById(R.id.etPassword);
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






    private boolean checkCredentials(String email, String password) {
        if (!email.contains("@") || email.length() < 6) {
            onInvalidEmail(R.string.error_invalid_email);
            return false;
        } else if (password.length() < 6) {
            onInvalidPassword(R.string.error_invalid_password);
            return false;
        }
        return true;
    }

    private void onInvalidEmail(int idError) {
        etEmail.setError(getString(idError));
        etEmail.requestFocus();
    }

    private void onInvalidPassword(int idError) {
        etPassword.setError(getString(idError));
        etPassword.requestFocus();
    }

    private void registerNewUser(String email, String password) {

        if (!(isbox2.isChecked() || isbox1.isChecked())){
            Toast.makeText(RegisterActivity.this ,"Solo se puede una" , Toast.LENGTH_SHORT).show();
            return;

        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) { // Sign in success,
                        Log.d(TAG, "createUserWithEmail:success");
                        FirebaseUser user = mAuth.getCurrentUser();
                        sendEmailVerification(user);
                    } else { // If sign in fails
                        Log.w(TAG, "createUserWithEmail:failure", task.getException());
                        Toast.makeText(RegisterActivity.this, "Error while registering new user", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void sendEmailVerification(FirebaseUser user) {
        Log.d(TAG, "started Verification");
        user.sendEmailVerification()
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "Verification email sent to " + user.getEmail());
                        showConfirmationDialog(R.string.confirm_email,
                                getString(R.string.please_confirm_email, user.getEmail()));
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
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {  // Sign in success,
                        Log.d(TAG, "signInWithEmail:success");
                        FirebaseUser user = mAuth.getCurrentUser();

                        Toast.makeText(RegisterActivity.this, "Ingresado con exito", Toast.LENGTH_SHORT).show();
                        EnviarAlInicio();



                        if (!user.isEmailVerified()) {
                            showConfirmationDialog(R.string.confirm_email,
                                    getString(R.string.please_confirm_email, user.getEmail()));
                        } else {
                            updateUI(user);
                        }
                    } else { // If sign in fails
                        Log.w(TAG, "signInWithEmail:failure", task.getException());
                        Toast.makeText(RegisterActivity.this, "Error while login", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void EnviarAlInicio() {
        Intent intent = new Intent(RegisterActivity.this, InicioActivity.class);
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
                onChangeContent(R.string.create_account, R.string.sign_in_free);
            } else { // if the user wants to login
                onChangeContent(R.string.action_sign_in, R.string.create_account);
            }
        });
        btnSignInUp.setOnClickListener(view -> {
            if (checkCredentials(etEmail.getText().toString(), etPassword.getText().toString())) {
                if (createNewAccount) { // user wants to create a new account
                    registerNewUser(etEmail.getText().toString(), etPassword.getText().toString());
                } else { // User wants to login in with an existing account
                    loginUser(etEmail.getText().toString(), etPassword.getText().toString());
                }
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














