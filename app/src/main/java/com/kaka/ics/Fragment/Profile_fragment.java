package com.kaka.ics.Fragment;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.google.gson.Gson;
import com.kaka.ics.API_retro.API_parameter;
import com.kaka.ics.AppUtils.AppPrefrences;
import com.kaka.ics.AppUtils.BaseUrl;
import com.kaka.ics.AppUtils.Internet_Connectivity;
import com.kaka.ics.AppUtils.Utilview;
import com.kaka.ics.Model.Profile_responce;
import com.kaka.ics.R;
import com.kaka.ics.View.MainActivity_drawer;

import cn.pedant.SweetAlert.SweetAlertDialog;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Profile_fragment extends Fragment
{
    Toolbar toolbar;
    TextView txToolbar, cart_count;
    ImageView imgToolbar, cart ,icEditProfile;

    private TextView name, mail, mobile, address;
    private API_parameter ApiService;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_profile,container,false);

        name = (TextView) view.findViewById(R.id.tx_name);
        mail = (TextView) view.findViewById(R.id.tx_mail);
        mobile = (TextView) view.findViewById(R.id.tx_mobile);
        address = (TextView) view.findViewById(R.id.tx_address);
        toolbar = (Toolbar) view.findViewById(R.id.toolbar);
        txToolbar = (TextView) view.findViewById(R.id.txToolbar);
        icEditProfile = (ImageView) view.findViewById(R.id.ic_editprofile);
        cart = (ImageView) view.findViewById(R.id.icon_cart);
        cart_count = (TextView) view.findViewById(R.id.cart_count);

        cart_count.setVisibility(View.GONE);
        cart.setVisibility(View.GONE);

        txToolbar.setText("Profile");
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp);
        ApiService = BaseUrl.getAPIService();

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utilview.hidekeyboard(getActivity());
                ((MainActivity_drawer)getActivity()).onBackPressed();
            }
        });

        icEditProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Fragment fm = new Update_Profile_fragment();
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragment_layout,fm)
                        .addToBackStack(fm.getClass().getSimpleName()).commit();
            }
        });
        if (Internet_Connectivity.isConnected(getActivity()))
        {          getProfile(); }
        else
        {      SweetAlertDialog pDialog = new SweetAlertDialog(getActivity(), SweetAlertDialog.ERROR_TYPE);
            pDialog.setTitleText("Oops...");
            pDialog.setContentText("No Internet Connection !");
            pDialog.show();                              }

        return view;
    }

    private void getProfile()
    {
        String user_id = AppPrefrences.getUserid(getActivity());

        final ProgressDialog dialog;
        dialog = new ProgressDialog(getActivity());
        dialog.setMessage("Processing");
        dialog.setCancelable(true);
        dialog.show();

        ApiService.PROFILE_CALL(user_id).enqueue(new Callback<Profile_responce>() {
            @Override
            public void onResponse(Call<Profile_responce> call, Response<Profile_responce> response) {
                Log.e("PROFILE RESPONSE.", "" + new Gson().toJson(response.body()));
                Log.e("PROFILE RESPONSE.", "-------------------------------------------------");
                dialog.dismiss();
                if (response.body().getResponce()) {

                    name.setText(""+ response.body().getData().getFName()+"  "+response.body().getData().getLName());
                    mobile.setText(""+response.body().getData().getMobileNo());
                    mail.setText(""+response.body().getData().getUserEmail());
                    address.setText(""+response.body().getData().getAddress());
                }
                else if (!response.body().getResponce())
                {
                    Toast.makeText(getActivity(), "No Data( false )", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    Toast.makeText(getActivity(), "Error while Connecting...", Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onFailure(Call<Profile_responce> call, Throwable t) {
                dialog.dismiss();
                Log.e("PROFILE error at call" , ""+t.getLocalizedMessage());
                Log.e("PROFILE error at call" , ""+t.getMessage());
                Log.e("PROFILE error at call" , ""+t.getCause());
            }
        });
    }
}
