package flaregradle.myapp.com.flare.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.MyApp.Flare.R;

import java.util.List;

import flaregradle.myapp.com.flare.DataItems.Group;

/**
 * Created by Karthik on 1/2/2015.
 */
public class GroupsAdapter extends ArrayAdapter<Group> {

    int _resource;
    Context _context;

    public GroupsAdapter(Context context, int resource, List<Group> objects) {
        super(context, resource, objects);
        _context = context;
        _resource = resource;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        LinearLayout groupView;
        final Group currentGroup = getItem(position);

        if(convertView == null)
        {
            groupView = new LinearLayout(_context);
            String inflater = _context.LAYOUT_INFLATER_SERVICE;
            LayoutInflater vi;
            vi = (LayoutInflater)_context.getSystemService(inflater);
            vi.inflate(_resource, groupView, true);
        }
        else
        {
            groupView = (LinearLayout)convertView;
        }

        TextView name = (TextView)groupView.findViewById(R.id.group_name);
        name.setText(currentGroup.Name);

        Integer groupSize = currentGroup.Contacts.size();
        String sizeString = groupSize+" Members";
        if(groupSize == 1)
            sizeString = groupSize+" Member";
        TextView count = (TextView)groupView.findViewById(R.id.group_count);
        count.setText(sizeString);

        return groupView;
    }
}
