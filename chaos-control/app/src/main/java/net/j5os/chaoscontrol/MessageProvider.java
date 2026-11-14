package net.j5os.chaoscontrol;
import android.content.*;
import android.database.*;
import android.net.Uri;
import android.os.*;
import java.io.*;
public class MessageProvider extends ContentProvider {
 public boolean onCreate(){return true;}
 public String getType(Uri u){return "text/plain";}
 public ParcelFileDescriptor openFile(Uri u,String mode)throws FileNotFoundException{if(!"/Mesaj.txt".equals(u.getPath())||!"r".equals(mode))throw new FileNotFoundException();return ParcelFileDescriptor.open(new File(getContext().getCacheDir(),"Mesaj.txt"),ParcelFileDescriptor.MODE_READ_ONLY);}
 public Cursor query(Uri u,String[] p,String s,String[] a,String sort){String[] cols=p==null?new String[]{"_display_name","_size"}:p;MatrixCursor c=new MatrixCursor(cols);Object[] row=new Object[cols.length];for(int j=0;j<cols.length;j++)row[j]="_display_name".equals(cols[j])?"Mesaj.txt":"_size".equals(cols[j])?new File(getContext().getCacheDir(),"Mesaj.txt").length():null;c.addRow(row);return c;}
 public Uri insert(Uri u,ContentValues v){throw new UnsupportedOperationException();}
 public int update(Uri u,ContentValues v,String s,String[] a){throw new UnsupportedOperationException();}
 public int delete(Uri u,String s,String[] a){throw new UnsupportedOperationException();}
}
