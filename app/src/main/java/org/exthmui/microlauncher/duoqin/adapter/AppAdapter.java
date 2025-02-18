package org.exthmui.microlauncher.duoqin.adapter;

import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;

import es.dmoral.toasty.Toasty;
import org.exthmui.microlauncher.duoqin.BuildConfig;
import org.exthmui.microlauncher.duoqin.misc.Application;
import org.exthmui.microlauncher.duoqin.R;

import java.util.List;
import java.util.Objects;

public class AppAdapter extends RecyclerView.Adapter<AppAdapter.ApplicationViewHolder> {
    private final List<Application> mApplicationList;
    private static View mItemView;
    private final int mLayoutMode;
    private static int mPosition = -1;

    private OnItemCallback onItemCallBack;

    /**
     * @param applicationList: 类型为Application的List集
     * @param layoutMode:      0，线性；1：网格
     */
    public AppAdapter(List<Application> applicationList, int layoutMode ) {
        this.mApplicationList = applicationList;
        this.mLayoutMode = layoutMode;
    }

    @NonNull
    @Override
    public ApplicationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        switch (this.mLayoutMode) {
            case 0:
                mItemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.app_list_item_linear, parent, false);
                break;
            case 1:
                mItemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.app_list_item_grid, parent, false);
                break;
        }
        return new ApplicationViewHolder(mItemView,this.mApplicationList);
    }

    @Override
    public void onBindViewHolder(@NonNull ApplicationViewHolder holder, int position) {
        Application application = this.mApplicationList.get(position);
        holder.mAppIconView.setImageDrawable(application.getAppIcon());
        holder.mText.setText(application.getAppLabel());
        holder.mAppItem.setOnClickListener(new AppClick(position));
        holder.mAppItem.setOnFocusChangeListener(new FocusChange(position,holder));

//      设置单击监听事件
        holder.mText.setOnClickListener(v -> {
            application.getAppIntent().addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            v.getContext().startActivity(application.getAppIntent());
        });
        holder.itemView.setOnClickListener(v -> {
            application.getAppIntent().addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            v.getContext().startActivity(application.getAppIntent());
        });

//      设置长按监听事件
        holder.mText.setOnLongClickListener(v -> {
            mPosition = holder.getAdapterPosition();
            Log.e("Adapter","mPosition="+mPosition);
            return false;
        });
        holder.itemView.setOnLongClickListener(v -> {
            mPosition = holder.getAdapterPosition();
            Log.e("Adapter","mPosition="+mPosition);
            return false;
        });


    }

    @Override
    public int getItemCount() {
        return mApplicationList == null ? 0 : mApplicationList.size();
    }

    public static class ApplicationViewHolder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener, MenuItem.OnMenuItemClickListener {

        private final ImageView mAppIconView;
        private final TextView mText;
        private final RelativeLayout mAppItem;
        private final List<Application> mApplicationList;

        public ApplicationViewHolder(@NonNull View view,List<Application> applicationList) {
            super(view);
            this.mAppIconView = view.findViewById(R.id.app_icon);
            this.mText = view.findViewById(R.id.app_title);
            this.mAppItem = view.findViewById(R.id.app_item);
            this.mApplicationList = applicationList;
            view.setOnCreateContextMenuListener(this);
        }

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
            menu.setHeaderTitle(this.mText.getText());
            menu.add(0, 0, Menu.NONE, R.string.app_menu_open);
            menu.add(0, 1, Menu.NONE,  R.string.app_menu_uninstall);
            menu.add(0, 2, Menu.NONE,  R.string.app_menu_info);
            MenuItem item1 = menu.findItem(0);
            MenuItem item2 = menu.findItem(1);
            MenuItem item3 = menu.findItem(2);
            item1.setOnMenuItemClickListener(this);
            item2.setOnMenuItemClickListener(this);
            item3.setOnMenuItemClickListener(this);
        }

        @Override
        public boolean onMenuItemClick(MenuItem item) {
            Application application = this.mApplicationList.get(mPosition);
            Intent intent = new Intent();
            switch (item.getItemId()){
                case 0:
                    mItemView.getContext().startActivity(application.getAppIntent());
                    break;
                case 1:
                    if(application.isSystemApp()){
                        Snackbar.make(itemView,R.string.this_is_system_app,Snackbar.LENGTH_SHORT).show();
                    }else{
                        Uri uri = Uri.fromParts("package", application.getPkgName(), null);
                        if (application.isAppSelf()){
                            Toasty.error(mItemView.getContext(),R.string.abort_msg_uninstall_self, Toasty.LENGTH_LONG ).show();
                        }else{
                            intent = new Intent(Intent.ACTION_DELETE, uri);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            mItemView.getContext().startActivity(intent);
                        }
                    }
                    break;
                case 2:
                    intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    intent.setData(Uri.fromParts("package", application.getPkgName(), null));
                    mItemView.getContext().startActivity(intent);
                    break;
            }
            return false;
        }

    }

    private class FocusChange implements View.OnFocusChangeListener {
        int position;
        ApplicationViewHolder holder;

        public FocusChange(int position, ApplicationViewHolder holder){
            this.position = position;
            this.holder = holder;
        }

        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            if (hasFocus) {
                holder.mAppItem.animate().scaleX(1.1f).scaleY(1.1f).start();
                holder.mAppItem.setBackgroundResource(R.drawable.border_app_item);
            }else{
                holder.mAppItem.animate().scaleX(1f).scaleY(1f).start();
                holder.mAppItem.setBackgroundResource(R.color.no_color);
            }
            if (onItemCallBack != null){
                onItemCallBack.onFocusChange(v,hasFocus,position);
            }
        }
    }

    private class AppClick implements View.OnClickListener {
        int position;

        public AppClick(int position){
            this.position = position;
        }

        @Override
        public void onClick(View v) {
            if (onItemCallBack != null){
                onItemCallBack.onItemClick(v,position);
            }
        }
    }

    public void setOnItemClickCallback(OnItemCallback onItemCallback){
        this.onItemCallBack = onItemCallback;
    }

}
