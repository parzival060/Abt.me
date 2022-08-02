package azoftware.com.whatsappro;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

public class ScanActivity  extends AppCompatActivity {

    TextView recipiente;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


       //* findViewById(R.id.recipiente);

        new IntentIntegrator(this).initiateScan();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode,resultCode,data);

            IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);

            String dates = result.getContents();

            recipiente.setText(dates);
    }
}
