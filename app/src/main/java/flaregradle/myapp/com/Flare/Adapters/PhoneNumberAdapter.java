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

import flaregradle.myapp.com.Flare.DataItems.PhoneNumber;

/**
 * Created by Karthik on 7/25/15.
 */
public class PhoneNumberAdapter extends ArrayAdapter<PhoneNumber> {

    int _resource;
    Context _context;

    public PhoneNumberAdapter(Context context, int resource, List<PhoneNumber> objects) {
        super(context, resource, objects);
        _resource = resource;
        _context = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        LinearLayout numberView;
        final PhoneNumber number = getItem(position);

        if(convertView == null ) {
            numberView = new LinearLayout(_context);
            String inflater = Context.LAYOUT_INFLATER_SERVICE;
            LayoutInflater vi;
            vi = (LayoutInflater)_context.getSystemService(inflater);
            vi.inflate(_resource, numberView, true);
        } else {
            numberView = (LinearLayout)convertView;
        }

        TextView phoneNumberText = (TextView)numberView.findViewById(R.id.phoneNumberText);
        ImageView flareImage = (ImageView)numberView.findViewById(R.id.phoneNumberHasFlare);
        ImageView isSelected = (ImageView)numberView.findViewById(R.id.phoneNumberSelected);

        if(number.isSelected) {
            isSelected.setImageResource(R.drawable.ic_radio_button_on_white_24dp);
        } else {
            isSelected.setImageResource(R.drawable.ic_radio_button_off_white_24dp);
        }

        flareImage.setImageResource(R.drawable.flare_notification);

        if(number.hasFlare) {
            flareImage.setVisibility(View.VISIBLE);
        } else {
            flareImage.setVisibility(View.GONE);
        }

        phoneNumberText.setText(number.number);
        phoneNumberText.setTextSize(15);

        return numberView;

    }
}
