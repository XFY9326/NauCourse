package tool.xfy9326.naucourse.methods.netInfoMethods;

import android.content.Context;

import androidx.annotation.NonNull;

abstract class BaseNetMethod {
    protected final Context context;

    BaseNetMethod(@NonNull Context context) {
        this.context = context;
    }

    public abstract int load() throws Exception;
}
