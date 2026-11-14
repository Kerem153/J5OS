package net.j5os.chaoscontrol;
import android.content.*;
import android.app.*;
import java.util.*;
public final class CrashPoints {
 public static synchronized int scan(Context c,String logs){
  SharedPreferences prefs=c.getSharedPreferences("chaos",0);
  Set<String> seen=new HashSet<>(prefs.getStringSet("seen",Collections.emptySet()));
  boolean locked=prefs.contains("cp_lock_boot")&&prefs.getInt("cp_lock_boot",-2)==ChaosActivity.SettingsBoot.boot(c); int points=prefs.getInt("points",0),added=0;String hist=prefs.getString("history","");
  for(String line:logs.split("\\n")){boolean anr=line.contains("am_anr");if(!anr&&!line.contains("am_crash"))continue;int a=line.indexOf('['),z=line.indexOf(']',a);if(a<0||z<0)continue;String[] f=line.substring(a+1,z).split(",");if(f.length<3)continue;String pkg=f[2].trim();if(pkg.isEmpty()||pkg.startsWith(c.getPackageName())||!seen.add(line.trim()))continue;int cp=pkg.equals("com.android.systemui")?500:pkg.contains("launcher")?250:anr?150:pkg.toLowerCase(Locale.ROOT).contains("subwaysurf")?100:50;if(locked)continue;points+=cp;added++;notifyCrash(c,pkg,cp,anr);hist=pkg+"  +"+cp+" CP"+(anr?" (ANR)":" (Crash)")+"\n"+hist;if(hist.length()>1600)hist=hist.substring(0,1600);}
  prefs.edit().putStringSet("seen",seen).putInt("points",points).putString("history",hist).apply();return added;
 }
 static void notifyCrash(Context c,String pkg,int cp,boolean anr){NotificationManager nm=c.getSystemService(NotificationManager.class);nm.createNotificationChannel(new NotificationChannel("crash_cp","Chaos Points",NotificationManager.IMPORTANCE_DEFAULT));String title=cp==500?"LAN TELEFONUN ANASINI AĞLATTIN!":anr?"UYGULAMA DONDU!":"AFERİN LA UYGULAMA ÇÖKTÜ!";Intent i=new Intent(c,MainActivity.class).putExtra("section","points");PendingIntent pi=PendingIntent.getActivity(c,77,i,PendingIntent.FLAG_IMMUTABLE|PendingIntent.FLAG_UPDATE_CURRENT);nm.notify(77,new Notification.Builder(c,"crash_cp").setSmallIcon(android.R.drawable.stat_notify_error).setContentTitle(title).setContentText(pkg+" +"+cp+" CP").setContentIntent(pi).setAutoCancel(true).build());}
}
