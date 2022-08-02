package azoftware.com.whatsappro;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SetupActivity extends AppCompatActivity {
    private EditText nombre, ciudad, genero, edad,estado;
    public static int bandera;
    private EditText numero, codigo;
    private Button enviar_numero, enviar_codigo;
    private Button guardarinfo;
    private CircleImageView imagen_setup;
    private FirebaseAuth auth;
    private DatabaseReference UserRef;
    private ProgressDialog dialog;
    private String CurrenUserID;
    final  static  int Gallery_PICK =1;
    private StorageReference UserProfileImagen;
    private Toolbar toolbar;
    private String token;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks callbacks;
    private String mVerification;
    private PhoneAuthProvider.ForceResendingToken mResendingToken;
    private FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);
        nombre=(EditText) findViewById(R.id.nombre_setup);
        ciudad=(EditText) findViewById(R.id.ciudad_setup);
        genero=(EditText) findViewById(R.id.genero_setup);


        codigo =(EditText)findViewById(R.id.codigo);

        enviar_codigo =(Button)findViewById(R.id.enviar_codigo);
        mAuth = FirebaseAuth.getInstance();
        edad=(EditText) findViewById(R.id.edad_setup);
        estado=(EditText) findViewById(R.id.estado_setup);
        guardarinfo=(Button)findViewById(R.id.boton_setup);
        imagen_setup=(CircleImageView)findViewById(R.id.imagen_setup);
        toolbar = (Toolbar)findViewById(R.id.toolbar_setup);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("2");
        dialog = new ProgressDialog(this);
        auth=FirebaseAuth.getInstance();
        CurrenUserID= auth.getCurrentUser().getUid();
        UserRef= FirebaseDatabase.getInstance().getReference().child("Usuarios");
        UserProfileImagen= FirebaseStorage.getInstance().getReference().child("ImagesPerfil");
        guardarinfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GuardarInfromacionDB();
            }
        });
        imagen_setup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent  intent = new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent,Gallery_PICK);

            }
        });

        UserRef.child(CurrenUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (snapshot.exists()){
                    if (snapshot.hasChild("imagen")){
                        String imagen = snapshot.child("imagen").getValue().toString();
                        Picasso.get()
                                .load(imagen)
                                .placeholder(R.drawable.welcome)
                                .error(R.drawable.welcome)
                                .into(imagen_setup);

                    }else{
                        Toast.makeText(SetupActivity.this, "Puede cargar una foto....", Toast.LENGTH_SHORT).show();
                    }
                }

            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) { }});



    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==Gallery_PICK && resultCode == RESULT_OK && data != null){
            Uri imageUri = data.getData();
            CropImage.activity(imageUri)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1,1)
                    .start(this);
        }
        if (requestCode==CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if(resultCode ==RESULT_OK){
                dialog.setTitle("");
                dialog.setMessage("...");
                dialog.setCanceledOnTouchOutside(false);
                dialog.show();
                final Uri resultUri = result.getUri();
                StorageReference filePath = UserProfileImagen.child(CurrenUserID+".jpg");
                final File url = new File(resultUri.getPath());
                filePath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if (task.isSuccessful()){
                            Toast.makeText(SetupActivity.this, "✓✓✓", Toast.LENGTH_SHORT).show();
                            UserProfileImagen.child(CurrenUserID+".jpg").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    final String downloadUri = uri.toString();
                                    UserRef.child(CurrenUserID).child("imagen").setValue(downloadUri)
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()){
                                                        Picasso.get()
                                                                .load(downloadUri)
                                                                .error(R.drawable.welcome)
                                                                .into(imagen_setup);
                                                        Toast.makeText(SetupActivity.this, "Imagen se guardo en la Base de datos", Toast.LENGTH_SHORT).show();
                                                        dialog.dismiss();
                                                    }else{
                                                        String error = task.getException().getMessage();
                                                        Toast.makeText(SetupActivity.this, "Failed:"+error, Toast.LENGTH_SHORT).show();
                                                        dialog.dismiss();
                                                    }
                                                }
                                            });
                                }
                            });
                        }
                    }
                });
            }else{
                Toast.makeText(this, "Imagen no es soportada Intentelo de nuevo", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        }
    }
    private void GuardarInfromacionDB() {
        String nom = nombre.getText().toString();
        String ciu = ciudad.getText().toString();
        String gen = genero.getText().toString();
        String eda = edad.getText().toString();
        String est = estado.getText().toString();
        if (TextUtils.isEmpty(nom)){
            Toast.makeText(this, "Debe ingresar su nombre", Toast.LENGTH_SHORT).show();
        }else if (TextUtils.isEmpty(ciu)){
            Toast.makeText(this, "Debe ingresar su ciudad", Toast.LENGTH_SHORT).show();
        }else if (TextUtils.isEmpty(gen)){
            Toast.makeText(this, "Debe ingresar su genero", Toast.LENGTH_SHORT).show();
        }else if (TextUtils.isEmpty(eda)){
            Toast.makeText(this, "Debe ingresar su edad", Toast.LENGTH_SHORT).show();
        }else if (TextUtils.isEmpty(est)){
            Toast.makeText(this, "INGRESE SU NUMERO", Toast.LENGTH_SHORT).show();
        }
            else{
            dialog.setTitle("Guardando su datos");
            dialog.setMessage("Por favor espere a que finalice el proceso");
            dialog.show();
            dialog.setCanceledOnTouchOutside(false);
            FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
                @Override
                public void onComplete(@NonNull Task<String> task) {
                    if (task.isSuccessful()){






                        token = task.getResult();
                        HashMap map = new HashMap();
                        map.put("nombre",nom);
                        map.put("ciudad",ciu);
                        map.put("genero", gen);
                        map.put("edad",eda);
                        map.put("estado",est);
                        map.put("token",token);
                        UserRef.child(CurrenUserID).updateChildren(map).addOnCompleteListener(new OnCompleteListener() {
                            @Override
                            public void onComplete(@NonNull Task task) {
                                if (task.isSuccessful()){



                                    EnviarAlInicio();
                                }else{
                                    String err = task.getException().getMessage();

                                }
                            }
                        });
                    }
                }
            });
        }
    }
    private void EnviarAlInicio() {
        Intent intent = new Intent(SetupActivity.this, InicioActivity.class);

        startActivity(intent);
        bandera=1;
        finish();

    }



}