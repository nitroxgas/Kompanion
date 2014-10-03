package br.com.casadalagoa.kompanion;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;


public class principal extends ActionBarActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks {

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;

    //public  HttpAsyncTask mHttpAsyncTask = new HttpAsyncTask();
    public String servidor = "http://192.168.1.110/sd/KompanionIrTempDHT_1_final/do.php";
    public final byte dispositivo = 1;
    boolean showConfig = true;
    public String data="";
    boolean conectar = true;
    public SharedPreferences mPrefs;


    public String getCurrentSsid(Context context) {

        String ssid = null;
        ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (networkInfo.isConnected()) {
            final WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            final WifiInfo connectionInfo = wifiManager.getConnectionInfo();
            if (connectionInfo != null && !(connectionInfo.getSSID().equals(""))) {
                //if (connectionInfo != null && !StringUtil.isBlank(connectionInfo.getSSID())) {
                ssid = connectionInfo.getSSID();
            }
        }
        return ssid;
    }

    private void Conectar() {
        // Verifica se há conexão e faz solicitação inicial
        if (isConnected()) {
            ConnectivityManager connectivityManager = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();
            // NetworkInfo mobNetInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

            if (activeNetInfo != null) {
                if (activeNetInfo.getTypeName().contains("WIFI")) {
                    if (showConfig)
                        Toast.makeText(getBaseContext(), "WIFI SSID (" + getCurrentSsid(this) + ")", Toast.LENGTH_LONG).show();
                    if (getCurrentSsid(this).contains("GeorgeHome")) {
                        servidor = mPrefs.getString("servidor_casa", "http://192.168.1.110/sd/KompanionIrTempDHT_1_final/do.php");
                        new HttpAsyncTask().execute(servidor, "");
                        if (showConfig)
                            Toast.makeText(getBaseContext(), "Acessando rede local casa...", Toast.LENGTH_LONG).show();
                        conectar = false;
                    } else {
                        servidor = mPrefs.getString("servidor_kompanion", "http://192.168.240.1/sd/KompanionIrTempDHT_1_final/do.php");
                        Toast.makeText(this, "Rede wifi externa a casa...", Toast.LENGTH_SHORT).show();
                        new HttpAsyncTask().execute(servidor,"");
                        conectar = false;
                    }

                } else {
                    servidor = mPrefs.getString("servidor_externo", "http://georgesilva.ddns.net:9898/sd/KompanionIrTempDHT_1_final/do.php");
                    Toast.makeText(this, "Rede Móvel", Toast.LENGTH_SHORT).show();
                    new HttpAsyncTask().execute(servidor,"");
                    conectar = false;
                }
            }
        } else {
            Toast.makeText(getBaseContext(), "Não Conectado!", Toast.LENGTH_LONG).show();
        }
        //if (showConfig) Log.v(LOG_TAG,"Conectar");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ac_principal);

       // new HttpAsyncTask().execute(servidor,"");

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));


        mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        showConfig = mPrefs.getBoolean("showConfig", false);
        Conectar();
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.container, PlaceholderFragment.newInstance(position + 1))
                .commit();
    }

    public void onSectionAttached(int number) {
        switch (number) {
            case 1:
                mTitle = getString(R.string.title_section1);
                break;
            case 2:
                mTitle = getString(R.string.title_section2);
                break;
            case 3:
                mTitle = getString(R.string.title_section3);
                break;
        }
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.principal, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_sair) {
            finish();
            return true;
        } else if (id == R.id.action_atualizar) {
            new HttpAsyncTask().execute(servidor,"");
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public PlaceholderFragment() {
        }


        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {

            final View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            Spinner spinner = (Spinner) rootView.findViewById(R.id.lista_devices);
            final ImageView imagem = (ImageView) rootView.findViewById(R.id.img_ar);
            ArrayAdapter<CharSequence> adapter;
            switch (getArguments().getInt(ARG_SECTION_NUMBER)) {
                case 1:  // Climatização
                    rootView.findViewById(R.id.lay_clima).setVisibility(View.VISIBLE);
                    rootView.findViewById(R.id.lay_devices).setVisibility(View.GONE);
                    rootView.findViewById(R.id.lay_ilum).setVisibility(View.GONE);

                    NumberPicker temp = (NumberPicker) rootView.findViewById(R.id.numberPicker);
                    temp.setMaxValue(30); temp.setMinValue(17);


                    adapter = ArrayAdapter.createFromResource(rootView.getContext(), R.array.condicionadores_array, android.R.layout.simple_spinner_item);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinner.setAdapter(adapter);

                    Spinner mode_spiner = (Spinner) rootView.findViewById(R.id.md_spinner);
                    ArrayAdapter<CharSequence> mode_adp = ArrayAdapter.createFromResource(rootView.getContext(), R.array.mode_values, android.R.layout.simple_spinner_item);
                    mode_adp.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    mode_spiner.setAdapter(mode_adp);

                    Spinner fan_spiner = (Spinner) rootView.findViewById(R.id.fan_spinner);
                    ArrayAdapter<CharSequence> fan_adp = ArrayAdapter.createFromResource(rootView.getContext(), R.array.fan_values, android.R.layout.simple_spinner_item);
                    fan_adp.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    fan_spiner.setAdapter(fan_adp);

                    spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                            switch (adapterView.getSelectedItemPosition()) {
                                case 0:
                                    imagem.setImageResource(R.drawable.ic_cassete_bg);
                                    break;
                                case 1:
                                    imagem.setImageResource(R.drawable.ic_cassete_bg);
                                    break;
                                case 2:
                                    imagem.setImageResource(R.drawable.ic_higlass);
                                    break;
                            }

                        }

                        public void onNothingSelected(AdapterView<?> parent) {
                            // Another interface callback
                        }
                    });
                    break;
                case 2:
                    rootView.findViewById(R.id.lay_clima).setVisibility(View.GONE);
                    rootView.findViewById(R.id.lay_devices).setVisibility(View.GONE);
                    rootView.findViewById(R.id.lay_ilum).setVisibility(View.VISIBLE);
                    adapter = ArrayAdapter.createFromResource(rootView.getContext(), R.array.ilum_array, android.R.layout.simple_spinner_item);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinner.setAdapter(adapter);
                    spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                            switch (adapterView.getSelectedItemPosition()){
                                case 0:
                                    imagem.setImageResource(R.drawable.ic_launcher);
                                    break;
                                case 1:
                                    imagem.setImageResource(R.drawable.bg_ft_logo);
                                    break;
                                case 2:
                                    imagem.setImageResource(R.drawable.ic_launcher);
                                    break;
                            }

                        }
                        public void onNothingSelected(AdapterView<?> parent) {
                            // Another interface callback
                        }
                    });
                    break;
                case 3:
                    rootView.findViewById(R.id.lay_clima).setVisibility(View.GONE);
                    rootView.findViewById(R.id.lay_devices).setVisibility(View.VISIBLE);
                    rootView.findViewById(R.id.lay_ilum).setVisibility(View.GONE);
                    adapter = ArrayAdapter.createFromResource(rootView.getContext(), R.array.devices_array, android.R.layout.simple_spinner_item);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinner.setAdapter(adapter);
                    spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                            switch (adapterView.getSelectedItemPosition()){
                                case 0:
                                    imagem.setImageResource(R.drawable.ic_launcher);
                                    break;
                                case 1:
                                    imagem.setImageResource(R.drawable.bg_ft_logo);
                                    break;
                                case 2:
                                    imagem.setImageResource(R.drawable.ic_launcher);
                                    break;
                            }
                        }
                        public void onNothingSelected(AdapterView<?> parent) {
                            // Another interface callback
                        }
                    });
                    break;
            }


            return rootView;
        }

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            ((principal) activity).onSectionAttached(
                    getArguments().getInt(ARG_SECTION_NUMBER));

        }
    }

    // Create GetText Metod
    public static String PostText(String uri, String data)  throws  UnsupportedEncodingException
    {
        try
        {
            // Defined URL  where to send data
            if (uri.equals("")) {
                uri="http://192.168.1.110/sd/KompanionIrTempDHT_1_final/do.php";
            }
            URL url = new URL(uri);
            HttpURLConnection  conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            //conn.setDoOutput(true);
            //conn.setDoInput(true);
            //conn.setChunkedStreamingMode(0);

            OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
            wr.write( data );
            wr.flush();

            //String text = "";
            BufferedReader reader;
            // Get the server response

            reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String line;

            // Read Server Response
            while((line = reader.readLine()) != null)
            {
                // Append server response in string
                sb.append(line + "\n");
            }
            wr.close();
            reader.close();

            //conn.disconnect();
            return sb.toString();
        }
        catch(Exception ex)
        {
            return ex.toString();
        }
        finally
        {

        }
        // Show response on activity
        //Toast.makeText(getBaseContext(), "(" + text.toString() + ") ", Toast.LENGTH_LONG).show();
    }
    //
    // Sessão para tratar das comunicações:
    //
    public static String GET(String url){
        InputStream inputStream = null;
        String result = "";
        try {

            // create HttpClient
            HttpClient httpclient = new DefaultHttpClient();

            // make GET request to the given URL
            HttpResponse httpResponse = httpclient.execute(new HttpGet(url));

            // receive response as inputStream
            inputStream = httpResponse.getEntity().getContent();

            // convert inputstream to string
            if(inputStream != null) {
                result = convertInputStreamToString(inputStream);
            }
            else
                result = "Não funcionou";

        } catch (Exception e) {
            result = "Exception no recebimento..." + e.toString();

        }

        return result;
    }

    private static String convertInputStreamToString(InputStream inputStream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(inputStream));
        String line ;
        String result = "";
        while((line = bufferedReader.readLine()) != null)
            result += line;
        inputStream.close();
        return result;
    }

    public boolean isConnected(){
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(this.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected())
            return true;
        else
            return false;
    }


    public class HttpAsyncTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            try{
                return PostText(urls[0], urls[1]);
            } catch (UnsupportedEncodingException e ) {
                return "Falhou";
            }


        }
        public String data = "";
        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            /*
             *  Verifica se o retorno não é nulo e se tem o tamanho esperado.
             */
            View clima = (View) findViewById(R.id.lay_clima);
            if (clima != null) {
                Button enviar = (Button) findViewById(R.id.bt_enviar);
                enviar.setOnClickListener(new Button.OnClickListener() {
                    public void onClick(View view) {
                        try {
                            if (dispositivo == 1) {
                                data = URLEncoder.encode("t", "UTF-8") + "="
                                        + URLEncoder.encode("send_ir_command", "UTF-8");

                                data += "&" + URLEncoder.encode("disp", "UTF-8") + "="
                                        + URLEncoder.encode("1", "UTF-8");

                                data += "&" + URLEncoder.encode("tipo", "UTF-8") + "="
                                        + URLEncoder.encode("1", "UTF-8");

                                Spinner modo = (Spinner) findViewById(R.id.md_spinner);
                                String valor = "";
                                switch (modo.getSelectedItemPosition()) {
                                    case 0:
                                        valor = "auto";
                                        break;
                                    case 1:
                                        valor = "cool";
                                        break;
                                    case 2:
                                        valor = "dry";
                                        break;
                                    case 3:
                                        valor = "heat";
                                        break;
                                    case 4:
                                        valor = "fan";
                                }
                                data += "&" + URLEncoder.encode("modo", "UTF-8") + "="
                                        + URLEncoder.encode(valor, "UTF-8");

                                modo = (Spinner) findViewById(R.id.fan_spinner);
                                valor = "";
                                switch (modo.getSelectedItemPosition()) {
                                    case 0:
                                        valor = "auto";
                                        break;
                                    case 1:
                                        valor = "low";
                                        break;
                                    case 2:
                                        valor = "midd";
                                        break;
                                    case 3:
                                        valor = "high";
                                }
                                data += "&" + URLEncoder.encode("FAN", "UTF-8") + "="
                                        + URLEncoder.encode(valor, "UTF-8");

                                NumberPicker temp = (NumberPicker) findViewById(R.id.numberPicker);
                                valor = String.valueOf(temp.getValue());

                                data += "&" + URLEncoder.encode("temperature", "UTF-8") + "="
                                        + URLEncoder.encode(valor, "UTF-8");

                                new HttpAsyncTask().execute(servidor, data);
                                //Toast.makeText(getBaseContext(), "(EnviandoPedido " + data + " )", Toast.LENGTH_LONG).show();
                            }
                        } catch (Exception e) {
                            Toast.makeText(getBaseContext(), "ERR(" + e.toString() + ")", Toast.LENGTH_LONG).show();
                        }
                    }
                });

                Button desliga = (Button) findViewById(R.id.bt_desliga);
                desliga.setOnClickListener(new Button.OnClickListener() {
                    public void onClick(View view) {
                        try {
                            if (dispositivo == 1) {
                                data = URLEncoder.encode("t", "UTF-8") + "="
                                        + URLEncoder.encode("send_ir_command", "UTF-8");

                                data += "&" + URLEncoder.encode("disp", "UTF-8") + "="
                                        + URLEncoder.encode("1", "UTF-8");

                                data += "&" + URLEncoder.encode("tipo", "UTF-8") + "="
                                        + URLEncoder.encode("1", "UTF-8");

                                data += "&" + URLEncoder.encode("modo", "UTF-8") + "="
                                        + URLEncoder.encode("off", "UTF-8");

                                new HttpAsyncTask().execute(servidor, data);
                                //Toast.makeText(getBaseContext(), "(EnviandoPedido)", Toast.LENGTH_LONG).show();
                            }
                        } catch (Exception e) {
                            Toast.makeText(getBaseContext(), "ERR(" + e.toString() + ")", Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
            View temp_view = (View) findViewById(R.id.lay_amb);
            if (temp_view != null) {


                    if (result.contains("Temperatura:")) {
                        TextView temperatura = (TextView) findViewById(R.id.tmp_amb);
                        temperatura.setText(result.toString());
                        //result = result.substring(result.indexOf("<temperatura>") + 13, result.indexOf("</temperatura>"));
                        //Toast.makeText(getBaseContext(), "(" + result.toString() + ")", Toast.LENGTH_LONG).show();
                } else
                    Toast.makeText(getBaseContext(), "(" + result.toString() + ")", Toast.LENGTH_LONG).show();
            }
        }
    }

}


    /*
   new HttpAsyncTask().execute("http://georgesilva.dyndns.org:8080/?relay=1.");
           }
                        /*
                           JSONObject obj = new JSONObject();
                           try {
                               obj.put("relay", "TVSALA");
                               obj.put("op", "PWR");
                           } catch (JSONException e) {

                           }
                           enviaDados(obj);
                       }

public void enviaDados(JSONObject jsonObject) {
        HttpClient client = new DefaultHttpClient();
        HttpPost post = new HttpPost("http://georgesilva.dyndns.org:8080/");
        post.setHeader("Content-type", "application/json");
        post.setHeader("Accept", "application/json");
        try {
        post.setEntity(new StringEntity(jsonObject.toString(), "UTF-8"));
        HttpResponse response = client.execute(post);
        } catch (UnsupportedEncodingException e) {
        } catch (ClientProtocolException e){
        } catch (IOException e){}

        */