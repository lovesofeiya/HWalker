package upenn.edu.cis542.stepcalculator;

import java.util.ArrayList;
import java.util.Set;

import android.app.AlertDialog;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class CalculationMain extends FragmentActivity implements OnItemClickListener{
	//for debug
	String tag = "bluetoothTest";
	
	//bluetooth
	ListView listView;
	private BluetoothAdapter BTAdapter; 
	protected int BT_ENABLE_RETURN = 2;
	
	ArrayAdapter<String> listAdapter;
	Set<BluetoothDevice> devicesArray;
	ArrayList<String> pairedDevices;
	
	IntentFilter filter;
	BroadcastReceiver receiver;
	
	/////////////////////
//    static String[] items = {"aa", "bb", "cc"};
//    static ListAdapter arrAdapter;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.calculation_main);
        
        initBluetoth();
        
    	//if the device does not support Bluetooth, simply return.
    	if (BTAdapter == null) 
    	{
    		BTErrorDialogFragment BTErrorDialog = new BTErrorDialogFragment();
    		BTErrorDialog.show(getSupportFragmentManager(), "No bluetooth");
    		//finish();
    	}
    	else 
    	{
    		if (!BTAdapter.isEnabled()) 
    		{ 
    			turnOnBT();
    			//Log.d("bluetoothTest", "3");
    		}
    		
    		getPairedBluetooth();
    		startDiscovery();
		} 
    }

    private void initBluetoth() {
	// TODO Auto-generated method stub
    	//check bluetooth
    	listView = (ListView)findViewById(R.id.listView_bluetooth);
    	listView.setOnItemClickListener(this);
    	listAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, 0);
    	listView.setAdapter(listAdapter);
    	
    	BTAdapter = BluetoothAdapter.getDefaultAdapter();
    	pairedDevices = new ArrayList<String>();
    	filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
    	
    	receiver = new BroadcastReceiver() {
    		public void onReceive(Context context, Intent intent) {
    		    String action = intent.getAction();
    		    // When discovery finds a device
    		    if (BluetoothDevice.ACTION_FOUND.equals(action)) {
    		    	BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
    		    	
    		    	String s = "";
  
	    			for(int j = 0; j < pairedDevices.size(); ++j)
	    			{
	    				if(device.getName().equals(pairedDevices.get(j)))
	    				{
	    					s = "(paired)";
	    					//Log.i(tag, "insert");	    					
	    					break;
	    				}	    				
	    			}
    		    	listAdapter.add(device.getName() + " " + s + " " + "\n" + device.getAddress()); 
		        }
    		    
    		    else if(BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action))
    		    {
    		    	
    		    }

    		    else if(BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action))
    		    {

    		    }
    		    
    		    else if(BluetoothAdapter.ACTION_STATE_CHANGED.equals(action))
    		    {
    		    	//when you run the app, someone turn off the bt
    		    	if(BTAdapter.getState() == BluetoothAdapter.STATE_OFF)
    		    	{
    		    		Log.d(tag, "bluetooth turned off");
    		    		turnOnBT();
    		    	}   		    		
    		    }
		    }
		};
		
		registerReceiver(receiver, filter);
		 filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
		registerReceiver(receiver, filter);
		 filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
		registerReceiver(receiver, filter);
		 filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
		registerReceiver(receiver, filter);
    }

	@Override
	protected void onPause()
	{
		//unregister the receiver
		super.onPause();
		unregisterReceiver(receiver);
	}
    
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.calculation_main, menu);
        return true;
    }
    
    //bluetooth
    public void startBluetooth(View view)
    {
 	
    }
    
    
    
    private void startDiscovery() {
		// TODO Auto-generated method stub
		BTAdapter.cancelDiscovery();
		BTAdapter.startDiscovery();
	}

	private void turnOnBT() {
		// TODO Auto-generated method stub
		Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE); 
		startActivityForResult(intent, 1);
	}

	private void getPairedBluetooth() {
		// TODO Auto-generated method stub
    	devicesArray = BTAdapter.getBondedDevices();
		// If there are paired devices
		if (devicesArray.size() > 0) {
		    // Loop through paired devices
		    for (BluetoothDevice device : devicesArray) {
		        // Add the name and address to an array adapter to show in a ListView
		        pairedDevices.add(device.getName());
		    }
		}
	}

    
    //after trying to open bluetooth
    //onActivityResult will be called before onResume, so we cannot show the dialog here
    //????should come back here later???
    protected void onActivityResult(int requestCode, int resultCode, Intent data) 
    {  
    	super.onActivityResult(requestCode, resultCode, data);
    	Log.d(tag, "" + resultCode);
    	if(resultCode == RESULT_CANCELED)
    	{
            showBTErrorDialog("Bluetooth Canceled!");
            //finish();
    	}
    	else if(resultCode == RESULT_OK)
    	{
    		getPairedBluetooth();
    		startDiscovery();
    	}
    	
    }


	public static class BTErrorDialogFragment extends DialogFragment {
	    String m_message;

	    static BTErrorDialogFragment newInstance(String message) {
	        BTErrorDialogFragment f = new BTErrorDialogFragment();

	        // Supply num input as an argument.
	        Bundle args = new Bundle();
	        args.putString("message", message);
	        f.setArguments(args);

	        return f;
	    }
		
		@Override
	    public Dialog onCreateDialog(Bundle savedInstanceState) {
	    	m_message = getArguments().getString("message");
			//Log.d("bluetoothTest", m_message);
	        // Use the Builder class for convenient dialog construction
	        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
	        builder.setMessage(m_message)
	               .setNegativeButton(R.string.btn_OK, new DialogInterface.OnClickListener() {
	                   public void onClick(DialogInterface dialog, int id) {
	                   }
	               });
	        // Create the AlertDialog object and return it
	        Dialog d = builder.create();
	        //d.setCanceledOnTouchOutside(false);
	        return d;
		}		 
	}
		
//	//custom dialog
//	public static class MyDialogFragment extends DialogFragment {
//	    String m_message;
//
//	    static MyDialogFragment newInstance(String message) {
//	        MyDialogFragment f = new MyDialogFragment();
//
//	        // Supply num input as an argument.
//	        Bundle args = new Bundle();
//	        args.putString("num", message);
//	        f.setArguments(args);
//
//	        return f;
//	    }
//
//	    @Override
//	    public Dialog onCreateDialog(Bundle savedInstanceState) {
//	    	m_message = getArguments().getString("num");	
//	    	
//	        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
//
//	        // Inflate and set the layout for the dialog
//	        // Pass null as the parent view because its going in the dialog layout
//	        builder.setPositiveButton(R.string.password, new DialogInterface.OnClickListener() {
//	                   @Override
//	                   public void onClick(DialogInterface dialog, int id) {
//	                       // sign in the user ...
//	                   }
//	               })
//	               .setNegativeButton(R.string.btn_OK, new DialogInterface.OnClickListener() {
//	                   public void onClick(DialogInterface dialog, int id) {
//	                       MyDialogFragment.this.getDialog().cancel();
//	                   }
//	               })
//	               .setMessage(m_message)
//	               .setAdapter(arrAdapter, new DialogInterface.OnClickListener() {
//	                   public void onClick(DialogInterface dialog, int id) {
//	                       // sign in the user ...
//	                   }
//	               })
//	               ;
//	               
//	        return builder.create();
//	    }
//	};  
          
	
	void showBTErrorDialog(String message) {

	    // DialogFragment.show() will take care of adding the fragment
	    // in a transaction.  We also want to remove any currently showing
	    // dialog, so make our own transaction and take care of that here.
	    FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
	    Fragment prev = getSupportFragmentManager().findFragmentByTag("dialog");
	    if (prev != null) {
	        ft.remove(prev);
	    }
	    ft.addToBackStack(null);

	    // Create and show the dialog.
	    BTErrorDialogFragment newFragment = BTErrorDialogFragment.newInstance(message);
	    ft.add(newFragment, "dialog");
		ft.commitAllowingStateLoss();
//	    newFragment.show(ft, "dialog");
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		// TODO Auto-generated method stub
		
	}
	
}
