package com.Hellman.CAFv2;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.ListPopupWindow;
import androidx.appcompat.widget.PopupMenu;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.Etiflex.Splash.Inventario.rv_adapter;
import com.Etiflex.Splash.ROC.ModelInventory;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.google.zxing.Result;
import com.uhf.uhf.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

import static com.Etiflex.Splash.GlobalPreferences.URL;
import static com.Etiflex.Splash.GlobalPreferences.main_list;
import static com.Etiflex.Splash.GlobalPreferences.tag_list;

public class CAF_AltaActivo extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    public CAF_AltaActivo() {}

    public static CAF_AltaActivo newInstance(String param1, String param2) {
        CAF_AltaActivo fragment = new CAF_AltaActivo();
        /*Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);*/
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }*/
    }
    private RelativeLayout CameraHolder;
    private ConstraintLayout PanelBuscador;
    private EditText et_buscador_activo, et_numero_activo, et_nombre_activo, et_persona_asignada, et_tipo_etiqueta, et_descripcion;
    private TextView btn_encontrar_activo_numero, btn_encontrar_activo_nombre, spinner_cedis, spinner_departamento, spinner_oficina, btn_add_photo_1, btn_add_photo_2;
    private ImageButton btn_camara_por_numero;
    private RecyclerView rv_buscador;
    private ArrayList<SearchModel> main_list_buscador;
    private boolean FIND_BY_CODE = false, SearchingIsAboutToStart = false;


    private String search_key;
    final Handler handler = new Handler();
    final Runnable runnable = () -> {
        searchItem(search_key);
        SearchingIsAboutToStart = false;
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_c_a_f__alta_activo, container, false);
        CameraHolder = view.findViewById(R.id.camera_holder);
        PanelBuscador = view.findViewById(R.id.Panel_buscador);
        et_buscador_activo = view.findViewById(R.id.et_buscador_activo);
        rv_buscador = view.findViewById(R.id.rv_buscador);
        et_buscador_activo = view.findViewById(R.id.et_buscador_activo);
        btn_encontrar_activo_numero = view.findViewById(R.id.txt_encontrar_activo_numero);
        btn_camara_por_numero = view.findViewById(R.id.btn_camara_buscador_numero);
        et_numero_activo  = view.findViewById(R.id.et_numero_activo);
        et_nombre_activo = view.findViewById(R.id.et_nombre_activo);
        et_persona_asignada = view.findViewById(R.id.et_persona_asignada);
        et_tipo_etiqueta = view.findViewById(R.id.et_tipo_etiqueta);
        et_descripcion = view.findViewById(R.id.et_descripcion);
        spinner_cedis = view.findViewById(R.id.spinner_Cedis);
        spinner_departamento = view.findViewById(R.id.spinner_Departamento);
        spinner_oficina = view.findViewById(R.id.spinner_Oficina);
        btn_add_photo_1 = view.findViewById(R.id.btn_add_photo_1);
        btn_add_photo_2 = view.findViewById(R.id.btn_add_photo_2);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        rv_buscador.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false));
        btn_encontrar_activo_numero.setOnClickListener(v->{
            PanelBuscador.setVisibility(View.VISIBLE);
        });
        btn_camara_por_numero.setOnClickListener(v->{
            HandleQRCodeReader(et_numero_activo, et_nombre_activo);
        });
        et_buscador_activo.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(SearchingIsAboutToStart){
                    handler.removeCallbacks(runnable);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                SearchingIsAboutToStart = true;
                search_key = s.toString();
                handler.postDelayed(runnable, 1000);
            }
        });
        spinner_cedis.setOnClickListener(v->{
            String[] products = {"Camera", "Laptop", "Watch", "Smartphone",
                    "Television", "Car", "Motor", "Shoes", "Clothes"};
            ListPopupWindow listPopupWindow = new ListPopupWindow(getContext());
            listPopupWindow.setAnchorView(v);
            listPopupWindow.setHeight(ListPopupWindow.WRAP_CONTENT);
            listPopupWindow.setWidth(spinner_cedis.getWidth());
            listPopupWindow.setAdapter(new ArrayAdapter(getContext(), R.layout.pop_up_cedis, products)); // list_item is your textView with gravity.
            
            listPopupWindow.show();
        });
        spinner_departamento.setOnClickListener(v->{});
        spinner_oficina.setOnClickListener(v->{});
        btn_add_photo_1.setOnClickListener(v->{});
        btn_add_photo_2.setOnClickListener(v->{});
    }

    private void HandleQRCodeReader(EditText et_numero_activo, EditText et_nombre_activo) {
        ZXingScannerView scanner = new ZXingScannerView(getContext());
        scanner.setResultHandler(result -> {
            scanner.stopCamera();
            CameraHolder.removeAllViews();
            CameraHolder.setVisibility(View.GONE);
            String active_code = result.getText().toLowerCase();
            et_buscador_activo.setText(active_code);
            FIND_BY_CODE = true;
            searchItem(active_code);
        });
        CameraHolder.addView(scanner);
        scanner.startCamera();
        CameraHolder.setVisibility(View.VISIBLE);
    }

    private void searchItem(String key) {
        main_list_buscador = new ArrayList<>();
        Volley.newRequestQueue(getContext()).add(new JsonObjectRequest(Request.Method.GET, URL+"AltaActivo/getSearch.php?key=" + key, null, response -> {
            JSONArray json= response.optJSONArray("Data");
            try {

                if(!FIND_BY_CODE){
                    for(int i=0; i<json.length();i++){
                        SearchModel model = new SearchModel();
                        JSONObject jsonObject = json.getJSONObject(i);
                        model.setId(jsonObject.optString("Id"));
                        model.setNumero(jsonObject.optString("Numero"));
                        model.setNombre(jsonObject.optString("Nombre"));
                        model.setDescripcion(jsonObject.optString("Descripcion"));

                        main_list_buscador.add(model);

                    }

                    if(main_list_buscador.size() > 0){
                        rv_buscador.setAdapter(new search_adapter(main_list_buscador));
                    }
                }else{
                    SearchModel model = new SearchModel();
                    JSONObject jsonObject = json.getJSONObject(0);
                    model.setId(jsonObject.optString("Id"));
                    model.setNumero(jsonObject.optString("Numero"));
                    model.setNombre(jsonObject.optString("Nombre"));
                    model.setDescripcion(jsonObject.optString("Descripcion"));
                    setUpResult(model);
                }

            } catch (JSONException | NullPointerException e) {
                Toast.makeText(getContext(), "Error de código, contacte a un desarrollador", Toast.LENGTH_SHORT).show();
            }

        },error -> {
            Toast.makeText(getContext(), "Error de conexión", Toast.LENGTH_SHORT).show();
        }));
    }

    private class SearchModel{
        private String Id;
        private String Numero;
        private String Nombre;
        private String Descripcion;

        public String getId() {
            return Id;
        }

        public void setId(String id) {
            Id = id;
        }

        public String getNumero() {
            return Numero;
        }

        public void setNumero(String numero) {
            Numero = numero;
        }

        public String getNombre() {
            return Nombre;
        }

        public void setNombre(String nombre) {
            Nombre = nombre;
        }

        public String getDescripcion() {
            return Descripcion;
        }

        public void setDescripcion(String descripcion) {
            Descripcion = descripcion;
        }
    }

    public class search_adapter extends RecyclerView.Adapter<search_adapter.ViewHolder> implements View.OnClickListener{

        public search_adapter(ArrayList<SearchModel> lista) {
            this.child_list = lista;
        }

        ArrayList<SearchModel> child_list;
        Context child_context;
        private View.OnClickListener listener;


        @Override
        public search_adapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_list_buscador,parent,false);
            view.setOnClickListener(this);
            child_context = parent.getContext();
            return new search_adapter.ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(search_adapter.ViewHolder holder, int position) {

            Glide.with(child_context).load("https://rfidmx.com/HellmanCAF/assets/Activo/"+child_list.get(position).getNumero()).override(160).into(holder.img_activo);

            holder.nombre.setText(child_list.get(position).getNombre());
            holder.descripcion.setText(child_list.get(position).getDescripcion());
            holder.numero.setText("Número de activo: " + child_list.get(position).getNumero());

        }

        @Override
        public int getItemCount() {
            return child_list.size();
        }

        @Override
        public void onClick(View view) {

        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            ImageView img_activo;
            TextView numero, nombre, descripcion;
            public ViewHolder(View itemView) {
                super(itemView);
                img_activo = itemView.findViewById(R.id.img_activo);
                numero = itemView.findViewById(R.id.numero_activo);
                nombre = itemView.findViewById(R.id.nombre_activo);
                descripcion = itemView.findViewById(R.id.descripcion_activo);
            }
        }
    }

    private void setUpResult(SearchModel result){
        et_numero_activo.setText(result.getNumero());
        et_nombre_activo.setText(result.getNombre());
        PanelBuscador.setVisibility(View.GONE);
    }

}