// Generated code from Butter Knife. Do not modify!
package flaregradle.myapp.com.Flare.Activities;

import android.view.View;
import butterknife.ButterKnife.Finder;
import butterknife.ButterKnife.ViewBinder;

public class GroupsActivity$$ViewBinder<T extends flaregradle.myapp.com.Flare.Activities.GroupsActivity> implements ViewBinder<T> {
  @Override public void bind(final Finder finder, final T target, Object source) {
    View view;
    view = finder.findRequiredView(source, 2131558454, "field '_groupsListView'");
    target._groupsListView = finder.castView(view, 2131558454, "field '_groupsListView'");
    view = finder.findRequiredView(source, 2131558453, "field 'mAdView'");
    target.mAdView = finder.castView(view, 2131558453, "field 'mAdView'");
  }

  @Override public void unbind(T target) {
    target._groupsListView = null;
    target.mAdView = null;
  }
}
