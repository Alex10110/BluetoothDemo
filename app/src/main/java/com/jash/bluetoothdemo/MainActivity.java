package com.jash.bluetoothdemo;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import android.os.ParcelUuid;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.Toast;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements Handler.Callback, DeviceAdapter.OnItemClickListener {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final UUID ID = UUID.fromString("7840949e-74f5-4f99-9958-77744d6ca70a");
    private BluetoothAdapter bluetoothAdapter;
    private RecyclerView recycler;
    private DeviceAdapter adapter;
    private Handler handler = new Handler(this);
    private BluetoothReceiver receiver;
    private BluetoothServerSocket server;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        recycler = ((RecyclerView) findViewById(R.id.recycler));
        adapter = new DeviceAdapter(this, new ArrayList<BluetoothDevice>());
        adapter.setListener(this);
        recycler.setAdapter(adapter);
        //蓝牙适配器
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter != null) {
            if (!bluetoothAdapter.isEnabled()) {
//                if (bluetoothAdapter.enable()) {
//                    Toast.makeText(this, "蓝牙开启成功", Toast.LENGTH_SHORT).show();
//                } else {
//                    Toast.makeText(this, "蓝牙开启失败", Toast.LENGTH_SHORT).show();
//                }
                Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(intent, 0);
            } else {
                loadBluetoothInfo();
            }
        } else {
            Toast.makeText(this, "本设备没有蓝牙", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (resultCode){
            case RESULT_OK:
                Toast.makeText(this, "蓝牙开启成功", Toast.LENGTH_SHORT).show();
                loadBluetoothInfo();
                break;
            case RESULT_CANCELED:
                Toast.makeText(this, "蓝牙开启失败", Toast.LENGTH_SHORT).show();
                finish();
                break;
        }
    }
    private void loadBluetoothInfo(){
        String name = bluetoothAdapter.getName();
        String address = bluetoothAdapter.getAddress();
        Log.d(TAG, name + ":" + address);
        //曾经和本机配对过的蓝牙设备
        Set<BluetoothDevice> devices = bluetoothAdapter.getBondedDevices();
        adapter.addAll(devices);
        //开始扫描设备
        bluetoothAdapter.startDiscovery();
        //连接特定的蓝牙设备
//        BluetoothDevice device = bluetoothAdapter.getRemoteDevice("");
        receiver = new BluetoothReceiver(handler);
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(receiver, filter);
        try {
            server = bluetoothAdapter.listenUsingRfcommWithServiceRecord("", ID);
            new Thread(){
                @Override
                public void run() {
                    BluetoothSocket socket;
                    try {
                        while ((socket = server.accept()) != null){
                            BluetoothDevice device = socket.getRemoteDevice();
                            DataInputStream dis = new DataInputStream(socket.getInputStream());
                            String s = device.getName() + ":" + dis.readUTF();
                            dis.close();
                            handler.obtainMessage(1, s).sendToTarget();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (receiver != null) {
            unregisterReceiver(receiver);
        }
    }

    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what){
            case 0:
                BluetoothDevice device = msg.getData().getParcelable(BluetoothDevice.EXTRA_DEVICE);
                adapter.add(device);
                break;
            case 1:
                Toast.makeText(this, (String) msg.obj, Toast.LENGTH_SHORT).show();
                break;
        }
        return true;
    }

    @Override
    public void onItemClick(final BluetoothDevice device) {
//        ParcelUuid[] uuids = device.getUuids();
        switch (device.getBondState()){
            case BluetoothDevice.BOND_NONE:
                break;
            case BluetoothDevice.BOND_BONDING:
                break;
            case BluetoothDevice.BOND_BONDED:
                break;
        }
//        device.createBond();
        new Thread(){
            @Override
            public void run() {
                try {
                    BluetoothSocket socket = device.createRfcommSocketToServiceRecord(ID);
                    //建立连接
                    socket.connect();
                    DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
                    dos.writeUTF("蓝牙测试");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    public static class BluetoothReceiver extends BroadcastReceiver{
        private Handler handler;

        public BluetoothReceiver(Handler handler) {
            this.handler = handler;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            Message msg = handler.obtainMessage(0);
            msg.setData(intent.getExtras());
            msg.sendToTarget();
        }
    }
}
