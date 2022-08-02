package azoftware.com.whatsappro;

import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.squareup.picasso.Picasso;

public class ImagenActivity extends AppCompatActivity {

    private ImageView imageview;
    private String imageUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_imagen);

        imageview = (ImageView)findViewById(R.id.imagen_view);
        imageUrl= getIntent().getStringExtra("url");

        Picasso.get().load(imageUrl).into(imageview);
    }
}