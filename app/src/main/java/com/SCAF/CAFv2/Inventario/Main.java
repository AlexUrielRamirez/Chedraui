package com.SCAF.CAFv2.Inventario;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.Etiflex.Splash.GlobalPreferences;
import com.Etiflex.Splash.Methods;
import com.SCAF.CAFv2.Administracion.Incidences.ManageIncidence;
import com.SCAF.CAFv2.Incidencias.ControlIncidencias;
import com.SCAF.SQLiteTools.SQLiteHelper;
import com.airbnb.lottie.LottieAnimationView;
import com.bumptech.glide.Glide;
import com.github.gcacace.signaturepad.views.SignaturePad;
import com.module.interaction.ModuleConnector;
import com.nativec.tools.ModuleManager;
import com.rfid.RFIDReaderHelper;
import com.rfid.ReaderConnector;
import com.rfid.rxobserver.RXObserver;
import com.rfid.rxobserver.bean.RXInventoryTag;
import com.uhf.uhf.R;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import static com.Etiflex.Splash.GlobalPreferences.DEVELOP_MODE;
import static com.Etiflex.Splash.GlobalPreferences.PAGE_STATE_INVENTORY;
import static com.Etiflex.Splash.GlobalPreferences.PAGE_STATE_SETTING_UBICATION;
import static com.Etiflex.Splash.GlobalPreferences.current_inventory_item;
import static com.Etiflex.Splash.GlobalPreferences.db_manager;

public class Main extends AppCompatActivity {

    public static int ParentHolderId = R.id.MainHolder;
    RXObserver rx = null;
    ToneGenerator mToneGenerator;

    public static ProgressBar mProgress;
    private ModelInventario model;
    public static ArrayList<ModelInventario> main_list;
    public static ArrayList<String> tag_list;
    public static TextView txt_contador, txt_cedis_actual;
    public static RecyclerView rv_content;
    public static rv_adapter adapter;
    public static int conter = 0;
    public static ConstraintLayout PanelUbicacion, PanelDescarga;
    public String IdIncidencia = null, IdCAF_for_incidence = null;
    public boolean IS_FULL = true;

    //Antenna
    String PORT = "dev/ttyS4";
    RFIDReaderHelper mReader;

    //Ubicación
    private TextView Spinner_Departamento, Spinner_Oficina, txt_download_progress;
    private PopupMenu menu_area, menu_oficinas;
    private ArrayList<ModelUbicaciones> main_list_areas, main_list_oficinas;
    private String IdArea = "0", IdOficina = "0";
    private ProgressBar pb_loading_ofices;

    //Incidencias
    public static ArrayList<com.SCAF.CAFv2.Administracion.Incidences.Main.model_incidencia> main_list_insidencias;
    public static ArrayList<String> tag_list_insidencias;
    public static boolean AreIncidences = false;

    //Detalle
    public static ConstraintLayout PanelDetalle;
    public static ImageView ImgDetalle;
    public static TextView NombreDetalle, DescripcionDetalle, AlmacenDetalle, CategoriaDetalle;
    public static Button btn_buscar, btn_insidencia;

    //Fragments
    public static RelativeLayout FragmentHolder;

    //Alerta
    public static LinearLayout AlertLayout;
    SQLiteDatabase db;

    //Error o vacío
    private ConstraintLayout mPanelEmpty;
    private LottieAnimationView mLottieEmpty;

    private com.SCAF.CAFv2.Administracion.Incidences.Main.model_incidencia current_local_incidencia_model = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new Methods().CambiarColorStatusBar(this, R.color.blue_selected);
        setContentView(R.layout.activity_main_inventario);
        GlobalPreferences.PAGE_STATE = PAGE_STATE_SETTING_UBICATION;
        AreIncidences = false;
        mToneGenerator = new ToneGenerator(AudioManager.STREAM_ALARM, 100);
        initViews();
        //getAreas();
        this.setTitle(getString(R.string.Inventario));
        PanelDescarga.setVisibility(View.VISIBLE);
        new Handler().postDelayed(() -> DescargarInformacion(), 1500);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == 134){
            if(GlobalPreferences.PAGE_STATE == PAGE_STATE_INVENTORY){
                mReader.realTimeInventory((byte) 0xff, (byte) 0x01);
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    private void connectToAntenna(){
        ModuleConnector connector = new ReaderConnector();
        if (connector.connectCom(PORT, 115200)) {
            ModuleManager.newInstance().setUHFStatus(true);
            try {
                mReader = RFIDReaderHelper.getDefaultHelper();
                mReader.registerObserver(rx);
            } catch (Exception e) {
                Log.e("Main", "error connecting to antenna->"+e.getMessage());
                e.printStackTrace();
            }
        }else{
            Log.e("Main", "error connecting to antenna");
        }
    }

    private void initViews() {
        FragmentHolder = findViewById(R.id.fragment_holder);
        AlertLayout = findViewById(R.id.alerta_incidencia);
        AlertLayout.setOnClickListener(v->{
            if(current_local_incidencia_model != null){
                GlobalPreferences.current_incidencia_model = current_local_incidencia_model;
                startActivityForResult(new Intent(Main.this, ManageIncidence.class), 0);
            }
        });
        PanelUbicacion = findViewById(R.id.Panel_ubicacion);
        pb_loading_ofices = findViewById(R.id.pb_loading_ofices);
        PanelDescarga = findViewById(R.id.Panel_descarga);
        txt_download_progress = findViewById(R.id.txt_progress);
        Spinner_Departamento = findViewById(R.id.spinner_Departamento);
        txt_cedis_actual = findViewById(R.id.txt_cedis_actual);
        txt_cedis_actual.setText("Cedis actual: " + GlobalPreferences.NOMBRE_CEDIS);
        Spinner_Oficina = findViewById(R.id.spinner_Oficina);
        Spinner_Departamento.setOnClickListener(v->{
            menu_area.show();
        });
        Spinner_Oficina.setOnClickListener(v->{
            if(IdArea.equals("0"))
                Toast.makeText(this, "Primero seleccione un departamento", Toast.LENGTH_SHORT).show();
            else
                menu_oficinas.show();
        });
        findViewById(R.id.btn_continuar).setOnClickListener(v->{
            if(!IdArea.equals("0") && !IdOficina.equals("0")){
                PanelDescarga.setVisibility(View.VISIBLE);
                new Handler().postDelayed(() -> DescargarInformacion(), 1500);
            }else{
                Toast.makeText(this, "Por favor, elija una ubicación válida", Toast.LENGTH_SHORT).show();
            }
        });
        mProgress = findViewById(R.id.ProgressCount);
        txt_contador = findViewById(R.id.txt_contador);
        rv_content = findViewById(R.id.rv_content);
        rv_content.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
        rv_content.getItemAnimator().setChangeDuration(0);
        PanelDetalle = findViewById(R.id.PanelDetalle);
        ImgDetalle = findViewById(R.id.img_detalle);
        NombreDetalle = findViewById(R.id.nombre_detalle);
        DescripcionDetalle = findViewById(R.id.descripcion_detalle);
        AlmacenDetalle = findViewById(R.id.almacen_detalle);
        CategoriaDetalle = findViewById(R.id.categoria_detalle);
        btn_buscar = findViewById(R.id.btn_buscar);
        btn_insidencia = findViewById(R.id.btn_insidencia);
        mPanelEmpty = findViewById(R.id.panel_empty);
        mLottieEmpty = findViewById(R.id.empty_lottie);
        findViewById(R.id.btn_onbackpressed).setOnClickListener(v-> mOnBackPressed());
        findViewById(R.id.btn_volver_empty).setOnClickListener(v-> mOnBackPressed());
    }

    @SuppressLint("SetTextI18n")
    private void DescargarInformacion() {

        conter = 0;
        txt_download_progress.setText("Consiguiendo recursos de lectura...");

        db_manager = new SQLiteHelper(this);
        db = db_manager.getWritableDatabase();

        Cursor c = db.rawQuery("SELECT " +
                "A.IdActivo," +
                "A.NoInvent," +
                "A.Sociedad," +
                "A.ActivoFijo," +
                "A.SN," +
                "A.FechaCapitalizacion," +
                "A.DenominacionDelActivoFijo," +
                "A.ValorAdquisicion," +
                "A.ValorLibros," +
                "A.SupraAF," +
                "A.NoSerie," +
                "A.NotaInventario," +
                "A.Emplazamiento," +
                "A.Local," +
                "A.ClaseAF," +
                "A.CentroCoste,"+
                "A.Status " +
                "FROM tb_activo A " +
                "WHERE A.Status != 2", null);

        if (c != null) {
            try {
                main_list = new ArrayList<>();
                tag_list = new ArrayList<>();
                c.moveToFirst();
                do {

                    model = new ModelInventario();
                    model.setId(c.getString(c.getColumnIndex("IdActivo")));
                    model.setEPC(c.getString(c.getColumnIndex("NoInvent")));
                    model.setNumero(c.getString(c.getColumnIndex("ActivoFijo")));
                    model.setNombre(c.getString(c.getColumnIndex("DenominacionDelActivoFijo")));
                    model.setDescripcion(c.getString(c.getColumnIndex("DenominacionDelActivoFijo")));
                    model.setSociedad(c.getString(c.getColumnIndex("Sociedad")));
                    model.setActivoFijo(c.getString(c.getColumnIndex("ActivoFijo")));
                    model.setSN(c.getString(c.getColumnIndex("SN")));
                    model.setFechaCapitalizacion(c.getString(c.getColumnIndex("FechaCapitalizacion")));
                    model.setDenominacionDelActivoFijo(c.getString(c.getColumnIndex("DenominacionDelActivoFijo")));
                    model.setValorAdquisicion(c.getString(c.getColumnIndex("ValorAdquisicion")));
                    model.setValorLibros(c.getString(c.getColumnIndex("ValorLibros")));
                    model.setSupraAF(c.getString(c.getColumnIndex("SupraAF")));
                    model.setNoSerie (c.getString(c.getColumnIndex("NoSerie")));
                    model.setNoInvent(c.getString(c.getColumnIndex("NoInvent")));
                    model.setNotaInventario(c.getString(c.getColumnIndex("NotaInventario")));
                    model.setEmplazamiento(c.getString(c.getColumnIndex("Emplazamiento")));
                    model.setLocal(c.getString(c.getColumnIndex("Local")));
                    model.setClaseAF(c.getString(c.getColumnIndex("ClaseAF")));
                    model.setCentroCoste(c.getString(c.getColumnIndex("CentroCoste")));

                    model.setStatus(c.getInt(c.getColumnIndex("Status")));

                    if(model.getStatus() != 1){
                        IS_FULL = false;
                    }

                    if (model.getStatus() == 1) {
                        conter = conter + 1;
                    }

                    main_list.add(model);
                    tag_list.add(model.getEPC().replaceAll(" ", ""));

                } while (c.moveToNext());

                mProgress.setMax(main_list.size());
                mProgress.setProgress(conter);
                txt_contador.setText(conter + "/" + main_list.size());
                adapter = new rv_adapter();
                rv_content.setAdapter(adapter);
                GlobalPreferences.PAGE_STATE = GlobalPreferences.PAGE_STATE_INVENTORY;
                findViewById(R.id.btn_terminar).setOnClickListener(v -> {
                    //if (conter >= main_list.size()) {
                    GlobalPreferences.PAGE_STATE = GlobalPreferences.PAGE_STATE_PROCESING;
                    ProgressDialog pd = new ProgressDialog(Main.this);
                    pd.setMessage("Procesando, por favor espere...");
                    pd.setCancelable(false);
                    pd.show();
                    StringBuilder ids = new StringBuilder();
                    for (int i = 0; i < main_list.size(); i++) {
                        if (main_list.get(i).getStatus() == 1) {
                            ids.append(main_list.get(i).getId() + ",");
                        }
                    }
                    String id_list = ids.toString();
                    id_list = id_list.substring(0, id_list.length() - 1);

                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
                    String currentDateandTime = sdf.format(new Date());

                    db.execSQL("UPDATE tb_activo "+
                            "SET Status = 1, FechaLectura = '"+currentDateandTime+"' "+
                            "WHERE IdActivo IN ("+id_list+")");

                    GlobalPreferences.PAGE_STATE = PAGE_STATE_SETTING_UBICATION;
                    PanelDetalle.setVisibility(View.GONE);
                    Spinner_Departamento.setText("Departamento");
                    Spinner_Oficina.setText("Oficina");
                    PanelUbicacion.setVisibility(View.VISIBLE);
                    Toast.makeText(Main.this, "¡Proceso finalizado con exito!", Toast.LENGTH_SHORT).show();
                    pd.dismiss();
                    Main.this.finish();
                /*} else {
                    final Dialog dialog = new Dialog(Main.this);
                    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
                    dialog.setContentView(R.layout.error_validating);
                    dialog.findViewById(R.id.btn_volver).setOnClickListener(v2 -> {
                        dialog.dismiss();
                    });
                    dialog.show();
                }*/
                });
                new Handler().postDelayed(() -> DownloadIncidences(), 1500);
            }catch (CursorIndexOutOfBoundsException e){
                mLottieEmpty.playAnimation();
                mPanelEmpty.setVisibility(View.VISIBLE);
            }

        }else{
            mLottieEmpty.playAnimation();
            mPanelEmpty.setVisibility(View.VISIBLE);
        }
    }

    private void DownloadIncidences() {
        txt_download_progress.setText("Consiguiendo registros de incidencias...");
        main_list_insidencias = new ArrayList<>();
        tag_list_insidencias = new ArrayList<>();

        Cursor c = db.rawQuery("SELECT " +
                "A.IdIncidencia," +
                "A.IdActivo," +
                "A.NombreAlta," +
                "A.FechaAlta," +
                "A.NombreBaja," +
                "A.FechaBaja," +
                "A.Motivo," +
                "A.Comentario," +
                "B.NoInvent," +
                "B.ActivoFijo," +
                "B.DenominacionDelActivoFijo,"+
                "A.Status,"+
                "B.NoInvent "+
                "FROM tb_incidences A " +
                "INNER JOIN tb_activo B ON B.IdActivo = A.IdActivo " +
                "WHERE A.Status = 2", null);

        if (c != null) {
            try{
                c.moveToFirst();
                do {
                    com.SCAF.CAFv2.Administracion.Incidences.Main.model_incidencia model = new com.SCAF.CAFv2.Administracion.Incidences.Main.model_incidencia();

                    model.setIdIncidencia(c.getString(c.getColumnIndex("IdIncidencia")));
                    model.setIdActivo(c.getString(c.getColumnIndex("IdActivo")));
                    model.setNombreActivo(c.getString(c.getColumnIndex("DenominacionDelActivoFijo")));
                    model.setNombreAlta(c.getString(c.getColumnIndex("NombreAlta")));
                    model.setFechaAlta(c.getString(c.getColumnIndex("FechaAlta")));
                    model.setMotivo(c.getString(c.getColumnIndex("Motivo")));
                    model.setComentario(c.getString(c.getColumnIndex("Comentario")));
                    model.setNombreBaja(c.getString(c.getColumnIndex("NombreBaja")));
                    model.setFechaBaja(c.getString(c.getColumnIndex("FechaBaja")));
                    model.setEPC(c.getString(c.getColumnIndex("NoInvent")));
                    model.setActivoFijo(c.getString(c.getColumnIndex("ActivoFijo")));
                    model.setDenominacionDelActivoFijo(c.getString(c.getColumnIndex("DenominacionDelActivoFijo")));
                    model.setStatusIncidencia(c.getInt(c.getColumnIndex("Status")));

                    main_list_insidencias.add(model);
                    tag_list_insidencias.add(model.getEPC().replaceAll(" ", ""));
                } while (c.moveToNext());
                AreIncidences = true;
                setUpData();
            }catch (CursorIndexOutOfBoundsException e){
                AreIncidences = false;
                setUpData();
            }
        }else{
            AreIncidences = false;
            setUpData();
        }
    }

    private void setUpReader() {
        rx = new RXObserver(){
            @Override
            protected void onInventoryTag(RXInventoryTag tag) {
                super.onInventoryTag(tag);

                String EPC = tag.strEPC.replaceAll(" ", "").substring(0,24);
                try {
                    switch (GlobalPreferences.PAGE_STATE){
                            case GlobalPreferences.PAGE_STATE_INVENTORY:
                                if(AreIncidences){
                                    if(tag_list_insidencias.contains(EPC)){
                                        new Thread(()->{
                                            if(AlertLayout.getVisibility() != View.VISIBLE){
                                                for(int position = 0; position < tag_list_insidencias.size(); position++){
                                                    if(main_list_insidencias.get(position).getEPC().equals(EPC)){
                                                        if(current_local_incidencia_model == null){
                                                            current_local_incidencia_model = main_list_insidencias.get(position);
                                                        }
                                                    }
                                                }
                                                Animation animation = AnimationUtils.loadAnimation(Main.this, R.anim.right_to_left_in);
                                                AlertLayout.setAnimation(animation);
                                                Main.this.runOnUiThread(() -> {
                                                    AlertLayout.setVisibility(View.VISIBLE);
                                                    animation.start();
                                                });
                                            }
                                        }).run();
                                    }
                                }
                                if(tag_list.contains(EPC)){
                                    new Thread(() -> {
                                        for(int position = 0; position < tag_list.size(); position++){
                                            final int final_position = position;
                                            if(main_list.get(position).getEPC().equals(EPC) && main_list.get(position).getStatus() == 0){
                                                main_list.get(position).setStatus(1);
                                                Main.this.runOnUiThread(() -> {
                                                    //adapter.notifyItemChanged(final_position, main_list.get(final_position));
                                                    adapter.notifyDataSetChanged();
                                                    conter = conter + 1;
                                                    mProgress.setProgress(conter);
                                                    txt_contador.setText(conter+"/"+ main_list.size());
                                                });
                                                break;
                                            }
                                        }
                                    }).run();
                                }
                                break;
                        }

                }catch (NullPointerException e){
                    Log.e("Hellmann", "Lecturas"+e.getMessage());
                }
            }
        };
        connectToAntenna();
    }

    private void setUpData(){
        if(!DEVELOP_MODE){
            setUpReader();
        }
        new Handler().postDelayed(() -> {
            txt_download_progress.setText("Cargando...");
            PanelUbicacion.setVisibility(View.GONE);
            PanelDescarga.setVisibility(View.GONE);
            if(IS_FULL || conter == main_list.size()){
                txt_contador.setText( main_list.size() + "/" + main_list.size());
                mProgress.setProgress(main_list.size());
                final Dialog dialog = new Dialog(this);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
                dialog.setContentView(R.layout.dialog_full_inventory);
                dialog.findViewById(R.id.btn_volver).setOnClickListener(v->{
                    dialog.dismiss();
                    Main.this.onBackPressed();
                });
                dialog.findViewById(R.id.btn_continuar).setOnClickListener(v->{
                    dialog.dismiss();
                });
                dialog.show();
            }
        }, 1500);
    }

    public class rv_adapter extends RecyclerView.Adapter<rv_adapter.ViewHolder> implements View.OnClickListener{
        private View.OnClickListener listener;
        Context context;
        @Override
        public rv_adapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_tag,parent,false);
            view.setOnClickListener(this);
            context = parent.getContext();
            return new rv_adapter.ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(rv_adapter.ViewHolder holder, int position) {
            holder.item_holder.setOnClickListener(v->{
                GlobalPreferences.current_inventory_item = main_list.get(position);
                startActivityForResult(new Intent(Main.this, Details.class), 0);
                /*GlobalPreferences.PAGE_STATE = GlobalPreferences.PAGE_STATE_DETAILS;
                GlobalPreferences.CURRENT_TAG = main_list.get(position).getEPC();
                setUpDetailData(main_list.get(position));
                PanelDetalle.setVisibility(View.VISIBLE);
                if(main_list.get(position).getStatus() == 2){
                    btn_insidencia.setEnabled(false);
                    btn_insidencia.setBackgroundColor(context.getColor(R.color.gray_light));
                }else{
                    btn_insidencia.setEnabled(true);
                    //btn_insidencia.setBackgroundColor(context.getColor(R.color.menu_orange));
                }
                btn_buscar.setOnClickListener(v_1->{
                    GlobalPreferences.CURRENT_TAG = main_list.get(position).getEPC();
                    startActivity(new Intent(Main.this, com.SCAF.CAFv2.BuscadorEPC.Buscador.class));
                });
                btn_insidencia.setOnClickListener(v1->{

                });*/
            });
            holder.nombre.setText(main_list.get(position).getNombre());
            holder.descripcion.setText(main_list.get(position).getDescripcion());
            holder.almacen.setText(main_list.get(position).getDescripcion());
            holder.categoria.setText("Número: "+main_list.get(position).getNumero());

            if(main_list.get(position).getStatus() == 1){
                holder.status.setText("ENCONTRADO");
                holder.tag_status.setCardBackgroundColor(context.getColor(R.color.green_2));
            }else {
                holder.status.setText("NO ENCONTRADO");
                holder.tag_status.setCardBackgroundColor(context.getColor(R.color.menu_orange));
            }
        }

        private void setUpDetailData(ModelInventario model) {
            NombreDetalle.setText("Nombre: "+model.getNombre());
            DescripcionDetalle.setText("Denominación: "+model.getDescripcion());
            //AlmacenDetalle.setText(": "+model.getDescripcion());
            CategoriaDetalle.setText("Número de activo: "+model.getNumero());
        }


        @Override
        public int getItemCount() {
            return main_list.size();
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public int getItemViewType(int position) {
            return position;
        }

        @Override
        public void onClick(View view) {

        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            ConstraintLayout item_holder;
            CardView tag_status;
            TextView nombre, descripcion, almacen, categoria, status;
            public ViewHolder(View itemView) {
                super(itemView);
                item_holder = itemView.findViewById(R.id.item_holder);
                tag_status = itemView.findViewById(R.id.tag_status);
                nombre = itemView.findViewById(R.id.nombre);
                descripcion = itemView.findViewById(R.id.descripcion);
                almacen = itemView.findViewById(R.id.almacen);
                categoria = itemView.findViewById(R.id.Categoria);
                status = itemView.findViewById(R.id.status);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK){
            switch (requestCode){
                case 0:
                    if(data.getDataString().equals("incidence-ok")){
                        for(int position = 0; position < main_list.size(); position++){
                            if(main_list.get(position).getId().equals(current_inventory_item.getId())){
                                main_list.remove(position);
                                adapter.notifyDataSetChanged();
                                mProgress.setMax(main_list.size());
                                //if(conter < main_list.size()){
                                //    conter = conter + 1;
                                //}else if(conter > main_list.size()){
                                //    conter = conter - 1;
                                //}
                                mProgress.setProgress(conter);
                                txt_contador.setText(conter+"/"+ main_list.size());
                                Toast.makeText(Main.this, "Incidencia generada correctamente", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                    break;
            }
        }
    }

    public static class ModelInventario{
        private String Id;
        private String EPC;
        private String Numero;
        private String Nombre;
        private String Descripcion;

        private String Sociedad;
        private String ActivoFijo;
        private String SN;
        private String FechaCapitalizacion;
        private String DenominacionDelActivoFijo;
        private String ValorAdquisicion;
        private String ValorLibros;
        private String SupraAF;
        private String NoSerie;
        private String NoInvent;
        private String NotaInventario;
        private String Emplazamiento;
        private String Local;
        private String ClaseAF;
        private String CentroCoste;
        private int Status;

        public String getId() {
            return Id;
        }

        public void setId(String id) {
            Id = id;
        }

        public String getEPC() {
            return EPC;
        }

        public void setEPC(String EPC) {
            this.EPC = EPC;
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

        public String getSociedad() {
            return Sociedad;
        }

        public void setSociedad(String sociedad) {
            Sociedad = sociedad;
        }

        public String getActivoFijo() {
            return ActivoFijo;
        }

        public void setActivoFijo(String activoFijo) {
            ActivoFijo = activoFijo;
        }

        public String getSN() {
            return SN;
        }

        public void setSN(String SN) {
            this.SN = SN;
        }

        public String getFechaCapitalizacion() {
            return FechaCapitalizacion;
        }

        public void setFechaCapitalizacion(String fechaCapitalizacion) {
            FechaCapitalizacion = fechaCapitalizacion;
        }

        public String getDenominacionDelActivoFijo() {
            return DenominacionDelActivoFijo;
        }

        public void setDenominacionDelActivoFijo(String denominacionDelActivoFijo) {
            DenominacionDelActivoFijo = denominacionDelActivoFijo;
        }

        public String getValorAdquisicion() {
            return ValorAdquisicion;
        }

        public void setValorAdquisicion(String valorAdquisicion) {
            ValorAdquisicion = valorAdquisicion;
        }

        public String getValorLibros() {
            return ValorLibros;
        }

        public void setValorLibros(String valorLibros) {
            ValorLibros = valorLibros;
        }

        public String getSupraAF() {
            return SupraAF;
        }

        public void setSupraAF(String supraAF) {
            SupraAF = supraAF;
        }

        public String getNoSerie() {
            return NoSerie;
        }

        public void setNoSerie(String noSerie) {
            NoSerie = noSerie;
        }

        public String getNoInvent() {
            return NoInvent;
        }

        public void setNoInvent(String noInvent) {
            NoInvent = noInvent;
        }

        public String getNotaInventario() {
            return NotaInventario;
        }

        public void setNotaInventario(String notaInventario) {
            NotaInventario = notaInventario;
        }

        public String getEmplazamiento() {
            return Emplazamiento;
        }

        public void setEmplazamiento(String emplazamiento) {
            Emplazamiento = emplazamiento;
        }

        public String getLocal() {
            return Local;
        }

        public void setLocal(String local) {
            Local = local;
        }

        public String getClaseAF() {
            return ClaseAF;
        }

        public void setClaseAF(String claseAF) {
            ClaseAF = claseAF;
        }

        public String getCentroCoste() {
            return CentroCoste;
        }

        public void setCentroCoste(String centroCoste) {
            CentroCoste = centroCoste;
        }

        public int getStatus() {
            return Status;
        }

        public void setStatus(int status) {
            Status = status;
        }
    }

    public class ModelUbicaciones{
        private String Id;
        private String Nombre;

        public String getId() {
            return Id;
        }

        public void setId(String id) {
            Id = id;
        }

        public String getNombre() {
            return Nombre;
        }

        public void setNombre(String nombre) {
            Nombre = nombre;
        }
    }

    @Override
    public void onBackPressed() {
        mOnBackPressed();
    }

    private void mOnBackPressed(){
        switch (GlobalPreferences.PAGE_STATE){
            case GlobalPreferences.PAGE_STATE_PROCESING:
                Toast.makeText(this, "Por favor, espere...", Toast.LENGTH_SHORT).show();
                break;
            case PAGE_STATE_SETTING_UBICATION:
                this.finish();
                break;
            case PAGE_STATE_INVENTORY:
                this.finish();
                break;
            case GlobalPreferences.PAGE_STATE_DETAILS:
                GlobalPreferences.PAGE_STATE = GlobalPreferences.PAGE_STATE_INVENTORY;
                PanelDetalle.setVisibility(View.GONE);
                break;
            case GlobalPreferences.PAGE_STATE_INCIDENCE_FOUND:
                GlobalPreferences.PAGE_STATE = GlobalPreferences.PAGE_STATE_INVENTORY;
                getFragmentManager().beginTransaction().remove(getFragmentManager().findFragmentByTag("ControlIncidencias")).commit();
                FragmentHolder.setVisibility(View.GONE);
                AlertLayout.setVisibility(View.GONE);
                break;
            default:
                conter = 0;
        }
    }

    //Network non usable methods
    /*interface api_network {
        @FormUrlEncoded
        @POST("/upload_alta.php")
        void setData(
                @Field("id_list") String IdList,
                Callback<Response> callback
        );
    }

    interface api_network_alta_insidencia {
        @Multipart
        @POST("/setAltaIncidencia.php")
        void setData(
                @Part("IdCAF") String IdCAf,
                @Part("CreatorName") String CreatorName,
                @Part("file") TypedFile file,
                Callback<Response> callback
        );
    }*/
    /*private void getAreas() {
        main_list_areas = new ArrayList<>();
        menu_area = new PopupMenu(Main.this, Spinner_Departamento);
        Volley.newRequestQueue(Main.this).add(new JsonObjectRequest(Request.Method.GET, GlobalPreferences.URL+"/HellmannCAF/webservices/Loaders/getAreas.php?IdCedis="+GlobalPreferences.ID_CEDIS, null, response -> {
            JSONArray json = response.optJSONArray("Data");

            try {
                for (int i = 0; i < json.length(); i++) {
                    ModelUbicaciones model = new ModelUbicaciones();
                    JSONObject jsonObject = null;
                    jsonObject = json.getJSONObject(i);
                    model.setId(jsonObject.optString("Id"));
                    model.setNombre(jsonObject.optString("Nombre"));

                    menu_area.getMenu().add(model.getNombre());
                    main_list_areas.add(model);

                }

                menu_area.setOnMenuItemClickListener(item -> {
                    Spinner_Departamento.setText(item.getTitle());
                    Spinner_Oficina.setText("Oficina");
                    for(int i = 0; i<main_list_areas.size(); i++){
                        if(main_list_areas.get(i).getNombre().equals(item.getTitle())){
                            IdOficina = "0";
                            IdArea = main_list_areas.get(i).getId();
                            getOficinas();
                        }
                    }
                    return false;
                });

            } catch (JSONException | NullPointerException e) {
                Log.e("Validacion", "JSON | Null Exception" + e);
            }

        }, error -> {
            Log.e("Validacion", "Volley error" + error);
        }));
    }

    private void getOficinas() {
        main_list_oficinas = new ArrayList<>();
        menu_oficinas = new PopupMenu(Main.this, Spinner_Oficina);
        pb_loading_ofices.setVisibility(View.VISIBLE);
        Volley.newRequestQueue(Main.this).add(new JsonObjectRequest(Request.Method.GET, GlobalPreferences.URL+"/HellmannCAF/webservices/Loaders/getOficinas.php?IdArea="+IdArea, null, response -> {
            JSONArray json = response.optJSONArray("Data");

            try {
                for (int i = 0; i < json.length(); i++) {
                    ModelUbicaciones model = new ModelUbicaciones();
                    JSONObject jsonObject = null;
                    jsonObject = json.getJSONObject(i);
                    model.setId(jsonObject.optString("Id"));
                    model.setNombre(jsonObject.optString("Nombre"));

                    menu_oficinas.getMenu().add(model.getNombre());
                    main_list_oficinas.add(model);

                }

                menu_oficinas.setOnMenuItemClickListener(item -> {
                    Spinner_Oficina.setText(item.getTitle());
                    for(int i = 0; i<main_list_oficinas.size(); i++){
                        if(main_list_oficinas.get(i).getNombre().equals(item.getTitle())){
                            IdOficina = main_list_oficinas.get(i).getId();
                        }
                    }
                    return false;
                });

                pb_loading_ofices.setVisibility(View.GONE);

            } catch (JSONException | NullPointerException e) {
                Log.e("Validacion", "JSON | Null Exception" + e);
            }

        }, error -> {
            Log.e("Validacion", "Volley error" + error);
        }));
    }*/
}