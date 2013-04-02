package upenn.edu.cis542.stepcalculator;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

import android.app.AlertDialog;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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
import android.widget.TextView;
import android.widget.Toast;

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
	ArrayList<BluetoothDevice> devices;

	IntentFilter filter;
	BroadcastReceiver receiver;

    public static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

	protected static final int SUCCESS_CONNECT = 0;
	protected static final int MESSAGE_READ = 1;

	ConnectedThread connectedThread;
	ConnectThread connect;

	//test bluetooth
	TextView tv_contentTime;


    Handler mHandler = new Handler()
    {
    	public void handleMessage(Message message)
    	{
    		Log.d(tag, "handle message");
    		super.handleMessage(message);
    		switch(message.what)
    		{
    		case SUCCESS_CONNECT:
    			Log.d(tag, "start connect");
    			connectedThread = new ConnectedThread((BluetoothSocket)message.obj);
    			//Toast.makeText(getApplicationContext(), "connected", 0).show();
    			String s = "successfully connected";
    			connectedThread.write(s.getBytes());
    			Log.d(tag, "connected");
    			connectedThread.start();
    			break;
    		case MESSAGE_READ:
    			byte[] readBuf = (byte[])message.obj;
    			String string = new String(readBuf);
    			//Toast.makeText(getApplicationContext(), string, 0).show();
    			Log.d(tag, "read");
    			tv_contentTime.setText(string);
    			break;
    		}
    	}
    };

	/////////////////////
//    static String[] items = {"aa", "bb", "cc"};
//    static ListAdapter arrAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.calculation_main);
        
        tv_contentTime = (TextView)findViewById(R.id.content_time);
        
        initBluetoth();
        
    	//if the device does not support Bluetooth, simply return.
    	if (BTAdapter == null) 
    	{
    		showBTErrorDialog("No bluetooth");
    	}
    	else 
    	{
    		
    		if (!BTAdapter.isEnabled()) 
    		{ 
    			turnOnBT();
    			//Log.d("bluetoothTest", "3");
    		}
    		
    		startBTSearch();
		} 
    }

    private void startBTSearch() {
		// TODO Auto-generated method stub
		//clear data first
		pairedDevices.clear();
		listAdapter.clear();
		getPairedBluetooth();
		startDiscovery();
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
    	
    	devices = new ArrayList<BluetoothDevice>();
    	
    	receiver = new BroadcastReceiver() {
    		public void onReceive(Context context, Intent intent) {
    		    String action = intent.getAction();
    		    // When discovery finds a device
    		    if (BluetoothDevice.ACTION_FOUND.equals(action)) {
    		    	BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
    		    	devices.add(device);
    		    	String s = "";
  
	    			for(int j = 0; j < pairedDevices.size(); ++j)
	    			{
	    				if(device.getAddress().equals(pairedDevices.get(j)))
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
    		    		//Log.d(tag, "bluetooth turned off");
    		    		turnOnBT();
    		    	}   		    		
    		    }
		    }
		};

		new AlertDialog.Builder(this).setTitle("Bluetooth devices")
		.setIcon( android.R.drawable.ic_dialog_info).setSingleChoiceItems(
				listAdapter, 0,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();}
					}).setNegativeButton("Cancel", null).show();

		registerReceiver(receiver, filter);
		 filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
		registerReceiver(receiver, filter);
		 filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
		registerReceiver(receiver, filter);
		 filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
		registerReceiver(receiver, filter);
    }

	@Override
	protected void onDestroy()
	{
		//unregister the receiver
		super.onDestroy();
		unregisterReceiver(receiver);
		//shup down the connection
		if(connectedThread != null)
			connectedThread.cancel();
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
		        pairedDevices.add(device.getAddress());
		    }
		}
	}

    
    //after trying to open bluetooth
    //onActivityResult will be called before onResume, so we cannot show the dialog here
    //????should come back here later???
    protected void onActivityResult(int requestCode, int resultCode, Intent data) 
    {  
    	super.onActivityResult(requestCode, resultCode, data);
    	//Log.d(tag, "" + resultCode);
    	if(resultCode == RESULT_CANCELED)
    	{
            showBTErrorDialog("Bluetooth Canceled!");
            //finish();
    	}
    	else if(resultCode == RESULT_OK)
    	{
    		startBTSearch();
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

		if(BTAdapter.isDiscovering())
		{
			BTAdapter.cancelDiscovery();
		}

//		if(!listAdapter.getItem(arg2).contains("paired"))
//		{
			//Log.d(tag, "paired device is selected");
			BluetoothDevice selectedDevice = devices.get(arg2);
			connect = new ConnectThread(selectedDevice);
			connect.start();
//		}
//		else 
//		{
//			//Toast.makeText(getApplicationContext(), "device is not paired", 0).show();
//		}
	}


	private class ConnectThread extends Thread {

		private final BluetoothSocket mmSocket;
	    private final BluetoothDevice mmDevice;

	    public ConnectThread(BluetoothDevice device) {
	    	Log.d(tag, "build connect");
	        // Use a temporary object that is later assigned to mmSocket,
	        // because mmSocket is final
	        BluetoothSocket tmp = null;
	        mmDevice = device;

	        // Get a BluetoothSocket to connect with the given BluetoothDevice
	        try {
                Log.d(tag, "try0");
	            // MY_UUID is the app's UUID string, also used by the server code
	            tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
	        } catch (IOException e) { Log.d(tag, "catch0"); }
	        mmSocket = tmp;
	    }

	    public void run() {
	    	Log.d(tag, "run connect");
	        // Cancel discovery because it will slow down the connection
	        BTAdapter.cancelDiscovery();

	        try {
	            // Connect the device through the socket. This will block
	            // until it succeeds or throws an exception
	            Log.d(tag, "try1");
	            mmSocket.connect();

	        } catch (IOException connectException) {
	            // Unable to connect; close the socket and get out
	        	Log.d(tag, "catch1");
	            try {
	                Log.d(tag, "try2");
	                mmSocket.close();

	            } catch (IOException closeException) { Log.d(tag, "catch2");}
	            return;
	        }

	        // Do work to manage the connection (in a separate thread)
	        Log.d(tag, "about to handle");
	        mHandler.obtainMessage(SUCCESS_CONNECT, mmSocket).sendToTarget();
	    }


		/** Will cancel an in-progress connection, and close the socket */
	    public void cancel() {
	        try {
	            mmSocket.close();
	        } catch (IOException e) { }
	    }
	}

	private class ConnectedThread extends Thread {
	    private final BluetoothSocket mmSocket;
	    private final InputStream mmInStream;
	    private final OutputStream mmOutStream;

	    public ConnectedThread(BluetoothSocket socket) {
	        mmSocket = socket;
	        InputStream tmpIn = null;
	        OutputStream tmpOut = null;

	        // Get the input and output streams, using temp objects because
	        // member streams are final
	        try {
	        	Log.d(tag, "try connected");
	            tmpIn = socket.getInputStream();
	            tmpOut = socket.getOutputStream();
	        } catch (IOException e) { Log.d(tag, "catch connected"); }

	        mmInStream = tmpIn;
	        mmOutStream = tmpOut;
	    }

	    public void run() {
	        byte[] buffer;  // buffer store for the stream
	        int bytes; // bytes returned from read()

	        // Keep listening to the InputStream until an exception occurs
	        while (true) {
	            try {
	            	Log.d(tag, "try read");
	                // Read from the InputStream
	            	buffer = new byte[1024];
	            	Log.d(tag, "try read1");
	                bytes = mmInStream.read(buffer);
	                // Send the obtained bytes to the UI activity

	                mHandler.obtainMessage(MESSAGE_READ, bytes, -1, buffer)
	                        .sendToTarget();
	            } catch (IOException e) { Log.d(tag, "read catch");
	                break;
	            }
	        }
	    }

	    /* Call this from the main activity to send data to the remote device */
	    public void write(byte[] bytes) {
	        try {
	            mmOutStream.write(bytes);
	        } catch (IOException e) { }
	    }

	    /* Call this from the main activity to shutdown the connection */
	    public void cancel() {
	        try {
	            mmSocket.close();
	        } catch (IOException e) { }
	    }
	}
}
