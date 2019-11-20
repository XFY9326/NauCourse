package tool.xfy9326.naucourse.methods.async;

import android.content.Context;

import androidx.annotation.NonNull;

@SuppressWarnings("unused")
abstract class BaseInfoDetailMethod<T> extends BaseInfoMethod<T> {
    BaseInfoDetailMethod(@NonNull Context context) {
        super(context);
    }

    public abstract int loadDetail(String url) throws Exception;

    @NonNull
    public abstract String getDetailData();
}
