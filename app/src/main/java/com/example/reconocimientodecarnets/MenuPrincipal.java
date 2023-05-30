package com.example.reconocimientodecarnets;


import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import Helpers.Carnet;


public class MenuPrincipal extends AppCompatActivity {

    private Button reconocerTexto, camara, galeria,camara2, galeria2;
    private ImageView imagen, imagen2;
    private Uri uri = null;
    private Uri uri2 = null;
    private ProgressDialog progressDialog;
    private TextRecognizer textRecognizer;
    private TextRecognizer textRecognizer2;
    private TextView txtView, txtView2, txtTodo;
    private String textoRecopilado;
    private Carnet carnet;
    private int grados = 360;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_principal);

        this.textoRecopilado = "";

        reconocerTexto = findViewById(R.id.ReconocerTexto);
        imagen = findViewById(R.id.imagen);
        imagen2 = findViewById(R.id.imagen2);
        txtView = findViewById(R.id.txtView);
        txtView2 = findViewById(R.id.txtView2);
        txtTodo = findViewById(R.id.txtTodo);
        camara = findViewById(R.id.Camara);
        galeria = findViewById(R.id.Galeria);
        camara2 = findViewById(R.id.Camara2);
        galeria2 = findViewById(R.id.Galeria2);

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Espere por favor");
        progressDialog.setCanceledOnTouchOutside(false);

        textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
        textRecognizer2 = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);

        reconocerTexto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(uri == null && uri2 == null){
                    Toast.makeText(MenuPrincipal.this,"Por favor seleccione una imagen", Toast.LENGTH_LONG).show();
                }else{
                    reconocerTextoImagen();
                }
            }
        });

        camara.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                abrirCamara();
            }
        });

        camara2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                abrirCamara2();
            }
        });

        galeria.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                abrirGaleria();
            }
        });

        galeria2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                abrirGaleria2();
            }
        });
    }

    private void reconocerTextoImagen() {
        progressDialog.setMessage("Preparando imagen");
        progressDialog.show();;

        try {
            InputImage inputImage = InputImage.fromFilePath(this,uri);
            InputImage inputImage2 = InputImage.fromFilePath(this,uri2);
            progressDialog.setTitle("Reconociendo texto");

            Task<Text> textTask = textRecognizer.process(inputImage).addOnSuccessListener(new OnSuccessListener<Text>() {
                @Override
                public void onSuccess(Text text) {
                    progressDialog.dismiss();
                    List<Text.TextBlock> lista = text.getTextBlocks();
                    String texto = text.getText();
                    Map<String, Object> map = new HashMap<>();
                    carnet = new Carnet();
                    carnet.setTexto(texto);
                    carnet.setListaBloqueAnverso(lista);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    progressDialog.dismiss();
                    Toast.makeText(MenuPrincipal.this,"No pudo reconocer el texto debido a: "+e.getMessage() , Toast.LENGTH_LONG).show();
                }
            });
            Task<Text> textTask2 = textRecognizer2.process(inputImage2).addOnSuccessListener(new OnSuccessListener<Text>() {
                @Override
                public void onSuccess(Text text) {
                    progressDialog.dismiss();
                    List<Text.TextBlock> lista = text.getTextBlocks();
                    String texto = text.getText();
                    Map<String, Object> map = new HashMap<>();
                    carnet.setListaBloqueReverso(lista);
                    carnet.setTexto("\n"+texto);
                    //map.put("CARNET",carnet.obtenerDatos());
                    try {
                        txtView.setText(carnet.imprimir(map));
                        txtTodo.setText(carnet.getTexto());
                        Bundle bundle = new Bundle();
                        Bundle datos = carnet.obtenerDatos();
                        Intent intent = new Intent(MenuPrincipal.this, MainActivity.class);
                        intent.putExtras(datos);
                        startActivity(intent);
                    } catch (Exception e) {
                        txtView.setText(e.getMessage());
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    progressDialog.dismiss();
                    Toast.makeText(MenuPrincipal.this,"No pudo reconocer el texto debido a: "+e.getMessage() , Toast.LENGTH_LONG).show();
                }
            });

        } catch (IOException e) {
            Toast.makeText(MenuPrincipal.this,"Error al preparar la imagen: "+e.getMessage() , Toast.LENGTH_LONG).show();
        }
    }

    private void abrirGaleria(){
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        galeriaARL.launch(intent);
    }

    private void abrirGaleria2(){
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        galeriaARL2.launch(intent);
    }

    private ActivityResultLauncher<Intent> galeriaARL = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            txtView.setText("");
            txtView2.setText("");
            if(result.getResultCode() == Activity.RESULT_OK){
                Intent data = result.getData();
                uri = data.getData();
                imagen.setImageURI(uri);
            }else{
                Toast.makeText(MenuPrincipal.this,"Cancelado por el usuario", Toast.LENGTH_LONG).show();
            }
        }
    });
    private ActivityResultLauncher<Intent> galeriaARL2 = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            txtView.setText("");
            txtView2.setText("");
            if(result.getResultCode() == Activity.RESULT_OK){
                Intent data = result.getData();
                uri2 = data.getData();
                imagen2.setImageURI(uri2);
            }else{
                Toast.makeText(MenuPrincipal.this,"Cancelado por el usuario", Toast.LENGTH_LONG).show();
            }
        }
    });

    private void abrirCamara(){
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE,"Titulo");
        values.put(MediaStore.Images.Media.DESCRIPTION,"Descripcion");

        uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,values);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT,uri);
        camaraARL.launch(intent);
    }

    private ActivityResultLauncher<Intent> camaraARL = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            txtView.setText("");
            txtView2.setText("");
            if(result.getResultCode() == Activity.RESULT_OK){
                imagen.setImageURI(uri);
            }else{
                Toast.makeText(MenuPrincipal.this,"Cancelado por el usuario", Toast.LENGTH_LONG).show();
            }
        }
    });

    private void abrirCamara2(){
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE,"Titulo");
        values.put(MediaStore.Images.Media.DESCRIPTION,"Descripcion");

        uri2 = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,values);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT,uri2);
        camaraARL2.launch(intent);
    }

    private ActivityResultLauncher<Intent> camaraARL2 = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            txtView.setText("");
            txtView2.setText("");
            if(result.getResultCode() == Activity.RESULT_OK){
                imagen2.setImageURI(uri2);
            }else{
                Toast.makeText(MenuPrincipal.this,"Cancelado por el usuario", Toast.LENGTH_LONG).show();
            }
        }
    });

    public void rotarDerecha(View vista){
        if(grados>=360){
            imagen.setRotation(360);
        }else {
            grados+=90;
            imagen.setRotation(grados);
        }
    }

    public void rotarIzquierda(View vista){
        if(grados<=0){
            imagen.setRotation(0);
        }else {
            grados-=90;
            imagen.setRotation(grados);
        }
    }
}