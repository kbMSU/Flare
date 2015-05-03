package flaregradle.myapp.com.Flare.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.MyApp.Flare.R;

import java.util.List;

import flaregradle.myapp.com.Flare.DataItems.Contact;

public class ContactsAdapter extends ArrayAdapter<Contact> {

    int _resource;
    Context _context;
    boolean _fromSos;

    public ContactsAdapter(Context context, int resource, List<Contact> objects, boolean fromSos) {
        super(context, resource, objects);
        _context = context;
        _resource = resource;
        _fromSos = fromSos;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        LinearLayout contactView;
        final Contact currentContact = getItem(position);

        if(convertView == null)
        {
            contactView = new LinearLayout(_context);
            String inflater = _context.LAYOUT_INFLATER_SERVICE;
            LayoutInflater vi;
            vi = (LayoutInflater)_context.getSystemService(inflater);
            vi.inflate(_resource, contactView, true);
        }
        else
        {
            contactView = (LinearLayout)convertView;
        }

        TextView name = (TextView)contactView.findViewById(com.MyApp.Flare.R.id.contact_name);
        TextView number = (TextView)contactView.findViewById(com.MyApp.Flare.R.id.contact_phone);
        ImageView image = (ImageView)contactView.findViewById(com.MyApp.Flare.R.id.contact_image);
        ImageView selection = (ImageView)contactView.findViewById(R.id.contact_selected);

        name.setText(currentContact.name);
        name.setTextSize(18);
        number.setText(currentContact.phoneNumber);
        try{
            if(currentContact.photo != null)
                image.setImageBitmap(currentContact.photo);
        }
        catch (Exception ex){

        }

        if(currentContact.selected) {
            selection.setImageResource(R.drawable.ic_radio_button_on_white_24dp);
        } else {
            selection.setImageResource(R.drawable.ic_radio_button_off_white_24dp);
        }

        return contactView;
    }
}
