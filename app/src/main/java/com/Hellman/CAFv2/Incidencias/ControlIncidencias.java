package com.Hellman.CAFv2.Incidencias;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.Etiflex.Splash.GlobalPreferences;
import com.Hellman.CAFv2.BuscadorEPC.Buscador;
import com.Hellman.CAFv2.Inventario.Main;
import com.bumptech.glide.Glide;
import com.github.gcacace.signaturepad.views.SignaturePad;
import com.uhf.uhf.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.Multipart;
import retrofit.http.POST;
import retrofit.http.Part;
import retrofit.mime.TypedFile;

public class ControlIncidencias extends android.app.Fragment {

    private static final String ARG_PARAM1 = "IdCAF", ARG_PARAM2 = "IdIncidencia";

    private String IdCAF, IdIncidencia;
    private ProgressDialog progressDialog;

    private ImageView img_incidencia, img_referencia;
    private TextView txt_nombre, txt_descripcion, txt_numero, txt_ubicacion, txt_encargado;
    private String local__epc = null;

    public ControlIncidencias() {}

    interface api_network_get_data {
        @FormUrlEncoded
        @POST("/getInfoIncidencia.php")
        void setData(
                @Field("IdCAF") String IdCAF,
                Callback<Response> callback
        );
    }

    interface api_network_clean_incidencia {
        @Multipart
        @POST("/cleanIncidencia.php")
        void setData(
                @Part("file") TypedFile file,
                @Part("IdCAF") String IdCAF,
                @Part("IdIncidencia") String IdIncidencia,
                @Part("Resolutor") String Resolutor,
                Callback<Response> callback
        );
    }

    public static ControlIncidencias newInstance(String idcaf, String idincidencia) {
        ControlIncidencias fragment = new ControlIncidencias();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, idcaf);
        args.putString(ARG_PARAM2, idincidencia);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            IdCAF = getArguments().getString(ARG_PARAM1);
            IdIncidencia = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_control_incidencias, container, false);
        img_incidencia = view.findViewById(R.id.img_incidencia);
        txt_nombre = view.findViewById(R.id.txt_nombre_incidencia);
        txt_descripcion = view.findViewById(R.id.txt_descripcion_incidencia);
        txt_numero = view.findViewById(R.id.txt_numero_activo_incidencia);
        img_referencia = view.findViewById(R.id.img_referencia_incidencia);
        txt_ubicacion = view.findViewById(R.id.txt_ubicacion_incidencia);
        txt_encargado = view.findViewById(R.id.txt_encargado_incidencia);
        progressDialog = new ProgressDialog(getContext());
        view.findViewById(R.id.btn_buscar_activo).setOnClickListener(v->{
            if(local__epc != null){
                GlobalPreferences.CURRENT_TAG = local__epc;
                startActivity(new Intent(getContext(), Buscador.class));
            }
        });
        view.findViewById(R.id.btn_resolver_incidencia).setOnClickListener(v->{
            final Dialog dialog = new Dialog(getContext());
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
            dialog.setContentView(R.layout.alert_insidence);
            EditText et_persona_alta = dialog.findViewById(R.id.et_persona_alta);
            dialog.findViewById(R.id.btn_volver).setOnClickListener(v2->{
                dialog.dismiss();
            });
            SignaturePad signaturePad = dialog.findViewById(R.id.signature_pad);
            dialog.findViewById(R.id.btn_limpiar_firma).setOnClickListener(v2->{
                signaturePad.clear();
            });
            dialog.findViewById(R.id.btn_continuar).setOnClickListener(v2->{
                if(!signaturePad.isEmpty()){
                    if(et_persona_alta.getText().toString().length() > 3){
                        progressDialog.setMessage("Actualizando índices...");
                        progressDialog.show();
                        try{
                            File f = new File(getContext().getCacheDir(), "tmp_bitmap");
                            f.createNewFile();
                            Bitmap bitmap = signaturePad.getSignatureBitmap();
                            ByteArrayOutputStream bos = new ByteArrayOutputStream();
                            bitmap.compress(Bitmap.CompressFormat.PNG, 0, bos);
                            byte[] bitmapdata = bos.toByteArray();
                            FileOutputStream fos = new FileOutputStream(f);
                            fos.write(bitmapdata);
                            fos.flush();
                            fos.close();

                            new RestAdapter.Builder().setEndpoint(GlobalPreferences.URL+"/HellmanCAF/webservices/Incidencias").build().create(api_network_clean_incidencia.class).setData(new TypedFile("multipart/form-data", f), IdCAF, IdIncidencia, et_persona_alta.getText().toString(), new Callback<Response>() {
                                @Override
                                public void success(Response response, Response response2) {
                                    try{
                                        if(new BufferedReader(new InputStreamReader(response.getBody().in())).readLine().equals("succes")){
                                            for(int position = 0; position < Main.main_list_insidencias.size(); position++){
                                                if(Main.main_list_insidencias.get(position).getIdCAF().equals(IdCAF)){
                                                    Main.tag_list_insidencias.remove(position);
                                                    Main.main_list_insidencias.remove(position);
                                                    GlobalPreferences.PAGE_STATE = GlobalPreferences.PAGE_STATE_INVENTORY;
                                                    //getFragmentManager().beginTransaction().remove(getFragmentManager().findFragmentByTag("ControlIncidencias")).commit();
                                                    Main.FragmentHolder.setVisibility(View.GONE);
                                                    break;
                                                }
                                            }
                                            dialog.dismiss();
                                            progressDialog.dismiss();
                                            Toast.makeText(getContext(), "Incidencia resuelta con éxito", Toast.LENGTH_SHORT).show();
                                        }else{
                                            Toast.makeText(getContext(), "Algo salió mal, intente nuevamente", Toast.LENGTH_SHORT).show();
                                            progressDialog.dismiss();
                                        }

                                    }catch (IOException e){
                                        Toast.makeText(getContext(), "Algo salió mal, intente nuevamente", Toast.LENGTH_SHORT).show();
                                        progressDialog.dismiss();
                                    }
                                }

                                @Override
                                public void failure(RetrofitError error) {
                                    progressDialog.dismiss();
                                    Log.e("ControlIncidencias", "Error->" + error.getMessage());
                                    Toast.makeText(getContext(), "No hay conexión con el servidor", Toast.LENGTH_SHORT).show();
                                }
                            });

                        }catch (IOException e){
                            Toast.makeText(getContext(), "Por favor, revise los permisos de almacenamiento", Toast.LENGTH_SHORT).show();
                        }
                    }else{
                        Toast.makeText(getContext(), "Por favor, ingrese un nombre válido", Toast.LENGTH_SHORT).show();
                    }
                }else{
                    Toast.makeText(getContext(), "Por favor, ingrese una firma válida", Toast.LENGTH_SHORT).show();
                }
            });
            dialog.show();
        });
        view.findViewById(R.id.btn_ignorar_incidencia).setOnClickListener(v->{
            for(int position = 0; position < Main.main_list_insidencias.size(); position++){
                if(Main.main_list_insidencias.get(position).getIdCAF().equals(IdCAF)){
                    Main.tag_list_insidencias.remove(position);
                    Main.main_list_insidencias.remove(position);
                    GlobalPreferences.PAGE_STATE = GlobalPreferences.PAGE_STATE_INVENTORY;
                    //getFragmentManager().beginTransaction().remove(getFragmentManager().findFragmentByTag("ControlIncidencias")).commit();
                    Main.FragmentHolder.setVisibility(View.GONE);
                    break;
                }
            }
        });
        getData();
        return view;
    }

    private void getData(){
        new RestAdapter.Builder().setEndpoint(GlobalPreferences.URL+"/HellmanCAF/webservices/Inventario").build().create(api_network_get_data.class).setData(IdCAF, new Callback<Response>() {
            @Override
            public void success(Response response, Response response2) {
                try{
                    JSONObject json = new JSONObject(new BufferedReader(new InputStreamReader(response.getBody().in())).readLine());
                    local__epc = json.getString("EPC");
                    Glide.with(getContext()).load(GlobalPreferences.URL+"/HellmanCAF/assets/Activo/"+json.getString("Numero")).override(240).into(img_incidencia);
                    txt_nombre.setText(json.getString("Nombre"));
                    txt_descripcion.setText(json.getString("Descripcion"));
                    txt_numero.setText("Número de activo " + json.getString("Numero"));
                    Glide.with(getContext()).load(GlobalPreferences.URL+"/HellmanCAF/assets/Activo/"+json.getString("Numero")+"_ref").override(480).into(img_referencia);
                    txt_ubicacion.setText("Ubicación: " + json.getString("Area") + ", " + json.getString("Oficina"));
                    txt_encargado.setText("Persona Asignada: " + json.getString("PAsignada"));
                }catch (IOException e){

                }catch (JSONException e){

                }
            }

            @Override
            public void failure(RetrofitError error) {

            }
        });
    }

}