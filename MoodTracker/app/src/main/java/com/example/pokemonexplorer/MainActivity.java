package com.example.pokemonexplorer;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private CoordinatorLayout root;
    private TextInputEditText editType;
    private ListView listView;
    private FloatingActionButton fab;

    private final ArrayList<Pokemon> data = new ArrayList<>();
    private PokemonArrayAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        root = findViewById(R.id.root);
        editType = findViewById(R.id.editType);
        listView = findViewById(R.id.listView);
        fab = findViewById(R.id.fab);

        adapter = new PokemonArrayAdapter(this, data);
        listView.setAdapter(adapter);

        fab.setOnClickListener(v -> {
            dismissKeyboard();

            String type = (editType.getText() == null) ? "" : editType.getText().toString().trim().toLowerCase();

            if (TextUtils.isEmpty(type)) {
                Snackbar.make(root, "Please enter a Pokémon type", Snackbar.LENGTH_LONG).show();
                return;
            }

            new FetchPokemonTask().execute(type);
        });
    }

    private void dismissKeyboard() {
        View view = getCurrentFocus();
        if (view == null) view = new View(this);

        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    private class FetchPokemonTask extends AsyncTask<String, Void, ArrayList<Pokemon>> {

        private String errorMsg = null;

        @Override
        protected ArrayList<Pokemon> doInBackground(String... strings) {
            String type = strings[0];
            ArrayList<Pokemon> list = new ArrayList<>();

            try {
                JSONObject typeObj = new JSONObject(httpGet("https://pokeapi.co/api/v2/type/" + type));
                JSONArray arr = typeObj.getJSONArray("pokemon");

                int limit = Math.min(arr.length(), 20);

                for (int i = 0; i < limit; i++) {
                    JSONObject poke = arr.getJSONObject(i).getJSONObject("pokemon");
                    String name = poke.getString("name");

                    JSONObject details = new JSONObject(httpGet("https://pokeapi.co/api/v2/pokemon/" + name));
                    JSONObject sprites = details.getJSONObject("sprites");
                    String image = sprites.isNull("front_default") ? null : sprites.getString("front_default");

                    JSONArray stats = details.getJSONArray("stats");

                    int hp = 0, atk = 0, def = 0, spAtk = 0, spDef = 0, speed = 0;

                    for (int s = 0; s < stats.length(); s++) {
                        JSONObject statObj = stats.getJSONObject(s);
                        int base = statObj.getInt("base_stat");
                        String statName = statObj.getJSONObject("stat").getString("name");

                        switch (statName) {
                            case "hp": hp = base; break;
                            case "attack": atk = base; break;
                            case "defense": def = base; break;
                            case "special-attack": spAtk = base; break;
                            case "special-defense": spDef = base; break;
                            case "speed": speed = base; break;
                        }
                    }

                    list.add(new Pokemon(name, image, hp, atk, def, spAtk, spDef, speed));
                }

                return list;

            } catch (Exception e) {
                String msg = e.getMessage();
                if (msg != null && msg.contains("404")) errorMsg = "Error: 404 (invalid type)";
                else errorMsg = "Error loading Pokémon";
                return null;
            }
        }

        @Override
        protected void onPostExecute(ArrayList<Pokemon> result) {
            if (result == null) {
                Snackbar.make(root, errorMsg == null ? "Error loading Pokémon" : errorMsg, Snackbar.LENGTH_LONG).show();
                return;
            }

            data.clear();
            data.addAll(result);
            adapter.notifyDataSetChanged();
        }
    }

    private String httpGet(String urlStr) throws Exception {
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(12000);
        conn.setReadTimeout(12000);

        int code = conn.getResponseCode();

        BufferedReader br;
        if (code >= 200 && code < 300) {
            br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        } else {
            br = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
        }

        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) sb.append(line);

        br.close();
        conn.disconnect();

        if (code < 200 || code >= 300) throw new RuntimeException("" + code);

        return sb.toString();
    }
}