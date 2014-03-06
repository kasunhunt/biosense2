package com.biosense2;


import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;


import android.app.ListActivity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends ListActivity {
    private TextView stateMessage;
    private TextView displayData;
    private ListView listView;
    //private Button refreshButton;
    private BroadcastReceiver mReceiver;
    private BluetoothAdapter mBluetoothAdapter;
    private ArrayAdapter<String> mArrayAdapter;
    private boolean receiverRegisted = false;
    private final static int REQUEST_ENABLE_BT = 1;
    
    //!
    public static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
	protected static final int MESSAGE_READ  = 1;
	protected static final int SUCCESS_CONNECT = 0;

    //Handler mHandler;
	ArrayList<String> pairedDevices2;
	ArrayList<BluetoothDevice> devices;
	ConnectThread connect;
	BluetoothSocket mmSocketTwo;
	private ConnectedThread mConnectedThread;

    //!
	
	
	
	Handler mHandler = new Handler(){
		
    	@Override
    	public void handleMessage(Message msg) {

    		super.handleMessage(msg);
    		switch(msg.what){
    		case SUCCESS_CONNECT:
    			//Do something
    			mConnectedThread = new ConnectedThread((BluetoothSocket)msg.obj);
    			Toast.makeText(getApplicationContext(), "CONNECTED", 0).show();
    			String s = "SND"; //request monitoring
    			mConnectedThread.write(s.getBytes());
    			Log.i("DEBUG", "Bluetooth writtern"); 
    			break;
    		case MESSAGE_READ:
    			//byte[] readBuf = (byte[])msg.obj;
    			//String string = new String (readBuf);
    	        String readMessage = (String) msg.obj;
    	        displayData.setText(readMessage);
    			
    			
    			break;
    		}
    	}


    };
	
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bluetoothInit();   
    }

    private void bluetoothInit() {
    	stateMessage = (TextView)findViewById(R.id.state_message);
        stateMessage.setText(R.string.idle_message);
		
        mArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
        this.setListAdapter(mArrayAdapter);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (mBluetoothAdapter == null) {
            stateMessage.setText(R.string.btnotsupported_message);
        }
        else{
            if(!mBluetoothAdapter.isEnabled()) {
                stateMessage.setText(R.string.btoff_message);
                turnOnBT(); //bluetooth enable request
            }
        }
		
	}
    
    private void turnOnBT() {
		// TODO Auto-generated method stub
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
	}

	@Override 
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == REQUEST_ENABLE_BT){
            if(resultCode == RESULT_CANCELED){
                stateMessage.setText(R.string.requestcanceled_message);
            }
            else if (resultCode == RESULT_OK){
                stateMessage.setText(R.string.bton_message);
            	pairedDevices2 = new ArrayList<String>();
                broadcastRecieverInit();

                getDeviceList();
                startDiscovery();
                
            }
        }
    }

	
    private void startDiscovery() {
		// TODO Auto-generated method stub
		mBluetoothAdapter.cancelDiscovery();
		mBluetoothAdapter.startDiscovery();
	}
    
    
    private void broadcastRecieverInit() {
    	pairedDevices2 = new ArrayList<String>();
		devices = new ArrayList<BluetoothDevice>();
		devices.clear();
		// TODO Auto-generated method stub
    	mReceiver = new BroadcastReceiver(){ // Create a BroadcastReceiver for ACTION_FOUND
            public void onReceive(Context context, Intent intent){
                String action = intent.getAction();

                if (BluetoothDevice.ACTION_FOUND.equals(action)){ // When discovery finds a device
                    // Get the BluetoothDevice object from the Intent
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    // Add the name and address to an array adapter to show in a ListView
                    devices.add(device);
					String s = "";
					//mArrayAdapter.add(pairedDevices2.size()+"***");

						//for(int i = 0;i<listAdapter.getCount();i++){
							for (int a = 0;a<pairedDevices2.size();a++){
								if(device.getName().equals(pairedDevices2.get(a))){
									//append
									s = "(Paired)";
									break;
								}

							}
						//}
						//ex:matt_hp (paired)
					mArrayAdapter.add(device.getName()+" "+s+" "+"\n"+device.getAddress());
                }
                else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)){
					//run some code
				}
				else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)){

				}
				else if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)){
					if (mBluetoothAdapter.getState() == mBluetoothAdapter.STATE_OFF){
						turnOnBT();
					}
				}
            }
        };

        // Register the BroadcastReceiver
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mReceiver, filter); // Don't forget to unregister during onDestroy  
        receiverRegisted = true;
        mBluetoothAdapter.startDiscovery();
	}
    
    /*
     private void broadcastRecieverInit() {
		// TODO Auto-generated method stub
    	mReceiver = new BroadcastReceiver(){ // Create a BroadcastReceiver for ACTION_FOUND
            public void onReceive(Context context, Intent intent){
                String action = intent.getAction();

                if (BluetoothDevice.ACTION_FOUND.equals(action)){ // When discovery finds a device
                    // Get the BluetoothDevice object from the Intent
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    // Add the name and address to an array adapter to show in a ListView
                    mArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                }
            }
        };

        // Register the BroadcastReceiver
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mReceiver, filter); // Don't forget to unregister during onDestroy  
        receiverRegisted = true;
        mBluetoothAdapter.startDiscovery();
	}
	
	private void getDeviceList() {
		// TODO Auto-generated method stub
		Log.i("DEBUG", "Get Device List Method");

    	Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                // Add the name and address to an array adapter to show in a ListView
                mArrayAdapter.add(device.getName() + "\n" + device.getAddress());
            }
        }
	}
	*/

	private void getDeviceList() {
		// TODO Auto-generated method stub
		Log.i("DEBUG", "Get Device List Method");
		//int count =0;
    	Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
    	
        if (pairedDevices.size() > 0) {
        	
            for (BluetoothDevice device : pairedDevices) {
                // Add the name and address to an array adapter to show in a ListView
                //mArrayAdapter.add(device.getName() +" (Paired) "+ "\n" + device.getAddress());
        		Log.i("DEBUG", "Before");
        		pairedDevices2.add(device.getName());
            	
        		Log.i("DEBUG", "After");

            }
            
        }
	}

    
    /*
    private void getDeviceList() {
		// TODO Auto-generated method stub

    	Set<BluetoothDevice> devicesArray = mBluetoothAdapter.getBondedDevices();
		if (devicesArray.size()>0){
			for(BluetoothDevice device:devicesArray){
        		Log.i("DEBUG", "Before");

				pairedDevices2.add(device.getName());
        		Log.i("DEBUG", "After");

			}	
		}
	}
  */
	@Override
    public void onDestroy() {
        super.onDestroy();

        if(receiverRegisted){
            unregisterReceiver(mReceiver);
        }
    }
    
	
	//*********************************REFRESH BUTTON***********************************
	public void refreshButtonClick(View view) {
		
	    // Have to implement intent for checking if bluetooth is on or off
		mArrayAdapter.clear();
		//pairedDevices2.clear();
		//devices.clear();
		//mArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
        //this.setListAdapter(mArrayAdapter);
		broadcastRecieverInit();
		getDeviceList();
        startDiscovery();
		Log.i("DEBUG", "Refresh Button Method");
		//bluetoothInit();
		
	}
	
	//*********************************REFRESH BUTTON***********************************
		public void getButtonClick(View view) {
			
			Handler mHandler = new Handler(){
				
		    	@Override
		    	public void handleMessage(Message msg) {

		    		super.handleMessage(msg);
		    		switch(msg.what){
		    		case SUCCESS_CONNECT:
		    			//Do something
		    			ConnectedThread connectedThread = new ConnectedThread((BluetoothSocket)msg.obj);
		    			Toast.makeText(getApplicationContext(), "CONNECTED", 0).show();
		    			String s = "GET"; //request monitoring
		    			connectedThread.write(s.getBytes());
		    			Log.i("DEBUG", "connected"); 
		    			break;
		    		case MESSAGE_READ:
		    			//byte[] readBuf = (byte[])msg.obj;
		    			//String string = new String (readBuf);
		    	        String readMessage = (String) msg.obj;
		    	        
		    			displayData.setText(readMessage);
		    	        

		    		}
		    	}};
			
		}
	//*********************************Item Click***********************************
	/*
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		// TODO Auto-generated method stub
		
		super.onListItemClick(l, v, position, id);
		
		String cheese  = mArrayAdapter.getItem(position);
		Log.i("CHECK", cheese.substring(0, 5));
		if (cheese.substring(0, 5).equals("HC-05")){
			try{
			Class ourClass = Class.forName("com.biosense2.Monitor");
			Intent ourIntent = new Intent(MainActivity.this, ourClass);
			startActivity(ourIntent);
			}
			catch(ClassNotFoundException e){
				e.printStackTrace();
			}
		}
	}
	*/
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		// TODO Auto-generated method stub
		if(mBluetoothAdapter.isDiscovering()){
			mBluetoothAdapter.cancelDiscovery();
		}			
		Toast.makeText(getApplicationContext(), mArrayAdapter.getItem(position),0).show();
		try{
			Toast.makeText(getApplicationContext(), devices.get(position).toString(),0).show();
		}
		catch(NullPointerException er){
			er.printStackTrace();
		}
		if(mArrayAdapter.getItem(position).contains("Paired")){
			BluetoothDevice selectedDevice = devices.get(position);
			//Toast.makeText(getApplicationContext(), "device is paired",0).show();	
			Log.i("DEBUG","connection started");

			connect = new ConnectThread(selectedDevice);
			connect.start();
			
			//edit
			//ConnectedThread connected = new ConnectedThread(selectedDevice);
			
		}
		else {
			Toast.makeText(getApplicationContext(), "device is not paired",0).show();
		}
		
	}
	
	//*********************************Managing Bluetooth Connection***********************************
	
	private class ConnectThread extends Thread {
	    private final BluetoothSocket mmSocket;
	    private final BluetoothDevice mmDevice;
	 
	    public ConnectThread(BluetoothDevice device) {
	        // Use a temporary object that is later assigned to mmSocket,
	        // because mmSocket is final
	        BluetoothSocket tmp = null;
	        mmDevice = device;
	 
	        // Get a BluetoothSocket to connect with the given BluetoothDevice
	        try {
	            // MY_UUID is the app's UUID string, also used by the server code
	            tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
	        } catch (IOException e) { }
	        mmSocket = tmp;
	        
	      
	        
	        mmSocketTwo =tmp;  //HIGHLY EXPERIMENTAL 
	    }
	 
	    public void run() {
	        // Cancel discovery because it will slow down the connection
	        mBluetoothAdapter.cancelDiscovery();
	 
	        try {
	            // Connect the device through the socket. This will block
	            // until it succeeds or throws an exception
	            mmSocket.connect();
	        } catch (IOException connectException) {
	            // Unable to connect; close the socket and get out
	            try {
	                mmSocket.close();
	            } catch (IOException closeException) { }
	            return;
	        }
	 
	        // Do work to manage the connection (in a separate thread)
	       // manageConnectedSocket(mmSocket);
	        //!
	        mHandler.obtainMessage(SUCCESS_CONNECT,mmSocket).sendToTarget();
	        //connected(mmSocket);//edit
	        //!
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
	            tmpIn = socket.getInputStream();
	            tmpOut = socket.getOutputStream();
	        } catch (IOException e) { }
	 
	        mmInStream = tmpIn;
	        mmOutStream = tmpOut;
	    }
	 
	    public void run() {
	        byte[] buffer = new byte[1024];  // buffer store for the stream
	        int bytes; // bytes returned from read()
	 
	        // Keep listening to the InputStream until an exception occurs
	        while (true) {
	            try {
	                // Read from the InputStream
	                bytes = mmInStream.read(buffer);
	                // Send the obtained bytes to the UI activity
	                mHandler.obtainMessage(MESSAGE_READ, bytes, -1, buffer)
	                        .sendToTarget();
	            } catch (IOException e) {
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
	
	
	//****************************Handler****************
}