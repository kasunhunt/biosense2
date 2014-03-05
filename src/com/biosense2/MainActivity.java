package com.biosense2;


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

    Handler mHandler;
	ArrayList<String> pairedDevices2;
	ArrayList<BluetoothDevice> devices;
    //!
	
	
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

                getDeviceList();

                broadcastRecieverInit();
                
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

    	Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                // Add the name and address to an array adapter to show in a ListView
                mArrayAdapter.add(device.getName() +" (Paired) "+ "\n" + device.getAddress());
            }
        }
	}

	@Override
    public void onDestroy() {
        super.onDestroy();

        if(receiverRegisted){
            unregisterReceiver(mReceiver);
        }
    }
    
	
	//*********************************REFRESH BUTTON***********************************
	public void refreshButtonClick(View view) {
	    // Do something in response to button
		mArrayAdapter.clear();
		//mArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
       // this.setListAdapter(mArrayAdapter);
		getDeviceList();
       // broadcastRecieverInit();
        startDiscovery();
		Log.i("DEBUG", "Refresh Button Method");
		//bluetoothInit();
		
	}
	//*********************************Item Click***********************************
	
	
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