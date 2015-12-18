// Generated code from Butter Knife. Do not modify!
package flaregradle.myapp.com.Flare.Activities;

import android.view.View;
import butterknife.ButterKnife.Finder;
import butterknife.ButterKnife.ViewBinder;

public class SettingsActivity$$ViewBinder<T extends flaregradle.myapp.com.Flare.Activities.SettingsActivity> implements ViewBinder<T> {
  @Override public void bind(final Finder finder, final T target, Object source) {
    View view;
    view = finder.findRequiredView(source, 2131558471, "field 'textMessageCheckBox'");
    target.textMessageCheckBox = finder.castView(view, 2131558471, "field 'textMessageCheckBox'");
    view = finder.findRequiredView(source, 2131558469, "field 'cloudMessageCheckBox'");
    target.cloudMessageCheckBox = finder.castView(view, 2131558469, "field 'cloudMessageCheckBox'");
    view = finder.findRequiredView(source, 2131558468, "field 'selectAllowSaveCheckBox'");
    target.selectAllowSaveCheckBox = finder.castView(view, 2131558468, "field 'selectAllowSaveCheckBox'");
    view = finder.findRequiredView(source, 2131558470, "field 'selectFindFriendsCheckBox'");
    target.selectFindFriendsCheckBox = finder.castView(view, 2131558470, "field 'selectFindFriendsCheckBox'");
  }

  @Override public void unbind(T target) {
    target.textMessageCheckBox = null;
    target.cloudMessageCheckBox = null;
    target.selectAllowSaveCheckBox = null;
    target.selectFindFriendsCheckBox = null;
  }
}
