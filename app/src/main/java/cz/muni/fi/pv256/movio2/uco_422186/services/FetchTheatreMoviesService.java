package cz.muni.fi.pv256.movio2.uco_422186.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import java.util.ArrayList;

import cz.muni.fi.pv256.movio2.uco_422186.MainActivity;
import cz.muni.fi.pv256.movio2.uco_422186.R;
import cz.muni.fi.pv256.movio2.uco_422186.data.Movies;
import cz.muni.fi.pv256.movio2.uco_422186.data.MoviesManager;
import cz.muni.fi.pv256.movio2.uco_422186.dto.APIResult;
import cz.muni.fi.pv256.movio2.uco_422186.dto.MovieDTO;
import cz.muni.fi.pv256.movio2.uco_422186.helpers.DtoMapper;
import cz.muni.fi.pv256.movio2.uco_422186.helpers.TimeHelpers;
import cz.muni.fi.pv256.movio2.uco_422186.models.Movie;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static android.content.Context.NOTIFICATION_SERVICE;

public class FetchTheatreMoviesService {

    private Context mContext;

    public FetchTheatreMoviesService(Context context) {
        mContext = context;
    }

    public void fetch() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.themoviedb.org/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        MovioDbService service = retrofit.create(MovioDbService.class);
        Call<APIResult> call = service.theatreMovies(TimeHelpers.getNowReleaseDate(), TimeHelpers.getEndReleaseDate());
        call.enqueue(new Callback<APIResult>() {

            @Override
            public void onResponse(Call<APIResult> call, Response<APIResult> response) {
                APIResult result = response.body();
                setThatreMovies(result);
                notifyActivity();
            }

            @Override
            public void onFailure(Call<APIResult> call, Throwable t) {
                call.cancel();

                Intent appIntent = new Intent(mContext, MainActivity.class);
                PendingIntent pIntent = PendingIntent.getActivity(mContext, 0, appIntent, 0);

                Notification n = new Notification.Builder(mContext)
                        .setContentTitle("Error occured when downloading movie list")
                        .setSmallIcon(R.mipmap.ic_launcher_round)
                        .setContentIntent(pIntent)
                        .setAutoCancel(true)
                        .build();

                NotificationManager notificationManager = (NotificationManager) mContext.getSystemService(NOTIFICATION_SERVICE);
                notificationManager.notify(0, n);
            }
        });
    }

    private void setThatreMovies(APIResult result) {
        Movies.theaterMovies = new ArrayList<>();
        for (MovieDTO movieDTO : result.movies) {
            Movie movie = DtoMapper.mapDTOToMovie(movieDTO);
            MoviesManager moviesManager = new MoviesManager(mContext);
            Movie dbMovie = moviesManager.getMovie(movie);
            movie.setFavorite(dbMovie != null);
            Movies.theaterMovies.add(movie);
        }
    }

    private void notifyActivity() {
       Intent broadcastIntent = new Intent();
       broadcastIntent.setAction(MainActivity.ResponseReceiver.ACTION_RESPONSE);
       broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
       mContext.sendBroadcast(broadcastIntent);
    }
}