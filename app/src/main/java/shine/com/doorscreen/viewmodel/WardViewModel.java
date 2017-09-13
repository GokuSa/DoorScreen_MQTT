package shine.com.doorscreen.viewmodel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;

import java.util.List;

import shine.com.doorscreen.database.WardDataBase;
import shine.com.doorscreen.mqtt.bean.Patient;
import shine.com.doorscreen.mqtt.bean.ReStart;
import shine.com.doorscreen.mqtt.bean.Staff;
import shine.com.doorscreen.mqtt.bean.Ward;

/**
 * author:
 * 时间:2017/7/12
 * qq:1220289215
 * 类描述：
 */

public class WardViewModel extends AndroidViewModel {
    private final LiveData<List<Patient>> mPatientObserver;
    private final LiveData<Ward> mWardObserver;
    private final LiveData<List<Staff>> mStaffObserver;
    private final LiveData<List<ReStart>> mReStartObserver;

    public WardViewModel(Application application) {
        super(application);
        mPatientObserver = WardDataBase.INSTANCE(application).patient().loadPatients();
        mWardObserver = WardDataBase.INSTANCE(application).ward().loadWard();
        mStaffObserver = WardDataBase.INSTANCE(application).staff().loadAllStaff();
        mReStartObserver = WardDataBase.INSTANCE(application).reStartDao().loadAll();
    }



    public LiveData<List<Patient>> getPatientObserver() {
        return mPatientObserver;
    }

    public LiveData<Ward> getWardObserver() {
        return mWardObserver;
    }

    public LiveData<List<Staff>> getStaffObserver() {
        return mStaffObserver;
    }

    public LiveData<List<ReStart>> getReStartObserver() {
        return mReStartObserver;
    }


    public List<ReStart> getReStartParams() {
        return WardDataBase.INSTANCE(this.getApplication()).reStartDao().getAll();
    }
}
