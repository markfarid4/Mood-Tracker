package com.example.pokemonexplorer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.InputStream;
import java.net.URL;
import java.util.List;

public class PokemonArrayAdapter extends ArrayAdapter<Pokemon> {

    private final LayoutInflater inflater;
    private final LruCache<String, Bitmap> cache;

    public PokemonArrayAdapter(Context context, List<Pokemon> objects) {
        super(context, 0, objects);
        inflater = LayoutInflater.from(context);

        int maxMemKb = (int)(Runtime.getRuntime().maxMemory() / 1024);
        int cacheKb = maxMemKb / 8;
        cache = new LruCache<String, Bitmap>(cacheKb) {
            @Override
            protected int sizeOf(String key, Bitmap value) {
                return value.getByteCount() / 1024;
            }
        };
    }

    static class ViewHolder {
        ImageView img;
        TextView name, hp, atk, def, spAtk, spDef, speed;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder h;

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.list_item_pokemon, parent, false);

            h = new ViewHolder();
            h.img = convertView.findViewById(R.id.imgPokemon);
            h.name = convertView.findViewById(R.id.txtName);
            h.hp = convertView.findViewById(R.id.txtHp);
            h.atk = convertView.findViewById(R.id.txtAtk);
            h.def = convertView.findViewById(R.id.txtDef);
            h.spAtk = convertView.findViewById(R.id.txtSpAtk);
            h.spDef = convertView.findViewById(R.id.txtSpDef);
            h.speed = convertView.findViewById(R.id.txtSpeed);

            convertView.setTag(h);
        } else {
            h = (ViewHolder) convertView.getTag();
        }

        Pokemon p = getItem(position);
        if (p == null) return convertView;

        h.name.setText(p.getName());
        h.hp.setText("HP: " + p.getHp());
        h.atk.setText("Attack: " + p.getAttack());
        h.def.setText("Defense: " + p.getDefense());
        h.spAtk.setText("Sp. Atk: " + p.getSpecialAttack());
        h.spDef.setText("Sp. Def: " + p.getSpecialDefense());
        h.speed.setText("Speed: " + p.getSpeed());

        h.img.setImageResource(android.R.drawable.ic_menu_gallery);

        String url = p.getImageUrl();
        if (url == null || url.trim().isEmpty() || url.equals("null")) return convertView;

        Bitmap bmp = cache.get(url);
        if (bmp != null) {
            h.img.setImageBitmap(bmp);
        } else {
            new ImageTask(h.img, url).execute();
        }

        return convertView;
    }

    private class ImageTask extends AsyncTask<Void, Void, Bitmap> {
        private final ImageView target;
        private final String url;

        ImageTask(ImageView target, String url) {
            this.target = target;
            this.url = url;
        }

        @Override
        protected Bitmap doInBackground(Void... voids) {
            try {
                InputStream is = new URL(url).openStream();
                return BitmapFactory.decodeStream(is);
            } catch (Exception e) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(Bitmap bmp) {
            if (bmp != null) {
                cache.put(url, bmp);
                target.setImageBitmap(bmp);
            }
        }
    }
}