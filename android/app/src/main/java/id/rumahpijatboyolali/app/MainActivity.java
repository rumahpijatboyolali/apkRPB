package id.rumahpijatboyolali.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends Activity {
    private static final String API_URL = "https://script.google.com/macros/s/AKfycbwk9OOgouAIw1scYBqg3sDHWeoKdHY1t8GBSJER1zl10eJlVQrFQk-CW9iKUCl4GuDAeg/exec";
    private static final int INK = Color.rgb(42, 33, 27), BROWN = Color.rgb(33, 25, 20);
    private static final int GOLD = Color.rgb(183, 138, 74), CREAM = Color.rgb(247, 242, 233), PAPER = Color.WHITE;
    private final ExecutorService io = Executors.newSingleThreadExecutor();
    private LinearLayout root, content;
    private String token = "";

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setStatusBarColor(BROWN); getWindow().setNavigationBarColor(BROWN);
        showLogin();
    }

    private int dp(float n) { return (int)(n * getResources().getDisplayMetrics().density); }
    private JSONObject obj(String... pairs) {
        JSONObject o = new JSONObject();
        try { for (int i=0;i+1<pairs.length;i+=2) o.put(pairs[i], pairs[i+1]); } catch(Exception ignored) {}
        return o;
    }
    private void request(String action, JSONObject data, ApiCallback cb) {
        JSONObject body = new JSONObject();
        try { body.put("action", action); body.put("token", token); body.put("data", data == null ? new JSONObject() : data); } catch(Exception ignored) {}
        io.execute(() -> {
            JSONObject response;
            try { response = post(body); } catch(Exception e) { response = new JSONObject(); try { response.put("success", false); response.put("message", e.getMessage()); } catch(Exception ignored) {} }
            final JSONObject out = response;
            runOnUiThread(() -> cb.done(out));
        });
    }
    private JSONObject post(JSONObject body) throws Exception {
        HttpURLConnection c = (HttpURLConnection)new URL(API_URL).openConnection();
        c.setRequestMethod("POST"); c.setConnectTimeout(20000); c.setReadTimeout(30000); c.setInstanceFollowRedirects(false);
        c.setDoOutput(true); c.setRequestProperty("Content-Type", "application/json; charset=utf-8");
        try (OutputStream os = c.getOutputStream()) { os.write(body.toString().getBytes(StandardCharsets.UTF_8)); }
        int code = c.getResponseCode();
        if (code == 301 || code == 302 || code == 303 || code == 307 || code == 308) {
            String location = c.getHeaderField("Location"); c.disconnect();
            if (location == null) throw new Exception("Server tidak memberi alamat respons.");
            HttpURLConnection redirected = (HttpURLConnection)new URL(location).openConnection();
            redirected.setConnectTimeout(20000); redirected.setReadTimeout(30000);
            code = redirected.getResponseCode();
            try (BufferedReader br = new BufferedReader(new InputStreamReader(code >= 400 ? redirected.getErrorStream() : redirected.getInputStream(), StandardCharsets.UTF_8))) {
                StringBuilder s = new StringBuilder(); String line; while ((line=br.readLine())!=null) s.append(line); return new JSONObject(s.toString());
            } finally { redirected.disconnect(); }
        }
        try (BufferedReader br = new BufferedReader(new InputStreamReader(code >= 400 ? c.getErrorStream() : c.getInputStream(), StandardCharsets.UTF_8))) {
            StringBuilder s = new StringBuilder(); String line; while ((line=br.readLine())!=null) s.append(line); return new JSONObject(s.toString());
        } finally { c.disconnect(); }
    }
    private interface ApiCallback { void done(JSONObject response); }
    private JSONObject result(JSONObject r) { return r.optJSONObject("result"); }
    private boolean ok(JSONObject r) { return r.optBoolean("success", false); }
    private void toast(String s) { Toast.makeText(this, s, Toast.LENGTH_LONG).show(); }

    private void base(String title) {
        root = new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setBackgroundColor(CREAM);
        LinearLayout bar = new LinearLayout(this); bar.setGravity(Gravity.CENTER_VERTICAL); bar.setPadding(dp(20),dp(18),dp(20),dp(16)); bar.setBackgroundColor(BROWN);
        TextView brand = text("RUMAH PIJAT\nBOYOLALI", 17, Color.WHITE, true); brand.setLetterSpacing(.06f); bar.addView(brand,new LinearLayout.LayoutParams(0,-2,1));
        TextView page = text(title.toUpperCase(Locale.ROOT),11,0xFFE0C798,true); bar.addView(page);
        root.addView(bar);
        ScrollView scroll = new ScrollView(this); content = new LinearLayout(this); content.setOrientation(LinearLayout.VERTICAL); content.setPadding(dp(18),dp(18),dp(18),dp(20)); scroll.addView(content);
        root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1)); setContentView(root);
    }
    private TextView text(String s,int size,int color,boolean bold) { TextView t=new TextView(this);t.setText(s);t.setTextSize(size);t.setTextColor(color);if(bold)t.setTypeface(Typeface.DEFAULT,Typeface.BOLD);return t; }
    private EditText field(String hint) { EditText e=new EditText(this);e.setSingleLine(true);e.setTextSize(15);e.setHint(hint);e.setPadding(dp(14),dp(10),dp(14),dp(10));e.setBackgroundColor(PAPER); LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);p.bottomMargin=dp(12);content.addView(e,p);return e; }
    private Button button(String label, boolean primary, View.OnClickListener click) { Button b=new Button(this);b.setText(label);b.setAllCaps(false);b.setTextColor(primary?Color.WHITE:INK);b.setBackgroundTintList(android.content.res.ColorStateList.valueOf(primary?GOLD:0xFFE8DFD2));b.setOnClickListener(click);LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);p.bottomMargin=dp(10);content.addView(b,p);return b; }
    private void gap(int h) { View v=new View(this);content.addView(v,new LinearLayout.LayoutParams(1,dp(h))); }
    private void heading(String s) { TextView t=text(s,21,INK,true);LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);p.bottomMargin=dp(14);content.addView(t,p); }
    private void showLogin() {
        base("Admin"); gap(52); heading("Masuk ke panel admin");
        TextView note=text("Kelola jadwal pijat dan catatan penjualan dari aplikasi Android.",14,0xFF76695D,false);LinearLayout.LayoutParams np=new LinearLayout.LayoutParams(-1,-2);np.bottomMargin=dp(22);content.addView(note,np);
        EditText username=field("Username"); EditText password=field("Password");password.setInputType(129);
        button("Masuk",true,v->{ JSONObject d=obj("username",username.getText().toString().trim(),"password",password.getText().toString());request("login",d,r->{JSONObject x=result(r);if(ok(r)&&x!=null&&x.optBoolean("success")){token=x.optString("token");showHome();}else toast(x==null?r.optString("message","Login gagal"):x.optString("message","Login gagal"));}); });
        TextView foot=text("Rumah Pijat Boyolali  ·  Admin",12,0xFF8B8177,false);foot.setGravity(Gravity.CENTER);content.addView(foot);
    }
    private void showHome() {
        base("Ringkasan"); heading("Ringkasan hari ini");
        TextView status=text("Memuat data…",14,INK,false);content.addView(status);
        request("dashboard",new JSONObject(),r->{if(!r.optBoolean("success")){status.setText(r.optString("message","Gagal memuat data"));if(status.getText().toString().contains("login"))showLogin();return;}JSONObject d=result(r);content.removeView(status);card("Pemasukan hari ini",money(d.optDouble("todayTotal")+d.optDouble("todayOtherIncome")),"Bersih: "+money(d.optDouble("todayNet")));card("Pemasukan bulan ini",money(d.optDouble("monthTotal")+d.optDouble("monthOtherIncome")),"Bersih: "+money(d.optDouble("monthNet")));card("Layanan hari ini",d.optInt("todayCount")+" transaksi","Bulan ini: "+d.optInt("monthCount")+" transaksi");});
        gap(10); heading("Menu admin");
        button("Jadwal pijat",true,v->showSchedules());button("Tambah transaksi",false,v->showAddTransaction());button("Daftar transaksi",false,v->showTransactions());
        button("Keluar",false,v->{request("logout",new JSONObject(),r->{token="";showLogin();});});
    }
    private void card(String title,String value,String sub) { LinearLayout box=new LinearLayout(this);box.setOrientation(LinearLayout.VERTICAL);box.setPadding(dp(16),dp(15),dp(16),dp(15));box.setBackgroundColor(PAPER);LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);p.bottomMargin=dp(10);content.addView(box,p);box.addView(text(title,13,0xFF817365,true));TextView amount=text(value,23,INK,true);LinearLayout.LayoutParams ap=new LinearLayout.LayoutParams(-1,-2);ap.topMargin=dp(8);box.addView(amount,ap);TextView secondary=text(sub,13,0xFF7C6D60,false);LinearLayout.LayoutParams sp=new LinearLayout.LayoutParams(-1,-2);sp.topMargin=dp(4);box.addView(secondary,sp); }
    private String money(double n) { return NumberFormat.getCurrencyInstance(new Locale("id","ID")).format(n).replace(",00", ""); }

    private void showSchedules() {
        base("Jadwal");heading("Jadwal pijat");
        EditText name=field("Nama pelanggan");EditText date=field("Tanggal (YYYY-MM-DD)");date.setText(today());EditText time=field("Jam (HH:MM)");EditText treatment=field("Treatment: HC atau OT");treatment.setText("HC");
        button("Simpan jadwal",true,v->{JSONObject d=obj("nama",name.getText().toString(),"tanggal",date.getText().toString(),"jam",time.getText().toString(),"treatment",treatment.getText().toString());request("saveSchedule",d,r->{JSONObject x=result(r);if(r.optBoolean("success")&&(x==null||x.optBoolean("success",true))){toast(x==null?"Jadwal disimpan":x.optString("message"));showSchedules();}else toast(x==null?r.optString("message"):x.optString("message"));});});
        gap(8);heading("Jadwal minggu ini");TextView status=text("Memuat jadwal…",14,INK,false);content.addView(status);
        Calendar cal=Calendar.getInstance();cal.setFirstDayOfWeek(Calendar.MONDAY);cal.set(Calendar.DAY_OF_WEEK,Calendar.MONDAY);String start=fmt(cal);cal.add(Calendar.DAY_OF_MONTH,6);String end=fmt(cal);
        request("schedules",obj("startDate",start,"endDate",end),r->{if(!r.optBoolean("success")){status.setText(r.optString("message"));return;}JSONArray rows=r.optJSONArray("result");content.removeView(status);if(rows==null||rows.length()==0){content.addView(text("Belum ada jadwal minggu ini.",14,0xFF76695D,false));return;}for(int i=0;i<rows.length();i++){JSONObject row=rows.optJSONObject(i);if(row!=null)scheduleCard(row);}});
        button("Kembali",false,v->showHome());
    }
    private void scheduleCard(JSONObject row) { LinearLayout box=new LinearLayout(this);box.setOrientation(LinearLayout.VERTICAL);box.setPadding(dp(14),dp(12),dp(14),dp(8));box.setBackgroundColor(PAPER);LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);p.bottomMargin=dp(9);content.addView(box,p);box.addView(text(row.optString("tanggal")+"  ·  "+row.optString("jam"),12,GOLD,true));box.addView(text(row.optString("nama"),17,INK,true));box.addView(text(row.optString("treatment","HC").toUpperCase(Locale.ROOT),12,0xFF76695D,false));Button del=new Button(this);del.setText("Hapus jadwal");del.setAllCaps(false);del.setOnClickListener(v->new AlertDialog.Builder(this).setMessage("Hapus jadwal ini?").setNegativeButton("Batal",null).setPositiveButton("Hapus",(a,b)->request("deleteSchedule",obj("id",row.optString("id"),"tanggal",row.optString("tanggal")),r->{toast(r.optString("message","Selesai"));showSchedules();})).show());box.addView(del); }

    private void showAddTransaction() {
        base("Transaksi");heading("Tambah transaksi");EditText name=field("Nama pelanggan");EditText date=field("Tanggal (YYYY-MM-DD)");date.setText(today());EditText amount=field("Nominal (contoh 100000)");amount.setInputType(2);
        button("Simpan transaksi",true,v->{JSONObject d=obj("nama",name.getText().toString(),"tanggal",date.getText().toString(),"nominal",amount.getText().toString());request("addTransaction",d,r->{if(r.optBoolean("success")){JSONObject x=result(r);toast(x==null?"Transaksi tersimpan":x.optString("message"));showHome();}else toast(r.optString("message","Gagal menyimpan"));});});button("Kembali",false,v->showHome());
    }
    private void showTransactions() {
        base("Transaksi");heading("Transaksi terbaru");TextView status=text("Memuat transaksi…",14,INK,false);content.addView(status);
        request("transactions",new JSONObject(),r->{if(!r.optBoolean("success")){status.setText(r.optString("message"));return;}JSONArray rows=r.optJSONArray("result");content.removeView(status);if(rows==null||rows.length()==0){content.addView(text("Belum ada transaksi.",14,0xFF76695D,false));return;}int limit=Math.min(rows.length(),60);for(int i=0;i<limit;i++){JSONObject row=rows.optJSONObject(i);if(row==null)continue;LinearLayout box=new LinearLayout(this);box.setOrientation(LinearLayout.VERTICAL);box.setPadding(dp(14),dp(12),dp(14),dp(12));box.setBackgroundColor(PAPER);LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);p.bottomMargin=dp(8);content.addView(box,p);box.addView(text(row.optString("tanggal"),12,GOLD,true));box.addView(text(row.optString("nama"),16,INK,true));box.addView(text(money(row.optDouble("nominal")),14,INK,false));}});
        button("Kembali",false,v->showHome());
    }

    private String fmt(Calendar calendar) { return new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(calendar.getTime()); }
    private String today() { return fmt(Calendar.getInstance()); }

    @Override protected void onDestroy(){io.shutdownNow();super.onDestroy();}
}
