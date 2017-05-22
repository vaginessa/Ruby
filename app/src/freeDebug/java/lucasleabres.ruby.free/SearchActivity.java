package lucasleabres.ruby.free;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.LinearInterpolator;
import android.view.animation.OvershootInterpolator;
import android.view.animation.ScaleAnimation;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.tumblr.jumblr.JumblrClient;
import com.tumblr.jumblr.exceptions.JumblrException;
import com.tumblr.jumblr.types.Post;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import cool.lucasbedolla.ruby.R;

public class SearchActivity extends FragmentActivity {

    public static final String TAG = "lucasleabres.ruby.free.SearchActivity";
    public Context context;
    @Bind(R.id.search_view)
    EditText editText;
    @Bind(R.id.searchBar)
    Toolbar searchBar;
    @Bind(R.id.search_back)
    ImageButton back;
    private Handler handler = new Handler(Looper.getMainLooper());
    private ViewPager pager;
    private int currentPage = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        ButterKnife.bind(this);
        //editText.addTextChangedListener(watcher);
        //search.setOnClickListener(this);
        context = this;

        startAnimation(editText,1000);
        editText.setImeActionLabel("Search",EditorInfo.IME_ACTION_SEARCH);


        MyPageAdapter adapter = new MyPageAdapter(getSupportFragmentManager(),getFragments());
        pager = (ViewPager) findViewById(R.id.pager);
        pager.setAdapter(adapter);
        TabLayout tabLayout = (TabLayout) findViewById(R.id.sliding_tabs);
        tabLayout.setupWithViewPager(pager);

        pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                Log.i("lucasleabres.ruby.free.SearchActivity", "page selected " + position);
                currentPage = position;
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });





    back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_SEARCH & editText.getText().toString()!="") {
                    if(currentPage==0){
                        NetworkChecker checker = new NetworkChecker(context);
                        AlertDialog dialog = checker.isConnected();
                        if(dialog!=null){
                            dialog.show();
                        }else{
                            //party!
                            searchTagged(1);
                        }

                    }else{
                        NetworkChecker checker = new NetworkChecker(context);
                        AlertDialog dialog = checker.isConnected();
                        if(dialog!=null){
                            dialog.show();
                        }else{
                            //party!
                            searchBlogs(0);
                        }
                    }

                    List<Fragment> blogFrags =  getSupportFragmentManager().getFragments();
                    for(Fragment frag:blogFrags){
                        SearchBlogFragment sBF = (SearchBlogFragment) frag;
                        sBF.progress.setVisibility(View.VISIBLE);
                    }
                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (getCurrentFocus()!=null){
                        imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
                    }

                    handled = true;
                }
                return handled;
            }
        });

    }

    private void startAnimation(View v,int milis) {

        v.setVisibility(View.VISIBLE);

        Animation scaleAnim = new ScaleAnimation(
                0f,1f,
                0f,1f,
                Animation.RELATIVE_TO_SELF,0.5f,
                Animation.RELATIVE_TO_SELF,0.5f);
        scaleAnim.setInterpolator(new OvershootInterpolator(8f));


        Animation alphaAnim = new AlphaAnimation(0f,1f);
        alphaAnim.setInterpolator(new LinearInterpolator());


        AnimationSet set = new AnimationSet(false);
        set.addAnimation(scaleAnim);
        //set.addAnimation(alphaAnim);
        set.setFillAfter(true);
        set.setDuration(milis);
        set.start();
        v.setAnimation(set);



    }

    @Override
    public void onBackPressed(){
        super.onBackPressed();
    }

    public void searchTagged(final int page){

        new Thread(new Runnable() {
            @Override
            public void run() {

                JumblrClient client = new JumblrClient(Constants.CONSUMER_KEY, Constants.CONSUMER_SECRET);
                final List<Post> posts = client.tagged(editText.getText().toString());
                Log.d("lucasleabres.ruby.free.SearchActivity", "run: tagged size" + posts.size());

                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (posts.size()!=0){
                                conductInflation(page, posts);
                            }else{
                                Toast.makeText(SearchActivity.this, "No tagged posts found :O", Toast.LENGTH_SHORT).show();
                            }

                        }
                    });


            }
        }).start();
    }


    private void searchBlogs(final int page) {
        Toast.makeText(SearchActivity.this, "search clicked", Toast.LENGTH_SHORT).show();

        new Thread(new Runnable() {
            @Override
            public void run() {

                JumblrClient client = new JumblrClient(Constants.CONSUMER_KEY, Constants.CONSUMER_SECRET);

                try{
                    final List<Post> posts  = client.blogPosts(editText.getText().toString());
                    Log.d("lucasleabres.ruby.free.SearchActivity", "run: tagged size" + posts.size());

                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (posts.size()!=0){
                                conductInflation(page,posts);
                            }else{
                                Toast.makeText(SearchActivity.this, "Blog not found.", Toast.LENGTH_SHORT).show();
                            }

                        }
                    });
                }catch (JumblrException e){
                    Log.d(TAG, "jumblr exception thrown.");
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(SearchActivity.this, "Blog not found.", Toast.LENGTH_SHORT).show();
                            Fragment frag = getSupportFragmentManager().getFragments().get(1);
                            SearchBlogFragment searchFrag = (SearchBlogFragment) frag;
                            searchFrag.progress.setVisibility(View.INVISIBLE);
                        }
                    });
                }
            }
        }).start();
    }


    private void conductInflation(int page, List<Post> tagged) {
            Log.d(TAG, "conductInflation: inflation on page:"+page);
            Fragment frag = getSupportFragmentManager().getFragments().get(page);
            SearchBlogFragment searchFrag = (SearchBlogFragment) frag;
            searchFrag.inflateResults(page,tagged);
    }

    private List<Fragment> getFragments() {
        List<Fragment> fList = new ArrayList<Fragment>();
        fList.add(SearchBlogFragment.getFragmentInstance(0));
        fList.add(SearchBlogFragment.getFragmentInstance(1));
        return fList;
    }

        class MyPageAdapter extends FragmentPagerAdapter {

            private String tabTitles[] = new String[] { "Hashtag Search", "Blog Search" };
            private List<Fragment> fragments;

            public MyPageAdapter(FragmentManager fm, List<Fragment> fragments) {
                super(fm);
                this.fragments = fragments;
            }

            @Override
            public Fragment getItem(int position) {

                return this.fragments.get(position);
            }

            @Override
            public int getCount() {
                return 2;
            }
            @Override
            public CharSequence getPageTitle(int position) {
                // Generate title based on item position
                return tabTitles[position];
            }
        }

}
