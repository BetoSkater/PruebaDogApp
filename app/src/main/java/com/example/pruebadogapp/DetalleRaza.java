package com.example.pruebadogapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DetalleRaza extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalle_raza);

        //Recepción de la raza seleccionada:

        Bundle bundle = getIntent().getExtras();
        String razaSeleccionada = bundle.getString("razaSeleccionada");

        //Una vez se tiene la raza, se realiza la consulta al webApi:

        descargarRazaPerros(razaSeleccionada);




    }

    public void descargarRazaPerros(String criterioBusqueda){
        try{
            //Se solicitan cinco imagenes distintas aleatorias de una raza en concreto.
            String url = "https://dog.ceo/api/"+criterioBusqueda+"breed/hound/images/random/5";

            new APIAsyncTask().execute(url);

        }catch(Exception e){
            //TODO controlar excepción
        }
    }
    public String contenidoObtenido(String url){
        HttpClient httpClient = new DefaultHttpClient();
        String resultado = null;
        HttpGet httpGet = new HttpGet(url);
        HttpResponse httpResponse = null;
        InputStream stream = null;
        try{
            httpResponse = httpClient.execute(httpGet);
            HttpEntity entity = httpResponse.getEntity();

            if(entity != null){
                stream = entity.getContent();
                resultado = convertirAString(stream);
            }
        } catch(Exception e){
            //TODO controlar excepción
        }finally{
            try{
                if(stream != null){
                    stream.close();
                }
            }catch(Exception e){
                //TODO controlar excepción
            }
        }
        return resultado;
    }

    public String convertirAString(InputStream inputStream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        String linea = "";
        String resultado = "";
        while((linea = bufferedReader.readLine()) != null){
            resultado += linea;
        }
        inputStream.close();
        return  resultado;
    }

    private class APIAsyncTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String...urls){
            return contenidoObtenido(urls[0]);
        }
        @Override
        protected void onPostExecute(String informacionObtenida){
            Toast.makeText(getBaseContext(),"Información obtenida con éxito.", Toast.LENGTH_LONG).show();
            try{
                //Conversión a JSON
                JSONArray jsonArray = new JSONArray(informacionObtenida);
                List<String> imagenesURL  = convertirJsonImagenesURL(jsonArray);

                //TODO pasar la lista al Carusel

               //Aquí imagino que es donde  tengo que colocar el adaptador del carrusel.

            }catch(Exception e){

            }
        }
    }
    public List<String> convertirJsonImagenesURL(JSONArray jsonArray) throws JSONException {

        String resultado = "";

        for (int i = 0; i< jsonArray.length(); i++){
            resultado = jsonArray.getJSONObject(i).optString("message").toString(); // message es lo que aparece en el WebApi, comprobar por si acaso.
        }

        //Según el webApi, el campo mensaje va a contener un string con las url separadas por comas, en este caso cinco urls.
        List<String> listaImagenes = Arrays.asList(resultado.split(","));
        return listaImagenes;
    }


}