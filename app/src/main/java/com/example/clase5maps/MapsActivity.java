package com.example.clase5maps;

import androidx.core.app.ActivityCompat;

import androidx.fragment.app.FragmentActivity;


import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Dot;
import com.google.android.gms.maps.model.Gap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PatternItem;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.PolyUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, LocationListener, GoogleMap.OnMapLongClickListener, GoogleMap.OnMarkerClickListener {

    private GoogleMap mMap;
    private Marker miUbicacion;
    private TextView site;
    private List<Marker> markers;
    private Polyline rastro;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        markers = Collections.synchronizedList(new ArrayList<>());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
        }, 11);

        site = findViewById(R.id.siteTxt);
        site.setTextColor(Color.RED);



    }

    public static double euclidianDistance(double lat1, double lat2, double lon1,
                                           double lon2) {

        final int R = 6371; // Radius of the earth

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = R * c * 1000; // convert to meters


        distance = Math.pow(distance, 2);

        return Math.sqrt(distance);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @SuppressLint("MissingPermission")
    @Override

    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        LatLng base = new LatLng(3.451647, -76.531982);
        miUbicacion = mMap.addMarker(new MarkerOptions().position(base).title("Posición Actual"));
        miUbicacion.setIcon((BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));

        //Solicitud de Ubicacion
        LocationManager manager = (LocationManager) getSystemService(LOCATION_SERVICE);

        manager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, this);

        mMap.setOnMapLongClickListener(this);
        mMap.setOnMarkerClickListener(this);
        List<PatternItem> pattern = Arrays.<PatternItem>asList(
                new Dot(), new Gap(10));
        rastro=mMap.addPolyline(new PolylineOptions().color(Color.BLUE).clickable(false).pattern(pattern).visible(true));
    }

    public void actualizarTextDistancias() {
        double min = Double.MAX_VALUE;
        String nom = "";
        for (Marker mark : markers) {
            double distance = euclidianDistance(mark.getPosition().latitude,
                    miUbicacion.getPosition().latitude,
                    mark.getPosition().longitude,
                    miUbicacion.getPosition().longitude);
            if (distance < min) {
                min = distance;
                nom = mark.getTitle();
            }

        }
        if (min == Double.MAX_VALUE) {
            site.setText("");
        } else if (min <= 40) {
            site.setText("Usted se encuentra en " + nom);
        } else {
            site.setText("La distancia a " + nom + " es de " + min + " metros");
        }

    }

    @Override
    public void onLocationChanged(Location location) {
        LatLng pos = new LatLng(location.getLatitude(), location.getLongitude());
        miUbicacion.setPosition(pos);
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(pos, 15));
        List<LatLng> points = rastro.getPoints();
        points.add(pos);
        rastro.setPoints(points);
        actualizarTextDistancias();

    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }

    @Override
    public void onMapLongClick(final LatLng latLng) {

        LayoutInflater layoutInflater = getLayoutInflater();

        final View view = layoutInflater.inflate(R.layout.fragment_input, null);
        final AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setMessage("Nombre de Marker");
        alertDialog.setCancelable(false);

        final EditText editText = (EditText) view.findViewById(R.id.markerName);

        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //site.setText(editText.getText().toString());
                MarkerOptions mo = new MarkerOptions().position(latLng).title(editText.getText().toString());
                Marker m = mMap.addMarker(mo);
                markers.add(m);
            }
        });

        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                alertDialog.dismiss();
            }
        });
        alertDialog.setView(view);
        alertDialog.show();
    }


    @Override
    public boolean onMarkerClick(Marker marker) {
        if (!marker.equals(miUbicacion)) {
            Toast.makeText(this, "La distancia al marcador " + marker.getTitle() + " es de " + euclidianDistance(marker.getPosition().latitude, miUbicacion.getPosition().latitude, marker.getPosition().longitude, miUbicacion.getPosition().longitude), Toast.LENGTH_SHORT).show();
        } else {
            Geocoder geo = new Geocoder(this, Locale.getDefault());
            String dir = "";
            try {
                dir = geo.getFromLocation(miUbicacion.getPosition().latitude, miUbicacion.getPosition().longitude, 1).get(0).getAddressLine(0);
            } catch (IOException e) {
                e.printStackTrace();
            }
            Toast.makeText(this, "Su dirección es " + dir, Toast.LENGTH_SHORT).show();
        }
        return false;
    }
}
