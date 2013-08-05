package cl.returnvoid.android.conmono.fragments;

import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.app.Activity;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;

import com.google.android.gms.location.LocationListener;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import cl.returnvoid.android.conmono.R;

public class PhotosMapFragment extends FragmentActivity implements LocationListener {
    private GoogleMap map;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_photos_map);

        map = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
        map.setMyLocationEnabled(true);
        map.setMapType(map.MAP_TYPE_NORMAL);
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        Criteria criteria = new Criteria();
        String provider = locationManager.getBestProvider(criteria, true);
        Location location = locationManager.getLastKnownLocation(provider);

        if(map != null){
            //map.setMapType(map.MAP_TYPE_NORMAL);
        }
    }

    private void drawMarker(Location location){
        map.clear();
        LatLng currentPosition = new LatLng(location.getLatitude(), location.getLongitude());
        MarkerOptions markerOptions = new MarkerOptions()
                .position(currentPosition)
                .snippet("Lat:" + location.getLatitude() + "Lng:" + location.getLongitude());
        map.addMarker(markerOptions);
    }

    private void animate(){
        getFragmentManager().beginTransaction().setCustomAnimations(R.animator.expand, R.animator.collapse);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.photos_map, menu);
        return true;
    }

    @Override
    public void onLocationChanged(Location location) {
        drawMarker(location);
    }
}
