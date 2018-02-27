package tool.xfy9326.naucourse.Views;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import tool.xfy9326.naucourse.R;

/**
 * Created by xfy9326 on 18-2-20.
 */

class InfoViewHolder extends RecyclerView.ViewHolder {
    final CardView cardView_info;
    final TextView textView_type;
    final TextView textView_title;
    final TextView textView_click;
    final TextView textView_post;
    final TextView textView_date;

    InfoViewHolder(View view) {
        super(view);
        cardView_info = view.findViewById(R.id.cardView_info);
        textView_type = view.findViewById(R.id.textView_info_type);
        textView_title = view.findViewById(R.id.textView_info_title);
        textView_click = view.findViewById(R.id.textView_info_click);
        textView_post = view.findViewById(R.id.textView_info_post);
        textView_date = view.findViewById(R.id.textView_info_date);
    }
}
