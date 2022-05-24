package com.example.pruebadogapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class DetalleRaza extends AppCompatActivity {
    TextView nombreRaza;
    ViewPager2 viewPager2;
    AdaptadorViewPager2 adaptadorViewPager2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalle_raza);
        nombreRaza = (TextView) findViewById(R.id.lblViewPager2);
        viewPager2 = (ViewPager2) findViewById(R.id.vp2Imagenes);

        //Recepción de la raza seleccionada:

        Bundle bundle = getIntent().getExtras();
        String razaSeleccionada = bundle.getString("razaSeleccionada");

        //Se indica la raza seleccionada encima del ViewPager2.

        nombreRaza.setText(razaSeleccionada.toUpperCase());


        //Una vez se tiene la raza, se realiza la consulta al webApi:

        descargarImagenesRaza(razaSeleccionada);

    }

    public void descargarImagenesRaza(String criterioBusqueda){
        try{
            //Se solicitan cinco imagenes distintas aleatorias de una raza en concreto.
            String url = "https://dog.ceo/api/breed/"+criterioBusqueda+"/images/random/5";

            new APIAsyncTask().execute(url);

        }catch(Exception e){
            Log.e("APICallError", "Exception", e);
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
            Log.e("APIResponseError", "Exception", e);
        }finally{
            try{
                if(stream != null){
                    stream.close();
                }
            }catch(Exception e){
                Log.e("StreamError", "Exception", e);
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
                //La respuesta tiene "message" (contiene todas las urls de las imagenes) y "status". Se almacena en un JSONObject para convertirlo posteriormente a una lista de urls.
                JSONObject jsonObject = new JSONObject(informacionObtenida);

                List<String> listado = convertirJsonImagenesURL(jsonObject);

               //Una vez se tienen los datos, se genera el ViewPAger2.
                LayoutInflater layoutInflater = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                adaptadorViewPager2 = new AdaptadorViewPager2(getApplicationContext(),listado);
                viewPager2.setAdapter(adaptadorViewPager2);

            }catch(Exception e){
                Log.e("AsyncTask","Exception",e);
            }
        }
    }
    public List<String> convertirJsonImagenesURL(JSONObject jsonObject) throws JSONException {

        //En este caso el mensaje del jsonObject viene como JSONArray, por lo que es mas sencillo de manejar.

        JSONArray jsonArray = jsonObject.getJSONArray("message");

        List<String> listadoImagenesRaza = new ArrayList<>();

        for(int i = 0; i<jsonArray.length(); i++){
            listadoImagenesRaza.add(jsonArray.get(i).toString());
        }
        return listadoImagenesRaza;
    }
}