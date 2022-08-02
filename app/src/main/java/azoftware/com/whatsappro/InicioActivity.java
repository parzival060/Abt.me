
package azoftware.com.whatsappro;

import static azoftware.com.whatsappro.PerfilActivity.Valor1;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

import dmax.dialog.SpotsDialog;


public class InicioActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private ViewPager myviewPager;
    private TabLayout mytabLayout;
    private AcesoTabsAdapter myacesoTabsAdapter;
    private String CurrentUserId;
    private FirebaseAuth mAuth;
    private DatabaseReference UserRef, RootRef;

    android.app.AlertDialog mDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inicio);
        toolbar=(Toolbar)findViewById(R.id.app_main_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("ChatFam");
        myviewPager = (ViewPager)findViewById(R.id.main_tabs_pager);
        myacesoTabsAdapter = new AcesoTabsAdapter(getSupportFragmentManager());
        myviewPager.setAdapter(myacesoTabsAdapter);
        mytabLayout = (TabLayout)findViewById(R.id.main_tabs);
        mytabLayout.setupWithViewPager(myviewPager);


        //**actualizacion

        mDialog = new SpotsDialog.Builder()


                .setContext(this)
                .setMessage(R.string.Cargando_contactos)
                .setCancelable(false)

                .build();

        mDialog.show();
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mDialog.dismiss();
            }
        },4000);


        UserRef = FirebaseDatabase.getInstance().getReference().child("Usuarios");
        RootRef = FirebaseDatabase.getInstance().getReference().child("Grupos");
        mAuth = FirebaseAuth.getInstance();
        CurrentUserId = mAuth.getCurrentUser().getUid();

    }


    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser curUser = mAuth.getCurrentUser();
        if (curUser == null){
            EnviarALogin();
        }else{
            ActualizarActividad("");
            VerificarUsuario();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        FirebaseUser curUser = mAuth.getCurrentUser();
        if (curUser != null){
            ActualizarActividad("");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        FirebaseUser curUser = mAuth.getCurrentUser();
        if (curUser != null){
            ActualizarActividad("");
        }
    }

    private void VerificarUsuario() {
        final String currentUserID = mAuth.getCurrentUser().getUid();
        UserRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.hasChild(currentUserID)){
                    CompletarDatosUsuario();

                }else {

                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) { }});
    }
    private void CompletarDatosUsuario() {
        Intent intent = new Intent(InicioActivity.this, SetupActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK| Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
    private void EnviarALogin(){
        Intent intent = new Intent(InicioActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK| Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void TBuscarAmigosWhats() {

        Intent intent = new Intent(InicioActivity.this, BuscarAmigosActivity.class);
        startActivity(intent);

    }











    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menus_opciones, menu);

        if(LocaleHelper.getLanguage(InicioActivity.this).equalsIgnoreCase("es"))
        {


        }else if(LocaleHelper.getLanguage(InicioActivity.this).equalsIgnoreCase("en")){


        }


        return true;
    }



    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        super.onOptionsItemSelected(item);
        if (item.getItemId() == R.id.buscar_contactos_menu){
            switch(Valor1){
                case 0:
                    setTitle("Buscar");
                case 1:
                    setTitle("Search");
            }

            TBuscarAmigosWhats();
        }

        if (item.getItemId() == R.id.miperfil_menu){
            Intent intent = new Intent(InicioActivity.this, MiperfilActivity.class);
            startActivity(intent);
        }
        if (item.getItemId() == R.id.cerrarsesion_menu){
            mAuth.signOut();
            EnviarALogin();
        }
        return true;
    }



    private void CrearNuevoGrupo() {
        AlertDialog.Builder builder = new AlertDialog.Builder(InicioActivity.this, R.style.AlertDialog);
        builder.setTitle("Nombre del grupo: ");

        final EditText nombregrupo = new EditText(InicioActivity.this);
        nombregrupo.setHint("ejm. Avengers");
        builder.setView(nombregrupo);

        builder.setPositiveButton("Crear", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String nombreg = nombregrupo.getText().toString();

                if (TextUtils.isEmpty(nombreg)){
                    Toast.makeText(InicioActivity.this, "Ingrese el nombre del grupo", Toast.LENGTH_SHORT).show();
                }else{
                    CrearGrupoFirebase(nombreg);
                }
            }
        }).setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();


    }

    private void CrearGrupoFirebase(String nombreg) {

        RootRef.child(nombreg).setValue("").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    Toast.makeText(InicioActivity.this, "Grupo creado con exito....", Toast.LENGTH_SHORT).show();
                }else{
                    String error = task.getException().getMessage().toString();
                    Toast.makeText(InicioActivity.this, "Error: "+error, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    private void ActualizarActividad( String estado){
        String CurrentTime, CurrentDate;
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy");
        CurrentDate = dateFormat.format(calendar.getTime());
        SimpleDateFormat dateFormat1 = new SimpleDateFormat("hh:mm a");
        CurrentTime = dateFormat1.format(calendar.getTime());

        HashMap<String, Object> EstadoOnline = new HashMap<>();
        EstadoOnline.put("hora", CurrentTime);
        EstadoOnline.put("fecha", CurrentDate);
        EstadoOnline.put("E", estado);

        UserRef.child(CurrentUserId).child("estadoUser").updateChildren(EstadoOnline);

    }



}