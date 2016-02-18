package com.offerland.app;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import datamodels.Constants;
import datamodels.Offer;

/**
 * Created by Shamyyoun on 6/27/2015.
 */
public class OfferActivity extends ActionBarActivity {
    // main objects
    private Offer offer;
    private Typeface typeface;

    // main views
    private ImageView imageOffer;
    private TextView textTitle;
    private TextView textDesc;
    private TextView textDiscount;
    private Button buttonViewOnMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_offer);

        initComponents();
    }

    /**
     * method, used to initialize components
     */
    private void initComponents() {
        offer = (Offer) getIntent().getSerializableExtra(Constants.KEY_OFFER);
        typeface = Typeface.createFromAsset(getAssets(), "roboto_l.ttf");
        imageOffer = (ImageView) findViewById(R.id.image_offer);
        textTitle = (TextView) findViewById(R.id.text_title);
        textDesc = (TextView) findViewById(R.id.text_desc);
        textDiscount = (TextView) findViewById(R.id.text_discount);
        buttonViewOnMap = (Button) findViewById(R.id.button_viewOnMap);

        // customize fonts
        textTitle.setTypeface(typeface);
        textDesc.setTypeface(typeface);
        textDiscount.setTypeface(typeface);
        buttonViewOnMap.setTypeface(typeface);

        // set data
        if (!offer.getImage().isEmpty())
            Picasso.with(this).load(offer.getImage()).into(imageOffer);

        textTitle.setText(offer.getTitle());
        textDesc.setText(offer.getDesc());
        String discount = "" + offer.getDiscount();
        discount = discount.substring(0, discount.indexOf("."));
        textDiscount.setText("Discount: " + discount + "%");

        // add listeners
        buttonViewOnMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(OfferActivity.this, OfferMapActivity.class);
                intent.putExtra(Constants.KEY_OFFER, offer);
                startActivity(intent);
            }
        });
    }
}
