package com.offerland.app;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.devspark.appmsg.AppMsg;
import com.melnykov.fab.FloatingActionButton;
import com.squareup.picasso.Picasso;

import java.util.List;

import adapters.MenuDrawerAdapter;
import database.CategoryDAO;
import database.InterestListDAO;
import database.UserDAO;
import datamodels.Category;
import datamodels.Constants;
import datamodels.User;
import json.CategoriesHandler;
import json.JsonReader;
import utils.InternetUtil;
import utils.ViewUtil;


public class MainActivity extends ActionBarActivity {
    public static final int MENU_DRAWER_GRAVITY = Gravity.START;

    // main objects
    private User user;
    private Typeface typefaceLight;
    private Typeface typefaceMedium;
    private CategoryDAO categoryDAO;
    private InterestListDAO interestListDAO;

    // main views
    private ImageButton buttonMenu;
    private DrawerLayout menuDrawer;
    private ImageView imageProfilePhoto;
    private TextView textFullName;
    private RecyclerView recyclerMenuDrawer;
    private FloatingActionButton fab;

    // dialog views
    private Dialog dialog;
    private View progressBar;
    private TextView textError;
    private Button buttonClose;

    // other variables
    private int lastSelectedId; // used to hold last selected item in menu drawer to skip if user clicks twice

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initComponents();
    }

    /**
     * method, used to initialize components
     */
    private void initComponents() {
        user = AppController.getInstance(this).getActiveUser();
        typefaceLight = Typeface.createFromAsset(getAssets(), "roboto_l.ttf");
        typefaceMedium = Typeface.createFromAsset(getAssets(), "roboto_m.ttf");
        categoryDAO = new CategoryDAO(this);
        interestListDAO = new InterestListDAO(this);

        // init main views
        buttonMenu = (ImageButton) findViewById(R.id.button_menu);
        menuDrawer = (DrawerLayout) findViewById(R.id.menuDrawer);
        imageProfilePhoto = (ImageView) findViewById(R.id.image_profilePhoto);
        textFullName = (TextView) findViewById(R.id.text_fullName);
        recyclerMenuDrawer = (RecyclerView) findViewById(R.id.recycler_menuDrawer);
        fab = (FloatingActionButton) findViewById(R.id.fab_requestOffer);

        // init dialog views
        dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_loading_data);
        dialog.setCancelable(false);
        dialog.setTitle(R.string.loading_data);
        progressBar = dialog.findViewById(R.id.progressBar);
        textError = (TextView) dialog.findViewById(R.id.text_error);
        buttonClose = (Button) dialog.findViewById(R.id.button_close);

        // customize fonts
        textFullName.setTypeface(typefaceMedium);
        textError.setTypeface(typefaceLight);
        buttonClose.setTypeface(typefaceLight);

        // customize menu drawer
        menuDrawer.setDrawerShadow(R.drawable.nd_shadow, MENU_DRAWER_GRAVITY);

        // customize recycler view
        recyclerMenuDrawer.setLayoutManager(new LinearLayoutManager(this));
        recyclerMenuDrawer.setItemAnimator(new DefaultItemAnimator());

        // set initial data
        textFullName.setText(user.getFirstName() + " " + user.getLastName());
        if (!user.getPhoto().isEmpty())
            Picasso.with(this).load(user.getPhoto()).into(imageProfilePhoto);

        // add listeners
        buttonMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (menuDrawer.isDrawerOpen(MENU_DRAWER_GRAVITY))
                    menuDrawer.closeDrawer(MENU_DRAWER_GRAVITY);
                else
                    menuDrawer.openDrawer(MENU_DRAWER_GRAVITY);
            }
        });
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, RequestOfferActivity.class);
                startActivityForResult(intent, Constants.REQUEST_REQUEST_OFFER);
            }
        });

        // get saved data flags
        interestListDAO.open();
        boolean hasInterestList = interestListDAO.hasItems();
        interestListDAO.close();
        categoryDAO.open();
        boolean hasCategories = categoryDAO.hasItems();
        categoryDAO.close();

        // check saved data
        if (hasInterestList && hasCategories) {
            // --has saved data--
            // get and display saved interest list
            interestListDAO.open();
            List<Category> interestList = interestListDAO.getAll();
            interestListDAO.close();
            displayMenuDrawerList(interestList);

            // show offers near me fragment
            FragmentManager fm = getSupportFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();
            ft.replace(R.id.container_main, new OffersNearMeFragment());
            ft.commit();

            // update interest list from server in background
            new InterestListTask(true).execute();
        } else {
            // no saved interest list >> load it from server
            new InterestListTask(false).execute();
        }
    }

    /**
     * method, used to display menu drawer list
     */
    private void displayMenuDrawerList(final List<Category> interestList) {
        // create static items in menu drawer as Category objects
        Category item1 = new Category(Constants.MENU_DRAWER_ITEM_INTEREST_LIST, getString(R.string.my_interest_list), R.drawable.interest_list_icon);
        Category item2 = new Category(Constants.MENU_DRAWER_ITEM_OFFERS_NEAR_ME, getString(R.string.offers_near_me), R.drawable.near_me_icon);
        Category item3 = new Category(Constants.MENU_DRAWER_ITEM_STORES_MAP, getString(R.string.stores_map), R.drawable.map_icon);
        Category item4 = new Category(Constants.MENU_DRAWER_ITEM_SEARCH, getString(R.string.search_offers), R.drawable.search_icon);
        Category item5 = new Category(Constants.MENU_DRAWER_ITEM_LOGOUT, getString(R.string.logout), R.drawable.logout_icon);

        // add them to interestList array list
        interestList.add(0, item4);
        interestList.add(0, item3);
        interestList.add(0, item2);
        interestList.add(0, item1);
        interestList.add(item5);

        // set menu drawer recycler adapter
        final MenuDrawerAdapter menuDrawerAdapter = new MenuDrawerAdapter(this, interestList, R.layout.recycler_menu_drawer_item);
        menuDrawerAdapter.setOnItemClickListener(new MenuDrawerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                // get selected item
                Category interestListItem = interestList.get(position);

                // check selected item id
                if (interestListItem.getId() == Constants.MENU_DRAWER_ITEM_INTEREST_LIST) {
                    // open update interest list activity
                    Intent intent = new Intent(MainActivity.this, UpdateInterestListActivity.class);
                    startActivityForResult(intent, Constants.REQUEST_UPDATE_INTEREST_LIST);

                    // close menu drawer
                    menuDrawer.closeDrawer(MENU_DRAWER_GRAVITY);
                    return;
                } else if (interestListItem.getId() == Constants.MENU_DRAWER_ITEM_SEARCH) {
                    // open search activity
                    Intent intent = new Intent(MainActivity.this, SearchActivity.class);
                    startActivity(intent);

                    // close menu drawer
                    menuDrawer.closeDrawer(MENU_DRAWER_GRAVITY);
                    return;
                } else if (interestListItem.getId() == Constants.MENU_DRAWER_ITEM_LOGOUT) {
                    // remove data from database
                    UserDAO userDAO = new UserDAO(getApplicationContext());
                    userDAO.open();
                    userDAO.deleteAll();
                    userDAO.close();
                    interestListDAO.open();
                    interestListDAO.deleteAll();
                    interestListDAO.close();
                    categoryDAO.open();
                    categoryDAO.deleteAll();
                    categoryDAO.close();

                    // null the active user in runtime
                    AppController.getInstance(getApplicationContext()).setActiveUser(null);

                    // goto login activity
                    Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                }

                // choice will show new fragment in main activity
                // ensure not same choice
                if (interestListItem.getId() != lastSelectedId) {
                    // prepare fm and ft
                    FragmentManager fm = getSupportFragmentManager();
                    FragmentTransaction ft = fm.beginTransaction();

                    if (interestListItem.getId() == Constants.MENU_DRAWER_ITEM_OFFERS_NEAR_ME) {
                        // show offers near me
                        ft.replace(R.id.container_main, new OffersNearMeFragment());
                    } else if (interestListItem.getId() == Constants.MENU_DRAWER_ITEM_STORES_MAP) {
                        // show stores map
                        ft.replace(R.id.container_main, new StoresMapFragment());
                    } else {
                        // show category offers
                        Bundle bundle = new Bundle();
                        bundle.putSerializable(Constants.KEY_CATEGORY, interestList.get(position));
                        CategoryOffersFragment fragment = new CategoryOffersFragment();
                        fragment.setArguments(bundle);
                        ft.replace(R.id.container_main, fragment);
                    }

                    // commit transaction
                    ft.commit();

                    // save selected item id
                    lastSelectedId = interestListItem.getId();
                }

                // close menu drawer
                menuDrawer.closeDrawer(MENU_DRAWER_GRAVITY);
            }
        });
        recyclerMenuDrawer.setAdapter(menuDrawerAdapter);
    }

    /**
     * async task, used to getAll interest list from server
     */
    private class InterestListTask extends AsyncTask<Void, Void, Void> {
        private boolean doInBackground;
        private MainActivity activity;
        private String interestListResponse;
        private String categoriesResponse;

        private InterestListTask(boolean doInBackground) {
            this.doInBackground = doInBackground;
            activity = MainActivity.this;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            // show progress
            showProgress(true);

            // check internet connection
            if (!InternetUtil.isConnected(activity)) {
                // show error
                showError(R.string.no_internet_connection);
                cancel(true);
            }
        }

        @Override
        protected Void doInBackground(Void... params) {
            // create and execute interest list request
            String interestListUrl = AppController.END_POINT + "/interest_list/" + user.getId();
            JsonReader jsonReader = new JsonReader(interestListUrl);
            interestListResponse = jsonReader.sendGetRequest();

            // create and execute categories request
            String categoriesUrl = AppController.END_POINT + "/categories";
            jsonReader = new JsonReader(categoriesUrl);
            categoriesResponse = jsonReader.sendGetRequest();

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            // validate responses
            if (interestListResponse == null || categoriesResponse == null) {
                showError(R.string.connection_error);

                return;
            }

            // --responses are valid, handle them--
            CategoriesHandler handler = new CategoriesHandler(interestListResponse);
            List<Category> interestList = handler.handle();

            handler = new CategoriesHandler(categoriesResponse);
            List<Category> categories = handler.handle();

            // check handling operations
            if (interestList == null || categories == null) {
                showError(R.string.connection_error);
                return;
            }

            // check their length
            if (interestList.size() == 0 || categories.size() == 0) {
                showError(R.string.connection_error);
                return;
            }

            // --two lists are okay--
            // save them in database
            interestListDAO.open();
            interestListDAO.deleteAll();
            interestListDAO.add(interestList);
            interestListDAO.close();

            categoryDAO.open();
            categoryDAO.deleteAll();
            categoryDAO.add(categories);
            categoryDAO.close();

            // display menu drawer list
            displayMenuDrawerList(interestList);

            // check doInBackground flag
            if (!doInBackground) {
                // show offers near me fragment
                FragmentManager fm = getSupportFragmentManager();
                FragmentTransaction ft = fm.beginTransaction();
                ft.replace(R.id.container_main, new OffersNearMeFragment());
                ft.commit();
                lastSelectedId = 1;
            }

            // hide progress
            showProgress(false);
        }

        private void showProgress(boolean show) {
            if (!doInBackground) {
                if (show) {
                    buttonClose.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            // cancel this task and finish activity
                            cancel(true);
                            activity.finish();
                        }
                    });
                    textError.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            // show progressBar
                            ViewUtil.showView(progressBar, true);
                            ViewUtil.showView(textError, false, View.INVISIBLE);

                            // execute another task
                            new InterestListTask(doInBackground).execute();
                        }
                    });
                    dialog.show();
                } else {
                    dialog.dismiss();
                }
            }
        }

        private void showError(int errorMsgRes) {
            if (!doInBackground) {
                String errorMsg = getString(errorMsgRes) + "\n" + getString(R.string.tap_here_to_retry);
                textError.setText(errorMsg);
                ViewUtil.showView(progressBar, false);
                ViewUtil.showView(textError, true, View.INVISIBLE);
            }
        }
    }

    /**
     * overridden method
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constants.REQUEST_UPDATE_INTEREST_LIST) {
            // check result
            if (resultCode == RESULT_OK) {
                // get new interest list from database
                interestListDAO.open();
                List<Category> interestList = interestListDAO.getAll();
                interestListDAO.close();
                displayMenuDrawerList(interestList);

                // show success msg
                AppMsg appMsg = AppMsg.makeText(this, R.string.your_interest_list_updated_successfully, AppMsg.STYLE_INFO);
                appMsg.setParent(R.id.view_main);
                AppMsg.cancelAll(this);
                appMsg.show();
            }
        } else if (requestCode == Constants.REQUEST_REQUEST_OFFER) {
            // check result
            if (resultCode == RESULT_OK) {
                // show success msg
                AppMsg appMsg = AppMsg.makeText(this, R.string.successful_request, AppMsg.STYLE_INFO);
                appMsg.setParent(R.id.view_main);
                AppMsg.cancelAll(this);
                appMsg.show();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    /**
     * overridden method
     */
    @Override
    public void onBackPressed() {
        if (menuDrawer.isDrawerOpen(MENU_DRAWER_GRAVITY)) {
            menuDrawer.closeDrawer(MENU_DRAWER_GRAVITY);
        } else {
            super.onBackPressed();
        }
    }

    /**
     * overridden method
     */
    @Override
    protected void onDestroy() {
        // check if forget active user or not
        boolean forgetMe = getIntent().getBooleanExtra(Constants.KEY_FORGET_ME, true);
        if (forgetMe)
            AppController.getInstance(getApplicationContext()).setActiveUser(null);

        super.onDestroy();
    }
}
