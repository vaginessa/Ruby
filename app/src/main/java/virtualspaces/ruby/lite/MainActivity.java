package virtualspaces.ruby.lite;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.api.client.auth.oauth.OAuthHmacSigner;
import com.google.api.client.auth.oauth.OAuthParameters;
import com.melnykov.fab.FloatingActionButton;
import com.tumblr.jumblr.JumblrClient;
import com.tumblr.jumblr.exceptions.JumblrException;
import com.tumblr.jumblr.types.Post;
import com.tumblr.jumblr.types.User;

import org.scribe.exceptions.OAuthConnectionException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import virtualspaces.ruby.lite.http.JsonModel;
import virtualspaces.ruby.lite.http.ServiceGenerator;
import virtualspaces.ruby.lite.http.TumblrAPIConnect;


public class MainActivity extends AppCompatActivity implements View.OnClickListener, LoadingListener {


    private final String TAG = "MAIN_ACTIVITY";
    private final int PERMISSION_REQ = 1;
    private Context context;

    private static String token;
    private static String token_secret;
    private static String blogName;

    private boolean isGrid;

    private List<Object> centralList;
    private List<Post> newPosts;
    private List<Post> posts;

    private Handler handler;
    private RecyclerAdapter mRecyclerAdapter;

    private JumblrClient jumblr;

    @Bind(R.id.refresher)
    SwipeRefreshLayout refreshLayout;

    @Bind(R.id.fab)
    FloatingActionButton fab;

    @Bind(R.id.progressBar)
    ProgressBar prog;


    @Bind(R.id.reconnect)
    Button reconnect;

    @Bind(R.id.recycler)
    RecyclerView mRecyclerView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        init();

        //check permissions
        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            //request permission here
            String[] request = {
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
            };

            ActivityCompat.requestPermissions(this, request, PERMISSION_REQ);

        } else {
            //load content
            createDataSet();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
    }


    @Override
    public void onResume() {
        super.onResume();
        //if preferences have changed refresh layout
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        if (isGrid != pref.getBoolean("grid", false)) {
            refreshLayout.setRefreshing(true);
            createDataSet();
            Toast.makeText(MainActivity.this, "Refreshing", Toast.LENGTH_SHORT).show();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    refreshLayout.setRefreshing(false);
                }
            }, 4000);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQ: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    createDataSet();

                } else {

                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            PERMISSION_REQ);

                    Toast.makeText(MainActivity.this,
                            "You must agree to permissions in order to continue.", Toast.LENGTH_LONG).show();

                }

            }
        }
    }

    private void init() {

        context = this;

        //get tokens tokens
        getTokens();

        handler = new Handler();

        //check saved layout style
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        isGrid = prefs.getBoolean("grid", false);

        //floating action button setup
        fab.setBackgroundColor(Color.WHITE);
        fab.attachToRecyclerView(mRecyclerView);

        if(isGrid){
            fab.setImageDrawable(getResources().getDrawable(R.drawable.menu_single));
        }else{
            fab.setImageDrawable(getResources().getDrawable(R.drawable.menu_grid));
        }

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //refreshes the ui + changes the
                if(isGrid){
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
                    prefs.edit().putBoolean("grid",false).apply();
                    fab.setImageDrawable(getResources().getDrawable(R.drawable.menu_grid));
                    isGrid = false;

                    refreshLayout.setRefreshing(true);
                    if(posts == null){
                        createDataSet();
                    }else{
                        setUpRecyclerView(posts);
                    }

                }else{

                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
                    prefs.edit().putBoolean("grid",true).apply();
                    fab.setImageDrawable(getResources().getDrawable(R.drawable.menu_single));

                    isGrid = true;

                    refreshLayout.setRefreshing(true);
                    if(posts == null){
                        createDataSet();
                    }else{
                        setUpRecyclerView(posts);
                    }
                }
            }
        });

        fab.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                new AlertDialog.Builder(context)
                        .setTitle("Sign out?")
                        .setCancelable(true)
                        .setPositiveButton("Yes, sign me out!", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
                                settings.edit().clear().commit();

                                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(intent);
                                    finish();
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })

                        .create().show();
                return false;
            }
        });


        //for the recycler adapter
        posts = new ArrayList<>();
    }

    private void createDataSet() {
        NetworkChecker checker = new NetworkChecker(this);
        //checker object will return null if connection detected
        AlertDialog dialog = checker.isConnected();
        if (dialog != null) {
            dialog.show();
            prog.setVisibility(View.INVISIBLE);
            reconnect.setVisibility(View.VISIBLE);
            reconnect.setOnClickListener(this);
        } else {
            reconnect.setVisibility(View.INVISIBLE);
            jumblrDash();
        }
    }

    private void loadPosts() {

        OAuthHmacSigner signer = new OAuthHmacSigner();
        signer.clientSharedSecret = Constants.CONSUMER_SECRET;
        signer.tokenSharedSecret = token_secret;
        OAuthParameters authorizer = new OAuthParameters();
        authorizer.consumerKey = Constants.CONSUMER_KEY;
        authorizer.signer = signer;
        authorizer.token = token;

        TumblrAPIConnect connect = ServiceGenerator.createService(
                TumblrAPIConnect.class, Constants.API_BASE_URL, authorizer);

        Call<JsonModel> element = connect.listRepos();

        element.enqueue(new Callback<JsonModel>() {
            @Override
            public void onResponse(Call<JsonModel> call, Response<JsonModel> response) {

                try {
                    Log.d(TAG, "onResponse: errbody:" + response.errorBody().string());
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onFailure(Call<JsonModel> call, Throwable t) {
                Log.d(TAG, "onFailure: failure.. Rats!" + t.getCause() + t.getLocalizedMessage() + t.getStackTrace());
            }
        });
    }

    private void jumblrDash() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {

                Log.d(TAG, "setting up jumblr client");
                jumblr = new JumblrClient(Constants.CONSUMER_KEY, Constants.CONSUMER_SECRET, token, token_secret);
                Log.d(TAG, "setting jumblr tokens");
                posts = jumblr.userDashboard();

                // Write the user's name
                User user = jumblr.user();
                blogName = user.getName();
                Log.i(TAG, "\t" + blogName);

                //save blog name to prefs
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString("blog", blogName);
                editor.apply();

                //final String picUrl = "https://api.tumblr.com/v2/blog/" + blogName + ".tumblr.com/avatar";


                Log.d(TAG, "post list size: " + posts.size());
                //populating recycler view

                handler.post(new Runnable() {
                    @Override
                    public void run() {

                        setUpRecyclerView(posts);
                    }
                });
                //must be set up on main thread

            }
        });
        thread.start();
    }

    private void setUpRecyclerView(List<Post> rawPosts) {

        //remove progress bar
        prog.setVisibility(View.GONE);
        // pull to refresh layout config
        refreshLayout.setVisibility(View.VISIBLE);
        refreshLayout.setColorSchemeResources(R.color.notification_green, R.color.liked_red, R.color.orange, R.color.search_blue);
        refreshLayout.setProgressBackgroundColorSchemeColor(Color.WHITE);
        refreshLayout.setProgressViewOffset(false, 0, 225);
        refreshLayout.setOnRefreshListener(refreshListener);

        centralList = PostSorter.returnCasted(rawPosts);

        //sets layout manager depending on shared preferences
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        if (isGrid) {
            StaggeredGridLayoutManager gridLayoutManager =
                    new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
            mRecyclerView.setLayoutManager(gridLayoutManager);

        } else {
            LinearLayoutManager layoutManager = new LinearLayoutManager(this);
            layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
            mRecyclerView.setLayoutManager(layoutManager);
        }

        mRecyclerAdapter = new RecyclerAdapter(centralList, mRecyclerView);
        mRecyclerAdapter.setAppCompat(this);
        mRecyclerAdapter.setOnLoadMoreListener(this);
        mRecyclerView.setAdapter(mRecyclerAdapter);
    }

    private void getTokens() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        token = preferences.getString("access_token", null);
        Log.d(TAG, "1, token: " + token);
        token_secret = preferences.getString("access_token_secret", null);
        Log.d(TAG, "1, token secret: " + token_secret);
    }

    /*
    listener and menu animation items
     */


    private SwipeRefreshLayout.OnRefreshListener refreshListener = new SwipeRefreshLayout.OnRefreshListener() {

        @Override
        public void onRefresh() {
            createDataSet();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    refreshLayout.setRefreshing(false);
                }
            }, 4200);
        }
    };

    //click listener for all menu
    @Override
    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.reconnect:
                NetworkChecker checker = new NetworkChecker(context);
                AlertDialog dialog = checker.isConnected();
                if (dialog != null) {
                    dialog.show();
                } else {
                    createDataSet();
                }
                break;
        }
    }

    @Override
    public void onLoadMore() {

        Log.d(TAG, "Loading More posts");
        //adding null equates to adding "loading view" into recyclerview
        Log.d(TAG, "onLoadMore: adding null to central list.");
        centralList.add(null);
        //call for more
        Log.d(TAG, "onLoadMore: starting new thread for getting new posts");
        new Thread(new Runnable() {
            @Override
            public void run() {
                // Make the request
                Log.d(TAG, "run: on load listener has been called for first time.");

                try {
                    Log.d(TAG, "run: creating new jumblr object in ONLOADMORE listener");

                    Log.d(TAG, "run: recycler adapter setloaded method called");

                    Map<String, Object> params = new HashMap<String, Object>();
                    params.put("limit", 20);
                    params.put("offset", centralList.size());

                    //jumblr.setRequestBuilder(new RequestBuilder(jumblr));

                    newPosts = jumblr.userDashboard(params);
                } catch (OAuthConnectionException o) {
                    Log.d(TAG, "run: error creating new posts in onload();");
                } catch (JumblrException j) {
                    Log.d(TAG, "run: jumblr exception thrown! wtf");
                } catch (OutOfMemoryError e) {
                    Log.e(TAG, "OUT OF MEMORY ERROR");
                }
                Log.d(TAG, "central list size:" + centralList.size());

                Log.d(TAG, "run:  handler is now posting update to recycleradapter");
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        //removing loading sign, populating new items
                        //Remove loading items

                        int size = centralList.size() - 1;
                        centralList.remove(centralList.size() - 1);
                        mRecyclerAdapter.notifyItemRemoved(centralList.size());


                        List<Object> newList = PostSorter.returnCasted(newPosts);
                        for (Object object : newList) {
                            centralList.add(object);
                        }

                        Log.d(TAG, "run: recycler adapter notify item range changed called");
                        mRecyclerAdapter.notifyItemRangeChanged(size, centralList.size());
                        mRecyclerAdapter.setLoaded();
                    }
                });


            }
        }).start();
    }
}