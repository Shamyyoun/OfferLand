package com.offerland.app;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import datamodels.Constants;
import datamodels.Offer;

public class OfferMapActivity extends ActionBarActivity {
    private Offer offer;
    private SupportMapFragment mapFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_offer_map);

        initComponents();
    }

    private void initComponents() {
        offer = (Offer) getIntent().getSerializableExtra(Constants.KEY_OFFER);
        mapFragment = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map));
        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                googleMap.setMyLocationEnabled(true);

                LatLng coordinate = new LatLng(offer.getLat(), offer.getLng());
                googleMap.addMarker(new MarkerOptions()
                        .title(offer.getTitle())
                        .snippet(offer.getDesc())
                        .position(coordinate));

                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(coordinate, 15);
                googleMap.animateCamera(cameraUpdate);
            }
        });
    }
}
