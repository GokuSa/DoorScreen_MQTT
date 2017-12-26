package shine.com.doorscreen.adapter;

import android.databinding.DataBindingUtil;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import java.util.List;

import shine.com.doorscreen.R;
import shine.com.doorscreen.databinding.ItemPaientInfoBinding;
import shine.com.doorscreen.entity.Patient;
import shine.com.doorscreen.util.Common;

/**
 * author:
 * 时间:2017/7/13
 * qq:1220289215
 * 类描述：
 */

public class PatientAdapter2 extends RecyclerView.Adapter<PatientAdapter2.PatientHoder> {
    List<Patient> mPatients = null;

    public void setPatients(final List<Patient> patients) {
        if (mPatients == null) {
            mPatients = patients;
            notifyItemRangeChanged(0, patients.size());
        } else {
            DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new DiffUtil.Callback() {
                @Override
                public int getOldListSize() {
                    return mPatients.size();
                }

                @Override
                public int getNewListSize() {
                    return patients.size();
                }

                @Override
                public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                    Patient patient = mPatients.get(oldItemPosition);
                    Patient patientNew = patients.get(newItemPosition);
                    return patient.getClientmac().equals( patientNew.getClientmac());
                }

                @Override
                public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                    Patient patient = mPatients.get(oldItemPosition);
                    Patient patientNew = patients.get(newItemPosition);
                    return
                             Common.equals(patient.getClientmac(),patientNew.getClientmac())
                            && Common.equals(patient.getBedno(),patientNew.getBedno())
                            && Common.equals(patient.getDoctorname(), patientNew.getDoctorname())
                            && Common.equals(patient.getPatientname(),patientNew.getPatientname()) ;
                }
            });

            diffResult.dispatchUpdatesTo(this);
            mPatients=patients;
        }
    }

    public void clear() {
        if (mPatients != null) {
            mPatients.clear();
            notifyDataSetChanged();
        }
    }

    @Override
    public PatientHoder onCreateViewHolder(ViewGroup parent, int viewType) {
        ItemPaientInfoBinding binding = DataBindingUtil.inflate(
                LayoutInflater.from(parent.getContext()), R.layout.item_paient_info, parent, false);
        return new PatientHoder(binding);
    }

    @Override
    public void onBindViewHolder(PatientHoder holder, int position) {
        holder.mBinding.setPatient(mPatients.get(position));
        holder.mBinding.executePendingBindings();
    }

    @Override
    public int getItemCount() {
        return null == mPatients ? 0 : mPatients.size();
    }

    static class PatientHoder extends RecyclerView.ViewHolder {
        final ItemPaientInfoBinding mBinding;

        public PatientHoder(ItemPaientInfoBinding binding) {
            super(binding.getRoot());
            mBinding = binding;
        }
    }

}
