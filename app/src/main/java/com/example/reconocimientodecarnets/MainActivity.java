package com.example.reconocimientodecarnets;


import static android.widget.Toast.makeText;
import static java.security.AccessController.getContext;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("ALL")
public class MainActivity extends AppCompatActivity {

    EditText ed_nro_doc,ed_fechavto,ed_complemento,ed_primer,ed_segundo,ed_paterno,ed_materno,ed_casada;
    Spinner sp_ext;
    Button reconocimiento, btn_guardar;
    RequestQueue requestQueue;
    private int posicion;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        reconocimiento = findViewById(R.id.reconocimiento);
        btn_guardar = findViewById(R.id.btn_guardar);
        requestQueue= Volley.newRequestQueue(MainActivity.this.getApplicationContext());

        posicion = 0;
        try {
            Bundle bundle = getIntent().getExtras();

            ed_nro_doc = findViewById(R.id.ed_nro_doc);
            ed_fechavto =findViewById(R.id.ed_fechavto);
            ed_complemento = findViewById(R.id.ed_complemento);
            ed_primer = findViewById(R.id.ed_primer);
            ed_segundo = findViewById(R.id.ed_segundo);
            ed_paterno = findViewById(R.id.ed_paterno);
            ed_materno = findViewById(R.id.ed_materno);
            ed_casada = findViewById(R.id.ed_casada);

            sp_ext = findViewById(R.id.sp_ext);
            ArrayList<String> listaDepartamentos = new ArrayList<>();
            listaDepartamentos.add("");
            listaDepartamentos.add("CH");
            listaDepartamentos.add("LP");
            listaDepartamentos.add("CB");
            listaDepartamentos.add("OR");
            listaDepartamentos.add("PO");
            listaDepartamentos.add("TJ");
            listaDepartamentos.add("SC");
            listaDepartamentos.add("BE");
            listaDepartamentos.add("PA");

            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, listaDepartamentos);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            sp_ext.setAdapter(adapter);
            posicion = (bundle.getString("EXTENSION").equals(""))?0:listaDepartamentos.indexOf(bundle.getString("EXTENSION"));


            ed_nro_doc.setText(bundle.getString("nro"));
            ed_fechavto.setText(bundle.getString("VENCIMIENTO"));
            sp_ext.setSelection(posicion);
            ed_complemento.setText(bundle.getString("complemento"));
            ed_primer.setText(bundle.getString("PRIMER_NOMBRE"));
            ed_segundo.setText(bundle.getString("SEGUNDO_NOMBRE"));
            ed_paterno.setText(bundle.getString("AP_PATERNO"));
            ed_materno.setText(bundle.getString("AP_MATERNO"));
            ed_casada.setText(bundle.getString("AP_CASADA"));
        }catch (Exception e){
            String mensaje = e.getMessage();
            e.getMessage();
        }

        reconocimiento.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, MenuPrincipal.class);
                startActivity(intent);
            }
        });
        btn_guardar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                ejecutarServicio("http://181.115.207.107/psolicitudes/sc_insertar_datos_imagen.php");
            }
        });
    }


    private void ejecutarServicio(String URL){
        ProgressDialog  progreso=new ProgressDialog(MainActivity.this);
        progreso.setMessage("Cargando...");
        progreso.show();
        StringRequest stringRequest=new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                progreso.hide();
                makeText(MainActivity.this.getApplicationContext() , "Operacion Exitosa", Toast.LENGTH_SHORT).show();
                mensaje_exito();
                limpiar_campos();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                progreso.hide();
                makeText(MainActivity.this.getApplicationContext() , error.toString(), Toast.LENGTH_SHORT).show();
            }
        }){
            @Override
            protected Map<String,String> getParams() throws AuthFailureError {
                Map<String,String> parametros=new HashMap<String, String>();

                parametros.put("primer_nombre", ed_primer.getText().toString());
                parametros.put("segundo_nombre", ed_segundo.getText().toString());
                parametros.put("apellido_paterno", ed_paterno.getText().toString());
                parametros.put("apellido_materno", ed_materno.getText().toString());
                parametros.put("apellido_casada", ed_casada.getText().toString());
                parametros.put("nro_carnet", ed_nro_doc.getText().toString());
                parametros.put("complemento", ed_complemento.getText().toString());
                parametros.put("fecha_vencimiento", ed_complemento.getText().toString());

                //  Toast.makeText(getActivity().getApplicationContext(), "Antes indice: "+ ed_fechavto.getText().toString(), Toast.LENGTH_SHORT).show();

                return parametros;
            }
        };
        requestQueue= Volley.newRequestQueue(MainActivity.this.getApplicationContext());
        requestQueue.add(stringRequest);
    }

    private void mensaje_exito(){

        AlertDialog.Builder dialogo2 = new AlertDialog.Builder(MainActivity.this);
        dialogo2.setTitle("Exito: Información guardada");
        dialogo2.setMessage("Se guardo la información correctamente");

        dialogo2.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogo2, int id) {
                // limpiarDatos();
                //  finish();
            }
        });
        dialogo2.show();

    }

    public void limpiar_campos() {
        ed_primer.setText("");
        ed_nro_doc.setText("");
        ed_complemento.setText("");
        ed_segundo.setText("");
        ed_paterno.setText("");
        ed_materno.setText("");
        ed_casada.setText("");
        ed_fechavto.setText("");
    }

}