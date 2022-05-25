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
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
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
    List<String> razasFinales = new ArrayList<>();

    TextView lblTitulo;
    ProgressBar barraCarga;
    RecyclerView listadoDinamico;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        lblTitulo = (TextView) findViewById(R.id.lblListaCarga);
        barraCarga = (ProgressBar) findViewById(R.id.prbBarra);
        listadoDinamico = (RecyclerView) findViewById(R.id.rcrView);

        barraCarga.setVisibility(View.VISIBLE);
        listadoDinamico.setVisibility(View.INVISIBLE);

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
    //Función que realiza la petición GET y devuelve el resultado como String.
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
    //Función que convierte la información obtenida (InputStream) en un String.
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
            lblTitulo.setText("Obteniendo el listado de todas las razas de perro");
            lblTitulo.setTextSize(15); //TODO
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

                //25/05: Se añaden las razas a un List<String>, hubiese sido más cómodo haber realizado toda esta parte así desde el principio.
                for(Raza razaBucle : listado){
                    razasFinales.add(razaBucle.getRaza().toString());
                }

                /* Ahora se almacena la lista de razas en local, se hace aqui ya que es donde se tiene la información de forma segura. Se usa SQLite.
                   Primero se crea la clase BaseDatosRazasHelper, la cual contiene la creación de la base de datos y las actualizaciones de la misma.
                */

                lblTitulo.setText("Almacenando los valores en SQLite");
                crearBaseDatosYGuardado(listado);

                /*
                Tras esto se necesita una imagen aleatoria de cada raza. Como se tienen que realizar 95 consultas a la vez, se han creado nuevos métodos especificos
                para realizar estas 95 consultas de golpe. Lo más correcto hubiese sido haber creado la lógica de las consultas en otro fichero estructurando
                los flujos de código. Por lo que haría falta una refactorización.
                 */

                lblTitulo.setText("Obtenieno una imagen aleatoria por cada raza");

                descargarImagenRaza(listado); //Llamada a la función para iniciar la consulta de las imágenes.

            } catch (Exception e) {
                Toast.makeText(getApplicationContext(), "Error en el jsonArray: " + e, Toast.LENGTH_LONG).show();
                System.out.println(e);
                Log.e("APIAsyncTask", "Exception", e);

            }
        }

        public List<Raza> convertirJsonRazas(JSONObject jsonObject) throws JSONException {
            List<Raza> lista = new ArrayList<>();

            //Obtención de los valores del campo "message"
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

        //Función para la creación y alamcenamiento de la lsita de las razas.
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


    //Metodos relativos a las consultas al WebAPi para la obtención de las imagenes:

    public void descargarImagenRaza(List<Raza> listaRazasSinFoto) {

        try {
            //Lo primero es obtener todas las urls necesarias.
            List<String> urlObtenidas = obtenerURLporRaza(listaRazasSinFoto);

            //Se traspasan los valores de la List<String> a String[] porque es lo que necesita el método execute.
            String[] url = new String[urlObtenidas.size()];
            for(int i = 0; i<url.length; i++){
                url[i] = urlObtenidas.get(i);
            }

            new APIAsyncTaskImages().execute(url); //TODO ojo con el Runnable

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
           //return urlRazaObtenida(urls[0]);
            String respuestaObtenida = "";
           try{
               for(int i = 0; i<urls.length; i++){
                   String respuestaBruta = urlRazaObtenida(urls[i]); //String tipo JSONobject, Keys -> "message" y "status"
                   JSONObject jsonObject = new JSONObject(respuestaBruta);
                   String enlace = jsonObject.get("message").toString(); //Se extrae el valor del campo mensaje (La url que se puede poner directamente en el navegador)

                  //Como el método doInBackGround(...) devuelve un String, no se puede crear una lista.
                   // Se concatena la respuesta con comas para posteriormente cortar por las comas y poder tener un listado.
                   if(i< urls.length - 1) {
                       respuestaObtenida = respuestaObtenida + enlace + ",";
                   }else{
                       respuestaObtenida = respuestaObtenida + enlace;
                   }
               }
           }catch(Exception e){
               Log.e("JSONObjectEx","Exception",e);
           }
            return respuestaObtenida;
        }

        @Override
        protected void onPostExecute(String informacionObtenida) {
            Toast.makeText(getBaseContext(), "Información obtenida con éxito.", Toast.LENGTH_LONG).show();
            try {
                List<RazaImagen> listadoRecycler = new ArrayList<>();

                String[] urlFinales = informacionObtenida.split(",");

                for(int i = 0; i < razasFinales.size(); i++){
                    RazaImagen perro = new RazaImagen();
                    perro.setRaza(razasFinales.get(i).toString());
                    perro.setUrl(urlFinales[i]);

                    listadoRecycler.add(perro);
                }
                lblTitulo.setText("Listado de todas las razas de perro:");
                lblTitulo.setTextSize(25);
                barraCarga.setVisibility(View.INVISIBLE);

                //Carga del RecyclerView
                listadoDinamico.setVisibility(View.VISIBLE);
                listadoDinamico.setHasFixedSize(true);
                listadoDinamico.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                RecyclerView.Adapter adaptador = new AdaptadorRazas(listadoRecycler, getApplicationContext());
                listadoDinamico.setAdapter(adaptador);

            } catch (Exception e) {
                Toast.makeText(getApplicationContext(), "Error en el jsonArray: " + e, Toast.LENGTH_LONG).show(); //TODO teniendo el log, poner algo mas nivel de usuario
                Log.e("APIAsyncTask", "Exception", e);
            }
        }

    }
    //función para generar la lista de urls necesarias para la consulta. Cada url es especifica para cada raza.
    public List<String> obtenerURLporRaza(List<Raza> listadoExtracción){
        List<String> listadoUrl = new ArrayList<>();

        for(Raza raza : listadoExtracción){
            String urlRaza = "https://dog.ceo/api/breed/" + raza.getRaza().toString() + "/images/random";
            listadoUrl.add(urlRaza);
        }
        return listadoUrl;
    }
}

