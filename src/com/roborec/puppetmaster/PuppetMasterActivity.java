package com.roborec.puppetmaster;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

public class PuppetMasterActivity extends Activity
{
	public enum Operand
	{
		SERVO(1),
		GLOBAL(2);
		
		private int id;
		private static final Map<Integer, Operand> lookupId = new HashMap<Integer, Operand>();

		Operand(int id){
			this.id = id;
		}

		static {
			for(Operand d : EnumSet.allOf(Operand.class))
			{
				lookupId.put(d.getId(), d);
			}
		}

		public int getId(){
			return id;
		}

		public static Operand get(int id){
			return lookupId.get(id);
		}
	}
	
	 // Debugging
    private static final String TAG = "PuppetMaster";
    private static final boolean DEBUG = false;

    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;
  
    // Name of the connected device
    private String mConnectedDeviceName = null;
    private BluetoothAdapter mBluetoothAdapter = null;
    private BluetoothChatService mChatService = null;
	
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        
        if(DEBUG) 
        	Log.d(TAG, "+++ ON CREATE +++");
        
        setContentView(R.layout.main);
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    @Override
    public void onStart()
    {
        super.onStart();
        
        if(DEBUG) 
        	Log.d(TAG, "++ ON START ++");
        
        // If BT is not on, request that it be enabled.
        // setupChat() will then be called during onActivityResult
        if (!mBluetoothAdapter.isEnabled())
        {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        } 

        // Otherwise, setup the chat session
        else 
        {
            if (mChatService == null) 
            	setupChat();
        }
    }

    @Override
    public synchronized void onResume()
    {
        super.onResume();
        
        if(DEBUG)
        	Log.d(TAG, "+ ON RESUME +");

        // Performing this check in onResume() covers the case in which BT was
        // not enabled during onStart(), so we were paused to enable it...
        // onResume() will be called when ACTION_REQUEST_ENABLE activity returns.
        if (mChatService != null)
        {
            // Only if the state is STATE_NONE, do we know that we haven't started already
            if (mChatService.getState() == BluetoothChatService.STATE_NONE)
            {
            	mChatService.start();
            }
        }
    }

    private void setupChat()
    {
        if(DEBUG)
        	Log.d(TAG, "setupChat()");
        
        //@+id/bluetoothConnect
        //Button button = new B 
       /* ListView pairedListView = (ListView) findViewById(R.id.paired_devices);
        pairedListView.setAdapter(mPairedDevicesArrayAdapter);
        pairedListView.setOnItemClickListener(mDeviceClickListener);*/
        
        ImageButton bluetoothConnectButton = (ImageButton) findViewById(R.id.bluetoothConnect);
        bluetoothConnectButton.setOnClickListener(new View.OnClickListener(){
			public void onClick(View v)
			{
				try{
					Intent serverIntent = new Intent(PuppetMasterActivity.this, DeviceListActivity.class);
					startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
				}
				catch (Exception e) {
					Log.e(TAG, e.getMessage(), e);
				}
			}
		});
        
        // Initialize the BluetoothChatService to perform bluetooth connections
        mChatService = new BluetoothChatService(this, mHandler);
        
        WalkerView walkerView = (WalkerView)findViewById(R.id.walkerView1);
        walkerView.setBluetoothService(mChatService);
    }

    @Override
    public synchronized void onPause()
    {
        super.onPause();
        
        if(DEBUG) 
        	Log.d(TAG, "- ON PAUSE -");
    }

    @Override
    public void onStop() 
    {
        super.onStop();
        
        if(DEBUG) 
        	Log.d(TAG, "-- ON STOP --");
    }

    @Override
    public void onDestroy() 
    {
        super.onDestroy();

        if(DEBUG) 
        	Log.d(TAG, "--- ON DESTROY ---");
        
        if (mChatService != null) 
        	mChatService.stop();
    }

    /**
     * Sends a message.
     * @param message  A string of text to send.
     */
    public void sendMessage(String message) 
    {
        if(DEBUG) 
        	Log.e(TAG, "send: " + message);
        
        // Check that we're actually connected before trying anything
        if (mChatService.getState() == BluetoothChatService.STATE_CONNECTED) 
        {  
            if (message.length() > 0)
            {
                byte[] send = message.getBytes();
                mChatService.write(send);
            }
        }
        else
        {  
            Toast.makeText(this, R.string.not_connected, Toast.LENGTH_SHORT).show();
        }
    }

    private final void setStatus(int blueToothStatus)
    {
        ImageButton bluetoothConnectButton = (ImageButton) findViewById(R.id.bluetoothConnect);
    	switch (blueToothStatus) {
	        case BluetoothChatService.STATE_CONNECTED:
	        	bluetoothConnectButton.setImageResource(R.drawable.bt_connected_button);
	            break;
	        case BluetoothChatService.STATE_CONNECTING:
	        	bluetoothConnectButton.setImageResource(R.drawable.bt_button);
	            break;
	        case BluetoothChatService.STATE_LISTEN:
	        case BluetoothChatService.STATE_NONE:
	        	bluetoothConnectButton.setImageResource(R.drawable.bt_disconnect_button);
	            break;
    	}
    }
    
    // The Handler that gets information back from the BluetoothChatService
    private final Handler mHandler = new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {
            switch (msg.what)
            {
	            case BluetoothChatService.MESSAGE_STATE_CHANGE:
	                if(DEBUG) 
	                	Log.d(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
	                setStatus(msg.arg1);
	                break;
	                
	            case BluetoothChatService.MESSAGE_WRITE:
	                byte[] writeBuf = (byte[]) msg.obj;
	                String writeMessage = new String(writeBuf);
	                if(DEBUG) 
	                	Log.d(TAG, "write: " + writeMessage);
	                //mConversationArrayAdapter.add("Me:  " + writeMessage);
	                break;
	                
	            case BluetoothChatService.MESSAGE_READ:
	                byte[] readBuf = (byte[]) msg.obj;
	                String readMessage = new String(readBuf, 0, msg.arg1);
	                if(DEBUG) 
	                	Log.d(TAG, "read: " + readMessage);
	                //mConversationArrayAdapter.add(mConnectedDeviceName+":  " + readMessage);
	                break;
	                
	            case BluetoothChatService.MESSAGE_DEVICE_NAME:
	                // save the connected device's name
	                mConnectedDeviceName = msg.getData().getString(BluetoothChatService.DEVICE_NAME);
	                Toast.makeText(getApplicationContext(), "Connected to " + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
	                break;
	                
	            case BluetoothChatService.MESSAGE_TOAST:
	                Toast.makeText(getApplicationContext(), msg.getData().getString(BluetoothChatService.TOAST), Toast.LENGTH_SHORT).show();
	                break;
            }
        }
    };

    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if(DEBUG) 
        	Log.d(TAG, "onActivityResult " + resultCode);
        
        if (requestCode == REQUEST_CONNECT_DEVICE && resultCode == Activity.RESULT_OK) 
        {
            connectDevice(data, true);
        }
    }

    private void connectDevice(Intent data, boolean secure) 
    {
        // Get the device MAC address
        String address = data.getExtras().getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
        // Get the BluetoothDevice object
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        // Attempt to connect to the device
        mChatService.connect(device, secure);
    }
}