package scu.cd.sc;
/**
 * 搜索设备显示类
 */
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import scu.cd.sc.R;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
public class DeviceListActivity extends Activity {
    // Debugging
    private static final String TAG = "DeviceListActivity";
    private static final boolean D = true;
    private static final int COMPUTER = 256;
    private static final int PHONE = 512;

    // Return Intent extra
    public static String EXTRA_DEVICE_ADDRESS = "device_address";

    // Member fields
    private BluetoothAdapter mBtAdapter;
  
    private SimpleAdapter mPairedDevicesSimpleAdapter;
    private SimpleAdapter mNewDevicesSimpleAdapter;
    private  List<Map<String, Object>> data ;
    private  List<Map<String, Object>> dataNew ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Setup the window
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.device_list);

        // Set result CANCELED incase the user backs out
        setResult(Activity.RESULT_CANCELED);

        // Initialize the button to perform device discovery
        Button scanButton = (Button) findViewById(R.id.button_scan);
        scanButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                doDiscovery();
                v.setVisibility(View.GONE);
            }
        });

        // Initialize array adapters. One for already paired devices and
        // one for newly discovered devices
        data = new ArrayList<Map<String,Object>>();
        dataNew = new ArrayList<Map<String,Object>>();
		mPairedDevicesSimpleAdapter = new SimpleAdapter(this, data , R.layout.device_name, 
				new String []{"blueName","blueMac","iconId"}, new int []{R.id.blueName,R.id.blueMac,R.id.pc_phone_image});
		mNewDevicesSimpleAdapter = new SimpleAdapter(this, dataNew , R.layout.device_name, 
				new String []{"blueName","blueMac","iconId"}, new int []{R.id.blueName,R.id.blueMac,R.id.pc_phone_image});

        // Find and set up the ListView for paired devices
        ListView pairedListView = (ListView) findViewById(R.id.paired_devices);
        pairedListView.setAdapter(mPairedDevicesSimpleAdapter);
        pairedListView.setOnItemClickListener(mDeviceClickListener);

        // Find and set up the ListView for newly discovered devices
        ListView newDevicesListView = (ListView) findViewById(R.id.new_devices);
        newDevicesListView.setAdapter(mNewDevicesSimpleAdapter);
        newDevicesListView.setOnItemClickListener(mDeviceClickListener);

        // Register for broadcasts when a device is discovered
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        this.registerReceiver(mReceiver, filter);

        // Register for broadcasts when discovery has finished
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        this.registerReceiver(mReceiver, filter);

        // Get the local Bluetooth adapter
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();

        // Get a set of currently paired devices
        Set<BluetoothDevice> pairedDevices = mBtAdapter.getBondedDevices();

        // If there are paired devices, add each one to the ArrayAdapter
        if (pairedDevices.size() > 0) {
            findViewById(R.id.title_paired_devices).setVisibility(View.VISIBLE);
            for (BluetoothDevice device : pairedDevices) {
            	Map<String, Object> map1 = new HashMap<String, Object>();
            	map1.put("blueName",device.getName());
            	map1.put("blueMac",device.getAddress());
            	int deviceNum = device.getBluetoothClass().getMajorDeviceClass();
            	if(deviceNum==PHONE){
	            	map1.put("iconId", R.drawable.mobile_phone);
            	}
            	else{
            		map1.put("iconId", R.drawable.pc);
            	}
            	data.add(map1);
            	mPairedDevicesSimpleAdapter.notifyDataSetChanged();
            }
        } else {
            String noDevices = getResources().getText(R.string.none_paired).toString();
            Map<String, Object> map1 = new HashMap<String, Object>();
            map1.put("blueName",noDevices);
            map1.put("blueMac","null");
        	map1.put("iconId", R.drawable.null_image);
        	data.add(map1);
        	mPairedDevicesSimpleAdapter.notifyDataSetChanged();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Make sure we're not doing discovery anymore
        if (mBtAdapter != null) {
            mBtAdapter.cancelDiscovery();
        }
        // Unregister broadcast listeners
        this.unregisterReceiver(mReceiver);
    }

    /**
     * Start device discover with the BluetoothAdapter
     */
    private void doDiscovery() {
        if (D) Log.d(TAG, "doDiscovery()");
        // Indicate scanning in the title
        setProgressBarIndeterminateVisibility(true);
        setTitle(R.string.scanning);
        // Turn on sub-title for new devices
        findViewById(R.id.title_new_devices).setVisibility(View.VISIBLE);

        // If we're already discovering, stop it
        if (mBtAdapter.isDiscovering()) {
            mBtAdapter.cancelDiscovery();
        }

        // Request discover from BluetoothAdapter
        mBtAdapter.startDiscovery();
    }

    // The on-click listener for all devices in the ListViews
    private OnItemClickListener mDeviceClickListener = new OnItemClickListener() {
        public void onItemClick(AdapterView<?> parent, View v, int position, long arg3) {
            // Cancel discovery because it's costly and we're about to connect
            mBtAdapter.cancelDiscovery();

            // Get the device MAC address
            TextView textView = (TextView)v.findViewById(R.id.blueMac);
            String info = textView.getText().toString();
            // Create the result Intent and include the MAC address
            Intent intent = new Intent();
            intent.putExtra(EXTRA_DEVICE_ADDRESS, info);
            // Set result and finish this Activity
            setResult(Activity.RESULT_OK, intent);
            finish();
        }
    };

    // The BroadcastReceiver that listens for discovered devices and
    // changes the title when discovery is finished
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // If it's already paired, skip it, because it's been listed already
                int deviceNum = device.getBluetoothClass().getMajorDeviceClass();
                if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                	Map<String, Object> map1 = new HashMap<String, Object>();
                	map1.put("blueName",device.getName());
	            	map1.put("blueMac",device.getAddress());
                	if(deviceNum==COMPUTER){
    	            	map1.put("iconId", R.drawable.pc);
                	}
                	else{
                		map1.put("iconId", R.drawable.mobile_phone);
                	}
                	dataNew.add(map1);
                	mNewDevicesSimpleAdapter.notifyDataSetChanged();
                }
            // When discovery is finished, change the Activity title
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                setProgressBarIndeterminateVisibility(false);
                setTitle(R.string.select_device);
                if (mNewDevicesSimpleAdapter.getCount() == 0) {
                    String noDevices = getResources().getText(R.string.none_found).toString();
                    Map<String, Object> map1 = new HashMap<String, Object>();
	            	map1.put("blueName",noDevices);
	            	map1.put("blueMac","null");
	            	map1.put("iconId", R.drawable.null_image);
                	dataNew.add(map1);
                	mNewDevicesSimpleAdapter.notifyDataSetChanged();
                }
            }
        }
    };

}
