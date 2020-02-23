package com.example.finalemucloud;

import android.content.Context;
import android.content.pm.PackageManager;
import android.media.Image;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class EmulatorAdapter extends RecyclerView.Adapter<EmulatorAdapter.EmulatorHolder> {

    private List<Emulator> emulators = new ArrayList<>();
    private OnItemClickListener listener;


    @NonNull
    @Override
    public EmulatorHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.new_emulator_item, parent, false);
        return new EmulatorHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull EmulatorHolder holder, int position) {
        Emulator currentEmulator = emulators.get(position);
        holder.textViewTitle.setText(currentEmulator.getTitle());

        Context context = holder.itemView.getContext();
        PackageManager pm = context.getPackageManager();
        InitialScan initialScan = new InitialScan(pm);

        holder.imageViewIcon.setImageDrawable(initialScan.findIcon(currentEmulator, context));

    }


    @Override
    public int getItemCount() {
        return emulators.size();
    }

    public void setEmulators(List<Emulator> emulators) {
        this.emulators = emulators;
        notifyDataSetChanged();
    }

    public Emulator getEmulatorAt(int position) {
        return emulators.get(position);
    }

    class EmulatorHolder extends RecyclerView.ViewHolder {
        private ImageView imageViewIcon;
        private TextView textViewTitle;
        private ImageView imageViewSettings;
        private Button buttonBackup;
        private Button buttonRestore;

        public EmulatorHolder(@NonNull View itemView) {
            super(itemView);
            imageViewIcon = itemView.findViewById(R.id.image_view_icon);
            textViewTitle = itemView.findViewById(R.id.text_view_title);
            imageViewSettings = itemView.findViewById(R.id.image_view_end_gear);
            buttonBackup = itemView.findViewById(R.id.button_backup);
            buttonRestore = itemView.findViewById(R.id.button_restore);

            imageViewSettings.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = getAdapterPosition();
                    if (listener != null && position != RecyclerView.NO_POSITION) {
                        listener.onItemClick(emulators.get(position));
                    }
                }
            });
            buttonBackup.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = getAdapterPosition();
                    if (listener != null && position != RecyclerView.NO_POSITION) {
                        listener.onBackupClick(emulators.get(position));
                    }
                }
            });
            buttonRestore.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = getAdapterPosition();
                    if (listener != null && position != RecyclerView.NO_POSITION) {
                        listener.onRestoreClick(emulators.get(position));
                    }
                }
            });
        }
    }

    public interface OnItemClickListener {
        void onItemClick(Emulator emulator);
        void onBackupClick(Emulator emulator);
        void onRestoreClick(Emulator emulator);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }
}
