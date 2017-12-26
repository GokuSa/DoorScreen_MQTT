package shine.com.doorscreen.adapter;

import android.databinding.DataBindingUtil;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import java.util.List;

import shine.com.doorscreen.R;
import shine.com.doorscreen.databinding.ItemStaffBinding;
import shine.com.doorscreen.entity.Staff;
import shine.com.doorscreen.util.Common;

/**
 * author:
 * 时间:2017/7/13
 * qq:1220289215
 * 类描述：医院工作人员适配器，包括医生和护士
 * 在有的项目中，医生护士是按某种规则排序的，这个后台已完成
 */

public class StaffAdapter extends RecyclerView.Adapter<StaffAdapter.StaffHoder>{
    private List<Staff> mStaffList=null;

    public void setStaffList(final List<Staff> staffList) {
        if (mStaffList == null) {
            mStaffList=staffList;
            notifyItemRangeChanged(0,staffList.size());
        }else{
            DiffUtil.DiffResult diffResult=DiffUtil.calculateDiff(new DiffUtil.Callback() {
                @Override
                public int getOldListSize() {
                    return mStaffList.size();
                }

                @Override
                public int getNewListSize() {
                    return staffList.size();
                }

                @Override
                public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                    return mStaffList.get(oldItemPosition).id==
                            staffList.get(newItemPosition).id;
                }

                @Override
                public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                    Staff old = mStaffList.get(oldItemPosition);
                    Staff staffNew = staffList.get(newItemPosition);
                    return old.id == staffNew.id
                            && Common.equals(old.getName(), staffNew.getName())
                            &&Common.equals(old.getTitle(), staffNew.getTitle())
                            &&Common.equals(old.getImg(), staffNew.getImg());
                }
            });
            diffResult.dispatchUpdatesTo(this);
            mStaffList=staffList;
        }
    }

    @Override
    public StaffHoder onCreateViewHolder(ViewGroup parent, int viewType) {
        ItemStaffBinding binding=DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.item_staff, parent, false);
        return new StaffHoder(binding);
    }

    @Override
    public void onBindViewHolder(StaffHoder holder, int position) {
        holder.mBinding.setStaff(mStaffList.get(position));
        holder.mBinding.executePendingBindings();
    }

    @Override
    public int getItemCount() {
        return null==mStaffList?0:mStaffList.size();
    }

    static class StaffHoder extends RecyclerView.ViewHolder{

        final ItemStaffBinding mBinding;

        public StaffHoder(ItemStaffBinding binding) {
            super(binding.getRoot());
            mBinding = binding;
        }


    }

}
