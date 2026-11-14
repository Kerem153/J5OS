package net.j5os.chaoscontrol;
import android.Manifest;
import android.app.*;
import android.content.*;
import android.content.pm.PackageManager;
import android.net.wifi.*;
import android.os.*;
import android.provider.Settings;
import android.view.*;
import android.widget.*;
import java.util.*;

public class NetworkActivity extends Activity {
 WifiManager wifi; TextView status,results; LinearLayout body; Switch alerts; boolean listening=false;
 final Handler h=new Handler(Looper.getMainLooper());
 final Set<String> observed=new HashSet<>();
 final Runnable tick=new Runnable(){public void run(){if(listening){scan();h.postDelayed(this,35000);}}};
 final BroadcastReceiver receiver=new BroadcastReceiver(){public void onReceive(Context c,Intent i){if(i.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED,false))showResults(true);else status.setText("Yeni sonuç yok; önceki tarama gösteriliyor.");}};
 int dp(int n){return (int)(n*getResources().getDisplayMetrics().density+.5f);}
 TextView label(String s,int size){TextView t=new TextView(this);t.setText(s);t.setTextSize(size);t.setTextColor(0xff57d9ff);t.setPadding(0,dp(8),0,dp(8));body.addView(t);return t;}
 void button(String s,Runnable r){Button b=new Button(this);b.setText(s);b.setAllCaps(false);body.addView(b);b.setOnClickListener(v->r.run());}
 @Override public void onCreate(Bundle b){super.onCreate(b);wifi=(WifiManager)getApplicationContext().getSystemService(WIFI_SERVICE);ScrollView scroll=new ScrollView(this);scroll.setBackgroundColor(0xff09151e);body=new LinearLayout(this);body.setOrientation(1);body.setPadding(dp(16),dp(20),dp(16),dp(20));scroll.addView(body);setContentView(scroll);label("NETWORK TOOLS",26);label("BELEŞ WIFI TARAYICI",18);label("Şifresiz ağlar ücretsiz internet garantisi vermez. Yalnızca izinli ağlara bağlan.",13);
 alerts=new Switch(this);alerts.setText("Güçlü titreşim ve bildirim");alerts.setTextColor(0xff57d9ff);alerts.setChecked(getSharedPreferences("chaos",0).getBoolean("wifi_alert",false));body.addView(alerts);alerts.setOnCheckedChangeListener((v,on)->{getSharedPreferences("chaos",0).edit().putBoolean("wifi_alert",on).apply();if(!on){((Vibrator)getSystemService(VIBRATOR_SERVICE)).cancel();getSystemService(NotificationManager.class).cancel(51);}});
 button("Wi-Fi tara",()->scan());button("Wi-Fi ayarları",()->startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS)));button("Ana menüye dön",()->finish());status=label("Tarama hazırlanıyor",14);results=label("",16);}
 boolean allowed(){return checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED;}
 void scan(){if(!allowed()){requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},7);return;}if(wifi==null||!wifi.isWifiEnabled()){status.setText("Wi-Fi kapalı. Ayarlardan aç.");return;}try{boolean started=wifi.startScan();showResults(false);status.setText(started?"Tarama başlatıldı…":"Android tarama sınırı: mevcut sonuçlar gösteriliyor.");}catch(SecurityException e){status.setText("Konum izni ve konum hizmeti gerekli.");}}
 @Override public void onRequestPermissionsResult(int r,String[] p,int[] g){super.onRequestPermissionsResult(r,p,g);if(r==7&&allowed())scan();else status.setText("Tarama için konum izni gerekli.");}
 void showResults(boolean fresh){if(!allowed())return;try{List<ScanResult> all=new ArrayList<>(wifi.getScanResults());all.sort((a,b)->Integer.compare(b.level,a.level));StringBuilder out=new StringBuilder();int open=0;boolean found=false;for(ScanResult s:all){if(s.SSID==null||s.SSID.isEmpty())continue;String cap=s.capabilities==null?"":s.capabilities.toUpperCase(Locale.ROOT);boolean free=!cap.contains("WEP")&&!cap.contains("WPA")&&!cap.contains("SAE")&&!cap.contains("EAP")&&!cap.contains("OWE")&&!cap.contains("WAPI")&&!cap.contains("DPP");if(free){open++;if(fresh&&observed.add(s.BSSID==null?s.SSID:s.BSSID))found=true;}out.append(free?"🔓 AÇIK  ":"🔒 KİLİTLİ  ").append(s.SSID).append("  ").append(s.level).append(" dBm\n\n");}results.setText(out.length()==0?"Ağ bulunamadı.":out.toString());status.setText(all.size()+" ağ • "+open+" şifresiz");if(found&&alerts.isChecked())notifyFound();}catch(Exception e){status.setText("Tarama okunamadı: konum hizmetini kontrol et.");}}
 void notifyFound(){NotificationManager nm=getSystemService(NotificationManager.class);nm.createNotificationChannel(new NotificationChannel("wifi_found","Beleş Wi-Fi",NotificationManager.IMPORTANCE_HIGH));PendingIntent pi=PendingIntent.getActivity(this,51,new Intent(this,NetworkActivity.class),PendingIntent.FLAG_IMMUTABLE|PendingIntent.FLAG_UPDATE_CURRENT);nm.notify(51,new Notification.Builder(this,"wifi_found").setSmallIcon(android.R.drawable.stat_notify_more).setContentTitle("Beleş WiFi var hayırlı uğurlu olsun!").setContentText("Şifresiz Wi-Fi ağı bulundu.").setContentIntent(pi).setAutoCancel(true).build());Vibrator v=(Vibrator)getSystemService(VIBRATOR_SERVICE);if(v!=null&&v.hasVibrator())v.vibrate(VibrationEffect.createWaveform(new long[]{0,650,100,650,100,650,100,650},new int[]{0,255,0,255,0,255,0,255},-1));}
 @Override protected void onResume(){super.onResume();listening=true;registerReceiver(receiver,new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));h.post(tick);}
 @Override protected void onPause(){listening=false;h.removeCallbacks(tick);unregisterReceiver(receiver);((Vibrator)getSystemService(VIBRATOR_SERVICE)).cancel();super.onPause();}
}
