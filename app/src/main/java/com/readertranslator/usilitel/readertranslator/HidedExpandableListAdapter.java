package com.readertranslator.usilitel.readertranslator;

        import android.content.Context;
        import android.view.LayoutInflater;
        import android.view.View;
        import android.view.ViewGroup;
        import android.widget.Button;
        import android.widget.ExpandableListView;
        import android.widget.FrameLayout;
        import android.widget.SimpleExpandableListAdapter;
        import android.widget.TextView;

        import java.util.ArrayList;
        import java.util.List;
        import java.util.Map;

// ExpandableListAdapter с возможностью скрытия элементов
public class HidedExpandableListAdapter extends SimpleExpandableListAdapter {

    private Context mContext;
    private List<Integer> hidedElements = new ArrayList(); // массив с id groupPosition-ов, которые не надо показывать
    private ExpandableListView expandableListView; // ссылка на текущий ExpandableListView
    private ExpandableListView linkedExpandableListView; // ссылка на связанный ExpandableListView
    private List<? extends Map<String, ?>> mGroupData;
    private List<? extends List<? extends Map<String, ?>>> mChildData;

    public HidedExpandableListAdapter(Context context,
                                         List<? extends Map<String, ?>> groupData, int groupLayout,
                                         String[] groupFrom, int[] groupTo,
                                         List<? extends List<? extends Map<String, ?>>> childData,
                                         int childLayout, String[] childFrom, int[] childTo
    ) {
        super(context, groupData, groupLayout, groupLayout, groupFrom, groupTo, childData,
                childLayout, childLayout, childFrom, childTo);
        this.mContext = context;
        this.mGroupData = groupData;
        this.mChildData = childData;
    }

    // задаем ссылку на текущий ExpandableListView
    public void setExpandableListView(ExpandableListView expandableListView){
        this.expandableListView = expandableListView;
    }
    // задаем ссылку на связанный ExpandableListView
    public void setLinkedExpandableListView(ExpandableListView linkedExpandableListView){
        this.linkedExpandableListView = linkedExpandableListView;
    }


    // скрываем все группы
    public void hideAllElements(){
        hidedElements.clear();
        for(int i=0; i<getGroupCount(); i++){
            hidedElements.add(i);
        }
        this.notifyDataSetChanged();
    }

    // скрываем одну группу
    public void hideElement(int idGroupPosition){
        hidedElements.add(idGroupPosition);
        this.notifyDataSetChanged();
    }

    // показываем одну группу
    public void showElement(int idGroupPosition){
        List<Integer> list = new ArrayList<>();
        list.add(idGroupPosition);
        hidedElements.removeAll(list);
        this.notifyDataSetChanged();
    }


    // динамически пересчитываем размер
    @Override
    public void onGroupCollapsed(int groupPosition) {
        super.onGroupCollapsed(groupPosition);
        ListUtils.setDynamicHeight(expandableListView);
        ListUtils.setDynamicHeight(linkedExpandableListView);
    }

    // динамически пересчитываем размер
    @Override
    public void onGroupExpanded(int groupPosition) {
        super.onGroupExpanded(groupPosition);
        ListUtils.setDynamicHeight(expandableListView);
        ListUtils.setDynamicHeight(linkedExpandableListView);
    }

    // кастомный ChildView
    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        convertView = inflater.inflate(R.layout.child_view, null);
        TextView textChild = (TextView) convertView.findViewById(R.id.textChild);
        textChild.setText((String)this.mChildData.get(groupPosition).get(childPosition).get("translationText"));
        return convertView;
    }

    // кастомный GroupView
    @Override
    public View getGroupView(final int groupPosition, boolean isExpanded, View convertView, final ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        convertView = inflater.inflate(R.layout.group_view, null);

        // кнопка для перемещения группы в связанный ExpandableListView
        Button button = (Button)convertView.findViewById(R.id.buttonChild);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                expandableListView.collapseGroup(groupPosition);
                // скрываем группу в текущем ExpandableListView
                hideElement(groupPosition);
                // показываем группу в связанном ExpandableListView
                ((HidedExpandableListAdapter)linkedExpandableListView.getExpandableListAdapter()).showElement(groupPosition);
                ListUtils.setDynamicHeight(expandableListView);
                ListUtils.setDynamicHeight(linkedExpandableListView);
            }
        });

        // задаем название группы
        String groupTitle = getGroup(groupPosition).toString()
                .replace("{groupName=","")
                .replace("}","");
        TextView textGroup = (TextView) convertView.findViewById(R.id.textGroup);
        textGroup.setText(groupTitle);

        // если группа скрыта - возвращаем пустой FrameLayout
        if(hidedElements.contains(groupPosition)) {
            convertView = new FrameLayout(this.mContext);
            FrameLayout.LayoutParams params=(FrameLayout.LayoutParams)convertView.getLayoutParams();
            if(params==null){params = new FrameLayout.LayoutParams(0,0);}
            params.height = 0;
            convertView.setLayoutParams(params);
        }

        return convertView;
    }

}
