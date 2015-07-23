package com.kii.sync;

import com.kii.cloud.storage.Kii;
import com.kii.cloud.storage.KiiBucket;
import com.kii.cloud.storage.KiiObject;
import com.kii.cloud.storage.callback.KiiQueryCallBack;
import com.kii.cloud.storage.query.KiiClause;
import com.kii.cloud.storage.query.KiiQuery;
import com.kii.cloud.storage.query.KiiQueryResult;
import com.kii.sync.providers.SyncProvider;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public class MainActivity extends Activity {

    private static final String TAG = "MainActivity";

    private List<Light> mLights = new ArrayList<Light>();

    private ListView mList;

    private LightsAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mList = (ListView)findViewById(R.id.list);
        mAdapter = new LightsAdapter(this);
        mList.setAdapter(mAdapter);

        Light l = new Light();
        l.mac = "11";
        l.name = "Light0";
        l.color = "black";
        l.brightness = "12";
        mLights.add(l);
        l = new Light();
        l.mac = "12";
        l.name = "Light1";
        l.color = "red";
        l.brightness = "12";
        mLights.add(l);
        l = new Light();
        l.mac = "13";
        l.name = "Light2";
        l.color = "blue";
        l.brightness = "12";
        mLights.add(l);
        l = new Light();
        l.mac = "14";
        l.name = "Light3";
        l.color = "green";
        l.brightness = "12";
        mLights.add(l);

        mAdapter.notifyDataSetChanged();
    }

    void downloadLights(){
        // Prepare the target bucket to be queried
        KiiBucket bucket = Kii.bucket("lights");

        // Define query conditions
        KiiQuery query = new KiiQuery();

        // Query the bucket
        bucket.query(new KiiQueryCallBack<KiiObject>() {
            @Override
            public void onQueryCompleted(int token, KiiQueryResult<KiiObject> result, Exception exception) {
                if (exception != null) {
                    // Error handling
                    Log.e(TAG, "query exception: "+ exception.getLocalizedMessage().toString());
                    return;
                }
                List<KiiObject> objLists = result.getResult();
                mLights.clear();
                for (KiiObject obj : objLists) {
                    // Do something with the first 10 objects
                    Light l = new Light();
                    l.mac = obj.getString("mac", "");
                    l.name = obj.getString("name");
                    l.color = obj.getString("color");
                    l.brightness = obj.getString("brightness");
                    mLights.add(l);
                }

                mAdapter.notifyDataSetChanged();

                // Fetching the next 10 objects (pagination handling)
                if (!result.hasNext()) {
                    return;
                }
                result.getNextQueryResult(this);
            }
        }, query);
    }

    void addLightToList(Light light) {
        if (mLights.contains(light)) {
            return;
        }
        Cursor cursor = getContentResolver()
                .query(SyncProvider.URI_LIGHTS, null, "MAC=(?)", new String[]{light.mac}, null);
        if (cursor.moveToFirst()) {
            light.name = cursor.getString(cursor.getColumnIndex("name"));
            light.color = cursor.getString(cursor.getColumnIndex("model"));
            light.brightness = cursor.getString(cursor.getColumnIndex("brightness"));
        }
        cursor.close();
        mLights.add(light);
        mAdapter.notifyDataSetChanged();
    }

    void addLightToDB(Light light) {
        ContentValues values = new ContentValues();
        values.put("MAC", light.mac);
        values.put("color", light.color);
        getContentResolver().insert(SyncProvider.URI_LIGHTS, values);
    }

    class LightsAdapter extends BaseAdapter {
        Context mContext = null;

        LightsAdapter(Context context) {
            super();
            mContext = context;
        }

        @Override
        public int getCount() {
            return mLights.size();
        }

        @Override
        public Object getItem(int position) {
            return mLights.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            if (view == null) {
                view = LayoutInflater.from(mContext).inflate(CardItemViewHolder.layout_id, parent, false);
                CardItemViewHolder holder = new CardItemViewHolder(view);
                view.setTag(holder);
            }
            final Light light = (Light) getItem(position);
            CardItemViewHolder holder = (CardItemViewHolder) view.getTag();
            holder.title.setText(light.name);
            holder.color.setText(light.color);
            holder.brightness.setText(light.brightness);
            return view;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_sync) {
            downloadLights();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
