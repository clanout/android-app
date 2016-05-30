package com.clanout.app.service;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;

import com.clanout.app.api.core.ApiManager;
import com.clanout.app.api.user.request.UpdateUserLocationApiRequest;
import com.clanout.app.api.user.response.UpdateUserLocationApiResponse;
import com.clanout.app.cache.core.CacheManager;
import com.clanout.app.cache.generic.GenericCache;
import com.clanout.app.common.analytics.AnalyticsHelper;
import com.clanout.app.config.GoogleAnalyticsConstants;
import com.clanout.app.model.Location;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import timber.log.Timber;


public class LocationService
{
    public static final String UNKNOWN_ZONE = "UNKNOWN_ZONE";

    private static LocationService instance;
    private boolean appAvailableInZone;

    public static void init(Context context, LocationManager locationManager, GoogleService
            googleService)
    {
        if (instance != null) {
            return;
        }

        instance = new LocationService(context, locationManager, googleService);
    }

    public static LocationService getInstance()
    {
        if (instance == null) {
            /* Analytics */
            AnalyticsHelper.sendCaughtExceptions(GoogleAnalyticsConstants.METHOD_Z16, false);
            /* Analytics */

            throw new IllegalStateException("[LocationService Not Initialized]");
        }

        return instance;
    }

    private Context context;
    private LocationManager locationManager;
    private GoogleService googleService;
    private GenericCache genericCache;

    private Location location;

    private LocationListener locationListener;

    private LocationService(Context context, LocationManager locationManager, GoogleService
            googleService)
    {
        this.context = context;
        this.locationManager = locationManager;
        this.googleService = googleService;
        this.genericCache = CacheManager.getGenericCache();
    }

    public boolean isLocationPermissionGranted()
    {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.M || ActivityCompat
                .checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED;
    }

    public boolean isLocationServiceAvailable()
    {
        return locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    public Observable<Location> fetchCurrentLocation()
    {
        if (location != null) {
            return Observable.just(location);
        }
        else {
            return Observable
                    .create(new Observable.OnSubscribe<android.location.Location>()
                    {
                        @Override
                        public void call(Subscriber<? super android.location.Location> subscriber)
                        {
                            try {
                                if (!LocationServices.FusedLocationApi
                                        .getLocationAvailability(googleService
                                                .getGoogleApiClient())
                                        .isLocationAvailable()) {
                                    Timber.v("[FusedLocationApi] Last Known Location Unavailable");
                                    subscriber.onNext(null);
                                    subscriber.onCompleted();
                                }
                                else {
                                    Timber.v("[FusedLocationApi] Last Known Location Available");


                                    android.location.Location location = LocationServices
                                            .FusedLocationApi
                                            .getLastLocation(googleService.getGoogleApiClient());

                                    subscriber.onNext(location);
                                    subscriber.onCompleted();
                                }
                            }
                            catch (Exception e) {

                                /* Analytics */
                                AnalyticsHelper.sendCaughtExceptions(GoogleAnalyticsConstants
                                        .METHOD_K, false);
                                /* Analytics */
                                subscriber.onError(e);
                            }
                        }
                    })
                    .subscribeOn(Schedulers.newThread())
                    .flatMap(new Func1<android.location.Location, Observable<android.location
                            .Location>>()
                    {
                        @Override
                        public Observable<android.location.Location> call(android.location
                                                                                  .Location
                                                                                  location)
                        {
                            if (location != null) {
                                return Observable.just(location);
                            }
                            else {

                                location = locationManager
                                        .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

                                if (location == null) {
                                    Timber.v("[LocationManager] Last Known Location Unavailable");
                                }
                                else {
                                    Timber.v("[LocationManager] Last Known Location Available");
                                }

                                return Observable.just(location);
                            }
                        }
                    })
                    .subscribeOn(Schedulers.newThread())
                    .flatMap(new Func1<android.location.Location, Observable<android.location
                            .Location>>()
                    {
                        @Override
                        public Observable<android.location.Location> call(android.location
                                                                                  .Location
                                                                                  location)
                        {
                            if (location != null) {
                                return Observable.just(location);
                            }

                            Timber.v("[FusedLocationApi] Refreshing Location ...");

                            return Observable
                                    .create(new Observable
                                            .OnSubscribe<android.location.Location>()
                                    {
                                        @Override
                                        public void call(final Subscriber<? super android
                                                .location.Location> subscriber)
                                        {
                                            LocationRequest locationRequest = new
                                                    LocationRequest();
                                            locationRequest
                                                    .setPriority(LocationRequest
                                                            .PRIORITY_HIGH_ACCURACY);

                                            locationListener = new LocationListener()
                                            {
                                                @Override
                                                public void onLocationChanged(android.location
                                                                                      .Location
                                                                                      location)
                                                {
                                                    Timber.v("[FusedLocationApi] Received Updated" +
                                                            " Location");
                                                    subscriber.onNext(location);
                                                    subscriber.onCompleted();
                                                }
                                            };


                                            LocationServices.FusedLocationApi
                                                    .requestLocationUpdates(googleService
                                                                    .getGoogleApiClient(),
                                                            locationRequest, locationListener);

                                        }
                                    })
                                    .subscribeOn(AndroidSchedulers.mainThread());
                        }
                    })
                    .map(new Func1<android.location.Location, Location>()
                    {
                        @Override
                        public Location call(android.location.Location googleApiLocation)
                        {
                            if (locationListener != null) {
                                LocationServices.FusedLocationApi
                                        .removeLocationUpdates(googleService
                                                .getGoogleApiClient(), locationListener);

                                Timber.v("[FusedLocationApi] Closed Location Listener");
                            }

                            try {
                                Location location = new Location();
                                location.setLongitude(googleApiLocation.getLongitude());
                                location.setLatitude(googleApiLocation.getLatitude());

                                return location;
                            }
                            catch (Exception e) {
                                /* Analytics */
                                AnalyticsHelper.sendCaughtExceptions(GoogleAnalyticsConstants
                                        .METHOD_L, false);
                                /* Analytics */

                                Timber.e("[Location fetch error] " + e.getMessage());
                                return null;
                            }
                        }
                    })
                    .doOnNext(new Action1<Location>()
                    {
                        @Override
                        public void call(Location location)
                        {
                            if (location != null) {
                                Timber.v("Location : " + location.toString());
                            }

                            AnalyticsHelper.sendEvents("LOCATION", "LOCATION_SET", null);
                            LocationService.this.location = location;
                        }
                    })
                    .subscribeOn(Schedulers.newThread());
        }
    }

    public Location getCurrentLocation()
    {
        return location;
    }

    public Observable<Boolean> pushUserLocation()
    {
        if (location == null) {
            /* Analytics */
            AnalyticsHelper.sendCaughtExceptions(GoogleAnalyticsConstants.METHOD_Z6, false);
            /* Analytics */

            return Observable.error(new IllegalStateException("Location Null"));
        }

        UpdateUserLocationApiRequest request = new UpdateUserLocationApiRequest(location
                .getLatitude(), location.getLongitude());
        return ApiManager.getUserApi()
                .updateUserLocation(request)
                .map(new Func1<UpdateUserLocationApiResponse, Boolean>()
                {
                    @Override
                    public Boolean call(UpdateUserLocationApiResponse response)
                    {
                        location.setName(response.getName());
                        location.setZone(response.getZone());
                        if (response.isRelocated()) {
                            CacheManager.clearFriendsCache();
                        }

                        return true;
                    }
                })
                .onErrorReturn(new Func1<Throwable, Boolean>()
                {
                    @Override
                    public Boolean call(Throwable e)
                    {
                              /* Analytics */
                        AnalyticsHelper.sendCaughtExceptions(GoogleAnalyticsConstants
                                .METHOD_FAILED_TO_PUSH_UPDATED_LOCATION, false);
                              /* Analytics */

                        Timber.v("[Failed to push updated location] " + e.getMessage());
                        return false;
                    }
                })
                .subscribeOn(Schedulers.newThread());
    }

    public boolean isAppAvailableInZone()
    {
        if (location.getZone().equals(UNKNOWN_ZONE)) {
            return false;
        }
        else {

            return true;
        }
    }
}
