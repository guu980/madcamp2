package com.example.week2.Adapter;

import android.content.Context;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.week2.Data.Permission;
import com.example.week2.Data.UserInfo;
import com.example.week2.Data.WalkingRecordInfo;
import com.example.week2.MainActivity;
import com.example.week2.R;
import com.example.week2.Retrofit.RetrofitAPI;
import com.google.gson.JsonObject;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class WalkingRecordAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private ArrayList<WalkingRecordInfo> walkingRecords;
    private Context context;
    private Retrofit mRetrofit;
    private RetrofitAPI mRetrofitAPI;
//    private MainActivity mainActivity;

    class WalkingRecordViewHolder extends RecyclerView.ViewHolder{
        private TextView locationInfo;
        private TextView dateInfo;
        private ImageView dogThumbnail;
        private TextView totalTimeInfo;
        private TextView distanceInfo;
        private Button deleteButton;

        public WalkingRecordViewHolder(@NonNull View itemView) {
            super(itemView);

            locationInfo = itemView.findViewById(R.id.start_end_locations);
            dateInfo = itemView.findViewById(R.id.date_information);
            dogThumbnail = itemView.findViewById(R.id.dog_thumbnail);
            totalTimeInfo = itemView.findViewById(R.id.totalTime);
            distanceInfo = itemView.findViewById(R.id.totalDistance);
            deleteButton = itemView.findViewById(R.id.records_del_button);

//            mainActivity = (MainActivity) context;

            deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    WalkingRecordInfo eachData = walkingRecords.get(getAdapterPosition());
                    removeRecord(eachData.getDbId());

                    walkingRecords.remove(getAdapterPosition());
                    notifyItemRemoved(getAdapterPosition());
                    notifyItemRangeChanged(getAdapterPosition(), walkingRecords.size());
                }
            });
        }
    }

    private void setWalkingRetrofitInit() {
        //"http://192.249.19.252:2580/"
        String baseUrl = "http://192.249.19.252:2580/";
        mRetrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        mRetrofitAPI = mRetrofit.create(RetrofitAPI.class);
    }

    public void removeRecord(String dbId) {
        setWalkingRetrofitInit();
        String deviceId = getDeviceId();
        Call<JsonObject> walkingData = mRetrofitAPI.removeWalkingREcord(deviceId, dbId);

        Callback<JsonObject> mRetrofitCallback = new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                Log.d("Retrofit Success", response.toString());
                //Log.d(TAG, result);
                if (response.body() != null) {
                    Log.i("record deleted","Success!!!");
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                t.printStackTrace();
                Log.e("Err", t.getMessage());
            }
        };

        walkingData.enqueue(mRetrofitCallback);
    }



    public void setAdapter(ArrayList<WalkingRecordInfo> records) {
        this.walkingRecords = records;
    }

    public WalkingRecordAdapter(Context context, ArrayList<WalkingRecordInfo> records) {
        this.context = context;
        this.walkingRecords = records;
    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.walking_record_rows, parent, false);
        return new WalkingRecordAdapter.WalkingRecordViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        WalkingRecordAdapter.WalkingRecordViewHolder viewHolder = (WalkingRecordAdapter.WalkingRecordViewHolder) holder;

        String preStartLat = walkingRecords.get(position).getStartLat();
        String StartLat = preStartLat;
        if(preStartLat.length() > 5)
        {
            StartLat = StartLat.substring(0,4);
        }

        String preStartLon = walkingRecords.get(position).getStratLon();
        String StartLon = preStartLon;
        if(preStartLon.length() > 5)
        {
            StartLon = StartLon.substring(0,4);
        }

        String preEndLat = walkingRecords.get(position).getEndLat();
        String EndLat = preEndLat;
        if(preEndLat.length() > 5)
        {
            EndLat = EndLat.substring(0,4);
        }

        String preEndLon = walkingRecords.get(position).getEndLon();
        String EndLon = preEndLon;
        if(preEndLon.length() > 5)
        {
            EndLon = EndLon.substring(0,4);
        }

        String locationInfoString = "(" + StartLat + ", " + StartLon + ")"
                + " -> " + "(" + EndLat + ", " + EndLon + ")";

        //.substring(0,3)
        viewHolder.locationInfo.setText(locationInfoString);

        String dateInfoString = walkingRecords.get(position).getDateYear() + "/" + walkingRecords.get(position).getDateMonth() + "/" + walkingRecords.get(position).getDateDay()
                + " " + walkingRecords.get(position).getDateHour() + ":" + walkingRecords.get(position).getDateMin();
        viewHolder.dateInfo.setText(dateInfoString);

        String totalTimeInfoString = walkingRecords.get(position).getTotalTimeHours() + ":" + walkingRecords.get(position).getTotalTimeMins();
        viewHolder.totalTimeInfo.setText(totalTimeInfoString);

        String distanceInfoString = walkingRecords.get(position).getTotalDistance();
        viewHolder.distanceInfo.setText(distanceInfoString + "km");
    }

    @Override
    public int getItemCount() {
        return walkingRecords.size();
    }

    public String getDeviceId()
    {
//        mainActivity = (MainActivity) RecordActivity.this.mainActivity;
//        TelephonyManager tm = (TelephonyManager) mainActivity.getSystemService(TELEPHONY_SERVICE);

        TelephonyManager telephonyManager;
        telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

        //getApplicationContext 가능한지 체크
        if (ActivityCompat.checkSelfPermission(context.getApplicationContext(), Permission.getCertainPerm(4)) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    Activity#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for Activity#requestPermissions for more details.
            return null;
        }

//        String deviceId = tm.getDeviceId();
        String deviceId = telephonyManager.getDeviceId();
        return deviceId;
    }
}