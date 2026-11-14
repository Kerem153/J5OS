package net.j5os.chaoscontrol;
import android.app.*;
import android.content.*;
import android.os.*;
import android.media.*;
import android.view.*;
import android.widget.*;
import java.util.concurrent.atomic.AtomicBoolean;
public class ChaosActivity extends Activity {
 LinearLayout body; TextView status; Handler h=new Handler(Looper.getMainLooper()); AtomicBoolean load=new AtomicBoolean(false); MediaPlayer player; boolean effects=false; int generation=0;
 void btn(String s,Runnable r){Button b=new Button(this);b.setText(s);b.setTextColor(0xffff5555);body.addView(b);b.setOnClickListener(v->r.run());}
 @Override public void onCreate(Bundle b){super.onCreate(b);ScrollView sc=new ScrollView(this);sc.setBackgroundColor(0xff190909);body=new LinearLayout(this);body.setOrientation(1);body.setPadding(20,20,20,20);sc.addView(body);setContentView(sc);status=new TextView(this);status.setTextColor(0xffff5555);status.setTextSize(22);status.setText("CHAOS MODE");body.addView(status);
 btn("PROVEN • 10 sn",()->proven());btn("SUPER VIBRATION",()->vibrate());btn("MESSAGE SPAM • 3 bildirim",()->messages());btn("TOTAL CHAOS • test süreçleri",()->new AlertDialog.Builder(this).setTitle("Test süreçleri çökecek; CP yeniden başlatana kadar kilitli").setNegativeButton("İptal",null).setPositiveButton("Başlat",(d,w)->total()).show());btn("GLITCH MODE",()->fake("GLITCH • SİMÜLASYON"));btn("FAKE PANIC",()->fake("KERNEL PANIC • SİMÜLASYON"));btn("FAKE ERROR",()->fake("SYSTEM ERROR • SİMÜLASYON"));btn("MAXIMUM CHAOS",()->{vibrate();messages();fake("MAXIMUM CHAOS • SİMÜLASYON");});btn("STOP ALL",()->stop());btn("Ana menü",()->finish());String q=getIntent().getStringExtra("quick");if(q!=null)h.post(()->{switch(q){case "proven":proven();break;case "vibrate":vibrate();break;case "messages":messages();break;case "total":new AlertDialog.Builder(this).setTitle("Test süreçlerini çökert ve CP kilitle?").setNegativeButton("İptal",null).setPositiveButton("Başlat",(d,w)->total()).show();break;case "panic":fake("KERNEL PANIC • SİMÜLASYON");break;case "glitch":fake("GLITCH • SİMÜLASYON");break;case "stop":stop();break;}});}
 boolean hot(){Intent i=registerReceiver(null,new IntentFilter(Intent.ACTION_BATTERY_CHANGED));if(i==null||i.getIntExtra("temperature",999)>=400)return true;return Build.VERSION.SDK_INT>=29&&getSystemService(PowerManager.class).getCurrentThermalStatus()>=PowerManager.THERMAL_STATUS_MODERATE;}
 void proven(){if(hot()||!load.compareAndSet(false,true)){status.setText("PROVEN: sıcak veya zaten çalışıyor");return;}status.setText("PROVEN • 10 saniye");long end=SystemClock.elapsedRealtime()+10000;int n=Math.min(4,Runtime.getRuntime().availableProcessors());for(int j=0;j<n;j++)new Thread(()->{double x=1;while(load.get()&&SystemClock.elapsedRealtime()<end){for(int k=0;k<10000;k++)x=Math.sin(x)+1;if(x<0)break;} }).start();h.post(new Runnable(){public void run(){if(!load.get())return;if(hot()||SystemClock.elapsedRealtime()>=end){load.set(false);status.setText("PROVEN durdu");}else h.postDelayed(this,500);}});}
 void vibrate(){Vibrator v=getSystemService(Vibrator.class);if(v!=null&&v.hasVibrator())v.vibrate(VibrationEffect.createWaveform(new long[]{0,600,100,600,100,600,100,600},-1));}
 void messages(){int g=++generation;NotificationManager nm=getSystemService(NotificationManager.class);nm.createNotificationChannel(new NotificationChannel("chaos_test","Chaos test",NotificationManager.IMPORTANCE_DEFAULT));for(int i=0;i<3;i++){final int k=i;h.postDelayed(()->{if(g==generation)nm.notify(80+k,new Notification.Builder(this,"chaos_test").setSmallIcon(android.R.drawable.stat_notify_more).setContentTitle("CHAOS TEST "+(k+1)).setContentText("Yerel test bildirimi").setAutoCancel(true).build());},i*800);}}
 void total(){getSharedPreferences("chaos",0).edit().putInt("cp_lock_boot",SettingsBoot.boot(this)).apply();for(Class<?> c:new Class<?>[]{CrashA.class,CrashB.class,CrashC.class})startService(new Intent(this,c));status.setText("TOTAL CHAOS • CP kilitli");}
 void fake(String s){new AlertDialog.Builder(this).setTitle(s).setMessage("Gerçek sistem hatası değildir.").setPositiveButton("Kapat",null).show();}
 void stop(){load.set(false);generation++;Vibrator v=getSystemService(Vibrator.class);if(v!=null)v.cancel();if(player!=null){player.release();player=null;}for(int i=0;i<3;i++)getSystemService(NotificationManager.class).cancel(80+i);status.setText("CHAOS durduruldu");}
 @Override protected void onActivityResult(int r,int c,Intent i){super.onActivityResult(r,c,i);if(r==8&&c==RESULT_OK&&i!=null){try{if(player!=null)player.release();player=MediaPlayer.create(this,i.getData());if(player!=null)player.start();}catch(Exception e){status.setText("Ses açılamadı");}}}
 @Override protected void onStop(){stop();super.onStop();}
 public static class SettingsBoot {static int boot(Context c){return android.provider.Settings.Global.getInt(c.getContentResolver(),"boot_count",-1);}}
 public static class CrashA extends Service {public IBinder onBind(Intent i){return null;} public int onStartCommand(Intent i,int f,int id){new Handler().post(()->{throw new RuntimeException("Chaos isolated test");});return START_NOT_STICKY;}}
 public static class CrashB extends CrashA {}
 public static class CrashC extends CrashA {}
}
