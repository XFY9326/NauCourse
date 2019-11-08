package tool.xfy9326.naucourse.methods.async;

import android.content.Context;

import androidx.annotation.NonNull;

abstract class BaseNetMethod {
    final Context context;

    BaseNetMethod(@NonNull Context context) {
        this.context = context;
    }

    public abstract int load() throws Exception;
}
