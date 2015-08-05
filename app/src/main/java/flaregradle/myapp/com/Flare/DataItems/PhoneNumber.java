package flaregradle.myapp.com.Flare.DataItems;

/**
 * Created by Karthik on 7/25/15.
 */
public class PhoneNumber {
    public boolean hasFlare;
    public String number;
    public boolean isSelected;

    public PhoneNumber(boolean flare,String num,boolean selected) {
        hasFlare = flare;
        number = num;
        isSelected = selected;
    }

    public boolean Contains(String num) {
        return number.contains(num);
    }
}
