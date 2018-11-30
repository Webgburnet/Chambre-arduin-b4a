package b4a.test;


import anywheresoftware.b4a.B4AMenuItem;
import android.app.Activity;
import android.os.Bundle;
import anywheresoftware.b4a.BA;
import anywheresoftware.b4a.BALayout;
import anywheresoftware.b4a.B4AActivity;
import anywheresoftware.b4a.ObjectWrapper;
import anywheresoftware.b4a.objects.ActivityWrapper;
import java.lang.reflect.InvocationTargetException;
import anywheresoftware.b4a.B4AUncaughtException;
import anywheresoftware.b4a.debug.*;
import java.lang.ref.WeakReference;

public class main extends Activity implements B4AActivity{
	public static main mostCurrent;
	static boolean afterFirstLayout;
	static boolean isFirst = true;
    private static boolean processGlobalsRun = false;
	BALayout layout;
	public static BA processBA;
	BA activityBA;
    ActivityWrapper _activity;
    java.util.ArrayList<B4AMenuItem> menuItems;
	public static final boolean fullScreen = false;
	public static final boolean includeTitle = true;
    public static WeakReference<Activity> previousOne;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (isFirst) {
			processBA = new BA(this.getApplicationContext(), null, null, "b4a.test", "b4a.test.main");
			processBA.loadHtSubs(this.getClass());
	        float deviceScale = getApplicationContext().getResources().getDisplayMetrics().density;
	        BALayout.setDeviceScale(deviceScale);
            
		}
		else if (previousOne != null) {
			Activity p = previousOne.get();
			if (p != null && p != this) {
                BA.LogInfo("Killing previous instance (main).");
				p.finish();
			}
		}
        processBA.runHook("oncreate", this, null);
		if (!includeTitle) {
        	this.getWindow().requestFeature(android.view.Window.FEATURE_NO_TITLE);
        }
        if (fullScreen) {
        	getWindow().setFlags(android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN,   
        			android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
		mostCurrent = this;
        processBA.sharedProcessBA.activityBA = null;
		layout = new BALayout(this);
		setContentView(layout);
		afterFirstLayout = false;
        WaitForLayout wl = new WaitForLayout();
        if (anywheresoftware.b4a.objects.ServiceHelper.StarterHelper.startFromActivity(processBA, wl, true))
		    BA.handler.postDelayed(wl, 5);

	}
	static class WaitForLayout implements Runnable {
		public void run() {
			if (afterFirstLayout)
				return;
			if (mostCurrent == null)
				return;
            
			if (mostCurrent.layout.getWidth() == 0) {
				BA.handler.postDelayed(this, 5);
				return;
			}
			mostCurrent.layout.getLayoutParams().height = mostCurrent.layout.getHeight();
			mostCurrent.layout.getLayoutParams().width = mostCurrent.layout.getWidth();
			afterFirstLayout = true;
			mostCurrent.afterFirstLayout();
		}
	}
	private void afterFirstLayout() {
        if (this != mostCurrent)
			return;
		activityBA = new BA(this, layout, processBA, "b4a.test", "b4a.test.main");
        
        processBA.sharedProcessBA.activityBA = new java.lang.ref.WeakReference<BA>(activityBA);
        anywheresoftware.b4a.objects.ViewWrapper.lastId = 0;
        _activity = new ActivityWrapper(activityBA, "activity");
        anywheresoftware.b4a.Msgbox.isDismissing = false;
        if (BA.isShellModeRuntimeCheck(processBA)) {
			if (isFirst)
				processBA.raiseEvent2(null, true, "SHELL", false);
			processBA.raiseEvent2(null, true, "CREATE", true, "b4a.test.main", processBA, activityBA, _activity, anywheresoftware.b4a.keywords.Common.Density, mostCurrent);
			_activity.reinitializeForShell(activityBA, "activity");
		}
        initializeProcessGlobals();		
        initializeGlobals();
        
        BA.LogInfo("** Activity (main) Create, isFirst = " + isFirst + " **");
        processBA.raiseEvent2(null, true, "activity_create", false, isFirst);
		isFirst = false;
		if (this != mostCurrent)
			return;
        processBA.setActivityPaused(false);
        BA.LogInfo("** Activity (main) Resume **");
        processBA.raiseEvent(null, "activity_resume");
        if (android.os.Build.VERSION.SDK_INT >= 11) {
			try {
				android.app.Activity.class.getMethod("invalidateOptionsMenu").invoke(this,(Object[]) null);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}
	public void addMenuItem(B4AMenuItem item) {
		if (menuItems == null)
			menuItems = new java.util.ArrayList<B4AMenuItem>();
		menuItems.add(item);
	}
	@Override
	public boolean onCreateOptionsMenu(android.view.Menu menu) {
		super.onCreateOptionsMenu(menu);
        try {
            if (processBA.subExists("activity_actionbarhomeclick")) {
                Class.forName("android.app.ActionBar").getMethod("setHomeButtonEnabled", boolean.class).invoke(
                    getClass().getMethod("getActionBar").invoke(this), true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (processBA.runHook("oncreateoptionsmenu", this, new Object[] {menu}))
            return true;
		if (menuItems == null)
			return false;
		for (B4AMenuItem bmi : menuItems) {
			android.view.MenuItem mi = menu.add(bmi.title);
			if (bmi.drawable != null)
				mi.setIcon(bmi.drawable);
            if (android.os.Build.VERSION.SDK_INT >= 11) {
				try {
                    if (bmi.addToBar) {
				        android.view.MenuItem.class.getMethod("setShowAsAction", int.class).invoke(mi, 1);
                    }
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			mi.setOnMenuItemClickListener(new B4AMenuItemsClickListener(bmi.eventName.toLowerCase(BA.cul)));
		}
        
		return true;
	}   
 @Override
 public boolean onOptionsItemSelected(android.view.MenuItem item) {
    if (item.getItemId() == 16908332) {
        processBA.raiseEvent(null, "activity_actionbarhomeclick");
        return true;
    }
    else
        return super.onOptionsItemSelected(item); 
}
@Override
 public boolean onPrepareOptionsMenu(android.view.Menu menu) {
    super.onPrepareOptionsMenu(menu);
    processBA.runHook("onprepareoptionsmenu", this, new Object[] {menu});
    return true;
    
 }
 protected void onStart() {
    super.onStart();
    processBA.runHook("onstart", this, null);
}
 protected void onStop() {
    super.onStop();
    processBA.runHook("onstop", this, null);
}
    public void onWindowFocusChanged(boolean hasFocus) {
       super.onWindowFocusChanged(hasFocus);
       if (processBA.subExists("activity_windowfocuschanged"))
           processBA.raiseEvent2(null, true, "activity_windowfocuschanged", false, hasFocus);
    }
	private class B4AMenuItemsClickListener implements android.view.MenuItem.OnMenuItemClickListener {
		private final String eventName;
		public B4AMenuItemsClickListener(String eventName) {
			this.eventName = eventName;
		}
		public boolean onMenuItemClick(android.view.MenuItem item) {
			processBA.raiseEvent(item.getTitle(), eventName + "_click");
			return true;
		}
	}
    public static Class<?> getObject() {
		return main.class;
	}
    private Boolean onKeySubExist = null;
    private Boolean onKeyUpSubExist = null;
	@Override
	public boolean onKeyDown(int keyCode, android.view.KeyEvent event) {
        if (processBA.runHook("onkeydown", this, new Object[] {keyCode, event}))
            return true;
		if (onKeySubExist == null)
			onKeySubExist = processBA.subExists("activity_keypress");
		if (onKeySubExist) {
			if (keyCode == anywheresoftware.b4a.keywords.constants.KeyCodes.KEYCODE_BACK &&
					android.os.Build.VERSION.SDK_INT >= 18) {
				HandleKeyDelayed hk = new HandleKeyDelayed();
				hk.kc = keyCode;
				BA.handler.post(hk);
				return true;
			}
			else {
				boolean res = new HandleKeyDelayed().runDirectly(keyCode);
				if (res)
					return true;
			}
		}
		return super.onKeyDown(keyCode, event);
	}
	private class HandleKeyDelayed implements Runnable {
		int kc;
		public void run() {
			runDirectly(kc);
		}
		public boolean runDirectly(int keyCode) {
			Boolean res =  (Boolean)processBA.raiseEvent2(_activity, false, "activity_keypress", false, keyCode);
			if (res == null || res == true) {
                return true;
            }
            else if (keyCode == anywheresoftware.b4a.keywords.constants.KeyCodes.KEYCODE_BACK) {
				finish();
				return true;
			}
            return false;
		}
		
	}
    @Override
	public boolean onKeyUp(int keyCode, android.view.KeyEvent event) {
        if (processBA.runHook("onkeyup", this, new Object[] {keyCode, event}))
            return true;
		if (onKeyUpSubExist == null)
			onKeyUpSubExist = processBA.subExists("activity_keyup");
		if (onKeyUpSubExist) {
			Boolean res =  (Boolean)processBA.raiseEvent2(_activity, false, "activity_keyup", false, keyCode);
			if (res == null || res == true)
				return true;
		}
		return super.onKeyUp(keyCode, event);
	}
	@Override
	public void onNewIntent(android.content.Intent intent) {
        super.onNewIntent(intent);
		this.setIntent(intent);
        processBA.runHook("onnewintent", this, new Object[] {intent});
	}
    @Override 
	public void onPause() {
		super.onPause();
        if (_activity == null) //workaround for emulator bug (Issue 2423)
            return;
		anywheresoftware.b4a.Msgbox.dismiss(true);
        BA.LogInfo("** Activity (main) Pause, UserClosed = " + activityBA.activity.isFinishing() + " **");
        processBA.raiseEvent2(_activity, true, "activity_pause", false, activityBA.activity.isFinishing());		
        processBA.setActivityPaused(true);
        mostCurrent = null;
        if (!activityBA.activity.isFinishing())
			previousOne = new WeakReference<Activity>(this);
        anywheresoftware.b4a.Msgbox.isDismissing = false;
        processBA.runHook("onpause", this, null);
	}

	@Override
	public void onDestroy() {
        super.onDestroy();
		previousOne = null;
        processBA.runHook("ondestroy", this, null);
	}
    @Override 
	public void onResume() {
		super.onResume();
        mostCurrent = this;
        anywheresoftware.b4a.Msgbox.isDismissing = false;
        if (activityBA != null) { //will be null during activity create (which waits for AfterLayout).
        	ResumeMessage rm = new ResumeMessage(mostCurrent);
        	BA.handler.post(rm);
        }
        processBA.runHook("onresume", this, null);
	}
    private static class ResumeMessage implements Runnable {
    	private final WeakReference<Activity> activity;
    	public ResumeMessage(Activity activity) {
    		this.activity = new WeakReference<Activity>(activity);
    	}
		public void run() {
			if (mostCurrent == null || mostCurrent != activity.get())
				return;
			processBA.setActivityPaused(false);
            BA.LogInfo("** Activity (main) Resume **");
		    processBA.raiseEvent(mostCurrent._activity, "activity_resume", (Object[])null);
		}
    }
	@Override
	protected void onActivityResult(int requestCode, int resultCode,
	      android.content.Intent data) {
		processBA.onActivityResult(requestCode, resultCode, data);
        processBA.runHook("onactivityresult", this, new Object[] {requestCode, resultCode});
	}
	private static void initializeGlobals() {
		processBA.raiseEvent2(null, true, "globals", false, (Object[])null);
	}
    public void onRequestPermissionsResult(int requestCode,
        String permissions[], int[] grantResults) {
        Object[] o;
        if (permissions.length > 0)
            o = new Object[] {permissions[0], grantResults[0] == 0};
        else
            o = new Object[] {"", false};
        processBA.raiseEventFromDifferentThread(null,null, 0, "activity_permissionresult", true, o);
            
    }

public anywheresoftware.b4a.keywords.Common __c = null;
public anywheresoftware.b4a.objects.ButtonWrapper _button1 = null;
public anywheresoftware.b4a.objects.EditTextWrapper _edittext1 = null;
public anywheresoftware.b4a.objects.LabelWrapper _label1 = null;
public anywheresoftware.b4a.objects.ButtonWrapper _achauf = null;
public anywheresoftware.b4a.objects.ButtonWrapper _alumie = null;
public anywheresoftware.b4a.objects.ButtonWrapper _aprise = null;
public anywheresoftware.b4a.objects.ButtonWrapper _echauf = null;
public anywheresoftware.b4a.objects.ButtonWrapper _elumie = null;
public anywheresoftware.b4a.objects.ButtonWrapper _eprise = null;
public anywheresoftware.b4a.objects.ButtonWrapper _arrete = null;
public anywheresoftware.b4a.objects.ButtonWrapper _descen = null;
public anywheresoftware.b4a.objects.ButtonWrapper _montee = null;
public anywheresoftware.b4a.objects.LabelWrapper _messages = null;
public anywheresoftware.b4a.objects.LabelWrapper _donnee_capteur = null;
public anywheresoftware.b4a.objects.ButtonWrapper _acquerir_capteur = null;
public b4a.example.udp _udp = null;

public static boolean isAnyActivityVisible() {
    boolean vis = false;
vis = vis | (main.mostCurrent != null);
return vis;}
public static String  _achauf_click() throws Exception{
byte[] _emission = null;
anywheresoftware.b4a.agraham.byteconverter.ByteConverter _bc = null;
String _message = "";
 //BA.debugLineNum = 107;BA.debugLine="Sub AChauf_Click";
 //BA.debugLineNum = 108;BA.debugLine="Dim emission() As Byte";
_emission = new byte[(int) (0)];
;
 //BA.debugLineNum = 109;BA.debugLine="Dim bc As ByteConverter";
_bc = new anywheresoftware.b4a.agraham.byteconverter.ByteConverter();
 //BA.debugLineNum = 110;BA.debugLine="Dim message As String";
_message = "";
 //BA.debugLineNum = 111;BA.debugLine="message=\"AChauf\"";
_message = "AChauf";
 //BA.debugLineNum = 112;BA.debugLine="Messages.Text=\"Allumer Chauffage\"";
mostCurrent._messages.setText((Object)("Allumer Chauffage"));
 //BA.debugLineNum = 113;BA.debugLine="emission=bc.StringToBytes(message,\"ASCII\")";
_emission = _bc.StringToBytes(_message,"ASCII");
 //BA.debugLineNum = 114;BA.debugLine="UDP.emission(\"192.168.1.205\",5500,emission)";
mostCurrent._udp._emission(mostCurrent.activityBA,"192.168.1.205",(int) (5500),_emission);
 //BA.debugLineNum = 115;BA.debugLine="End Sub";
return "";
}
public static String  _acquerir_capteur_click() throws Exception{
byte[] _emission = null;
anywheresoftware.b4a.agraham.byteconverter.ByteConverter _bc = null;
String _message = "";
 //BA.debugLineNum = 147;BA.debugLine="Sub Acquerir_capteur_Click";
 //BA.debugLineNum = 148;BA.debugLine="Dim emission() As Byte";
_emission = new byte[(int) (0)];
;
 //BA.debugLineNum = 149;BA.debugLine="Dim bc As ByteConverter";
_bc = new anywheresoftware.b4a.agraham.byteconverter.ByteConverter();
 //BA.debugLineNum = 150;BA.debugLine="Dim message As String";
_message = "";
 //BA.debugLineNum = 151;BA.debugLine="message=\"Acquer\"";
_message = "Acquer";
 //BA.debugLineNum = 152;BA.debugLine="Messages.Text=\"Acquisition des capteurs\"";
mostCurrent._messages.setText((Object)("Acquisition des capteurs"));
 //BA.debugLineNum = 153;BA.debugLine="emission=bc.StringToBytes(message,\"ASCII\")";
_emission = _bc.StringToBytes(_message,"ASCII");
 //BA.debugLineNum = 154;BA.debugLine="UDP.emission(\"192.168.1.205\",5500,emission)";
mostCurrent._udp._emission(mostCurrent.activityBA,"192.168.1.205",(int) (5500),_emission);
 //BA.debugLineNum = 155;BA.debugLine="End Sub";
return "";
}
public static String  _activity_create(boolean _firsttime) throws Exception{
 //BA.debugLineNum = 36;BA.debugLine="Sub Activity_Create(FirstTime As Boolean)";
 //BA.debugLineNum = 37;BA.debugLine="Activity.LoadLayout(\"layout1\")";
mostCurrent._activity.LoadLayout("layout1",mostCurrent.activityBA);
 //BA.debugLineNum = 38;BA.debugLine="UDP.Initialise(3200)";
mostCurrent._udp._initialise(mostCurrent.activityBA,(int) (3200));
 //BA.debugLineNum = 40;BA.debugLine="End Sub";
return "";
}
public static String  _alumie_click() throws Exception{
byte[] _emission = null;
anywheresoftware.b4a.agraham.byteconverter.ByteConverter _bc = null;
String _message = "";
 //BA.debugLineNum = 98;BA.debugLine="Sub ALumie_Click";
 //BA.debugLineNum = 99;BA.debugLine="Dim emission() As Byte";
_emission = new byte[(int) (0)];
;
 //BA.debugLineNum = 100;BA.debugLine="Dim bc As ByteConverter";
_bc = new anywheresoftware.b4a.agraham.byteconverter.ByteConverter();
 //BA.debugLineNum = 101;BA.debugLine="Dim message As String";
_message = "";
 //BA.debugLineNum = 102;BA.debugLine="message=\"ALumie\"";
_message = "ALumie";
 //BA.debugLineNum = 103;BA.debugLine="Messages.Text=\"Allumer Lumiere\"";
mostCurrent._messages.setText((Object)("Allumer Lumiere"));
 //BA.debugLineNum = 104;BA.debugLine="emission=bc.StringToBytes(message,\"ASCII\")";
_emission = _bc.StringToBytes(_message,"ASCII");
 //BA.debugLineNum = 105;BA.debugLine="UDP.emission(\"192.168.1.205\",5500,emission)";
mostCurrent._udp._emission(mostCurrent.activityBA,"192.168.1.205",(int) (5500),_emission);
 //BA.debugLineNum = 106;BA.debugLine="End Sub";
return "";
}
public static String  _aprise_click() throws Exception{
byte[] _emission = null;
anywheresoftware.b4a.agraham.byteconverter.ByteConverter _bc = null;
String _message = "";
 //BA.debugLineNum = 89;BA.debugLine="Sub APrise_Click";
 //BA.debugLineNum = 90;BA.debugLine="Dim emission() As Byte";
_emission = new byte[(int) (0)];
;
 //BA.debugLineNum = 91;BA.debugLine="Dim bc As ByteConverter";
_bc = new anywheresoftware.b4a.agraham.byteconverter.ByteConverter();
 //BA.debugLineNum = 92;BA.debugLine="Dim message As String";
_message = "";
 //BA.debugLineNum = 93;BA.debugLine="message=\"APrise\"";
_message = "APrise";
 //BA.debugLineNum = 94;BA.debugLine="Messages.Text=\"Allumer Prise\"";
mostCurrent._messages.setText((Object)("Allumer Prise"));
 //BA.debugLineNum = 95;BA.debugLine="emission=bc.StringToBytes(message,\"ASCII\")";
_emission = _bc.StringToBytes(_message,"ASCII");
 //BA.debugLineNum = 96;BA.debugLine="UDP.emission(\"192.168.1.205\",5500,emission)";
mostCurrent._udp._emission(mostCurrent.activityBA,"192.168.1.205",(int) (5500),_emission);
 //BA.debugLineNum = 97;BA.debugLine="End Sub";
return "";
}
public static String  _arrete_click() throws Exception{
byte[] _emission = null;
anywheresoftware.b4a.agraham.byteconverter.ByteConverter _bc = null;
String _message = "";
 //BA.debugLineNum = 137;BA.debugLine="Sub Arrete_Click";
 //BA.debugLineNum = 138;BA.debugLine="Dim emission() As Byte";
_emission = new byte[(int) (0)];
;
 //BA.debugLineNum = 139;BA.debugLine="Dim bc As ByteConverter";
_bc = new anywheresoftware.b4a.agraham.byteconverter.ByteConverter();
 //BA.debugLineNum = 140;BA.debugLine="Dim message As String";
_message = "";
 //BA.debugLineNum = 141;BA.debugLine="message=\"Arrete\"";
_message = "Arrete";
 //BA.debugLineNum = 142;BA.debugLine="Messages.Text=\"Arreter le Store\"";
mostCurrent._messages.setText((Object)("Arreter le Store"));
 //BA.debugLineNum = 143;BA.debugLine="emission=bc.StringToBytes(message,\"ASCII\")";
_emission = _bc.StringToBytes(_message,"ASCII");
 //BA.debugLineNum = 144;BA.debugLine="UDP.emission(\"192.168.1.205\",5500,emission)";
mostCurrent._udp._emission(mostCurrent.activityBA,"192.168.1.205",(int) (5500),_emission);
 //BA.debugLineNum = 145;BA.debugLine="End Sub";
return "";
}
public static String  _button1_click() throws Exception{
byte[] _emission = null;
anywheresoftware.b4a.agraham.byteconverter.ByteConverter _bc = null;
String _message = "";
 //BA.debugLineNum = 42;BA.debugLine="Sub button1_click";
 //BA.debugLineNum = 43;BA.debugLine="Dim emission() As Byte";
_emission = new byte[(int) (0)];
;
 //BA.debugLineNum = 44;BA.debugLine="Dim bc As ByteConverter";
_bc = new anywheresoftware.b4a.agraham.byteconverter.ByteConverter();
 //BA.debugLineNum = 45;BA.debugLine="Dim message As String";
_message = "";
 //BA.debugLineNum = 46;BA.debugLine="message=EditText1.Text'\"EPrise\"";
_message = mostCurrent._edittext1.getText();
 //BA.debugLineNum = 47;BA.debugLine="Label1.Text=message";
mostCurrent._label1.setText((Object)(_message));
 //BA.debugLineNum = 48;BA.debugLine="emission=bc.StringToBytes(message,\"ASCII\")";
_emission = _bc.StringToBytes(_message,"ASCII");
 //BA.debugLineNum = 49;BA.debugLine="UDP.emission(\"192.168.1.205\",5500,emission)";
mostCurrent._udp._emission(mostCurrent.activityBA,"192.168.1.205",(int) (5500),_emission);
 //BA.debugLineNum = 50;BA.debugLine="End Sub";
return "";
}
public static String  _descen_click() throws Exception{
byte[] _emission = null;
anywheresoftware.b4a.agraham.byteconverter.ByteConverter _bc = null;
String _message = "";
 //BA.debugLineNum = 127;BA.debugLine="Sub Descen_Click";
 //BA.debugLineNum = 128;BA.debugLine="Dim emission() As Byte";
_emission = new byte[(int) (0)];
;
 //BA.debugLineNum = 129;BA.debugLine="Dim bc As ByteConverter";
_bc = new anywheresoftware.b4a.agraham.byteconverter.ByteConverter();
 //BA.debugLineNum = 130;BA.debugLine="Dim message As String";
_message = "";
 //BA.debugLineNum = 131;BA.debugLine="message=\"Descen\"";
_message = "Descen";
 //BA.debugLineNum = 132;BA.debugLine="Messages.Text=\"Descendre le Store\"";
mostCurrent._messages.setText((Object)("Descendre le Store"));
 //BA.debugLineNum = 133;BA.debugLine="emission=bc.StringToBytes(message,\"ASCII\")";
_emission = _bc.StringToBytes(_message,"ASCII");
 //BA.debugLineNum = 134;BA.debugLine="UDP.emission(\"192.168.1.205\",5500,emission)";
mostCurrent._udp._emission(mostCurrent.activityBA,"192.168.1.205",(int) (5500),_emission);
 //BA.debugLineNum = 135;BA.debugLine="End Sub";
return "";
}
public static String  _echauf_click() throws Exception{
byte[] _emission = null;
anywheresoftware.b4a.agraham.byteconverter.ByteConverter _bc = null;
String _message = "";
 //BA.debugLineNum = 80;BA.debugLine="Sub EChauf_Click";
 //BA.debugLineNum = 81;BA.debugLine="Dim emission() As Byte";
_emission = new byte[(int) (0)];
;
 //BA.debugLineNum = 82;BA.debugLine="Dim bc As ByteConverter";
_bc = new anywheresoftware.b4a.agraham.byteconverter.ByteConverter();
 //BA.debugLineNum = 83;BA.debugLine="Dim message As String";
_message = "";
 //BA.debugLineNum = 84;BA.debugLine="message=\"EChauf\"";
_message = "EChauf";
 //BA.debugLineNum = 85;BA.debugLine="Messages.Text=\"Eteindre Chauffage\"";
mostCurrent._messages.setText((Object)("Eteindre Chauffage"));
 //BA.debugLineNum = 86;BA.debugLine="emission=bc.StringToBytes(message,\"ASCII\")";
_emission = _bc.StringToBytes(_message,"ASCII");
 //BA.debugLineNum = 87;BA.debugLine="UDP.emission(\"192.168.1.205\",5500,emission)";
mostCurrent._udp._emission(mostCurrent.activityBA,"192.168.1.205",(int) (5500),_emission);
 //BA.debugLineNum = 88;BA.debugLine="End Sub";
return "";
}
public static String  _elumie_click() throws Exception{
byte[] _emission = null;
anywheresoftware.b4a.agraham.byteconverter.ByteConverter _bc = null;
String _message = "";
 //BA.debugLineNum = 71;BA.debugLine="Sub ELumie_Click";
 //BA.debugLineNum = 72;BA.debugLine="Dim emission() As Byte";
_emission = new byte[(int) (0)];
;
 //BA.debugLineNum = 73;BA.debugLine="Dim bc As ByteConverter";
_bc = new anywheresoftware.b4a.agraham.byteconverter.ByteConverter();
 //BA.debugLineNum = 74;BA.debugLine="Dim message As String";
_message = "";
 //BA.debugLineNum = 75;BA.debugLine="message=\"ELumie\"";
_message = "ELumie";
 //BA.debugLineNum = 76;BA.debugLine="Messages.Text=\"Eteindre Lumiere\"";
mostCurrent._messages.setText((Object)("Eteindre Lumiere"));
 //BA.debugLineNum = 77;BA.debugLine="emission=bc.StringToBytes(message,\"ASCII\")";
_emission = _bc.StringToBytes(_message,"ASCII");
 //BA.debugLineNum = 78;BA.debugLine="UDP.emission(\"192.168.1.205\",5500,emission)";
mostCurrent._udp._emission(mostCurrent.activityBA,"192.168.1.205",(int) (5500),_emission);
 //BA.debugLineNum = 79;BA.debugLine="End Sub";
return "";
}
public static String  _eprise_click() throws Exception{
byte[] _emission = null;
anywheresoftware.b4a.agraham.byteconverter.ByteConverter _bc = null;
String _message = "";
 //BA.debugLineNum = 62;BA.debugLine="Sub EPrise_Click";
 //BA.debugLineNum = 63;BA.debugLine="Dim emission() As Byte";
_emission = new byte[(int) (0)];
;
 //BA.debugLineNum = 64;BA.debugLine="Dim bc As ByteConverter";
_bc = new anywheresoftware.b4a.agraham.byteconverter.ByteConverter();
 //BA.debugLineNum = 65;BA.debugLine="Dim message As String";
_message = "";
 //BA.debugLineNum = 66;BA.debugLine="message=\"EPrise\"";
_message = "EPrise";
 //BA.debugLineNum = 67;BA.debugLine="Messages.Text=\"Eteindre Prise\"";
mostCurrent._messages.setText((Object)("Eteindre Prise"));
 //BA.debugLineNum = 68;BA.debugLine="emission=bc.StringToBytes(message,\"ASCII\")";
_emission = _bc.StringToBytes(_message,"ASCII");
 //BA.debugLineNum = 69;BA.debugLine="UDP.emission(\"192.168.1.205\",5500,emission)";
mostCurrent._udp._emission(mostCurrent.activityBA,"192.168.1.205",(int) (5500),_emission);
 //BA.debugLineNum = 70;BA.debugLine="End Sub";
return "";
}
public static String  _globals() throws Exception{
 //BA.debugLineNum = 17;BA.debugLine="Sub Globals";
 //BA.debugLineNum = 18;BA.debugLine="Dim button1 As Button";
mostCurrent._button1 = new anywheresoftware.b4a.objects.ButtonWrapper();
 //BA.debugLineNum = 19;BA.debugLine="Dim EditText1 As EditText";
mostCurrent._edittext1 = new anywheresoftware.b4a.objects.EditTextWrapper();
 //BA.debugLineNum = 20;BA.debugLine="Dim Label1 As Label";
mostCurrent._label1 = new anywheresoftware.b4a.objects.LabelWrapper();
 //BA.debugLineNum = 21;BA.debugLine="Private AChauf As Button";
mostCurrent._achauf = new anywheresoftware.b4a.objects.ButtonWrapper();
 //BA.debugLineNum = 22;BA.debugLine="Private ALumie As Button";
mostCurrent._alumie = new anywheresoftware.b4a.objects.ButtonWrapper();
 //BA.debugLineNum = 23;BA.debugLine="Private APrise As Button";
mostCurrent._aprise = new anywheresoftware.b4a.objects.ButtonWrapper();
 //BA.debugLineNum = 24;BA.debugLine="Private EChauf As Button";
mostCurrent._echauf = new anywheresoftware.b4a.objects.ButtonWrapper();
 //BA.debugLineNum = 25;BA.debugLine="Private ELumie As Button";
mostCurrent._elumie = new anywheresoftware.b4a.objects.ButtonWrapper();
 //BA.debugLineNum = 26;BA.debugLine="Private EPrise As Button";
mostCurrent._eprise = new anywheresoftware.b4a.objects.ButtonWrapper();
 //BA.debugLineNum = 27;BA.debugLine="Private Arrete As Button";
mostCurrent._arrete = new anywheresoftware.b4a.objects.ButtonWrapper();
 //BA.debugLineNum = 28;BA.debugLine="Private Descen As Button";
mostCurrent._descen = new anywheresoftware.b4a.objects.ButtonWrapper();
 //BA.debugLineNum = 29;BA.debugLine="Private Montee As Button";
mostCurrent._montee = new anywheresoftware.b4a.objects.ButtonWrapper();
 //BA.debugLineNum = 30;BA.debugLine="Private Messages As Label";
mostCurrent._messages = new anywheresoftware.b4a.objects.LabelWrapper();
 //BA.debugLineNum = 31;BA.debugLine="Private Donnee_capteur As Label";
mostCurrent._donnee_capteur = new anywheresoftware.b4a.objects.LabelWrapper();
 //BA.debugLineNum = 32;BA.debugLine="Private Acquerir_capteur As Button";
mostCurrent._acquerir_capteur = new anywheresoftware.b4a.objects.ButtonWrapper();
 //BA.debugLineNum = 34;BA.debugLine="End Sub";
return "";
}
public static String  _montee_click() throws Exception{
byte[] _emission = null;
anywheresoftware.b4a.agraham.byteconverter.ByteConverter _bc = null;
String _message = "";
 //BA.debugLineNum = 117;BA.debugLine="Sub Montee_Click";
 //BA.debugLineNum = 118;BA.debugLine="Dim emission() As Byte";
_emission = new byte[(int) (0)];
;
 //BA.debugLineNum = 119;BA.debugLine="Dim bc As ByteConverter";
_bc = new anywheresoftware.b4a.agraham.byteconverter.ByteConverter();
 //BA.debugLineNum = 120;BA.debugLine="Dim message As String";
_message = "";
 //BA.debugLineNum = 121;BA.debugLine="message=\"Montee\"";
_message = "Montee";
 //BA.debugLineNum = 122;BA.debugLine="Messages.Text=\"Monter le Store\"";
mostCurrent._messages.setText((Object)("Monter le Store"));
 //BA.debugLineNum = 123;BA.debugLine="emission=bc.StringToBytes(message,\"ASCII\")";
_emission = _bc.StringToBytes(_message,"ASCII");
 //BA.debugLineNum = 124;BA.debugLine="UDP.emission(\"192.168.1.205\",5500,emission)";
mostCurrent._udp._emission(mostCurrent.activityBA,"192.168.1.205",(int) (5500),_emission);
 //BA.debugLineNum = 125;BA.debugLine="End Sub";
return "";
}

public static void initializeProcessGlobals() {
    
    if (main.processGlobalsRun == false) {
	    main.processGlobalsRun = true;
		try {
		        b4a.example.udp._process_globals();
main._process_globals();
		
        } catch (Exception e) {
			throw new RuntimeException(e);
		}
    }
}public static String  _process_globals() throws Exception{
 //BA.debugLineNum = 11;BA.debugLine="Sub Process_Globals";
 //BA.debugLineNum = 15;BA.debugLine="End Sub";
return "";
}
public static String  _udp_packetarrived(anywheresoftware.b4a.objects.SocketWrapper.UDPSocket.UDPPacket _packet) throws Exception{
String _message = "";
 //BA.debugLineNum = 52;BA.debugLine="Sub UDP_PacketArrived (packet As UDPPacket)";
 //BA.debugLineNum = 53;BA.debugLine="Dim message As String";
_message = "";
 //BA.debugLineNum = 54;BA.debugLine="message=UDP.reception(packet)";
_message = mostCurrent._udp._reception(mostCurrent.activityBA,_packet);
 //BA.debugLineNum = 55;BA.debugLine="If message=\"acknowledged\" Then";
if ((_message).equals("acknowledged")) { 
 //BA.debugLineNum = 56;BA.debugLine="Label1.Text=message";
mostCurrent._label1.setText((Object)(_message));
 }else {
 //BA.debugLineNum = 58;BA.debugLine="Donnee_capteur.Text=message";
mostCurrent._donnee_capteur.setText((Object)(_message));
 };
 //BA.debugLineNum = 61;BA.debugLine="End Sub";
return "";
}
}
