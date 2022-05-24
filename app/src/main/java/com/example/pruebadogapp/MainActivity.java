package com.example.pruebadogapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

//apache
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;


import android.os.AsyncTask;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    SQLiteDatabase baseDatos = null;
    RecyclerView listadoDinámico;
    private RecyclerView.Adapter adaptador;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listadoDinámico = (RecyclerView) findViewById(R.id.rcrView);

        //Llamada a la función para la obtención de las razas mediante el webAPi:

        descargarRazaPerros();

        //Llamada a la función para la creación y almacenamiento de los valores obtenidos:
        //Se llama dentro de la función que descarga los datos de la API.


        //TODO Mostrar la información descargada en un RecyclerVIew (nombre Raza + imagen aleatoria de las obtenidas con la url)


        //TODO añadir un OnCLick --> nueva actividad con un ViewPAger con varias fotografias de la raza seleccionada.

        //Todo subir a GitHub

    }
    //TODO Descargar lista de las razas:
    //Función para la descarga de la información:
    public void descargarRazaPerros(){
        try{
            String url = "https://dog.ceo/api/breeds/list/all";
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

    public String convertirAString(InputStream inputStream) throws IOException{
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        String linea = "";
        String resultado = "";
        while((linea = bufferedReader.readLine()) != null){
            resultado += linea;
          }
        inputStream.close();
        return  resultado;
    }

    private class APIAsyncTask extends AsyncTask<String, Void, String>{
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
                List<Raza> listado = convertirJsonRazas(jsonArray);

                //TODO pasar lista a metodo que almacene en la base de datos local
                crearBaseDatosYGuardado(listado);

                //TODO Falta encontrar la imagen aleatoria de la raza para crear un lista de objetos que contengan ambos valores.
                //Para esto en principio haría falta realizar una consulta.

                descargarImagenesRaza(List<Raza> listadoRazasBusqueda);

                List<RazaImagen> listadoValores = new ArrayList<>(); //Está vacio, aún no se han añadido valores. Tengo pensado llenarlo con un for.

                listadoDinámico.setHasFixedSize(true);
                listadoDinámico.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                adaptador = new AdaptadorRazas(listadoValores,getParent().getApplicationContext());
                listadoDinámico.setAdapter(adaptador);



            }catch(Exception e){

            }
        }
    }
    public List<Raza> convertirJsonRazas(JSONArray jsonArray) throws JSONException{
        List<Raza> lista = new ArrayList<>();
        for (int i = 0; i< jsonArray.length(); i++){
            Raza raza =  new Raza();

            String razaPerro = jsonArray.getJSONObject(i).optString("message").toString(); // message es lo que aparece en el WebApi, comprobar por si acaso.
            raza.setRaza(razaPerro);
            lista.add(raza);
        }
        return lista;
    }

    //TODO Guardar la lista en SQLite

    public void crearBaseDatosYGuardado( List<Raza> lista){
        BaseDatosRazasHelper bdhl = new BaseDatosRazasHelper(this, "BaseDatosRazas", null, 1);

        baseDatos = bdhl.getWritableDatabase();
        //Una vez se tiene la base de datos, se hace una comprobación de que existe y se almacenan los valores.
        if( baseDatos != null){
            for(int i = 0; i<lista.size(); i++){
                int identificador = i++;
                String nombreRaza = lista.get(i).getRaza();

                baseDatos.execSQL("INSERT INTO Razas (id, nombre) " + "VALUES (" + identificador + ", '" + nombreRaza + "')" );

                Toast.makeText(getApplicationContext(), "Se ha alamcenado la información en SQLite", Toast.LENGTH_LONG).show();
            }
        }
    }





}

