package com.SCAF.CAFv2.Historial;

import com.Etiflex.Splash.GlobalPreferences;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.POST;

public class ControladorHistorial {

    private interface api_network_save_hitory{
        @FormUrlEncoded
        @POST("/insertHistorial.php")
        void setData(
                @Field("data") String data,
                Callback<Response> callback
        );
    }

    public void GuardarHistorico(String IdCedis, String IdUsuario, String Tipo, String IdReferencia){

        String data = "{\"IdCedis\":\""+IdCedis+"\"," +
                       "\"IdUsuario\":\""+IdUsuario+"\"," +
                       "\"Tipo\":\""+Tipo+"\"," +
                       "\"IdReferencia\":\""+IdReferencia+"\"}";
        new RestAdapter.Builder().setEndpoint(GlobalPreferences.URL + "/HellmannCAF/webservices/Historial").build().create(api_network_save_hitory.class).setData(data, new Callback<Response>() {
            @Override
            public void success(Response response, Response response2) {
            }

            @Override
            public void failure(RetrofitError error) {
            }
        });
    }

}
