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
import org.json.JSONObject;


import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    SQLiteDatabase baseDatos = null;
    RecyclerView listadoDinamico;
    private RecyclerView.Adapter adaptador;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listadoDinamico = (RecyclerView) findViewById(R.id.rcrView);

        //Llamada a la función para la obtención de las razas mediante el webAPi:

        descargarRazaPerros();

    }

    //Métodos para la consulta de todas las razas en el WebApi.
    //Función para la obtención de todos los datos relativos a las razas disponibles.:
    public void descargarRazaPerros() {
        try {
            String url = "https://dog.ceo/api/breeds/list/all";
            new APIAsyncTask().execute(url);
        } catch (Exception e) {
            Log.e("ApiCallError", "Exception", e);
        }
    }
    //Función para obtener el resultado como String.
    public String contenidoObtenido(String url) {
        HttpClient httpClient = new DefaultHttpClient();
        String resultado = null;
        HttpGet httpGet = new HttpGet(url);
        HttpResponse httpResponse = null;
        InputStream stream = null;
        try {
            httpResponse = httpClient.execute(httpGet);
            HttpEntity entity = httpResponse.getEntity();

            if (entity != null) {
                stream = entity.getContent();
                resultado = convertirAString(stream);
            }
        } catch (Exception e) {
            Log.e("ApiResponseError", "Exception", e);
        } finally {
            try {
                if (stream != null) {
                    stream.close();
                }
            } catch (Exception e) {
                Log.e("ApiStreamError", "Exception", e);
            }
        }
        return resultado;
    }
    //Función que convierte la información obtenida en un String.
    public String convertirAString(InputStream inputStream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        String linea = "";
        String resultado = "";
        while ((linea = bufferedReader.readLine()) != null) {
            resultado += linea;
        }
        inputStream.close();
        return resultado;
    }
    //Clase que permite que la consulta se realice en segundo plano de forma asincrona.
    private class APIAsyncTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            return contenidoObtenido(urls[0]);
        }

        @Override
        protected void onPostExecute(String informacionObtenida) {
            Toast.makeText(getBaseContext(), "Información obtenida con éxito.", Toast.LENGTH_LONG).show();
            try {
                //La respuesta tiene "message" (contiene todas las razas) y "status". Se almacena en un JSONObject para convertirlo posteriormente a una lista.
                JSONObject jsonObject = new JSONObject(informacionObtenida);

                //Paso de JSONObject a lista ArrayList
                List<Raza> listado = convertirJsonRazas(jsonObject);

                /* Ahora se almacena la lista de razas en local, se hace aqui ya que es donde se tiene la información de forma segura. Se usa SQLite.
                   Primero se crea la clase BaseDatosRazasHelper, la cual contiene la creación de la base de datos y lasactualizaciones.
                */
                crearBaseDatosYGuardado(listado);

                //Tras esto se necesita una imagen aleatoria de cada raza, como estamos en un AsycTask, se pueden hacer llamadas a los métodos del webApi desde aqui

                //TODO Falta por terminar la consulta múltiple. De momento se ha realizado el RecyclerView con una imagen genérica.

                /*
                //PAra descargar una imagen aleatoria por raza, se tiene que hacer uso de un bucle
                for(int i = 0; i<listado.size(); i++){
                    descargarImagenRaza(listado.get(i).getRaza().toString());
                }

                //TODO Falta encontrar la imagen aleatoria de la raza para crear un lista de objetos que contengan ambos valores.
                //Para esto en principio haría falta realizar una consulta.

                //descargarImagenesRaza(List<Raza> listadoRazasBusqueda); //TODO

                */

                //Lista que se va a volcar en el RecyclerVIew.
                List<RazaImagen> listadoValores = new ArrayList<>();


                for (Raza raza : listado) {
                        RazaImagen razaImagen = new RazaImagen();
                        razaImagen.setRaza(raza.getRaza());
                        razaImagen.setUrl("https://images.dog.ceo/breeds/hound-afghan/n02088094_10715.jpg"); //TODO Imagen estandar hasta que consiga terminar la consulta múltiple.
                        listadoValores.add(razaImagen);

                }

                //Todo esto tiene que ir tras la segunda consulta. Una vez se tenga una Lista con las imagenes buenas.
                //Puesta en marcha del RecyclerView.
                listadoDinamico.setHasFixedSize(true);
                listadoDinamico.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                adaptador = new AdaptadorRazas(listadoValores, getApplicationContext());
                listadoDinamico.setAdapter(adaptador);


            } catch (Exception e) {
                Toast.makeText(getApplicationContext(), "Error en el jsonArray: " + e, Toast.LENGTH_LONG).show(); //TODO teniendo el log, poner algo mas nivel de usuario
                System.out.println(e);
                Log.e("APIAsyncTask", "Exception", e);

            }
        }

        public List<Raza> convertirJsonRazas(JSONObject jsonObject) throws JSONException {
            List<Raza> lista = new ArrayList<>();

            //Obtención de los valores del campo "mensaje"
            JSONObject jsonObjectConvertido = jsonObject.getJSONObject("message");
            //El mensaje es un String que contiene grupos de clave-valor. La mayoria de los valores están en blanco porque son aclaraciones de la raza.
            //Con un iterador, se recorren lo que son los campos "Clave", que en este caso son las razas, el valor que nos interesa. Se almacena en una Lista de Raza.
            Iterator iterador = jsonObjectConvertido.keys();

            while (iterador.hasNext()) {
                String razaObtenida = iterador.next().toString();

                Raza raza = new Raza();
                raza.setRaza(razaObtenida);

                lista.add(raza);
            }
            return lista;
        }


        public void crearBaseDatosYGuardado(List<Raza> lista) {
            //Creación de la base de datos haciendo uso de la clase BaseDatosRazasHelper
            BaseDatosRazasHelper bdhl = new BaseDatosRazasHelper(getApplicationContext(), "BaseDatosRazas", null, 1);

            baseDatos = bdhl.getWritableDatabase();
            //Una vez se tiene la base de datos con modo escritura, se hace una comprobación de que existe y se almacenan los valores de las razas.
            if (baseDatos != null) {
                int identificador = 0;
                for (int i = 0; i < lista.size(); i++) {
                    identificador += 1;
                    String nombreRaza = lista.get(i).getRaza();

                    baseDatos.execSQL("INSERT INTO Razas (id, nombre) " + "VALUES (" + identificador + ", '" + nombreRaza + "')");
                }
                Toast.makeText(getApplicationContext(), "Se ha alamcenado la información en SQLite", Toast.LENGTH_LONG).show();
            }
        }
    }

    //TODO En construcción. Lo silencio ya que no está terminado. Aún así tocaría refactorizar.
    /*
    //Metodos para la obtención de las imagenes:

    public void descargarImagenRaza(String nombreRaza) {
        try {
            String url = "https://dog.ceo/api/breed/" + nombreRaza + "/images/random";
            new APIAsyncTaskImages().execute(url);

        } catch (Exception e) {
           Log.e("APIAsyncTask:RndImg", "Exception", e);
        }
    }

    public String urlRazaObtenida(String url) {
        HttpClient httpClient = new DefaultHttpClient();
        String resultado = null;
        HttpGet httpGet = new HttpGet(url);
        HttpResponse httpResponse = null;
        InputStream stream = null;
        try {
            httpResponse = httpClient.execute(httpGet);
            HttpEntity entity = httpResponse.getEntity();

            if (entity != null) {
                stream = entity.getContent();
                resultado = convertirUrlAString(stream);
            }
        } catch (Exception e) {
            //TODO controlar excepción
        } finally {
            try {
                if (stream != null) {
                    stream.close();
                }
            } catch (Exception e) {
                //TODO controlar excepción
            }
        }
        return resultado;
    }

    public String convertirUrlAString(InputStream inputStream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        String linea = "";
        String resultado = "";
        while ((linea = bufferedReader.readLine()) != null) {
            resultado += linea;
        }
        inputStream.close();
        return resultado;
    }

    private class APIAsyncTaskImages extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            return urlRazaObtenida(urls[0]);
        }

        @Override
        protected void onPostExecute(String informacionObtenida) {
            Toast.makeText(getBaseContext(), "Información obtenida con éxito.", Toast.LENGTH_LONG).show();
            try {

                List<RazaImagen> listadoValores = new ArrayList<>(); //Está vacio, aún no se han añadido valores. Tengo pensado llenarlo con un for.

                listadoDinamico.setHasFixedSize(true);
                listadoDinamico.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                adaptador = new AdaptadorRazas(listadoValores, getParent().getApplicationContext());
                listadoDinamico.setAdapter(adaptador);


            } catch (Exception e) {
                Toast.makeText(getApplicationContext(), "Error en el jsonArray: " + e, Toast.LENGTH_LONG).show(); //TODO teniendo el log, poner algo mas nivel de usuario
                Log.e("APIAsyncTask", "Exception", e);

            }
        }

        public List<Raza> convertirJsonUrl(JSONObject jsonObject) throws JSONException {
            List<Raza> lista = new ArrayList<>();

            //Obtención de los valores del campo "mensaje"
            JSONObject jsonObjectConvertido = jsonObject.getJSONObject("message");
            //El mensaje es un String que contiene grupos de clave-valor. La mayoria de los valores están en blanco porque son aclaraciones de la raza.
            //Con un iterador, se recorren lo que son los campos "Clave", que en este caso son las razas, el valor que nos interesa. Se almacena en una Lista de Raza.
            Iterator iterador = jsonObjectConvertido.keys();

            while (iterador.hasNext()) {
                String razaObtenida = iterador.next().toString();

                Raza raza = new Raza();
                raza.setRaza(razaObtenida);

                lista.add(raza);
            }
            return lista;
        }




    }
    */
}

