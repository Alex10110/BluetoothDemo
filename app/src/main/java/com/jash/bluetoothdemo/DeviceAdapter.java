package com.jash.bluetoothdemo;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Collection;
import java.util.List;

/**
 * Created by jash
 * Date: 15-10-20
 * Time: 下午3:00
 */
public class DeviceAdapter extends RecyclerView.Adapter<DeviceAdapter.ViewHolder> implements View.OnClickListener {
    private Context context;
    private List<BluetoothDevice> list;
    private OnItemClickListener listener;
    private RecyclerView recyclerView;

    public DeviceAdapter(Context context, List<BluetoothDevice> list) {
        this.context = context;
        this.list = list;
    }

    public void setListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(android.R.layout.simple_list_item_2, parent, false);
        view.setOnClickListener(this);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        BluetoothDevice device = list.get(position);
        holder.name.setText(device.getName());
        holder.address.setText(device.getAddress());
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public void addAll(Collection<? extends BluetoothDevice> collection){
        int size = list.size();
        list.addAll(collection);
        notifyItemRangeInserted(size, collection.size());
    }

    public void add(BluetoothDevice device){
        if (!list.contains(device)) {
            list.add(0, device);
            notifyItemInserted(0);
        }
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        this.recyclerView = recyclerView;
    }

    @Override
    public void onClick(View v) {
        int position = recyclerView.getChildAdapterPosition(v);
        if (listener != null) {
            listener.onItemClick(list.get(position));
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{

        private TextView name;
        private TextView address;

        public ViewHolder(View itemView) {
            super(itemView);
            name = ((TextView) itemView.findViewById(android.R.id.text1));
            address = ((TextView) itemView.findViewById(android.R.id.text2));
        }
    }
    public interface OnItemClickListener{
        void onItemClick(BluetoothDevice device);
    }
}
