package com.readertranslator.usilitel.readertranslator;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;

public class ListUtils {

    // меняем высоту ExpandableListView в зависимости от скрытых элементов
    public static void setDynamicHeight(ExpandableListView mListView) {

        HidedExpandableListAdapter mListAdapter = (HidedExpandableListAdapter)mListView.getExpandableListAdapter();
        if (mListAdapter == null) {
            return;
        }
        int height = 0;
        int desiredWidth = View.MeasureSpec.makeMeasureSpec(mListView.getWidth(), View.MeasureSpec.UNSPECIFIED);
        for (int i = 0; i < mListAdapter.getGroupCount(); i++) {
            View listItem = mListAdapter.getGroupView(i, mListView.isGroupExpanded(i), mListView, mListView);
            listItem.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED);
            height += listItem.getMeasuredHeight();

            if(mListView.isGroupExpanded(i)){
                View listItemChild = mListAdapter.getChildView(i, 0, true, null, (ViewGroup) mListView.getParent());
                listItemChild.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED);
                height += listItemChild.getMeasuredHeight();
            }

        }
        ViewGroup.LayoutParams params = mListView.getLayoutParams();
        params.height = height + (mListView.getDividerHeight() * (mListAdapter.getGroupCount() - 1));
        mListView.setLayoutParams(params);
        mListView.requestLayout();

    }
}