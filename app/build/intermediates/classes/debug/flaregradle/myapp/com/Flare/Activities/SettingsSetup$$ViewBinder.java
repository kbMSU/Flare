// Generated code from Butter Knife. Do not modify!
package flaregradle.myapp.com.Flare.Activities;

import android.view.View;
import butterknife.ButterKnife.Finder;
import butterknife.ButterKnife.ViewBinder;

public class SettingsSetup$$ViewBinder<T extends flaregradle.myapp.com.Flare.Activities.SettingsSetup> implements ViewBinder<T> {
  @Override public void bind(final Finder finder, final T target, Object source) {
    View view;
    view = finder.findRequiredView(source, 2131558470, "field 'textMessageCheckBox'");
    target.textMessageCheckBox = finder.castView(view, 2131558470, "field 'textMessageCheckBox'");
    view = finder.findRequiredView(source, 2131558468, "field 'cloudMessageCheckBox'");
    target.cloudMessageCheckBox = finder.castView(view, 2131558468, "field 'cloudMessageCheckBox'");
  }

  @Override public void unbind(T target) {
    target.textMessageCheckBox = null;
    target.cloudMessageCheckBox = null;
  }
}
