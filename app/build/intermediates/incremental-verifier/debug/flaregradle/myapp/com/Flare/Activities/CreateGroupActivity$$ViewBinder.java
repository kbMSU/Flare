// Generated code from Butter Knife. Do not modify!
package flaregradle.myapp.com.Flare.Activities;

import android.view.View;
import butterknife.ButterKnife.Finder;
import butterknife.ButterKnife.ViewBinder;

public class CreateGroupActivity$$ViewBinder<T extends flaregradle.myapp.com.Flare.Activities.CreateGroupActivity> implements ViewBinder<T> {
  @Override public void bind(final Finder finder, final T target, Object source) {
    View view;
    view = finder.findRequiredView(source, 2131558421, "field '_contactsView'");
    target._contactsView = finder.castView(view, 2131558421, "field '_contactsView'");
    view = finder.findRequiredView(source, 2131558467, "field '_phoneText'");
    target._phoneText = finder.castView(view, 2131558467, "field '_phoneText'");
    view = finder.findRequiredView(source, 2131558453, "field 'mAdView'");
    target.mAdView = finder.castView(view, 2131558453, "field 'mAdView'");
  }

  @Override public void unbind(T target) {
    target._contactsView = null;
    target._phoneText = null;
    target.mAdView = null;
  }
}
