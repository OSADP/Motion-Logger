package com.example.mitmotionapp;

import java.util.HashMap;
import java.util.Stack;



import com.example.mitmotionapp.records.StartRecords;
import com.example.mitmotionapp.review.Review;


import com.jsiegel.mitmotionapp.R;
import com.example.mitmotionapp.setting.Settings;

import android.os.Bundle;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TabHost;
import android.widget.TextView;

public class MainActivity extends FragmentActivity {
    /* Your Tab host */
    private TabHost mTabHost;

    /* A HashMap of stacks, where we use tab identifier as keys..*/
    private HashMap<String, Stack<Fragment>> mStacks;

    /*Save current tabs identifier in this..*/
    private String mCurrentTab;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /*  
         *  Navigation stacks for each tab gets created.. 
         *  tab identifier is used as key to get respective stack for each tab
         */
        mStacks             =   new HashMap<String, Stack<Fragment>>();
        mStacks.put(AppConstants.TAB_A, new Stack<Fragment>());
        mStacks.put(AppConstants.TAB_B, new Stack<Fragment>());
        mStacks.put(AppConstants.TAB_C, new Stack<Fragment>());
        mStacks.put(AppConstants.TAB_D, new Stack<Fragment>());

        mTabHost                =   (TabHost)findViewById(android.R.id.tabhost);
        mTabHost.setOnTabChangedListener(listener);
        mTabHost.setup();

        initializeTabs();
    }


    private View createTabView(final int id, String text) {
        View view = LayoutInflater.from(this).inflate(R.layout.tabs_icon, null);
        
        ImageView imageView =   (ImageView) view.findViewById(R.id.tab_icon);
        imageView.setImageDrawable(getResources().getDrawable(id));
        TextView textview = (TextView) view.findViewById(R.id.text_icon);
        textview.setText(text);
        return view;
    }

    public void initializeTabs(){
        /* Setup your tab icons and content views.. Nothing special in this..*/
        TabHost.TabSpec spec    =   mTabHost.newTabSpec(AppConstants.TAB_A);
        mTabHost.setCurrentTab(-3);
        spec.setContent(new TabHost.TabContentFactory() {
            public View createTabContent(String tag) {
                return findViewById(R.id.realtabcontent);
            }
        });
        spec.setIndicator(createTabView(R.drawable.records_selector, "Record"));
        mTabHost.addTab(spec);
        spec                    =   mTabHost.newTabSpec(AppConstants.TAB_B);
        spec.setContent(new TabHost.TabContentFactory() {
            public View createTabContent(String tag) {
                return findViewById(R.id.realtabcontent);
            }
        });
        spec.setIndicator(createTabView(R.drawable.review_selector, "Review"));
        mTabHost.addTab(spec);
        spec                    =   mTabHost.newTabSpec(AppConstants.TAB_C);
        spec.setContent(new TabHost.TabContentFactory() {
            public View createTabContent(String tag) {
                return findViewById(R.id.realtabcontent);
            }
        });
        spec.setIndicator(createTabView(R.drawable.setting_selector,"Settings" ));
        mTabHost.addTab(spec);
    }


    /*Comes here when user switch tab, or we do programatically*/
    TabHost.OnTabChangeListener listener    =   new TabHost.OnTabChangeListener() {
      public void onTabChanged(String tabId) {
        /*Set current tab..*/
        mCurrentTab                     =   tabId;

        if(mStacks.get(tabId).size() == 0){
          /*
           *    First time this tab is selected. So add first fragment of that tab.
           *    Don't need animation, so that argument is false.
           *    We are adding a new fragment which is not present in stack. So add to stack is true.
           */
          if(tabId.equals(AppConstants.TAB_A)){
        	  pushFragments(tabId, new StartRecords(), false,true);
          }else if(tabId.equals(AppConstants.TAB_B)){
            pushFragments(tabId, new Review(), false,true);
          }
          else if(tabId.equals(AppConstants.TAB_C)){
              pushFragments(tabId, new Settings(), false,true);
            }
        }else {
          /*
           *    We are switching tabs, and target tab is already has at least one fragment. 
           *    No need of animation, no need of stack pushing. Just show the target fragment
           */
          pushFragments(tabId, mStacks.get(tabId).lastElement(), false,false);
        }
      }
    };


    /* Might be useful if we want to switch tab programatically, from inside any of the fragment.*/
    public void setCurrentTab(int val){
          mTabHost.setCurrentTab(val);
    }


    /* 
     *      To add fragment to a tab. 
     *  tag             ->  Tab identifier
     *  fragment        ->  Fragment to show, in tab identified by tag
     *  shouldAnimate   ->  should animate transaction. false when we switch tabs, or adding first fragment to a tab
     *                      true when when we are pushing more fragment into navigation stack. 
     *  shouldAdd       ->  Should add to fragment navigation stack (mStacks.get(tag)). false when we are switching tabs (except for the first time)
     *                      true in all other cases.
     */
    public void pushFragments(String tag, Fragment fragment,boolean shouldAnimate, boolean shouldAdd){
      if(shouldAdd)
          mStacks.get(tag).push(fragment);
      FragmentManager   manager         =   getSupportFragmentManager();
      FragmentTransaction ft            =   manager.beginTransaction();
      if(shouldAnimate)
          ft.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left);
      ft.replace(R.id.realtabcontent, fragment);
      ft.commit();
    }


    public void popFragments(){
      /*    
       *    Select the second last fragment in current tab's stack.. 
       *    which will be shown after the fragment transaction given below 
       */
      Fragment fragment             =   mStacks.get(mCurrentTab).elementAt(mStacks.get(mCurrentTab).size() - 2);

      /*pop current fragment from stack.. */
      mStacks.get(mCurrentTab).pop();

      /* We have the target fragment in hand.. Just show it.. Show a standard navigation animation*/
      FragmentManager   manager         =   getSupportFragmentManager();
      FragmentTransaction ft            =   manager.beginTransaction();
      ft.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right);
      ft.replace(R.id.realtabcontent, fragment);
      ft.commit();
    }   


    @Override
    public void onBackPressed() {
       	if(((BaseFragment)mStacks.get(mCurrentTab).lastElement()).onBackPressed() == false){
       		/*
       		 * top fragment in current tab doesn't handles back press, we can do our thing, which is
       		 * 
       		 * if current tab has only one fragment in stack, ie first fragment is showing for this tab.
       		 *        finish the activity
       		 * else
       		 *        pop to previous fragment in stack for the same tab
       		 * 
       		 */
       		if(mStacks.get(mCurrentTab).size() == 1){
       			super.onBackPressed();  // or call finish..
       		}else{
       			popFragments();
       		}
       	}else{
       		//do nothing.. fragment already handled back button press.
       	}
    }


    /*
     *   Imagine if you wanted to get an image selected using ImagePicker intent to the fragment. Ofcourse I could have created a public function
     *  in that fragment, and called it from the activity. But couldn't resist myself.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(mStacks.get(mCurrentTab).size() == 0){
            return;
        }

        /*Now current fragment on screen gets onActivityResult callback..*/
        mStacks.get(mCurrentTab).lastElement().onActivityResult(requestCode, resultCode, data);
    }
}