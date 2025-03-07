package com.kaka.ics.Fragment;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
//import com.google.android.gms.vision.Frame;
import com.google.gson.Gson;
import com.kaka.ics.API_retro.API_parameter;
import com.kaka.ics.AppUtils.AppPrefrences;
import com.kaka.ics.AppUtils.BaseUrl;
import com.kaka.ics.AppUtils.DatabaseHandler;
import com.kaka.ics.AppUtils.HttpHandler;
import com.kaka.ics.AppUtils.Utilview;
import com.kaka.ics.Model.Advance_Pay;
import com.kaka.ics.Model.Order_Responce;
import com.kaka.ics.R;
import com.kaka.ics.View.MainActivity_drawer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import instamojo.library.InstamojoPay;
import instamojo.library.InstapayListener;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class Order_fragment extends Fragment {

    private Button placeOrder;

    Toolbar toolbar;
    TextView txToolbar, cnt;
    ImageView imgToolbar, cart, ic_map, close_map;
    private Activity activity;
    private TextView subtotal, payable, advpay, headadv, tx_mmap;
    private DatabaseHandler db;
    private EditText name, mobile, mobileopt, landmark, address;
    private RadioGroup radio_payment;
    private String pay_mode="online", nam, phone, optphone, landm, addr;
    private API_parameter ApiService;
    JSONArray passArray;
    private DatabaseHandler dbcart;
    static double advance = 0;
    private FrameLayout Frame_Map;
    ScrollView scrollview;
    LinearLayout llinearlyt;
    SupportMapFragment mMap;
    ProgressDialog dialog;
    BroadcastReceiver broadcastReceiver;

    private GoogleMap googleMap;
    private Location locationC;
    int mapon=0;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        activity = (Activity) context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_order, container, false);
        ((MainActivity_drawer) getActivity()).lockDrawer();

        placeOrder = (Button) view.findViewById(R.id.place_order);

        toolbar = (Toolbar) view.findViewById(R.id.toolbar);
        txToolbar = (TextView) view.findViewById(R.id.txToolbar);
        imgToolbar = (ImageView) view.findViewById(R.id.imgToolbar);
        ic_map = (ImageView) view.findViewById(R.id.ic_map);

        subtotal = (TextView) view.findViewById(R.id.tx_subtotal);
        payable = (TextView) view.findViewById(R.id.tx_payable);
        advpay = (TextView) view.findViewById(R.id.tx_advpay);
        name = (EditText) view.findViewById(R.id.ed_name);
        mobile = (EditText) view.findViewById(R.id.ed_mobile);
        mobileopt = (EditText) view.findViewById(R.id.ed_mobileopt);
        headadv = (TextView) view.findViewById(R.id.head_adv);
        landmark = (EditText) view.findViewById(R.id.ed_landmark);
        address = (EditText) view.findViewById(R.id.ed_aaddress);

        cart = (ImageView) view.findViewById(R.id.icon_cart);
        cnt = (TextView) view.findViewById(R.id.cart_count);

        scrollview = (ScrollView) view.findViewById(R.id.scrllvu);
        llinearlyt = (LinearLayout) view.findViewById(R.id.ll_ttoolbar);

        Frame_Map = (FrameLayout) view.findViewById(R.id.map_fragment);
        tx_mmap = (TextView) view.findViewById(R.id.tx_mmap);
        close_map = (ImageView) view.findViewById(R.id.close_map);

        FragmentManager fragmentManager = ((MainActivity_drawer) getActivity()).getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.map_fragment , new Map_Fragment()).commit();

        new Advance_pay().execute();
        cart.setVisibility(View.GONE);
        cnt.setVisibility(View.GONE);
        dbcart = new DatabaseHandler(getActivity());
        ApiService = BaseUrl.getAPIService();

        db = new DatabaseHandler(getActivity());
        txToolbar.setText("Final Order");
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utilview.hidekeyboard(getActivity());
                ((MainActivity_drawer) getActivity()).onBackPressed();
            }
        });

        subtotal.setText("₹" + db.getdaystotal());

        name.setText("" + AppPrefrences.getName(getActivity()));
        mobile.setText(""+AppPrefrences.getMobile(getActivity()));

        Click_Listeners();

        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(localBroadcastRec, new IntentFilter("StringAddr"));

        return view;
    }

    private void Click_Listeners() {

        close_map.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mapon=0;
                close_map.setVisibility(View.GONE);
                Frame_Map.setVisibility(View.GONE);
            }
        });


        ic_map.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (mapon==0)
                {         Frame_Map.setVisibility(View.VISIBLE);
                    close_map.setVisibility(View.VISIBLE);
                    Frame_Map.setAlpha(0.0f);
                    Frame_Map.animate().translationX(0.1f).translationY(0.1f).alpha(1.0f).setListener(null);
                mapon=1;            }

                else
                {                 //Frame_Map.setVisibility(View.GONE);
                        Frame_Map.animate().translationY(0).alpha(0.0f).setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                 super.onAnimationEnd(animation);
                                 close_map.setVisibility(View.GONE);
                                 Frame_Map.setVisibility(View.GONE);
                            }
                        });
                mapon=0;        }

                //MapDialog_fragment mapf = new MapDialog_fragment();
                //mapf.show(getFragmentManager(), null);

            }
        });

        placeOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validate()) {
                    passArray = new JSONArray();
                    make_JSON();
//                    Toast.makeText(activity, "Order Placed...", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getActivity(), "Please fill data correctly...", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    // Google Map Address Receiver
    private final BroadcastReceiver localBroadcastRec = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                String Faddress = intent.getStringExtra("Addr");
                address.setText("" + Faddress);

                //Log.e("FINALY Address in Delivery Fragment :","------------------"+intent.getStringExtra("Addr"));
            }
        }
    };


    private boolean validate() {
        nam = name.getText().toString().trim();
        phone = mobile.getText().toString().trim();
        optphone = mobileopt.getText().toString().trim();
        landm = landmark.getText().toString();
        addr = address.getText().toString().trim();


        if (nam.matches("") && phone.matches("") && addr.matches("") && landm.matches("")) {
            Toast.makeText(getActivity(), "Please fill data correctly", Toast.LENGTH_SHORT).show();
            return false;
        } else if (nam.matches("")) {
            Toast.makeText(getActivity(), "Please enter Name", Toast.LENGTH_SHORT).show();
            return false;
        } else if (!validCellPhone(phone)) {
            Toast.makeText(getActivity(), "Please enter valid mobile no.", Toast.LENGTH_SHORT).show();
            return false;
        } else if (landm.matches("")) {
            Toast.makeText(getActivity(), "Please enter landmark", Toast.LENGTH_SHORT).show();
            return false;
        } else if (addr.matches("")) {
            Toast.makeText(getActivity(), "Please enter address", Toast.LENGTH_SHORT).show();
            return false;
        }  else {
            return true;
        }
    }

    private void make_JSON() {
        int cnt = 0;

        ArrayList<HashMap<String, String>> dlist = db.getCartAll();

        Log.e("Order Fragment ", "------------------------------------" + dlist.get(0));


        for (int i = 0; i < dlist.size(); i++) {

            cnt++;

            Log.e("Order Fragment ", "------------------------------------");

            HashMap<String, String> cob = dlist.get(i);

            String userid = AppPrefrences.getUserid(getActivity());
            String address = AppPrefrences.getAddress(getActivity());

            String catid = cob.get("category_id");
            String subcatid = cob.get("subcat_id");
            String qty = cob.get("num_helpers");
            String dfrom = cob.get("date_from");
            String dto = cob.get("date_to");
            String workdetail = cob.get("work_details");


            JSONObject jObj = new JSONObject();

            try {
                jObj.put("user_id", userid);
                jObj.put("payment_mode", pay_mode);
                jObj.put("address", addr);
                jObj.put("sub_cat_no", qty);                        // give count of number of helpers
                jObj.put("sub_cat_id", subcatid);
                jObj.put("p_cat_id", catid);
                jObj.put("date_from", dfrom);
                jObj.put("date_to", dto);
                jObj.put("description", workdetail);
                passArray.put(jObj);
            } catch (JSONException ex) {
                ex.printStackTrace();
            }
        }
        if (cnt == dlist.size()) {
            Log.e(advance+"INSTAMOJO CALL___",""+AppPrefrences.getMail(getActivity())+" --- "+phone+" --- "+advance+" --- "+pay_mode+" --- "+nam);
            if (advance==0)
            {
                Call_Order(passArray);
            }
            else {
                /*Intent payin = new Intent(getActivity() , Payment_Activity.class);
                payin.putExtra("amail",AppPrefrences.getMail(getActivity()));
                payin.putExtra("aphone",phone);
                payin.putExtra("aamt",""+10);
                payin.putExtra("apur",pay_mode);
                payin.putExtra("abnm",nam);
                startActivity(payin);*/
                callInstamojoPay(AppPrefrences.getMail(getActivity()), phone,""+advance , pay_mode, nam);
            }
            //Call_Order(passArray);
            Log.e("JSON ARRAY IS >>> ", "" + passArray);
            Log.e(" SIZE ......... ", " COUNT ........ " + cnt + "...... List Size is ..... " + dlist.size() + " |||||  DATABASE COUNT " + dbcart.getCartCount());
        }
    }

    private void Call_Order(JSONArray jsonarray) {
        final ProgressDialog dialog;
        dialog = new ProgressDialog(getActivity());
        dialog.setMessage("Processing");
        dialog.setCancelable(true);
        dialog.show();

        ApiService.Order_Cart(jsonarray).enqueue(new Callback<Order_Responce>() {
            @Override
            public void onResponse(Call<Order_Responce> call, Response<Order_Responce> response) {
                Log.e("ORDER RESPONSE ...", "" + new Gson().toJson(response.body().getData()));
                Log.e("ORDER RESPONSE ...", "-------------------------------------------------");
                dialog.dismiss();
                if (response.body().getResponce()) {
                    Clear_ed();
                    dbcart.clearCart();
                    if (advance>0) {
                        Log.e("PAY UPDATE "," ADVANCE IS  "+advance);
                        CALL_PAYUP(response.body().getData().getOrderId(),1);      }
                    else
                    {
                        Log.e("PAY UPDATE "," ADVANCE IS  "+advance);
                        CALL_PAYUP(response.body().getData().getOrderId(),0);                  }
                    Utilview.clear_Fragments(getActivity());
                    Booking_fragment bookfr = new Booking_fragment();
                    getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragment_layout, bookfr)
                            .addToBackStack(null).commit();
                    Toast.makeText(activity, "Order Placed...", Toast.LENGTH_SHORT).show();
                } else if (!response.body().getResponce()) {
                    dialog.dismiss();
                    Toast.makeText(getActivity(), "No Data( false )", Toast.LENGTH_SHORT).show();
                } else {
                    dialog.dismiss();
                    Toast.makeText(getActivity(), "Error while Connecting...", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Order_Responce> call, Throwable t) {
                dialog.dismiss();
                Log.e("error at call 1 ", "" + t.getLocalizedMessage());
                Log.e("error at call 2 ", "" + t.getMessage());
                Log.e("error at call 3 ", "" + t.getCause());
                Log.e("error at call 4 ", "" + t.getStackTrace());
            }
        });
    }


    class Advance_pay extends AsyncTask<String, String, String> {
        String output = "";
        String url;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = new ProgressDialog(getActivity());
            dialog.setMessage("Processing");
            dialog.setCancelable(true);
            dialog.show();
        }

        @Override
        protected String doInBackground(String... strings) {

            try {
                url = "https://www.lotusenterprises.net/manpower/Api/get_advance";
            } catch (Exception e) {
                e.printStackTrace();
            }
            Log.e("GET ADVANCE PAY .....", url);
            output = HttpHandler.makeServiceCall(url);
            //   Log.e("getcomment_url", output);
            System.out.println("getcomment_url" + output);

            return output;
        }


        @Override
        protected void onPostExecute(String s) {
            String adv="";
            super.onPostExecute(s);
            if (output == null) {
                dialog.dismiss();
            } else {
                try {
                    dialog.dismiss();
                    JSONObject obj = new JSONObject(output);
                    String responce = obj.getString("responce");
                    JSONArray Data_array = obj.getJSONArray("data");
                    for (int i = 0; i < Data_array.length(); i++) {

                        JSONObject c = Data_array.getJSONObject(i);
                        String id = c.getString("id");
                        adv = c.getString("advance");
                        advance = Double.parseDouble(adv);
                        Log.e("ORDER ADVANCE...", " --- " + id + " --- " + adv);
                    }
                    Log.e("ORDER ADVANCE... ", "---------------------" + advance + "----------------------------");
                    //advance = (advance * Double.parseDouble(db.getTotalAmount())) / 100;
                    advance = (advance * Double.parseDouble(""+db.getdaystotal())) / 100;
                    Log.e("ADVANCE VALUES ___", "___________advance % " + advance + " _________Total Amount_____ " + Double.parseDouble(""+db.getdaystotal()));
                    headadv.setText("Advance "+ "("+adv+"%)");
                    advpay.setText("₹" + advance);
                    long ftotl = (long)(dbcart.getdaystotal() - advance);
                    payable.setText("₹" + ftotl);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    ///   INSTA_MOJO PAYMENT /////////////

    private void callInstamojoPay(String email, String phone, String amount, String purpose, String buyername) {
        final Activity activity = getActivity();
//      String  Payment_method = radio_online_pay.getText().toString();

        InstamojoPay instamojoPay = new InstamojoPay();
        IntentFilter filter = new IntentFilter("ai.devsupport.instamojo");
        getActivity().registerReceiver(instamojoPay, filter);
        JSONObject pay = new JSONObject();
        try {
            pay.put("email", email);
            pay.put("phone", phone);
            pay.put("purpose", purpose);
            pay.put("amount", amount);
            pay.put("name", buyername);
            pay.put("send_sms", true);
            pay.put("send_email", true);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        initListener();
        instamojoPay.start(activity, pay, listener);
    }

    InstapayListener listener;

    private void initListener() {
        listener = new InstapayListener() {
            @Override
            public void onSuccess(String response) {
                //   pay_status = "Success";
                Toast.makeText(activity, "Payment Success", Toast.LENGTH_SHORT).show();
                Call_Order(passArray);
            }

            @Override
            public void onFailure(int i, String s) {
                        Log.e("Error "," Pay "+s+"________________"+i);
                Toast.makeText(activity, "Error payment/transaction failed...", Toast.LENGTH_SHORT).show();
            }
        };
    }

    /// eof INSTA_MOJO PAYMENT /////////////

    private void CALL_PAYUP(String order_id, int adv)
    {
        ApiService.PAY_ADV(AppPrefrences.getUserid(getActivity()), order_id,adv,"online").enqueue(new Callback<Advance_Pay>() {
            @Override
            public void onResponse(Call<Advance_Pay> call, Response<Advance_Pay> response) {
                Log.e("PAY_ADV RESPONSE.", "" + new Gson().toJson(response.body()));
                Log.e("PAY_ADV RESPONSE.", "-------------------------------------------------");
                if (response.body().getResponce()) {
                    Toast.makeText(getActivity(), "Payment code Submitted...", Toast.LENGTH_SHORT).show();
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
            public void onFailure(Call<Advance_Pay> call, Throwable t) {
                Log.e("error at adv 1 ", "" + t.getLocalizedMessage());
                Log.e("error at adv 2 ", "" + t.getMessage());
                Log.e("error at adv 3 ", "" + t.getCause());
                Log.e("error at adv 4 ", "" + t.getStackTrace());
            }
        });
    }

    private void Clear_ed()
    {
        name.getText().clear();
        mobile.getText().clear();
        mobileopt.getText().clear();
        landmark.getText().clear();
        address.getText().clear();
    }

        public boolean validCellPhone(String number) {
            return !TextUtils.isEmpty(number) && (number.length() == 10) && android.util.Patterns.PHONE.matcher(number).matches();
        }

   /* private BroadcastReceiver POST_ORDER = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            String type = intent.getStringExtra("type");

            if (type.contentEquals("1")) {
                updateData(type);
            }
        }
    };

    private void updateData(String tt)
    {
        Toast.makeText(activity, "Value Received is - "+tt, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onPause() {
        super.onPause();
        getActivity().unregisterReceiver(POST_ORDER);
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().registerReceiver(POST_ORDER,new IntentFilter("POST_ORDER"));
    }
*/
    @Override
    public void onDestroy() {
        try {
            activity.unregisterReceiver(broadcastReceiver);
            if (dialog.isShowing() && dialog != null) {
                dialog.dismiss();
            }
        }catch (Exception e)
        {

        }
        super.onDestroy();
    }
}
