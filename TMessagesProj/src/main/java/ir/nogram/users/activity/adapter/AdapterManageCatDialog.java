package ir.nogram.users.activity.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.List;

import ir.nogram.messanger.R;
import ir.nogram.users.activity.holder.HoldCatDialog;
import ir.nogram.users.activity.holder.HoldChangeContact;
import ir.nogram.users.activity.views.CustomTextView;

public class AdapterManageCatDialog extends RecyclerView.Adapter<AdapterManageCatDialog.MyViewHolder> {
    public List<HoldCatDialog> holdCatDialogs;
    Context context;

    public interface AdapterManageCatDialogDelegate{
        public void editClicked(HoldCatDialog catId) ;
        public void deldetClicked(HoldCatDialog catId) ;
        public void addClicked(HoldCatDialog catId) ;
    }
    AdapterManageCatDialogDelegate adapterManageCatDialogDelegate ;
    public AdapterManageCatDialog(List<HoldCatDialog> pr, Context context , AdapterManageCatDialogDelegate adapterManageCatDialogDelegate) {
        this.holdCatDialogs = pr;
        this.context = context;
        this.adapterManageCatDialogDelegate = adapterManageCatDialogDelegate ;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public CustomTextView ctxCatName;
        ViewGroup layoutRoot;
        ImageView imgEdit;
        ImageView imgAddDialogToCat;
        ImageView imgDeleteCat;

        public MyViewHolder(View view) {
            super(view);
            layoutRoot = (ViewGroup) view.findViewById(R.id.layoutRoot);
            ctxCatName = (CustomTextView) view.findViewById(R.id.ctxCatName);
            imgDeleteCat = (ImageView) view.findViewById(R.id.imgDeleteCat);
            imgEdit = (ImageView) view.findViewById(R.id.imgEdit);
            imgAddDialogToCat = (ImageView) view.findViewById(R.id.imgAddDialogToCat);
        }
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_manage_cat, parent, false);

        return new MyViewHolder(itemView);
    }

    public void add(int position, HoldCatDialog item) {
        holdCatDialogs.add(position, item);
        notifyItemInserted(position);
//		notifyDataSetChanged();
    }

    public void remove(HoldChangeContact item) {
        int position = holdCatDialogs.indexOf(item);
        holdCatDialogs.remove(position);
        notifyItemRemoved(position);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {

        final HoldCatDialog cat = holdCatDialogs.get(position);
        holder.ctxCatName.setText(cat.catName);
        holder.layoutRoot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        holder.imgAddDialogToCat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                adapterManageCatDialogDelegate.addClicked(cat);
            }
        });
        holder.imgDeleteCat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
             adapterManageCatDialogDelegate.deldetClicked(cat);
            }
        });
        holder.imgEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                adapterManageCatDialogDelegate.editClicked(cat);
            }
        });
    }


    @Override
    public int getItemCount() {
        return holdCatDialogs.size();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }
}
