package com.kii.sync;

import com.kii.cloud.storage.Kii;
import com.kii.cloud.storage.KiiBucket;
import com.kii.cloud.storage.KiiObject;
import com.kii.cloud.storage.KiiUser;
import com.kii.cloud.storage.callback.KiiBucketCallBack;
import com.kii.cloud.storage.callback.KiiObjectCallBack;
import com.kii.cloud.storage.callback.KiiQueryCallBack;
import com.kii.cloud.storage.query.KiiClause;
import com.kii.cloud.storage.query.KiiQuery;
import com.kii.cloud.storage.query.KiiQueryResult;
import com.kii.sync.providers.SyncProvider;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.EditText;
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
        fetchLocalLights();

//        Light l = new Light();
//        l.mac = "4";
//        l.name = "Light4";
//        l.color = "black";
//        l.brightness = "13";
//        mLights.add(l);
//        l = new Light();
//        l.mac = "5";
//        l.name = "Light5";
//        l.color = "red";
//        l.brightness = "13";
//        mLights.add(l);
//        l = new Light();
//        l.mac = "6";
//        l.name = "Light6";
//        l.color = "blue";
//        l.brightness = "13";
//        mLights.add(l);
//        l = new Light();
//        l.mac = "7";
//        l.name = "Light7";
//        l.color = "green";
//        l.brightness = "13";
//        mLights.add(l);

    }

    void fetchLocalLights(){
        Light light = new Light();
        Cursor cursor = getContentResolver()
                .query(SyncProvider.URI_LIGHTS, null, null, null, null);
        while(cursor.moveToNext()){
            light = new Light();
            light.mac = cursor.getString(cursor.getColumnIndex("MAC"));
            light.name = cursor.getString(cursor.getColumnIndex("name"));
            light.color = cursor.getString(cursor.getColumnIndex("color"));
            light.brightness = cursor.getString(cursor.getColumnIndex("brightness"));
            mLights.add(light);
        }
        mAdapter.notifyDataSetChanged();
    }

    void syncLightsFromServer(){
        // Prepare the target bucket to be queried
        KiiBucket bucket = KiiUser.getCurrentUser().bucket("lights");

        // Define query conditions
        KiiQuery query = new KiiQuery();

        // Query the bucket
        bucket.query(new KiiQueryCallBack<KiiObject>() {
            @Override
            public void onQueryCompleted(int token, KiiQueryResult<KiiObject> result,
                    Exception exception) {
                if (exception != null) {
                    // Error handling
                    Log.e(TAG, "query exception: " + exception.getLocalizedMessage().toString());
                    return;
                }
                List<KiiObject> objLists = result.getResult();
                mLights.clear();
                for (KiiObject obj : objLists) {
                    // Do something with the first 10 objects
                    Light l = new Light();
                    l.mac = obj.getString("mac", "mac");
                    l.name = obj.getString("name");
                    l.color = obj.getString("color");
                    l.brightness = obj.getString("brightness");
                    addLightToDB(l);
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

    void syncLightsFromLocal() {

        final Cursor cursor = getContentResolver()
                .query(SyncProvider.URI_LIGHTS, null, null, null, null);

        //clear the light's bucket of the user
        KiiBucket bucket = KiiUser.getCurrentUser().bucket("lights");
        bucket.delete(new KiiBucketCallBack<KiiBucket>() {
            @Override
            public void onDeleteCompleted(int token, Exception e) {
                if (e != null) {
                    Log.v("BucketDelete", "delete failed: " + e.getLocalizedMessage());
                    new uploadLightsToServer().execute(cursor);
                    return;
                }
                new uploadLightsToServer().execute(cursor);
                Log.v("BucketDelete", "delete successful");
            }
        });
    }

    class uploadLightsToServer extends AsyncTask<Cursor, Void, Void>{

        ProgressDialog mProgressDialog = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            mProgressDialog = new ProgressDialog(MainActivity.this);
            mProgressDialog.setMessage("uploading");
            mProgressDialog.setCancelable(false);
            mProgressDialog.show();

        }

        @Override
        protected Void doInBackground(Cursor[] cursors) {
            while(cursors[0].moveToNext()){
                // Create an object in an application-scope bucket.
                KiiObject object = KiiUser.getCurrentUser().bucket("lights").object();

                // Set key-value pairs
                object.set("mac", cursors[0].getString(cursors[0].getColumnIndex("MAC")));
                object.set("name", cursors[0].getString(cursors[0].getColumnIndex("name")));
                object.set("color", cursors[0].getString(cursors[0].getColumnIndex("color")));
                object.set("brightness",
                        cursors[0].getString(cursors[0].getColumnIndex("brightness")));

                // Save the object
                object.save(new KiiObjectCallBack() {
                    @Override
                    public void onSaveCompleted(int token, KiiObject object, Exception exception) {
                        if (exception != null) {
                            // Error handling
                            return;
                        }
                    }
                });
            }
            cursors[0].close();
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            mProgressDialog.dismiss();
        }
    }

    void addLightToDB(Light light) {
        ContentValues values = new ContentValues();
        values.put("MAC", light.mac);
        values.put("name", light.name);
        values.put("color", light.color);
        values.put("brightness", light.brightness);
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

    public final static int LOCAL_SYNC = 0;

    public final static int SERVER_SYNC = 1;

    public static int SYNC = 0;

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
        if (id == R.id.action_server_sync) {
            SYNC = SERVER_SYNC;
            getContentResolver().delete(SyncProvider.URI_LIGHTS, null, null);
            syncLightsFromServer();
            return true;
        }else if(id == R.id.action_local_sync){
            SYNC = LOCAL_SYNC;
            syncLightsFromLocal();
        }else if(id == R.id.action_add_light){

            final View addLightView = LayoutInflater.from(MainActivity.this).inflate(R.layout.layout_add_light, null);

            final EditText mac = (EditText) addLightView.findViewById(R.id.mac);
            final EditText name = (EditText) addLightView.findViewById(R.id.name);
            final EditText color = (EditText) addLightView.findViewById(R.id.color);
            final EditText brightness = (EditText) addLightView.findViewById(R.id.brightness);

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Add device").setView(addLightView).setPositiveButton("Add", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    Light l = new Light();
                    l.mac = mac.getText().toString();
                    l.name = name.getText().toString();
                    l.color = color.getText().toString();
                    l.brightness = brightness.getText().toString();

                    mLights.add(l);
                    mAdapter.notifyDataSetChanged();
                    addLightToDB(l);

                }
            }).setNegativeButton("Cancel", null);
            builder.show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
