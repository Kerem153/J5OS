package net.j5os.chaoscontrol;
import android.app.*;
import android.os.*;
import android.content.*;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.*;
import android.widget.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.*;

public class MainActivity extends Activity {
 final int green=0xff39ff88, dark=0xff090e13, muted=0xffa1b3ac;
 final Handler h=new Handler(Looper.getMainLooper());
 final ExecutorService worker=Executors.newSingleThreadExecutor();
 LinearLayout body; TextView cpu,ram,disk,battery,root,score,history,network; ProgressBar rb,db,bb,cb;
 android.content.SharedPreferences prefs; boolean running=false,busy=false; long lastTotal=-1,lastIdle=-1,lastRx=-1,lastTx=-1,lastTime=0;
 String run(String cmd) throws Exception {
  Process p=new ProcessBuilder("su","-c",cmd).redirectErrorStream(true).start();
  ByteArrayOutputStream b=new ByteArrayOutputStream();
  Thread t=new Thread(()->{try(InputStream in=p.getInputStream()){byte[] a=new byte[4096];int n;while((n=in.read(a))!=-1){if(b.size()<2000000)b.write(a,0,n);}}catch(Exception e){}});
  t.start(); if(!p.waitFor(8,TimeUnit.SECONDS)){p.destroyForcibly();throw new IOException("timeout");} t.join(1000);
  if(p.exitValue()!=0)throw new IOException("root unavailable");return b.toString("UTF-8");
 }
 int dp(int n){return (int)(n*getResources().getDisplayMetrics().density+.5f);}
 TextView text(String s,int size,int color){TextView v=new TextView(this);v.setText(s);v.setTextSize(size);v.setTextColor(color);v.setPadding(0,dp(4),0,dp(4));return v;}
 LinearLayout card(){LinearLayout l=new LinearLayout(this);l.setOrientation(1);l.setPadding(dp(16),dp(12),dp(16),dp(12));GradientDrawable g=new GradientDrawable();g.setColor(0xff122019);g.setCornerRadius(dp(12));l.setBackground(g);LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);p.bottomMargin=dp(10);body.addView(l,p);return l;}
 TextView metric(String label){LinearLayout c=card();c.addView(text(label,12,muted));TextView v=text("—",22,green);c.addView(v);return v;}
 ProgressBar bar(TextView v){ProgressBar b=new ProgressBar(this,null,android.R.attr.progressBarStyleHorizontal);b.setMax(100);b.setProgressTintList(android.content.res.ColorStateList.valueOf(green));((LinearLayout)v.getParent()).addView(b,new LinearLayout.LayoutParams(-1,dp(7)));return b;}
 void button(LinearLayout c,String label,Runnable action){Button b=new Button(this);b.setText(label);b.setTextColor(green);b.setAllCaps(false);c.addView(b);b.setOnClickListener(v->action.run());}
 @Override public void onCreate(Bundle b){super.onCreate(b);prefs=getSharedPreferences("chaos",0);ScrollView sc=new ScrollView(this);sc.setBackgroundColor(dark);body=new LinearLayout(this);body.setOrientation(1);body.setPadding(dp(16),dp(20),dp(16),dp(16));sc.addView(body);setContentView(sc);
 TextView title=text("CHAOS CONTROL",28,green);title.setTypeface(null,Typeface.BOLD);body.addView(title);body.addView(text("GALAXY J CHAOS  /  SYSTEM MONITOR",12,muted));
 cpu=metric("İŞLEMCİ • Snapdragon 410");cb=bar(cpu);ram=metric("RAM");rb=bar(ram);disk=metric("DEPOLAMA • /data");db=bar(disk);battery=metric("BATARYA • SICAKLIK");bb=bar(battery);network=metric("AĞ TRAFİĞİ");root=metric("ROOT DURUMU");
 LinearLayout points=card();points.addView(text("CHAOS POINTS",14,muted));score=text("",30,green);points.addView(score);history=text("",14,muted);points.addView(history);button(points,"Çökme kayıtlarını tara",()->scan());showScore();
 LinearLayout commands=card();commands.addView(text("SİSTEM KOMUTLARI",14,green));button(commands,"Yeniden başlat",()->confirm("Normal yeniden başlat","reboot"));button(commands,"TWRP Recovery",()->confirm("Recovery moduna geç","reboot recovery"));button(commands,"Download Mode",()->confirm("Download moduna geç","reboot download"));button(commands,"Açılış animasyonu bilgisi",()->worker.execute(()->{String s;try{s=run("ls -l /system/media/bootanimation.zip");}catch(Exception e){s="Okunamadı";}final String x=s;h.post(()->new AlertDialog.Builder(this).setTitle("Boot animation").setMessage(x).setPositiveButton("Tamam",null).show());}));button(commands,"Root terminal",()->terminal());
 }
 void confirm(String title,String cmd){new AlertDialog.Builder(this).setTitle(title+"?").setNegativeButton("İptal",null).setPositiveButton("Onayla",(d,w)->worker.execute(()->{try{run(cmd);}catch(Exception e){h.post(()->Toast.makeText(this,"Komut sonucu alınamadı",0).show());}})).show();}
 void terminal(){EditText ed=new EditText(this);ed.setHint("Örn. id");new AlertDialog.Builder(this).setTitle("Root terminal • komut tam yetkiyle çalışır").setView(ed).setNegativeButton("İptal",null).setPositiveButton("Çalıştır",(d,w)->{String cmd=ed.getText().toString();if(cmd.trim().isEmpty())return;worker.execute(()->{String s;try{s=run(cmd);}catch(Exception e){s=e.toString();}final String result=s.length()>12000?s.substring(0,12000):s;h.post(()->{ScrollView scroll=new ScrollView(this);TextView v=text(result,12,green);v.setTypeface(Typeface.MONOSPACE);scroll.addView(v);new AlertDialog.Builder(this).setTitle("Terminal").setView(scroll).setPositiveButton("Tamam",null).show();});});}).show();}
 String mb(long b){return String.format(Locale.US,"%.0f MB",b/1048576.0);}
 void refresh(){ActivityManager.MemoryInfo m=new ActivityManager.MemoryInfo();((ActivityManager)getSystemService(ACTIVITY_SERVICE)).getMemoryInfo(m);long used=m.totalMem-m.availMem;ram.setText(mb(used)+" / "+mb(m.totalMem));rb.setProgress((int)(100.0*used/m.totalMem));StatFs fs=new StatFs(Environment.getDataDirectory().getPath());long total=fs.getTotalBytes(),du=total-fs.getAvailableBytes();disk.setText(mb(du)+" / "+mb(total));db.setProgress((int)(100.0*du/total));
 Intent b=registerReceiver(null,new IntentFilter(Intent.ACTION_BATTERY_CHANGED));if(b!=null){int level=b.getIntExtra("level",0),scale=b.getIntExtra("scale",100);int pct=level*100/Math.max(1,scale);battery.setText(pct+"%  •  "+b.getIntExtra("temperature",0)/10.0+" °C");bb.setProgress(pct);}
 long now=SystemClock.elapsedRealtime(),rx=android.net.TrafficStats.getTotalRxBytes(),tx=android.net.TrafficStats.getTotalTxBytes();if(lastTime>0&&now>lastTime&&rx>=lastRx&&tx>=lastTx)network.setText(String.format(Locale.US,"↓ %.1f KB/s   ↑ %.1f KB/s",(rx-lastRx)*1000.0/(now-lastTime)/1024,(tx-lastTx)*1000.0/(now-lastTime)/1024));lastTime=now;lastRx=rx;lastTx=tx;
 if(!busy){busy=true;worker.execute(()->{String out="";try{out=run("id; head -n 1 /proc/stat");}catch(Exception e){}final String s=out;h.post(()->{busy=false;root.setText(s.contains("uid=0")?"MAGISK ROOT • AKTİF":"ROOT YOK / İZİN GEREKLİ");for(String line:s.split("\\n"))if(line.startsWith("cpu ")){try{String[] a=line.trim().split("\\s+");long sum=0;for(int i=1;i<Math.min(a.length,9);i++)sum+=Long.parseLong(a[i]);long idle=Long.parseLong(a[4])+(a.length>5?Long.parseLong(a[5]):0);if(lastTotal>=0&&sum>lastTotal){int pct=(int)(100*(sum-lastTotal-(idle-lastIdle))/(double)(sum-lastTotal));pct=Math.max(0,Math.min(100,pct));cpu.setText(pct+"%");cb.setProgress(pct);}lastTotal=sum;lastIdle=idle;}catch(Exception e){}}});});}}
 final Runnable tick=new Runnable(){public void run(){if(running){refresh();h.postDelayed(this,3000);}}};
 @Override protected void onResume(){super.onResume();running=true;h.post(tick);scan();}
 @Override protected void onPause(){running=false;h.removeCallbacks(tick);super.onPause();}
 void showScore(){int p=prefs.getInt("points",0);score.setText(p+" CP • LV "+(1+p/500));history.setText(prefs.getString("history","Henüz kayıt yok.")+(p>=5000?"\n🏆 Chaos Master":"")+(p>=10000?"\n👑 Ultimate Chaos":""));}
 void scan(){worker.execute(()->{try{
 String logs=run("logcat -b events -d -v epoch -s am_crash am_anr am_kill");
 Set<String> seen=new HashSet<>(prefs.getStringSet("seen",Collections.emptySet()));int points=prefs.getInt("points",0);String hist=prefs.getString("history","");int added=0;
 for(String line:logs.split("\\n")){if(!line.contains("am_crash")&&!line.contains("am_anr"))continue;int a=line.indexOf('['),z=line.indexOf(']',a);if(a<0||z<0)continue;String[] fields=line.substring(a+1,z).split(",");if(fields.length<3)continue;String pkg=fields[2].trim();if(pkg.isEmpty()||pkg.equals(getPackageName()))continue;String key=line.trim();if(!seen.add(key))continue;boolean anr=line.contains("am_anr");int cp=pkg.equals("com.android.systemui")?500:pkg.contains("launcher")?250:anr?150:pkg.toLowerCase(Locale.ROOT).contains("subwaysurf")?100:50;points+=cp;added++;hist=pkg+"  +"+cp+" CP"+(anr?" (ANR)":" (Crash)")+"\n"+hist;if(hist.length()>1600)hist=hist.substring(0,1600);}
 prefs.edit().putStringSet("seen",seen).putInt("points",points).putString("history",hist.isEmpty()?"Henüz kayıt yok.":hist).apply();final int n=added;h.post(()->{showScore();Toast.makeText(this,n+" yeni çökme kaydı",0).show();});
 }catch(Exception e){h.post(()->Toast.makeText(this,"Çökme kayıtları okunamadı; root izni gerekli",0).show());}});}
}
