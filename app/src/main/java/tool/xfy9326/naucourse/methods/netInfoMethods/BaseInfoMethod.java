package tool.xfy9326.naucourse.methods.netInfoMethods;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

abstract class BaseInfoMethod<T> extends BaseNetMethod {

    BaseInfoMethod(@NonNull Context context) {
        super(context);
    }

    @Nullable
    public abstract T getData(boolean checkTemp) throws Exception;
}
