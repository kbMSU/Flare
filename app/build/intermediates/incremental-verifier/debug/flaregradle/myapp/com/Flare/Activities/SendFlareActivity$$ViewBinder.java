// Generated code from Butter Knife. Do not modify!
package flaregradle.myapp.com.Flare.Activities;

import android.view.View;
import butterknife.ButterKnife.Finder;
import butterknife.ButterKnife.ViewBinder;

public class SendFlareActivity$$ViewBinder<T extends flaregradle.myapp.com.Flare.Activities.SendFlareActivity> implements ViewBinder<T> {
  @Override public void bind(final Finder finder, final T target, Object source) {
    View view;
    view = finder.findRequiredView(source, 2131558421, "field '_contactsView'");
    target._contactsView = finder.castView(view, 2131558421, "field '_contactsView'");
    view = finder.findRequiredView(source, 2131558500, "field '_flareMessage'");
    target._flareMessage = finder.castView(view, 2131558500, "field '_flareMessage'");
    view = finder.findRequiredView(source, 2131558467, "field '_phoneText'");
    target._phoneText = finder.castView(view, 2131558467, "field '_phoneText'");
    view = finder.findRequiredView(source, 2131558407, "field '_busyIndicator'");
    target._busyIndicator = finder.castView(view, 2131558407, "field '_busyIndicator'");
  }

  @Override public void unbind(T target) {
    target._contactsView = null;
    target._flareMessage = null;
    target._phoneText = null;
    target._busyIndicator = null;
  }
}
